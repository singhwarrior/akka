package com.karma.akka.actor.overall

import scala.concurrent.duration._
import akka.actor.{ Actor, Timers, ActorLogging }
import akka.actor.ActorSystem
import akka.actor.Props
  
object BootScheduler extends App {
  val tickSystem = ActorSystem("tick-system")
  val myActor = tickSystem.actorOf(Props[MyActor])
}

object MyActor {
  private case object TickKey
  private case object FirstTick
  private case object Tick
}

class MyActor extends Actor with Timers with ActorLogging {

  import MyActor._

  timers.startSingleTimer(TickKey, FirstTick, 500.millis)
  
  override def receive = {
    case FirstTick =>
      log.info("First Tick")
      timers.startPeriodicTimer(TickKey, Tick, 1.second)
    case Tick =>
      log.info("Next Tick")

  }
}