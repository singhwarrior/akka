package com.karma.akka.actor.overall

import akka.actor.{Actor,ActorLogging,Props,Terminated,ActorRef}

object VoiceActor{
  def props(echoActor : ActorRef) : Props = Props(new VoiceActor(echoActor))
}

class VoiceActor(echoActor : ActorRef) extends Actor with ActorLogging{
  override def preStart = {
    context.watch(echoActor)
  }
  
  override def receive = {
    case Terminated(echoActor) =>
      log.info("EchoActor stopped, ActorRef:{}", echoActor)
      context.stop(self)
  }
}