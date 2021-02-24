package com.karma.akka.actor.overall

import akka.actor.Actor
import akka.actor.PoisonPill
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.ActorSystem
import akka.actor.Terminated

object Boot extends App{
  val system = ActorSystem("reverse-counter-system")
  val echoCount = system.actorOf(Props[EchoCounter], "echoCounter")
  val reverseCounter = system.actorOf(ReverseCounter.props(10),"reverseCounter")
  reverseCounter.tell(ReverseCounter.Decreament, echoCount)
  
}



object ReverseCounter{
  def props(count : Int) = Props(new ReverseCounter(count))
  //Messages
  case object Decreament
}


class ReverseCounter(var count : Int) extends Actor with ActorLogging{
  
  import ReverseCounter._
  
  override def receive = {
    case Decreament =>
      if(count > 0){
        log.info("count={}", count)
        count = count-1
        sender() ! count
      }else
        self ! PoisonPill
      
  }
}

class EchoCounter extends Actor with ActorLogging{
  
  import ReverseCounter._

  override def receive = {
    case count : Int =>
      context.watch(sender())
      log.info("Received Count = {}", count)
      sender() ! Decreament
    case Terminated(actor) =>
      self ! PoisonPill
  }
}