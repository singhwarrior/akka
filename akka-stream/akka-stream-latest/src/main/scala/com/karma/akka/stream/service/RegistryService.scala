package com.karma.akka.stream.service

import scala.collection.mutable.ListBuffer
import com.karma.akka.stream.db.constants.TABLE_NAME
import org.mongodb.scala.bson.collection.immutable.Document
import com.karma.akka.stream.db.ServiceRegistryDao
import com.karma.akka.stream.db.DAOManager

class RegistryService {

  def serviceRegistryDao = DAOManager().getDaoForCollection(TABLE_NAME.SERVICE_REGISTRY).get.asInstanceOf[ServiceRegistryDao]
  
  def getAllServices(): List[String] = {
    serviceRegistryDao.getAllServices()
  }

  def getSuccessTopics(serviceName: String): List[String] = {
    serviceRegistryDao.getSuccessTopics(serviceName)
  }
  
  def getPredecessorTopics(serviceName : String) : List[String] = {
    serviceRegistryDao.getPredecessorTopics(serviceName)
  }

  def getFailureTopic(serviceName: String): String = {
    serviceRegistryDao.getFailureTopic(serviceName)
  }
  
   def getRequeueTopics(serviceName: String): Map[String,String] = {
    serviceRegistryDao.getRequeueTopics(serviceName)
  }

  def getMaxCount(serviceName: String): Int = {
    serviceRegistryDao.getMaxCount(serviceName)
  }

}