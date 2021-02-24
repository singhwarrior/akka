package com.karma.akka.stream.db

import org.mongodb.scala.MongoClient
import com.karma.akka.stream.db.constants.TABLE_NAME
import scala.collection.mutable.ListBuffer
import org.mongodb.scala.Observer
import org.mongodb.scala.Completed
import org.mongodb.scala.Observable
//import org.bson.Document
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import org.json.simple.JSONObject
import scala.util.Success
import scala.util.Failure
import scala.concurrent.ExecutionContext
import org.json.simple.parser.JSONParser
import org.slf4j.LoggerFactory
import org.mongodb.scala.MongoCollection


class ServiceRegistryDao(mongoClient : MongoClient) extends CollectionDao{
  
  val logger = LoggerFactory.getLogger(getClass)
  
  override def collection : MongoCollection[Document] = mongoClient.getDatabase("arms_db").getCollection(TABLE_NAME.SERVICE_REGISTRY)
  
  def getAllServices() : List[String] = {
    var services = new ListBuffer[String]
    val docs = collection.find()
    val response = Await.result(docs.toFuture(), Duration(10, TimeUnit.SECONDS))
    response.foreach(doc => {
      services += doc.get("service_name").get.asString().getValue
    })
    services.toList
  }
  
  def getSuccessTopics(serviceName: String): List[String]={
    val result = collection.find(equal("service_name", serviceName)).first()
    val doc = Await.result(result.toFuture(), Duration(10, TimeUnit.SECONDS))
    if (doc != null && doc.isEmpty)
      null
    else{
      val topics = new ListBuffer[String]()
      val iter = doc.get("monitor_topics").get.asArray().getValues.iterator()
      while(iter.hasNext()){
        val monitorTopics = iter.next()
        val successTopic = monitorTopics.asDocument().get("monitor_topic").asString().getValue()
        topics += successTopic
      }
      topics.toList
    }
  }
  
  def getPredecessorTopics(serviceName : String) : List[String] = {
    val result = collection.find(equal("service_name", serviceName)).first()
    val doc = Await.result(result.toFuture(), Duration(10, TimeUnit.SECONDS))
    if (doc != null && doc.isEmpty)
      null
    else{
      val topics = new ListBuffer[String]()
      val iter = doc.get("monitor_topics").get.asArray().getValues.iterator()
      while(iter.hasNext()){
        val monitorTopics = iter.next()
        val successTopic = monitorTopics.asDocument().get("predecessor_topic").asString().getValue()
        topics += successTopic
      }
      topics.toList
    }
  }
  
  def getFailureTopic(serviceName: String): String = {
    val result = collection.find(equal("service_name", serviceName)).first()
    val doc = Await.result(result.toFuture(), Duration(10, TimeUnit.SECONDS))
    if (doc != null && doc.isEmpty)
      null
    else
      doc.get("failure_info").get.asDocument().get("failed_topic").asString().getValue
  }
  
  def getRequeueTopics(serviceName: String): Map[String,String] = {
    val result = collection.find(equal("service_name", serviceName)).first()
    val doc = Await.result(result.toFuture(), Duration(10, TimeUnit.SECONDS))
    if (doc != null && doc.isEmpty)
      null
    else{
      if(doc.get("failure_info").get.asDocument().get("requeue_topic_map").asDocument().containsKey("NO_LANE")){
        Map("NO_LANE" -> doc.get("failure_info").get.asDocument().get("requeue_topic_map").asDocument().get("NO_LANE").asString().getValue)
      }else{
        Map("CAR_LANE" -> doc.get("failure_info").get.asDocument().get("requeue_topic_map").asDocument().get("CAR_LANE").asString().getValue,
        "CRUISE_LANE" -> doc.get("failure_info").get.asDocument().get("requeue_topic_map").asDocument().get("CRUISE_LANE").asString().getValue,
        "TRUCK_LANE" -> doc.get("failure_info").get.asDocument().get("requeue_topic_map").asDocument().get("TRUCK_LANE").asString().getValue,
        "MINITRUCK_LANE" -> doc.get("failure_info").get.asDocument().get("requeue_topic_map").asDocument().get("MINITRUCK_LANE").asString().getValue)        
      }
    }
  }
  
  def getMaxCount(serviceName: String): Int = {
     val result = collection.find(equal("service_name", serviceName)).first()
    val doc = Await.result(result.toFuture(), Duration(10, TimeUnit.SECONDS))
    if (doc != null && doc.isEmpty)
      0
    else{
      doc.get("failure_info").get.asDocument().get("max_requeue_count").asDouble().intValue()
    }
  }
  
  
}