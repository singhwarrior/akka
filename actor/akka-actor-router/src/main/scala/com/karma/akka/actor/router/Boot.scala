package com.karma.akka.actor.router

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Timers
import scala.concurrent.duration._
import akka.actor.ActorRef
import akka.routing.FromConfig
import com.karma.akka.actor.router.Ticker.Tick
import akka.actor.SupervisorStrategy._
import akka.actor.OneForOneStrategy
import akka.routing.BroadcastPool
import scala.concurrent.Await
import akka.actor.Terminated
import akka.actor.PoisonPill
import scala.concurrent.Future
import akka.dispatch.MonitorableThreadFactory
import akka.actor.CoordinatedShutdown
import java.io.BufferedReader
import java.io.FileReader
import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter

object Boot extends App {
  val actorSystem = ActorSystem("router-system")
  val ticker = actorSystem.actorOf(Props[Ticker], "ticker")
  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run() = {
      val writer = new BufferedWriter(new FileWriter(new File("/Users/singg/openws/akka-actor-router/out.txt")))
      writer.write("Shutdown hook called2")
      writer.close()
      println("Terminating actor system")
      actorSystem.terminate()
      println("Actor System terminated")
    }
  })
  println("begin sleep")
  Thread.sleep(5000L)
  println("done sleeping")
}

object Ticker {
  case object TickKey
  case object Tick
}

class Ticker extends Actor with Timers with ActorLogging {

  import Ticker._

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 20.seconds) {
    case _: ArithmeticException => Stop
    case _: NullPointerException => Restart
    case _: IllegalArgumentException => Stop
    case _: Exception => Escalate
  }

  timers.startSingleTimer(TickKey, Tick, 2.seconds)
  //  val router1: ActorRef = context.actorOf(FromConfig.props(Props[Worker]), "router1")
  val router1: ActorRef = context.actorOf(BroadcastPool(5).withSupervisorStrategy(OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 30 seconds) {
    case _: ArithmeticException => Escalate
    case _: NullPointerException => Restart
    case _: IllegalArgumentException => Escalate
    case _: Exception => Escalate
  }).props(Props[Worker]), "router1")

  context.watch(router1)

  override def receive = {
    case Tick =>
      router1 ! Tick
      timers.startPeriodicTimer(TickKey, Tick, 2.seconds)
    case Terminated(router1) =>
      println("============================================================")
      self ! stop
      context.system.terminate()
    case _: ArithmeticException =>
      println("============================================================")
    //TODO Stash should come here
  }
}

class Worker extends Actor with ActorLogging {

  override def preStart() = {
    try {
//            Expressions.init()
      println("======================================")
    } catch {
      case e: IllegalArgumentException =>
        log.error("FATAL : Illegal argument excption!!", e)
        context.parent ! PoisonPill
      //          println(e)
    }
  }

  override def receive = {
    //    case e: Exception => throw e
    case Tick =>
      log.info("Message came")
      try {
        Expressions.callExpression(1, 1, "divide")
      } catch {
        case e: Exception => throw e
      }

    case _ =>
    //TODO Stash should come here
  }

  override def postStop() = {
    println("Wait, I have been called!!")
  }
}