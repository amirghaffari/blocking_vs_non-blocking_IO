/**
@author Amir Ghaffari (amir_ghaffari@yahoo.com)
*/

package benchmark.nonblocking

import akka.actor._
import scala.concurrent.Future



class Worker (masterRef:ActorRef) extends Actor {
	val myself: ActorRef=self;
	val master:ActorRef=masterRef;
	override def receive: Receive = {
		case Task(taskNode) =>
			if(taskNode.getNodeType()==NodeType.Computation){
				var result=0.0;
				val value=taskNode.getValue();
				var start = System.currentTimeMillis
				result= calculatePiFor(value);
				var totalTime = System.currentTimeMillis - start;
				println("Calculated Pi for " + value + " is "+result+".\nElapsed time: %1d ms".format(totalTime))
				master ! (Done, totalTime, NodeType.Computation)
			}
			else if(taskNode.getNodeType()==NodeType.IO){
				import context.dispatcher
				val value=taskNode.getValue();
				val future = Future {
					var start = System.currentTimeMillis
					doIO(value); // run the IO operation in a Future asynchronously
					var totalTime = System.currentTimeMillis - start;
					(totalTime, value)
				}

				future onSuccess {
				   case (totalTime: Long, value:Int) =>
                       println("IO took " + value + " ms")
					   master ! (Done, totalTime, NodeType.IO)
				}

				future onFailure {
					case e: Exception =>
						println("Exception in Future: "+e.getMessage)
						sender() ! Status.Failure(e)
				}
			}
			else {
				println("Invalid node type: " + taskNode.getNodeType())
				sender() ! Status.Failure(new Exception("Invalid node type: " + taskNode.getNodeType()))
			}
			master ! Ready // ready to handle the next task

		case any:Any =>
			println("Invalid value recieved by "+ self.path.name+": "+ any )
	}

	def gcd(a: Int, b: Int): Int =
		if (b == 0) a else gcd(b, a % b)

	def calculatePiFor(input: Int): Double = {
		var acc = 0.0
		for (i ← 0 until input)
			acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1)
		acc
	}

	def doIO(input: Int) = {
		Thread.sleep(input);
	}
}
