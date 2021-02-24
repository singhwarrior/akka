package com.karma.akka.actor

import akka.actor.{Actor,ActorRef}

class GreetActor(ackActor:ActorRef) extends Actor{
  def receive = {
    case Greet =>
      ackActor ! "Hello how are you!!"
    case DontTalk =>
      ackActor ! "Shutup!!"
    case KeepQuiet =>
      ackActor ! "zzzzzzzzzzz!!"
    case Echo =>
      ackActor ! "Shutup!!"
      
  }
}

case object Greet
case object DontTalk
case object KeepQuiet
case object Echo