#AKKA ROUTER and TICK

###Actor Creation###

#####Actor with constructor arguments#####
* If constructor of actor has arguments then recommended way to create actor using companion object. See ReverseCounter Actor in Boot.scala	

```scala
	def props(count : Int) = Props(new ReverseCounter(count))
	val reverseCounter = system.actorOf(ReverseCounter.props(10),"reverseCounter")
```
	
* Constructor based actor creation can be done as follows also but not value classes like Int etc

```scala
	val reverseCounter = system.actorOf(Props[MyActor],arg1)
```

#####Actor without constructor arguments#####
* If constructor does not ask for an argument then following can be used as in Boot.scala the EchoCounter creation is done

```scala
	val echoCount = system.actorOf(Props[EchoCounter], "echoCounter")
```

###Actor Life Cycle###

Following are life cycle methods in an AKKA Actor

```scala
def preStart(): Unit = ()

def postStop(): Unit = ()

def preRestart(reason: Throwable, message: Option[Any]): Unit = {
  context.children foreach { child ⇒
    context.unwatch(child)
    context.stop(child)
  }
  postStop()
}

def postRestart(reason: Throwable): Unit = {
  preStart()
}
```

![Actor Life-Cycle] (docs/actor-life-cycle.png)

###Life Cycle Monitoring###

If some actor want to check whether another actor is alive or dead. Then it has to register for getting message called Terminated from that actor:

See how EchoCounter has registered for DecreamentCounter actor:

```scala
	case count : Int =>
      context.watch(sender())
      log.info("Received Count = {}", count)
      sender() ! Decreament
	case Terminated(actor) =>
      self ! PoisonPill
```
###Actor Selection###


```scala
	// will look all children to serviceB with names starting with worker
	context.actorSelection("/user/serviceB/worker*")
	// will look up all siblings beneath same supervisor
	context.actorSelection("../*")
```

Using identity ActorRef can be achieved using identity message 

```scala
	import akka.actor.{ Actor, Props, Identify, ActorIdentity, Terminated }

	class Follower extends Actor {
  		val identifyId = 1
  		context.actorSelection("/user/another") ! Identify(identifyId)

  		def receive = {
    			case ActorIdentity(`identifyId`, Some(ref)) ⇒
      			context.watch(ref)
      			context.become(active(ref))
    			case ActorIdentity(`identifyId`, None) ⇒ context.stop(self)
  		}

  		def active(another: ActorRef): Actor.Receive = {
    			case Terminated(`another`) ⇒ context.stop(self)
  		}
	}
```


###Send Messages###

#####Message Patterns#####
* ! means “fire-and-forget”, e.g. send a message asynchronously and return immediately. Also known as tell.
* ? sends a message asynchronously and returns a Future representing a possible reply. Also known as ask.

#####Tell:Fire-Forget#####

* Preferred way of sending messages. Non blocking wait for messages. It will not wait for some time.
* Can be used tell or !

```scala
	actorA.tell(message, actorB)
	actorA ! message //Implicit way of telling that sender is actorA itself
```

#####Ask:Send-And-Receive-Future#####

* It is also a asynchronous way of sending message which returns a future object
* pipeTo is onComplete handler, by which we can connect an actor
* timeout goes as implicit parameter, see Boot1.scala


```scala
	
  	implicit val timeout = Timeout(5 seconds)

	// Following is just saying that for actorA, actorB, actorC ask pattern has been used
	// Eventually all of them will return future with corresponding return value and when mapTo[Int] is called
	// it means the corresponding value is returned.
	// If the time taken by any of the actor to return message is more than timeout then it will send delivered to daed letters
  	val f: Future[Result] = for {
    		x <- (actorA ? Request).mapTo[Int]
    		s <- (actorB ask Request).mapTo[String]
    		d <- (actorC ? Request).mapTo[Double]
  	} yield Result(x, s, d)

  	val out = (f pipeTo actorD)
```

