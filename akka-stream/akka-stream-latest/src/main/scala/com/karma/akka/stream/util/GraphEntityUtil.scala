package com.karma.akka.stream.util

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber


import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source

import akka.stream.Supervision
import akka.stream.ActorAttributes
import akka.kafka.ConsumerMessage
import akka.kafka.scaladsl.Consumer
import akka.kafka.ConsumerSettings
import org.apache.kafka.clients.consumer.ConsumerConfig
import akka.kafka.Subscriptions
import akka.kafka.ProducerMessage
import org.slf4j.LoggerFactory
import org.apache.kafka.clients.producer.ProducerRecord
import akka.kafka.ConsumerMessage.CommittableOffsetBatch
import akka.kafka.ProducerSettings
import com.karma.akka.stream.service.AutoRequeueService
import akka.kafka.scaladsl.Producer
import akka.Done
import com.karma.akka.stream.model.ServiceRegistry
import com.karma.akka.stream.db.constants.ARMSConstants

object GraphEntityUtil {

  val logger = LoggerFactory.getLogger(GraphEntityUtil.getClass)
//  val splunkLogger = Logger.getLogger("splunkLog")



   def getFlowForLane(serviceName: String, laneName: String, producerSettings: ProducerSettings[String, String]): Flow[ConsumerMessage.CommittableMessage[String, String], Done, NotUsed] = {
    val requeueTopics = ServiceRegistry().getRequeueTopics(serviceName)
    val failureTopic = ServiceRegistry().getFailureTopic(serviceName)
    if (laneName.equals(ARMSConstants.NO_LANE)) {
      Flow[ConsumerMessage.CommittableMessage[String, String]]
        .filter(msg => {
          new AutoRequeueService().isRequeueNeeded(msg.record.value(), serviceName)
        }).map(msg => {
          val requeueTopic = requeueTopics.get(laneName).get
          val requeueMessage = ProducerMessage.Message(new ProducerRecord[String, String](requeueTopic, msg.record.value), msg.committableOffset)
          logger.info("SERVICE_NAME={},FAILURE_TOPIC={},REQUEUE_TOPIC={}", serviceName, failureTopic, requeueTopic)
          requeueMessage
        })
        .via(Producer.flow(producerSettings))
        .map(_.message.passThrough)
        .batch(20, first => CommittableOffsetBatch.empty.updated(first)) { (batch, elem) =>
          batch.updated(elem)
        }
        .mapAsync(3)(_.commitScaladsl())
    } else {
      Flow[ConsumerMessage.CommittableMessage[String, String]]
        .filter(msg => {
          msg.record.value().contains(laneName) && new AutoRequeueService().isRequeueNeeded(msg.record.value(), serviceName)
        }).map(msg => {
          val requeueTopic = requeueTopics.get(laneName).get
          val requeueMessage = ProducerMessage.Message(new ProducerRecord[String, String](requeueTopic, msg.record.value), msg.committableOffset)
          logger.info("SERVICE_NAME={},FAILURE_TOPIC={},REQUEUE_TOPIC={}", serviceName, failureTopic, requeueTopic)
          requeueMessage
        })
        .via(Producer.flow(producerSettings))
        .map(_.message.passThrough)
        .batch(20, first => CommittableOffsetBatch.empty.updated(first)) { (batch, elem) =>
          batch.updated(elem)
        }
        .mapAsync(3)(_.commitScaladsl())
    }
  }



}