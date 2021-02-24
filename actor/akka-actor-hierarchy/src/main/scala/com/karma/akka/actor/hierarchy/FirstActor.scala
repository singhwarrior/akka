package com.karma.akka.actor.hierarchy

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

class FirstActor extends Actor{
  def receive = {
    case "next" => 
      val secondActor : ActorRef = context.actorOf(Props[SecondActor], "SECOND-ACTOR")
      //SECOND-ACTOR is the first actor below /user/
      println(secondActor)
      secondActor ! "hello"
    case _ => 
      val thirdActor : ActorRef = context.actorOf(Props[SecondActor], "THIRD-ACTOR")
      //THIRD-ACTOR is the first actor below /user/
      println(thirdActor)
      thirdActor ! "world"
      
  }
}

class SecondActor extends Actor{
  def receive = {
    case "hello" => 
      println("Got Hello as message!!")
    case _ =>
      println("No hello as message")
      
  }
}