* The onComplete, onSuccess, or onFailure methods of the Future can be used to register a callback to get a notification when the Future completes, giving you a way to avoid blocking.

###Forward Messages###

You can forward a message from one actor to another. This means that the original sender address/reference is maintained even though the message is going through a ‘mediator’. This can be useful when writing actors that work as routers, load-balancers, replicators etc.

```scala
	target forward message
```

###Receive timeout###

TODO

###Timers, scheduled messages###

* See BootScheduler.scala
* Timers is used to have a scheduler within an actor
* TimerScheduler is not thread safe, hence we should be using it within the actor. Following is the TimeScheduler API,

```scala
	abstract def startPeriodicTimer(key: Any, msg: Any, interval: FiniteDuration): Unit
	//Start a periodic timer that will send msg to the self actor at a fixed interval.
```
###Stopping Actors###

There are three ways of stopping actors:

##### Using stop #####

* Best way to stop
* Example

```scala
class MyActor extends Actor {

  val child: ActorRef = ???

  def receive = {
    case "interrupt-child" ⇒
      context stop child

    case "done" ⇒
      context stop self
  }
}
```

* This procedure ensures that actor system sub-trees terminate in an orderly fashion, propagating the stop command to the leaves and collecting their confirmation back to the stopped supervisor. 
* invoking postStop, dumping mailbox, publishing Terminated on the DeathWatch, telling its supervisor)

##### PoisonPill #####

* PoisonPill is enqueued as ordinary messages and will be handled after messages that were already queued in the mailbox.
* Example:

```scala
	victim ! PoisonPill
```

##### Killing an Actor #####

```scala
	context.watch(victim) // watch the Actor to receive Terminated message once it dies
	victim ! Kill
	expectMsgPF(hint = "expecting victim to terminate") {
  		case Terminated(v) if v == victim ⇒ v // the Actor has indeed terminated
	}
```

##### Graceful Stop #####

* When gracefulStop() returns successfully, the actor’s postStop() hook will have been executed: there exists a happens-before edge between the end of postStop() and the return of gracefulStop()
* In the above example a custom Manager.Shutdown message is sent to the target actor to initiate the process of stopping the actor. You can use PoisonPill for this, but then you have limited possibilities to perform interactions with other actors before stopping the target actor. Simple cleanup tasks can be handled in postStop.
* Example:

```scala
	import akka.pattern.gracefulStop
	import scala.concurrent.Await

	try {
  		val stopped: Future[Boolean] = gracefulStop(actorRef, 5 seconds, Manager.Shutdown)
  		Await.result(stopped, 6 seconds)
  		// the actor has been stopped
	} catch {
  		// the actor wasn't stopped within 5 seconds
  		case e: akka.pattern.AskTimeoutException ⇒
	}
```

### Become/Unbecome ###

* At runtime we can change the receive functionality
* Can be used to implement the FSM(Finite State Machine)

```scala
	class HotSwapActor extends Actor {
  		import context._
  		def angry: Receive = {
    			case "foo" ⇒ sender() ! "I am already angry?"
    			case "bar" ⇒ become(happy)
  		}

  		def happy: Receive = {
    			case "bar" ⇒ sender() ! "I am already happy :-)"
    			case "foo" ⇒ become(angry)
  		}

  		def receive = {
    			case "foo" ⇒ become(angry)
    			case "bar" ⇒ become(happy)
  		}
	}
```

### Stash ###

* Invoking stash() adds the current message (the message that the actor received last) to the actor’s stash
* It is typically invoked when handling the default case in the actor’s message handler to stash messages that aren’t handled by the other cases

```scala
import akka.actor.Stash
class ActorWithProtocol extends Actor with Stash {
  def receive = {
    case "open" ⇒
      unstashAll()
      context.become({
        case "write" ⇒ // do writing...
        case "close" ⇒
          unstashAll()
          context.unbecome()
        case msg ⇒ stash()
      }, discardOld = false) // stack on top instead of replacing
    case msg ⇒ stash()
  }
}
```
### Message Dispatcher ###

TODO