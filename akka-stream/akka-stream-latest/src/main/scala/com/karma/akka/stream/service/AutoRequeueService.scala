package com.karma.akka.stream.service

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

import com.karma.akka.stream.db.DAOManager
import com.karma.akka.stream.db.FailedEventDao
import com.karma.akka.stream.db.constants.TABLE_NAME
import com.karma.akka.stream.model.ServiceRegistry

class AutoRequeueService {
  
  def isRequeueNeeded(failedEvent : String, serviceName : String) : Boolean = {
    
    val failedEventDao = DAOManager().getDaoForCollection(TABLE_NAME.FAILED_EVENT).get.asInstanceOf[FailedEventDao]
    val parser = new JSONParser()
    val jsonObject = parser.parse(failedEvent).asInstanceOf[JSONObject]
    val event = jsonObject.get("event").asInstanceOf[JSONObject]
    val asupId = event.get("asupId").toString()

    val response = failedEventDao.isFailureEventExists(asupId, serviceName)
    if(response.isEmpty){
      failedEventDao.insertDocument(asupId,serviceName, failedEvent)
      true
    }else{
      val maxCount = ServiceRegistry().getMaxCount(serviceName)
      if(response.get.getInteger("q_count").intValue() < maxCount){
        failedEventDao.update(asupId,serviceName)
        true
      }else
         false
    }
  }
}