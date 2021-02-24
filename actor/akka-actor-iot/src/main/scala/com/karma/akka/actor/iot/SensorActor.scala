package com.karma.akka.actor.iot

import akka.actor.{Actor,ActorRef,ActorLogging,Props}
import akka.actor.Terminated
import akka.actor.PoisonPill

object SensorActor{
  def props = Props(new SensorActor())
}

class SensorActor extends Actor with ActorLogging {
  var requestId : Option[Long] = None
  override def receive = {
    case DeviceManager.DeviceRegistered =>
      requestId = Some(1)
      sender() ! DeviceActor.RecordTemperature(1, 24.0)
    case DeviceActor.TemperatureRecorded(requestId) =>
      if(requestId == this.requestId.getOrElse(0))
        log.info("Temprature Recorded by {} ",requestId)
        sender() ! PoisonPill
  }
}