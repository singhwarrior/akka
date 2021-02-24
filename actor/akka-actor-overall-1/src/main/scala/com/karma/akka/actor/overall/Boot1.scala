package com.karma.akka.actor.overall

import akka.actor.Actor
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.util.Timeout
import akka.pattern.{ ask, pipe }
//import akka.compat.Future
import scala.concurrent.Future
import akka.actor.ActorLogging
import scala.concurrent.Await
import akka.event.Logging

object Boot1 extends App {

  val system = ActorSystem("ask-system")

  import system.dispatcher
  val actorA = system.actorOf(Props[ActorA])
  val actorB = system.actorOf(Props[ActorB])
  val actorC = system.actorOf(Props[ActorC])
  val actorD = system.actorOf(Props[ActorD])
  implicit val timeout = Timeout(5 seconds)

  val f: Future[Result] = for {
    x <- (actorA ? Request).mapTo[Int]
    s <- (actorB ask Request).mapTo[String]
    d <- (actorC ? Request).mapTo[Double]
  } yield Result(x, s, d)

  val out = (f pipeTo actorD)


}

final case class Result(x: Int, s: String, d: Double)
case object Request

class ActorA extends Actor {
  override def receive = {
    case Request =>
      operation()
      sender() ! (Math.random() * 5).asInstanceOf[Int]
  }

  def operation(): Int = {
    var i = 1
    for (i <- 1 to 8) { Thread.sleep(1000) }
    i
  }
}

class ActorB extends Actor {
  override def receive = {
    case Request => sender() ! "Some String"
  }

  def operation(): Int = {
    var i = 1
    for (i <- 1 to 10) { Thread.sleep(1000) }
    i
  }
}

class ActorC extends Actor {
  override def receive = {
    case Request => sender() ! (Math.random() * 5)
  }
  def operation(): Int = {
    var i = 1
    for (i <- 1 to 20) { Thread.sleep(1000) }
    i
  }
}

class ActorD extends Actor with ActorLogging {
  override def receive = {

    case Result(x, s, d) =>
      log.info("Got result from {}:{}:{}", x, s, d)
      operation()
      sender() ! Result(x,s,d)
  }

  def operation() = {
    println("Some operation")
  }
}