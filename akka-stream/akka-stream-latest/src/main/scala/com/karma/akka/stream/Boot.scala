package com.karma.akka.stream

import akka.actor.ActorSystem
import org.slf4j.{ Logger, LoggerFactory }
import akka.stream.ActorMaterializer
import akka.kafka.ProducerSettings
import org.apache.kafka.common.serialization.StringSerializer
import scala.concurrent.Future
import akka.Done
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.producer.ProducerRecord
import akka.kafka.scaladsl.Producer
import akka.kafka.ConsumerSettings
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import akka.kafka.scaladsl.Consumer
import akka.kafka.Subscriptions
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Keep
import akka.kafka.ProducerMessage
import akka.kafka.ConsumerMessage.CommittableOffsetBatch
import akka.stream.alpakka.mongodb.scaladsl.MongoSink
import org.mongodb.scala.MongoClient
import org.mongodb.scala.bson.collection.immutable.Document
import com.mongodb.async.client.Observable
import org.mongodb.scala.Completed
import org.mongodb.scala.Observer
import akka.actor.Props
//import org.bson.BsonDocument
import scala.collection.mutable.ListBuffer
import akka.stream.scaladsl.Flow
import akka.kafka.ConsumerMessage
import akka.NotUsed
import com.karma.akka.stream.db.DAOManager
import com.karma.akka.stream.service.RegistryService
import com.karma.akka.stream.db.constants.TABLE_NAME
import com.karma.akka.stream.model.ServiceRegistry

//import akka.event.Logging

