package com.karma.akka.stream

import com.karma.akka.stream.service.RegistryService
import com.karma.akka.stream.db.DAOManager
import org.slf4j.LoggerFactory
import scala.collection.mutable.ListBuffer
import com.karma.akka.stream.model.ServiceRegistry
import com.karma.akka.stream.service.AutoRequeueService

object Boot1 {
  val logger = LoggerFactory.getLogger(Boot.getClass)
  def main(args: Array[String]): Unit = {
    
    // Initializes all DAOs
    DAOManager().init("mongodb://127.0.0.1:27017")
    // Initializes all registered services and loads into ServiceRegistry object
    // All info like input,output,failure and re-queue topic details are here and loaded 
    // at the start
    ServiceRegistry().init().foreach(serviceName => {
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

      logger.info(loggerContent,all.toList: _*)
    })
    
    val failedEvent = "{\"event\":{\"asupId\":\"2018022620130118\",\"asupType\":\"DOT-MGMTLOG\",\"asupSize\":\"0.11993789672851562MB\",\"rawAsupPath\":\"/asupmail/spool/asups/post/asupdw/2018022620130118.headers\",\"asupProcessingLane\":\"CAR_LANE\",\"producedBy\":\"routing-service\",\"asupReceivedTimestamp\":1519731671198,\"producedTimestamp\":1519731672962,\"receivedTimestamp\":0,\"completedTimestamp\":1519731672962,\"addOnAttributes\":{\"secureFlag\":\"false\"},\"status\":\"SUCCEED\"},\"failureCategory\":\"PROCESSING_ERROR\",\"failureSubCategory\":\"FILE_IO_ERROR\",\"failureType\":\"DATA_PARSING\",\"failureException\":\"net.sf.sevenzipjbinding.SevenZipException\",\"failureDescription\":\"Archivefilecan'tbeopenedwithnoneoftheregisteredcodecs\"}"
    val serviceName = "SES"
    logger.info("REQUEUE_NEEDED={}",new AutoRequeueService().isRequeueNeeded(failedEvent, serviceName))
  }
}