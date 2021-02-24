package com.karma.akka.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.softwaremill.react.kafka.ReactiveKafka
import org.reactivestreams.Publisher
import com.softwaremill.react.kafka.ConsumerProperties
import org.apache.kafka.common.serialization.StringDeserializer
import com.softwaremill.react.kafka.ProducerProperties
import org.apache.kafka.common.serialization.StringSerializer
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink
import com.softwaremill.react.kafka.ProducerMessage

object StreamBoot {
  def main(args: Array[String]): Unit = {
    implicit val actorSystem = ActorSystem("ReactiveKafka")
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
    Source.fromPublisher(publisher).map(m => ProducerMessage(m.value().toUpperCase))
      .to(Sink.fromSubscriber(subscriber)).run()
      
  }
}