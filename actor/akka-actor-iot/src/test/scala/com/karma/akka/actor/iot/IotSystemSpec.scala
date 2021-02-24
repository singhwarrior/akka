package com.karma.akka.actor.iot

import akka.testkit.{TestKit,TestProbe}
import akka.actor.ActorSystem
import org.scalatest.{Matchers,FlatSpecLike,BeforeAndAfterAll}

class IotSystemSpec(_system : ActorSystem) extends TestKit(_system) 
  with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll {
  
//  def this() = this(ActorSystem("iot-system-spec"))
//  
// 
//  override def afterAll = shutdown(system)
//  
//  "IOT-System Test" should "reply with latest temperature reading" in {
//    val probe = TestProbe()
//    
//    val deviceActor = system.actorOf(DeviceActor.props("group", "device"))
//    deviceActor.tell(DeviceActor.RecordTemperature(1, 24.0), probe.ref)
//    probe.expectMsg(DeviceActor.TemperatureRecorded(1))
//    
//    deviceActor.tell(DeviceActor.ReadTemperature(2), probe.ref)
////    val response = probe.expectMsgType[DeviceActor.RespondTemperature]
//    probe.expectMsg(DeviceActor.RespondTemperature(2,Some(24.0)))
////    response.requestId should === (2)
////    response.value should === (Some(24.0))
//
//    deviceActor.tell(DeviceActor.RecordTemperature(2, 25.0), probe.ref)
//    probe.expectMsg(DeviceActor.TemperatureRecorded(2))
//    
//    deviceActor.tell(DeviceActor.ReadTemperature(4), probe.ref)
////    val response1 = probe.expectMsgType[DeviceActor.RespondTemperature]
////    response1.requestId should === (4)
////    response1.value should === (Some(25.0))
//    probe.expectMsg(DeviceActor.RespondTemperature(4,Some(25.0)))    
//  }
  
}