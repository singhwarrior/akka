package com.karma.akka.actor.router

import akka.actor.Actor

object App1ControlActor {
  case object Stop
}

class App1ControlActor extends Actor{
  import App1ControlActor._
  override def receive() = {
    case Stop =>
      println("Stopping application")
      System.exit(1)
  }
}