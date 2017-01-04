/**
@author Amir Ghaffari (amir_ghaffari@yahoo.com)
*/

package benchmark.blocking

import akka.actor._

class Worker extends Actor {
	override def receive: Receive = {
		case Task(taskNode) =>
			if(taskNode.getNodeType()==NodeType.Computation){ // if the received task is computational
				var result=0.0;
				val value=taskNode.getValue();
				var start = System.currentTimeMillis
				result= calculatePiFor(value);
				var totalTime = System.currentTimeMillis - start;
				println("Calculated Pi for " + value + " is "+result+".\nElapsed time: %1d ms".format(totalTime))
				sender ! (Done, totalTime, NodeType.Computation)
			}

			if(taskNode.getNodeType()==NodeType.IO){  // if the received task is an IO task
				var result=0;
				val value=taskNode.getValue();
				var start = System.currentTimeMillis
				doIO(value);
				var totalTime = System.currentTimeMillis - start;
				println("IO took " + value + " ms")
				sender ! (Done, totalTime, NodeType.IO)
			}

			sender ! Ready // ready to handle the next task
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
