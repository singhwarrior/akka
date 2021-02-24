#IOT Application to see actor hierarchy and design

###Actors###
#####IOTSupervisor Actor
It refers to the actor which is responsible for supervising all iot system actors

#####Device Manager Actor
This actor is the manager of all device group actors. This creates device group actor dynamically. 
That is, it registers all device group. Instead if tell forward has been used, that means the sender will
directly interact with device actor.

#####Device Group Actor
This actor is the group of all device actors. This creates device actor dynamically. Instead if tell forward has been used, 
that means the sender will directly interact with device actor.

#####Device Actor
It refers to the actor which is responsible for sensor data

###Guidelines to define level of hierarchy###
* In general, prefer larger granularity. Introducing more fine-grained actors than needed causes more problems than it solves.
* Add finer granularity when the system requires:
	* Higher concurrency.
	* Complex conversations between actors that have many states. We will see a very good example for this in the next chapter.
	* Sufficient state that it makes sense to divide into smaller actors.
	* Multiple unrelated responsibilities. Using separate actors allows individuals to fail and be restored with little impact on others.
