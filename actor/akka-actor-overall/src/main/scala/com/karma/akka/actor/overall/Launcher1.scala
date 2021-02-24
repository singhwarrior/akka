package com.karma.akka.actor.overall

import akka.actor.ActorSystem
import akka.actor.PoisonPill
import scala.io.StdIn
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import akka.event.Logging
import akka.event.EventStream

object Launcher1 extends App {

  if (args.length != 1)
    System.exit(0)
  else {
    val env = args(0)
    println(env)
    val conf: Config = ConfigFactory.load()
    val someValue = conf.getInt("akka.env."+env+".some.value")
    val echoSystem = ActorSystem("echo-system", conf)
    val log = echoSystem.log

    try {
      log.info("someValue={}", someValue)
      val echoActor = echoSystem.actorOf(EchoActor.props())
      val voiceActor = echoSystem.actorOf(VoiceActor.props(echoActor))

      println(echoActor)
      println(voiceActor)

      echoActor.tell("Hello", echoActor)
      echoActor.tell("test1", voiceActor)
      echoActor.tell("test2", voiceActor)
      echoActor.tell("world1", voiceActor)
      echoActor.tell("world2", voiceActor)

      echoActor.tell(EchoActor, voiceActor)
      echoActor ! PoisonPill

      StdIn.readLine()
    } catch {
      case e: Exception =>
        println(e)
    } finally {
      echoSystem.terminate()
    }
  }

}