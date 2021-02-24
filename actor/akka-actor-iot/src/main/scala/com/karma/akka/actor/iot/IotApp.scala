package com.karma.akka.actor.iot

import akka.actor.ActorSystem
import scala.io.StdIn

object IotApp extends App {
  val iotSystem: ActorSystem = ActorSystem("iot-system")
  try {
    val iotSupervisor = iotSystem.actorOf(IotSupervisor.props(), "iot-supervisor")
    val deviceManager = iotSystem.actorOf(DeviceManager.props, "device-mgr")
    val sensorActor = iotSystem.actorOf(SensorActor.props, "sensor")
    deviceManager.tell(DeviceManager.RequestTrackDevice("group1", "device1"), sensorActor)
    deviceManager.tell(DeviceManager.RequestTrackDevice("group1", "device2"), sensorActor)
    deviceManager.tell(DeviceManager.RequestTrackDevice("group1", "device3"), sensorActor)
    deviceManager.tell(DeviceManager.RequestTrackDevice("group1", "device4"), sensorActor)

//    iotSystem.actorOf(DeviceGroupQuery.props("device1"->, 0, requester, timeout), name)DeviceGroupQuery
    StdIn.readLine()
  } finally {
    iotSystem.terminate()
  }

}