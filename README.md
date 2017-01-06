# Blocking vs Non-Blocking IO in Scala

This article compares the scalability of the blocking IO model with the non-blocking IO model in Scala. To do so, I have developed an application that performs both computational and IO tasks evenly. The application is implemented in both blocking and non-blocking IO models. In the blocking IO model, an actor calls the IO functions synchronously and waits for the execution to complete. However, in the non-blocking model, actors call the IO functions asynchronously and don't wait for the result. In this model, the function will return a [*Future*](http://docs.scala-lang.org/overviews/core/futures.html) immediately. A Future is the promise of a value that will eventually become available. If the Future computation completes successfully, the *onSuccess* method will be called. If the computation fails, the *onFailure* method is called to handle the failure.

There are two types of actors in the application, i.e. *Master* and *Worker*. The master actor creates a number of worker actors based on the argument provided at the command line. If no argument is provided, the number of available CPU cores on the executing machine will be used.

The master actor also generates 100 computational and 100 IO tasks to distribute among the worker actors randomly. Initially, master sends one task to each worker. A worker will send the task's results back to the master once the task is completed. The worker also will send a *Ready* message to the master to express its readiness for the next task. The master actor records the time and will send a new task to the worker if any undone task is available. This will continue until all the tasks, i.e. 100 computational and 100 IO tasks, are completed.

Once all the task are done, master will print the statistical information, i.e. the number of seconds elapsed to complete all the tasks, and the average elapsed time for the computational and IO tasks.

A computational task calculates the value of [*Pi*](https://github.com/amirghaffari/Calculating-Pi-Using-Akka) for one of the following inputs which is selected randomly:
```(308836549,433275937,505880809,281779493,346726528,367281330, 497563398, 465600232, 526508662, 662777539)```

To simulate an IO task, the actor sleeps for one of the following values (milliseconds) which is selected randomly :
```(5000, 5500, 6000, 6500, 7000, 5000, 5500, 6000, 6500, 7000)```



## Scalability Measurement
### Platform
All the experiments ran on a single machine with Intel Core i7-4790 Processor (8M Cache) and 12.0 GB RAM. The operating system is Windows 7 Professional.

### Results

The following graph compares the scalability of the blocking IO with the non-blocking IO models. Each experiment ran 5 times and the median value is used for higher accuracy. In the scalability measurements, both the blocking and non-blocking models ran with different number of worker actors. The scalability graph shows the time that it took to complete the 200 tasks with ```4,5,6,..., 24``` worker actors.

![Scalability Graph](https://github.com/amirghaffari/blocking_vs_non-blocking_IO/blob/master/scalability.png "Scalability Graph")

We see from the above scalability graph that the non-blocking model (the green curve) performs better than the blocking one (the purple curve) when the number of worker actors are less than 22. For example, the experiment for the non-blocking model with 4 workers completed in 92 seconds, but the blocking model completed in 238 seconds. The reason is that in the blocking model, approximately *50%* of the worker actors are blocked and waiting for the IO tasks results, but in the non-blocking model all the worker actors are performing actively as they run the IO tasks asynchronously.

However, when the number of worker actors increases, the performance of the blocking IO model improves better than the non-blocking one. For example, both models show the same performance for 22 worker actors, i.e. it took 50 seconds for both models to finish all the 200 tasks. The reason is that when there are more worker actors, even when 50% of the workers are waiting for the IO results in the blocking IO model, there are still enough worker actors to handle the computational tasks and leverage the available computational resources.

The conclusion is that when the blocking IO model is used, we need more worker actors to handle the computational tasks in comparison with the non-blocking IO model. The reason is that when IO tasks are called synchronously, there are more idle actors, and so more actors is needed to leverage the available computational resources. With proper number of actors the blocking model performs as good as the non-blocking model.


## How to Run

```{r, engine='bash', count_lines}
git clone https://github.com/amirghaffari/blocking_vs_non-blocking_IO.git
cd blocking_vs_non-blocking_IO
sbt clean compile
sbt "run 10" 
```

**Note**) to switch between the blocking and non-blocking IO models, in the *build.sbt* file, change the comment accordingly as shown in the following screen shot:

![switch between the blocking and non-blocking IO models](https://github.com/amirghaffari/blocking_vs_non-blocking_IO/blob/master/switch.png "switch between the blocking and non-blocking IO models")

**Note**) to run the experiments in a batch mode, use the `run.bat` file.


