package com.karma.akka.actor.iot

import akka.actor.{Actor,ActorLogging, Props}
import com.karma.akka.actor.iot.DeviceActor.RespondTemperature
import com.karma.akka.actor.iot.DeviceActor.TemperatureRecorded

object IotSupervisor{
  def props() : Props = Props(new IotSupervisor)
}

class IotSupervisor extends Actor with ActorLogging{
  override def preStart = log.info("IoT Application started")
  override def postStop = log.info("IoT Application stopped")
  override def receive = {
    case RespondTemperature(requestId, value) =>
      log.info("request-id={}, current-temprature={}", requestId, value.getOrElse(0.0))
    case TemperatureRecorded(requestId) =>
      log.info("Temprature recorded for request-id={}", requestId)
  }

}