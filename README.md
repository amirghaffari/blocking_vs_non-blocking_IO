# Blocking vs Non-Blocking IO in Scala

This Scala application compares the scalability of the blocking IO model with the non-blocking IO model. To do so, I have developed an application that does both computational and IO tasks evenly. The application is implemented in both blocking and non-blocking IO models. In the blocking IO model, an actor calls the IO functions synchronously and waits for the execution to complete. However, in the non-blocking model, actors call the IO functions asynchronously and don't wait for the result. In this model, the function will return a [*Future*](http://docs.scala-lang.org/overviews/core/futures.html) immediately. A Future is the promise of a value that will eventually become available. The method *onSuccess* belongs to a Future will be called once the result becomes available. If a Futures fails, the method *onFailure* will be called to handle the failure.

There are two types of actor in the application, i.e. *Master* and *Worker*. The Master creates a number of worker actors based on the argument provided at the command line. If no argument is provided, the number of available cores on the machine is used for creating the worker actors.

Master also generates 100 computational and 100 IO tasks to distribute among the worker actors randomly. Initially, Master sends one task to each worker. A worker will send the task's results back to the master actor once the task is completed. The worker also will send a *Ready* message to the master to express its readiness for the next task. The master actor records the time and will send a new task to the worker if there is any task left undone. This will continue until all tasks are completed.

Once all the task are done, the master will print the statistical information, i.e. the number of seconds passed to complete all the tasks, and the average elapsed time for the computational and IO tasks.

A computational task calculates the value of [*Pi*](https://github.com/amirghaffari/Calculating-Pi-Using-Akka) for one of the following random inputs:
```(308836549,433275937,505880809,281779493,346726528,367281330, 497563398, 465600232, 526508662, 662777539)```

To simulate an IO task, the actor sleeps for one of the following random values (milliseconds):
```(5000, 5500, 6000, 6500, 7000, 5000, 5500, 6000, 6500, 7000)```



## Scalability Measurement
### Platform
All the experiments ran on a single machine with Intel Core i7-4790 Processor (8M Cache) and 12.0 GB RAM. The operating system is Windows 7 Professional.

### Results

The following graph compares the scalability of the blocking IO with the non-blocking IO model. Each experiment ran 5 times and the median value is used in the graphs for the sake of accuracy. In the scalability measurements, both the blocking and non-blocking models are run with different number of worker actors. The scalability graph shows the time that took to complete the 200 tasks with ```4,5,6,..., 24``` worker actors.

![Scalability Diagram](https://github.com/amirghaffari/blocking_vs_non-blocking_IO/blob/master/scalability.png "Scalability Diagram")

We see from the above scalability diagram that the non-blocking model (the purple curve) performs better than the blocking one (the green curve) when the number of worker actors are less that 22. For example, when we have 4 worker actors, the non-blocking is completed in 92 seconds, but the blocking model is completed in 238 seconds. The reason is that in the blocking model, approximately *50%* of the worker actors are blocked and waiting for the IO tasks' result, but in the non-blocking model, all the worker actors are performing actively as they run the IO tasks asynchronously.

However, when the number of worker actors increases, the performance of the blocking IO model improves and for 22 workers they show the same performance, i.e. it took 50 seconds to do all the 200 tasks. The reason is that when there are more worker actors, even when 50% of the workers are waiting for IO results in the blocking IO model, we still have enough worker actors to handle the computational tasks and leverage the available computational resources.

I conclude that when the blocking IO model is used, we need more worker actors to handle the computational tasks in comparison with the non-blocking IO model. The reason is that when IO tasks are called synchronously, we have more inactive actors, and so to leverage the available computational resources we need more actors.


## How to Run

```{r, engine='bash', count_lines}
git clone https://github.com/amirghaffari/blocking_vs_non-blocking_IO.git
cd blocking_vs_non-blocking_IO
sbt clean compile
sbt "run 10" 
```

**Note**) to switch between the blocking and non-blocking IO models, in the *build.sbt* file, change the comment accordingly as shown in the following screen shot

![switch between the blocking and non-blocking IO models](https://github.com/amirghaffari/blocking_vs_non-blocking_IO/blob/master/switch.png "switch between the blocking and non-blocking IO models")
