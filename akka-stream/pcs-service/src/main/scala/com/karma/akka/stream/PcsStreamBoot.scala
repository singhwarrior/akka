package com.karma.akka.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.softwaremill.react.kafka.ReactiveKafka
import com.softwaremill.react.kafka.ConsumerProperties
import org.apache.kafka.common.serialization.StringDeserializer
import com.softwaremill.react.kafka.ProducerProperties
import org.apache.kafka.common.serialization.StringSerializer
import com.softwaremill.react.kafka.ProducerMessage
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.ConnectionFactory
import org.apache.hadoop.hbase.client.Connection
import scala.concurrent.duration._
//import scala.concurrent.duration.TimeUnit
import java.util.concurrent.TimeUnit

object PcsStreamBoot {

  def main(args: Array[String]): Unit = {
    implicit val actorSystem = ActorSystem("perf-counter-parser-system")
    implicit val materializer = ActorMaterializer()

    val kafka = new ReactiveKafka
    val publisher = kafka.consume(ConsumerProperties(
      bootstrapServers = "rtp-tuborg-vm01.gdl.eng.netapp.com:9092,rtp-tuborg-vm03.gdl.eng.netapp.com:9092,rtp-tuborg-vm08.gdl.eng.netapp.com:9092",
      topic = "LOWERCASE_STRING",
      groupId = "LOWERCASE_GRP1",
      valueDeserializer = new StringDeserializer()))
    val subscriber = kafka.publish(ProducerProperties(
      bootstrapServers = "rtp-tuborg-vm01.gdl.eng.netapp.com:9092,rtp-tuborg-vm03.gdl.eng.netapp.com:9092,rtp-tuborg-vm08.gdl.eng.netapp.com:9092",
      topic = "UPPERCASE_STRING",
      valueSerializer = new StringSerializer()))
    val schemaPath = "/Users/singg/openws/perf-counter-legacy-core/schema";
    Source.fromPublisher(publisher)
          .map(asupExtractedEventJson => JsonWriter.write(asupExtractedEventJson.value(), createConnection))
          .to(Sink.ignore).run()
    println("done")
//    actorSystem.registerOnTermination(println("terminated"))
//    actorSystem.scheduler.scheduleOnce(Duration.create(60, TimeUnit.SECONDS), new Runnable() {
//      def run() = {
//        println("Shutting down the system")
//        actorSystem.shutdown()
//        println("System has been shut down!")
//      }
//    })
    /*
         * Dont write hbaseConnection.close() here. It will be called.
         */
    //       hbaseConnection.close();
  }

  def createConnection(): Connection = {
    val conf = HBaseConfiguration.create();
    val ZOOKEEPER_QUORUM = "rtpwil-bigdata-qa17.rtp.openenglab.netapp.com,rtpwil-bigdata-qa18.rtp.openenglab.netapp.com,rtpwil-bigdata-qa19.rtp.openenglab.netapp.com"
    val PORT = "2181"
    conf.set("hbase.zookeeper.property.clientPort", PORT);
    conf.set("hbase.zookeeper.quorum", ZOOKEEPER_QUORUM);
    ConnectionFactory.createConnection(conf);
  }
}