object Boot {
  val logger = LoggerFactory.getLogger(Boot.getClass)
  def main(args: Array[String]): Unit = {
    
    // Initializes all DAOs
    DAOManager().init("mongodb://127.0.0.1:27017")
    // Initializes all registered services and loads into ServiceRegistry object
    // All info like input,output,failure and re-queue topic details are here and loaded 
    // at the start
    ServiceRegistry().init().foreach(serviceName => {
      val loggerContent = "SERVICE_NAME={}," +
        "FAILURE_TOPIC={}," +
        "REQUEUE_TOPICS={}," +
        "SUCCESS_TOPICS={}," +
        "PREDECESSOR_TOPICS={}," +
        "MAX_REQUEUE_COUNT={}"
      val all = new ListBuffer[Object]()
      all += serviceName
      all += ServiceRegistry().getFailureTopic(serviceName)
      all += ServiceRegistry().getRequeueTopics(serviceName)
      all += ServiceRegistry().getSuccessTopics(serviceName)
      all += ServiceRegistry().getPredecessorTopics(serviceName)
      all += ServiceRegistry().getMaxCount(serviceName).toString()

      logger.info(loggerContent,all.toList: _*)
    })
    
    implicit val actorSystem: ActorSystem = ActorSystem("arms-streaming")
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    //Consumer
    val consumerConfig = actorSystem.settings.config.getConfig("akka.kafka.consumer")
    val consumerSettings =
      ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers("localhost:9092")
        .withGroupId("group1")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    //Producer
    val producerConfig = actorSystem.settings.config.getConfig("akka.kafka.producer")
    val producerSettings =
      ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
        .withBootstrapServers("localhost:9092")

    logger.info("ARMS-STREAMING started...")

    val client = MongoClient(s"mongodb://127.0.0.1:27017")
    //    val db = client.getDatabase("alpakka-mongo")
    val db = client.getDatabase("arms_db")
    val serviceRegistry = db.getCollection(TABLE_NAME.SERVICE_REGISTRY)
    for (cur <- serviceRegistry.find()) {
      val sb = new StringBuilder
      sb.append("SERVICE=" + cur.get("service_name").get.asString().getValue).append(",")
      val iter = cur.get("monitor_topics").get.asArray().getValues.iterator()
      while (iter.hasNext()) {
        val monitor_topics = iter.next()
        sb.append("monitor_topic=" + monitor_topics.asDocument().get("monitor_topic").asString().getValue()).append(",")
        sb.append("predecessor_topic=" + monitor_topics.asDocument().get("predecessor_topic").asString().getValue()).append(",")
      }
      sb.append("FAILURE_TOPIC=" + cur.get("failure_info").get.asDocument().get("failed_topic").asString().getValue).append(",")
      if (cur.get("failure_info").get.asDocument().get("requeue_topic_map").asDocument().containsKey("NO_LANE")){
        sb.append("REQUEUE_TOPIC" + cur.get("failure_info").get.asDocument().get("requeue_topic_map").asDocument().get("NO_LANE").asString().getValue)
      }else {
        sb.append("CAR_LANE_REQUEUE_TOPIC=" + cur.get("failure_info").get.asDocument().get("requeue_topic_map").asDocument().get("CAR_LANE").asString().getValue).append(",")
        sb.append("CRUISE_LANE_REQUEUE_TOPIC=" + cur.get("failure_info").get.asDocument().get("requeue_topic_map").asDocument().get("CRUISE_LANE").asString().getValue).append(",")
        sb.append("TRUCK_LANE_REQUEUE_TOPIC=" + cur.get("failure_info").get.asDocument().get("requeue_topic_map").asDocument().get("TRUCK_LANE").asString().getValue).append(",")
        sb.append("MINITRUCK_LANE_REQUEUE_TOPIC=" + cur.get("failure_info").get.asDocument().get("requeue_topic_map").asDocument().get("MINITRUCK_LANE").asString().getValue)       
      }
      logger.info(sb.toString())
    }

    val numbersColl = db.getCollection("numbers")
    val stringsColl = db.getCollection("strings")

    // Logic of writing the failed events to FAILED_EVENT table
    val topics = Map("failed_asups" -> "requeue_asups", "failed_asups1" -> "requeue_asups1")
    for (topic <- topics) {
      val failure_topic = topic._1
      val requeue_topic = topic._2
      val kafkaSource = Consumer.committableSource(consumerSettings, Subscriptions.topics(failure_topic))
      val flow = Flow[ConsumerMessage.CommittableMessage[String, String]].map(msg => {
            println(msg.record.topic())
            println(msg.record.value + "===========================")
            val replacementDoc: Document = Document("x" -> 2, "y" -> 3)

            val insertObservable: Observable[Completed] = numbersColl.insertOne(replacementDoc)

            insertObservable.subscribe(new Observer[Completed] {
              override def onNext(result: Completed): Unit = println(s"onNext: $result")
              override def onError(e: Throwable): Unit = println(s"onError: $e")
              override def onComplete(): Unit = println("onComplete")
            })
            msg})
            .map(msg => {
            val test = ProducerMessage.Message(new ProducerRecord[String, String](requeue_topic, msg.record.value), msg.committableOffset)
            test
          })
          .via(Producer.flow(producerSettings))
          .map(_.message.passThrough)
          .batch(20, first => CommittableOffsetBatch.empty.updated(first)) { (batch, elem) =>
            batch.updated(elem)
          }
          .mapAsync(3)(_.commitScaladsl())
          val sink = Sink.ignore
        val done = kafkaSource.via(flow).runWith(sink)
            
            
//      val done =
//        Consumer.committableSource(consumerSettings, Subscriptions.topics(failure_topic))
//          .map(msg => {
//            println(msg.record.topic())
//            println(msg.record.value + "===========================")
//            val replacementDoc: Document = Document("x" -> 2, "y" -> 3)
//
//            val insertObservable: Observable[Completed] = numbersColl.insertOne(replacementDoc)
//
//            insertObservable.subscribe(new Observer[Completed] {
//              override def onNext(result: Completed): Unit = println(s"onNext: $result")
//              override def onError(e: Throwable): Unit = println(s"onError: $e")
//              override def onComplete(): Unit = println("onComplete")
//            })
//            msg
//          })
//          .map(msg => {
//            val test = ProducerMessage.Message(new ProducerRecord[String, String](requeue_topic, msg.record.value), msg.committableOffset)
//            test
//          })
//          .via(Producer.flow(producerSettings))
//          .map(_.message.passThrough)
//          .batch(20, first => CommittableOffsetBatch.empty.updated(first)) { (batch, elem) =>
//            batch.updated(elem)
//          }
//          .mapAsync(3)(_.commitScaladsl())
//          .runWith(Sink.ignore)

    }
    //Logic of writing the all the events to EVENT_META_DATA and AGGREGATED_EVENT_TABLE
    val successTopics = List("success_topic")
    for (successTopic <- successTopics) {
      val source = Consumer.committableSource(consumerSettings, Subscriptions.topics(successTopic))

      val done = Consumer.committableSource(consumerSettings, Subscriptions.topics(successTopic))
        .map(msg => {
          println(msg.record.topic())
          println(msg.record.value + "===========================")
          val replacementDoc: Document = Document("x" -> 2, "y" -> 3)
          val insertObservable: Observable[Completed] = stringsColl.insertOne(replacementDoc)
          insertObservable.subscribe(new Observer[Completed] {
            override def onNext(result: Completed): Unit = println(s"onNext: $result")
            override def onError(e: Throwable): Unit = println(s"onError: $e")
            override def onComplete(): Unit = println("onComplete")
          })
          msg.committableOffset
        })
        .batch(20, first => CommittableOffsetBatch.empty.updated(first)) { (batch, elem) =>
          batch.updated(elem)
        }
        .mapAsync(3)(_.commitScaladsl())
        .runWith(Sink.ignore)
    }

    //    val nbThreads =  Thread.getAllStackTraces().keySet().size();
    //    logger.info("Total Number of Threads = "+nbThreads)

    //    var nbRunning = 0;
    //    for (t <- Thread.getAllStackTraces().keySet().toArray()) {
    //      logger.info("Thread Name = "+(t.asInstanceOf[Thread]).getName)
    //      if ((t.asInstanceOf[Thread]).getState==Thread.State.RUNNABLE) nbRunning = nbRunning + 1;
    //    }
    //    logger.info("Total Number of Running Threads = "+nbRunning)

    //    val idActor = actorSystem.actorOf(Props(new IDActor))
    //    idActor ! "start"
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run() = {
        logger.warn("Termination to Actor System has been called!!")
        logger.warn("Actor System terminating...")
        actorSystem.terminate()
        logger.warn("Actor System terminated!!")
      }
    })
  }

}