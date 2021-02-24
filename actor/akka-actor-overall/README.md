#AKKA Actor Concepts & Guidelines - Part 1(Theory)

###Actor System###
* Better to actor system one per application, More systems in same JVM will not hinder other but recommend to put them separate
* ActorSystem launches many threads

###Actor###
* A container for State, Behavior, a Mailbox, Child Actors and a Supervisor Strategy. All of this is encapsulated behind an Actor Reference
* Actor has lifecycle. It is not destroyed on its own. Programmer has to take care of destroying it

#####Actor References#####
* Via the actor reference only we send messages to actor, whether the actor is in same machine or in remote machine
* Actor does not expose its inner state but anything is done via Actor Reference

#####State#####
* Actor can have internal state. But we don't need to worry because each actor has its own loghtweight thread separate from 
other actors of the system 
* If Actor got stopped, the parent actor needs to restart it by setting the state from scratch
* We can also persist the state  and can be recovered the same state of actor by replaying

#####Behavior#####
* What to take action on recieving a message from other actor
* At runtime behavior can change, see become and unbecome

#####MailBox#####
* Per actor one mailbox queue
* Order of messages from same actor to another actor will be maintained
* Different actors sending message to same actor, order my differ(It depends on enqueue of mailbox)
* The order from multiple actors will be interleaved to a single target actor but will be ordered from that target
actor to another actor
* Different queues can be used for ordering. Then order will be according to that. e.g.: Priority MailBox. Default is FIFO

#####Child Actors#####
* Creation context.actorOf(...)
* context.stop(child)

#####Supervision Strategy#####
* See Supervision and Monitoring structure

#####Actor Termination#####
If actor is not restarted and stopped by its own or stopped by Supervisor. Then, the messages will be stored in Dead letters
of actor-system with actor reference

###Supervision and Monitoring###

#####What Supervision Means#####
* Resume the subordinate, keeping its accumulated internal state
* Restart the subordinate, clearing out its accumulated internal state
* Stop the subordinate permanently
* Escalate the failure, thereby failing itself

For supervision there is another mailbox which order is not like ordinary messages. 

#####Top Level Actors#####
* User Guardian(/user) => Guardian of user created actors. If it escalates a failure, root guardian(/) will be stop guardian and in turn stop whole actor system. Guardian’s supervisor strategy determines how the top-level normal actors are supervised. Since Akka 2.1 it is possible to configure this using the setting akka.actor.guardian-supervisor-strategy, which takes the fully-qualified class-name of a SupervisorStrategyConfigurator. 

* System Guardian(/system) => Parllel logging actors come under this, Logging remains active until root Guardian stop whole system
* root Guardian(/) => The root guardian is the grand-parent of all so-called “top-level” actors and supervises all the special actors mentioned in Top-Level Scopes for Actor Paths using the SupervisorStrategy.stoppingStrategy, whose purpose is to terminate the child upon any type of Exception. Root actor is called "Bubble Walker" because above it there is not actor
for supervision. 

#####What Restarting means#####
* Step 1:Suspend actor and all children recursively
* Step 2:Call old instance preReStart hook -> Defualt sending termination to all children and postStop
* Step 3:Wait for all children to stop
* Step 4:Create actor instance again
* Step 5:Invoke postRestart on new instance -> Default  calls preStart
* Step 6: send restart request to all children which were not killed in step 3; restarted children will follow the same process recursively, from step 2
* Step 7:resume actor

#####Monitoring#####
* Any Actor can monitor any actor. 
* It is different from supervision. Where actor escalates failure message to upper layer. 
* Monitoring actor will look for termination of the actor, whether it is dead or alive. So that it can decide to 
retry latter
* Monitoring actor has to listen for Terminated message from the monitored actor for which it has to invoke watch(monitoredActor)
* To stop listening it has to invoke unwatch(monitoredActor)

#####Delayed restarts with the BackoffSupervisor pattern#####
* Keep on trying restart an actor at delayed time intervals i.e. after 0.2s, 0.4s etc
* Could be applicable for some actor which connects with DB

#####OneForOneStrategy and AllForOneStrategy#####
* Default is OneForOneStrategy
* AllForOneStrategy is used when all children are tighlty coupled and stopping one actor can affect the functionality of all

###Actor References, Paths and Addresses###
* Special case of ActorRef
	* PromiseActorRef
	* DeadLetterActorRef
	* EmptyLocalActorRef
* Actor Reference cannot be created with an Actor but Path can be created with Actor instance
* Actor Path anchors:
	* "akka://my-sys/user/service-a/worker1"                   // purely local
	* "akka.tcp://my-sys@host.example.com:5678/user/service-b" // remote
* Logical Path represents the hierarchy of actors. But there is a physical path as well by which actor can be reached directly if it is running in different network host

#####Creating Actors and obtaining actor references#####
* Actors beneath guardian actor can be created using ActorSystem.actorOf
* Actors beneath an actor can be created using ActorContext.actorOf

#####Looking up actors using path#####
* ActorSystem.actorSelection(path)
* context.actorSelection(path) => context.actorSelection("/user/serviceA") [This is lookup using absolute path]
* context.actorSelection(path) => context.actorSelection("../serviceB") [This is lookup using relative path]
* context.actorSelection("../*") Will give ActorReferences to all actors which are sibling to it including it. [This is using regex or logical operators]

###Akka and the Java Memory Model###
* The actor send rule: the send of the message to an actor happens before the receive of that message by the same actor.
* The actor subsequent processing rule: processing of one message happens before processing of the next message by the same actor.

###Message Delivery Reliability###
* TO-DO

###Configuration###
* Basic default configurations are already there in reference.conf of akka library
* ActorSystem is the only consumer(or user) of configurations
* For user defined configurations, application.conf/application.json/application.properties should be kept at the root of classpath
 

