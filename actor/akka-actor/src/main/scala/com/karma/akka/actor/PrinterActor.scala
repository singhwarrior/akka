package com.karma.akka.actor

import akka.actor.Actor

class PrinterActor extends Actor{
  def receive = {
    case s : String => println(s)
    case _ => println("Wrong Message")
  }
}