package com.karma.akka.actor

import akka.actor.{ ActorSystem, Props, ActorRef }

object StartConversationSystem extends App {
  
  val system: ActorSystem = ActorSystem("Conversation-System")
  
  val printer: ActorRef = system.actorOf(Props[PrinterActor], "printer")
  val greeter: ActorRef = system.actorOf(Props(new GreetActor(printer)), "GreetActor")

  //akka://Conversation-System
  println(system)
  //akka://Conversation-System/user/printer
  println(printer)
  //akka://Conversation-System/user/GreetActor
  println(greeter)
  
  //! is equivalent of tell
  greeter.tell(Greet, greeter)  
  greeter.tell(DontTalk, greeter)
  greeter.tell(KeepQuiet, greeter)
  greeter.tell(Echo, greeter)
  
  //val terminated = system.terminate()

}