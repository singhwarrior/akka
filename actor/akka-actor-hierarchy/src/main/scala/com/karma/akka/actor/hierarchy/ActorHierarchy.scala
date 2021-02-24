

package com.karma.akka.actor.hierarchy

import akka.actor.{ActorSystem,Props,ActorRef}
import scala.io.StdIn

object ActorHierarchy extends App{
  
  val system : ActorSystem = ActorSystem("actor-system")
  val firstActor : ActorRef = system.actorOf(Props[FirstActor], "FIRST-ACTOR")
  
  //FIRST-ACTOR is the first actor below /user/
  println(firstActor)
  firstActor ! "next"  
  firstActor ! "nothing"
  
  println(">>> Press enter to terminate the program <<<")
  try StdIn.readLine()
  finally system.terminate()
}