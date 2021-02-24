package com.karma.akka.actor.overall

import akka.actor.{Actor,PoisonPill,ActorSystem,Props,ActorLogging}
import scala.concurrent.duration._

object PingPong extends App{
  val system = ActorSystem("ping-pong")
  
  val pinger = system.actorOf(Props[Pinger], "pinger")
  val ponger = system.actorOf(Props[Ponger], "ponger")
  
  import system.dispatcher
  system.scheduler.scheduleOnce(5000 millis){
    ponger.tell(Pong, pinger)
  }
}

case object Ping
case object Pong

class Pinger extends Actor with ActorLogging{
  var count = 10
  override def receive = {
    case Ping =>
      if(count > 0){
        count -= 1
        log.info("Pong")
        sender() ! Pong
      }else{
        sender() ! PoisonPill
        self ! PoisonPill
      }
  }
}

class Ponger extends Actor with ActorLogging{
  override def receive = {
    case Pong =>
      log.info("Ping")
      sender() ! Ping
  }
}