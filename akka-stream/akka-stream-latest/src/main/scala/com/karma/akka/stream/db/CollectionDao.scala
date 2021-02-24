package com.karma.akka.stream.db

import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.MongoCollection

trait CollectionDao {
  def collection : MongoCollection[Document]
}