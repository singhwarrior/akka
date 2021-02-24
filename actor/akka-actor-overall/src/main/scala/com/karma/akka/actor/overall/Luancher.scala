package com.karma.akka.actor.overall

import akka.actor.ActorSystem
import akka.actor.PoisonPill
import scala.io.StdIn

object Luancher extends App {

  val echoSystem = ActorSystem("echo-system")
  try {
    
    val echoActor = echoSystem.actorOf(EchoActor.props())
    val voiceActor = echoSystem.actorOf(VoiceActor.props(echoActor))

    echoActor.tell("Hello", voiceActor)
    echoActor.tell("test1", voiceActor)
    echoActor.tell("test2", voiceActor)
    echoActor.tell("world1", voiceActor)
    echoActor.tell("world2", voiceActor)

    //echoActor ! PoisonPill
    
    StdIn.readLine()
  } finally {
    echoSystem.terminate()
  }

}