package com.karma.akka.stream

import akka.kafka.ConsumerSettings
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import com.karma.akka.stream.db.DAOManager
import akka.kafka.ProducerSettings
import org.apache.kafka.common.serialization.StringSerializer
import com.karma.akka.stream.model.ServiceRegistry
import scala.collection.mutable.ListBuffer
import org.slf4j.LoggerFactory
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import org.apache.kafka.clients.producer.ProducerRecord
import akka.kafka.ConsumerMessage.CommittableOffsetBatch
import akka.kafka.ProducerMessage
import akka.kafka.ConsumerMessage
import akka.stream.scaladsl.Flow
import akka.kafka.scaladsl.Producer
import com.karma.akka.stream.service.AutoRequeueService
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.GraphDSL
import akka.stream.scaladsl.Broadcast
import org.apache.kafka.clients.consumer.ConsumerRecord
import akka.NotUsed
import akka.Done
import akka.stream.ClosedShape
import com.karma.akka.stream.db.constants.ARMSConstants
import com.karma.akka.stream.util.GraphEntityUtil
import com.karma.akka.stream.util.KafkaUtil

object Boot2 {

  val logger = LoggerFactory.getLogger(Boot.getClass)

  def main(args: Array[String]): Unit = {
    // Initializes all DAOs
    DAOManager().init("mongodb://127.0.0.1:27017")
    // Initializes all registered services and loads into ServiceRegistry object
    // All info like input,output,failure and re-queue topic details are here and loaded
    // at the start
    val allMicroServices = ServiceRegistry().init()
    allMicroServices.foreach(serviceName => {
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

      logger.info(loggerContent, all.toList: _*)
    })

    implicit val actorSystem: ActorSystem = ActorSystem("arms-streaming")
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val consumerSettings = KafkaUtil.getConsumerSettings(actorSystem)
    val producerSettings = KafkaUtil.getProducerSettings(actorSystem)

    logger.info("ARMS-STREAMING started...")

    val autoRequeueService = new AutoRequeueService

    val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      allMicroServices.foreach(serviceName => {
        val failure_topic = ServiceRegistry().getFailureTopic(serviceName)
        val kafkaSource = Consumer.committableSource(consumerSettings, Subscriptions.topics(failure_topic))
        val requeueTopics = ServiceRegistry().getRequeueTopics(serviceName)

        if (requeueTopics.contains(ARMSConstants.NO_LANE)) {
          kafkaSource ~> GraphEntityUtil.getFlowForLane(serviceName, ARMSConstants.NO_LANE, producerSettings) ~> Sink.ignore
        } else {
          val bcast = builder.add(Broadcast[ConsumerMessage.CommittableMessage[String, String]](4))
          kafkaSource ~> bcast.in
          bcast.out(0) ~> GraphEntityUtil.getFlowForLane(serviceName, ARMSConstants.CAR_LANE, producerSettings) ~> Sink.ignore
          bcast.out(1) ~> GraphEntityUtil.getFlowForLane(serviceName, ARMSConstants.CRUISE_LANE, producerSettings) ~> Sink.ignore
          bcast.out(2) ~> GraphEntityUtil.getFlowForLane(serviceName, ARMSConstants.TRUCK_LANE, producerSettings) ~> Sink.ignore
          bcast.out(3) ~> GraphEntityUtil.getFlowForLane(serviceName, ARMSConstants.TRUCK_LANE, producerSettings) ~> Sink.ignore
        }
      })
      ClosedShape
    })
    g.run()

    //    allMicroServices.foreach(serviceName => {
    //      val failure_topic = ServiceRegistry().getFailureTopic(serviceName)
    //      val kafkaSource = Consumer.committableSource(consumerSettings, Subscriptions.topics(failure_topic))
    //      val flow = Flow[ConsumerMessage.CommittableMessage[String, String]]
    //        .filter(msg => {
    //          autoRequeueService.isRequeueNeeded(msg.record.value(), serviceName)
    //        })
    //        .map(msg => {
    //          val requeueTopics = ServiceRegistry().getRequeueTopics(serviceName)
    //          val requeue_topic = requeueTopics.get("NO_LANE").get
    //          logger.info("SERVICE_NAME={},FAILURE_TOPIC={},REQUEUE_TOPIC={}", serviceName, failure_topic, requeue_topic)
    //          val requeueMessage = ProducerMessage.Message(new ProducerRecord[String, String](requeue_topic, msg.record.value), msg.committableOffset)
    //          requeueMessage
    //        })
    //        .via(Producer.flow(producerSettings))
    //        .map(_.message.passThrough)
    //        .batch(20, first => CommittableOffsetBatch.empty.updated(first)) { (batch, elem) =>
    //          batch.updated(elem)
    //        }
    //        .mapAsync(3)(_.commitScaladsl())
    //      val sink = Sink.ignore
    //      val done = kafkaSource.via(flow).runWith(sink)
    //    })
  }

}