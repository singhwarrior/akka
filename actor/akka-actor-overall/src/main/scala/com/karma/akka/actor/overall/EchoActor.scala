package com.karma.akka.actor.overall

import akka.actor.{Actor,ActorLogging,Props}

object EchoActor{
  def props() : Props = Props(new EchoActor)
}

class EchoActor extends Actor with ActorLogging{
  override def receive = {
    case msg : String =>
      log.debug("event={}", msg)
      println(msg)
    case _ =>
      log.error("Return wrong message format")
  }
}