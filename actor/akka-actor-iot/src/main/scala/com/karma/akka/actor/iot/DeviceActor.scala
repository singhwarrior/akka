package com.karma.akka.actor.iot

import akka.actor.{ Actor, ActorLogging, Props }
import akka.actor.ActorRef

object DeviceActor {

  def props(groupId: String, deviceId: String): Props = Props(new DeviceActor(groupId, deviceId))

  final case class RecordTemperature(requestId: Long, value: Double)
  final case class TemperatureRecorded(requestId: Long)

  final case class ReadTemperature(requestId: Long)
  final case class RespondTemperature(requestId: Long, value: Option[Double])
}

class DeviceActor(groupId: String, deviceId: String) extends Actor with ActorLogging {
  import DeviceActor._

  var lastRecordedTemprature: Option[Double] = None

  override def preStart = log.info("Device actor {}-{} started", groupId, deviceId)
  override def postStop = log.info("Device actor {}-{} stopped", groupId, deviceId)
  override def receive = {
    case DeviceManager.RequestTrackDevice(`groupId`, `deviceId`) ⇒
      sender() ! DeviceManager.DeviceRegistered

    case DeviceManager.RequestTrackDevice(groupId, deviceId) ⇒
      log.warning(
        "Ignoring TrackDevice request for {}-{}.This actor is responsible for {}-{}.",
        groupId, deviceId, this.groupId, this.deviceId)

    case ReadTemperature(requestId) =>
      sender() ! RespondTemperature(requestId, lastRecordedTemprature)
    case RecordTemperature(requestId, value) =>
      lastRecordedTemprature = Some(value)
      sender() ! TemperatureRecorded(requestId)
  }
}