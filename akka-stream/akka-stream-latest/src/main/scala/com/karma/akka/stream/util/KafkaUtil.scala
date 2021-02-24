package com.karma.akka.stream.util

import akka.kafka.ConsumerSettings
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import org.apache.kafka.common.serialization.StringSerializer

object KafkaUtil {
  def getConsumerSettings(actorSystem: ActorSystem): ConsumerSettings[String, String] = {
    val consumerConfig = actorSystem.settings.config.getConfig("akka.kafka.consumer")
    val consumerSettings =
      ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers("localhost:9092")
        .withGroupId("group1")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    consumerSettings
  }

  def getProducerSettings(actorSystem: ActorSystem): ProducerSettings[String, String] = {
    val producerConfig = actorSystem.settings.config.getConfig("akka.kafka.producer")
    val producerSettings =
      ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
        .withBootstrapServers("localhost:9092")
    producerSettings
  }
}