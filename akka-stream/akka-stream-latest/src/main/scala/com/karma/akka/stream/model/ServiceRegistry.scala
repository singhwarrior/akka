package com.karma.akka.stream.model

import com.karma.akka.stream.service.RegistryService

case class ServiceRegistry() {
  import ServiceRegistry._
  def init() : Set[String] = allServices.keySet
  def getFailureTopic(serviceName: String): String = allServices.get(serviceName).get.failureTopic
  def getRequeueTopics(serviceName: String): Map[String, String] = allServices.get(serviceName).get.requeueTopics
  def getPredecessorTopics(serviceName: String): List[String] = allServices.get(serviceName).get.inputTopics
  def getSuccessTopics(serviceName: String): List[String] = allServices.get(serviceName).get.inputTopics
  def getMaxCount(serviceName: String): Int = allServices.get(serviceName).get.maxRequeueCount
}

object ServiceRegistry {
  private val allServices: Map[String, MicroService] = {
    var services: scala.collection.mutable.Map[String, MicroService] = scala.collection.mutable.Map()
    val registryService = new RegistryService()
    registryService.getAllServices().foreach(serviceName => {
      val service = MicroService(
        serviceName,
        registryService.getPredecessorTopics(serviceName), registryService.getRequeueTopics(serviceName),
        registryService.getSuccessTopics(serviceName), registryService.getFailureTopic(serviceName),
        registryService.getMaxCount(serviceName))
      services += ((serviceName, service))
    })
    services.toMap
  }
  
}

case class MicroService(
  serviceName: String,
  inputTopics: List[String], requeueTopics: Map[String, String],
  successTopics: List[String], failureTopic: String,
  maxRequeueCount: Int) 