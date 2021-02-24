package com.karma.akka.stream.db

import org.mongodb.scala.MongoClient
import com.karma.akka.stream.db.constants.TABLE_NAME

// Change it to pass the DB host and port
case class DAOManager() {
  import DAOManager._
  def init(connectionUrl:String) = mongoClient = MongoClient(connectionUrl)
  def getDaoForCollection(collectionName : String) : Option[CollectionDao] = getDao(collectionName)
}

object DAOManager{
  private var mongoClient : MongoClient = null
  private def getDao(collectionName : String) : Option[CollectionDao] = {
    collectionName match{
      case TABLE_NAME.SERVICE_REGISTRY => Some(new ServiceRegistryDao(mongoClient))
      case TABLE_NAME.FAILED_EVENT =>  Some(new FailedEventDao(mongoClient)) 
      case _ => None
    }
  }

}