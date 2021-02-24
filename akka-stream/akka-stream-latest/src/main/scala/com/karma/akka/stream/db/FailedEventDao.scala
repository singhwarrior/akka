package com.karma.akka.stream.db

import org.mongodb.scala.MongoClient
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.Observable
import org.mongodb.scala.Completed
import com.karma.akka.stream.db.constants.TABLE_NAME
import org.json.simple.parser.JSONParser
import org.json.simple.JSONObject
import org.mongodb.scala.Observer
import org.slf4j.LoggerFactory
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import com.mongodb.client.result.UpdateResult

class FailedEventDao(mongoClient: MongoClient) extends CollectionDao {

  val logger = LoggerFactory.getLogger(getClass)

  override def collection: MongoCollection[Document] = mongoClient.getDatabase("arms_db").getCollection(TABLE_NAME.FAILED_EVENT)

  def isFailureEventExists(asupId: String, serviceName: String): Option[Document] = {
    val result = collection.find(and(equal("asup_id", asupId), equal("service_name", serviceName))).first()
    val doc = Await.result(result.toFuture(), Duration(10, TimeUnit.SECONDS))
    if (doc == null)
      None
    else
      Some(doc)
  }

  def insertDocument(asupId: String, serviceName: String, failureEvent: String): Boolean = {
    
    val parser = new JSONParser()
    val jsonObject = parser.parse(failureEvent).asInstanceOf[JSONObject]
    val failureCategory = jsonObject.get("failureCategory").asInstanceOf[String]
    val failureSubCategory = jsonObject.get("failureSubCategory").asInstanceOf[String]
    val failureType = jsonObject.get("failureType").asInstanceOf[String]
    val failureException = jsonObject.get("failureException").asInstanceOf[String]
    val failureDescription = jsonObject.get("failureDescription").asInstanceOf[String]
    val timestamp = System.currentTimeMillis()
    
    val failedEventDoc = Document("asup_id" -> asupId, "service_name" -> serviceName,
      "q_count" -> 1, "event" -> failureEvent, "failureCategory" -> failureCategory,
      "failureCategory" -> failureCategory, "failureSubCategory" -> failureSubCategory,
      "failureType" -> failureType, "failureException" -> failureException,
      "failureDescription" -> failureDescription, "timestamp" -> timestamp)
      
    val insertObservable: Observable[Completed] = collection.insertOne(failedEventDoc)
    val result = Await.result(insertObservable.toFuture(), Duration(10, TimeUnit.SECONDS))
    logger.info("Insert done and result = {}", result.head)
    true
  }

  def update(asupId: String, serviceName: String): Boolean = {
    val updateObservable: Observable[UpdateResult] = collection.updateOne(and(equal("asup_id", asupId), equal("service_name", serviceName)), inc("q_count", 1))
    val result = Await.result(updateObservable.toFuture(), Duration(10, TimeUnit.SECONDS))
    logger.info("Insert done and result = {}", result.head)
    true
  }
}