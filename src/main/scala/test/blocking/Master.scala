/**
@author Amir Ghaffari (amir_ghaffari@yahoo.com)
*/

package benchmark.blocking

import akka.actor._
import collection.mutable._
import scala.util.control._

case object Start
case object Done
case object Ready
case class Task(taskNode: TaskNode)

object NodeType extends Enumeration {
	val Computation, IO = Value
}

class TaskNode (argPosition:Int, argTaken:Boolean, argType:NodeType.Value, argValue:Int) {
	private var taken:Boolean=argTaken;
	private var position:Int=argPosition;
	private var nodeType:NodeType.Value=argType;
	private var value:Int =argValue;

	def isTaken(): Boolean = {
		return taken
	}
	def setTaken(taken:Boolean) {
		return this.taken=taken;
	}
	def getPosition(): Int = {
		return position
	}
	def getNodeType(): NodeType.Value = {
		return nodeType
	}
	def getValue(): Int = {
		return value
	}
}

class Master (numWorkers:Int) extends Actor {
	private final val WORKER_NUMBER = numWorkers;
	private final val TASK_NUMBER = 200
	private final val MAX_RANDOM_NUMBER  = 10
	private var num_done=0;
	private var startTime: Long=0;
	private var elapsedTime : Long = 0;
	private var sumElapsedIO: Long=0;
	private var sumElapsedComput: Long=0;
	private var numIO=0
	private var numComput=0
	private var taskList = new LinkedList[TaskNode]();
	private var taskNode : TaskNode = null;
	private val loop = new Breaks;
	private val computValues = Array(308836549,433275937,505880809,281779493,346726528,367281330, 497563398, 465600232, 526508662, 662777539)
	private val IOValues = Array(5000, 5500, 6000, 6500, 7000, 5000, 5500, 6000, 6500, 7000)

	def sendTask(actorRef: ActorRef) {
		loop.breakable {
			var found: Boolean =false;
			for (taskNode <- taskList){
				if(!taskNode.isTaken()){
					found=true;
					taskNode.setTaken(true);
					actorRef ! Task(taskNode)
					loop.break;
				}
			}
		}
	}

	override def receive: Receive = {
		case Start =>
			val numIO:Int=TASK_NUMBER/2; // a limit for the number of IO tasks
			val numComput:Int=TASK_NUMBER-numIO; // a limit for the number of computational tasks
			var countIO=0;
			var countComput=0;
			startTime=System.currentTimeMillis
			for (i <- 0 until TASK_NUMBER){
				val r = new scala.util.Random
				var next=r.nextInt(2); // select the type of task randomly (0:Computation, 1:IO)
				if(countComput==numComput) // if the number of computational tasks exceeds the limit, add only IO task
					next=1
				if(countIO==numIO) // if the number of IO tasks exceeds the limit, add only computational task
					next=0
				var index=r.nextInt(MAX_RANDOM_NUMBER);
				if(next==0){ // create a computational task
					var value=computValues(index) // select an integer from computValues to calculate Pi for
					taskNode = new TaskNode(i,false, NodeType.Computation, value);
					countComput+=1
				}
				else{ // create an IO task
					var value=IOValues(index) // select an amount from IOValues to wait to simulate an IO operation
					taskNode = new TaskNode(i,false, NodeType.IO, value);
					countIO+=1
				}
				taskList = taskList.append(LinkedList(taskNode));
			}
			for (i <- 0 until WORKER_NUMBER){
				var worker=context.actorOf(Props[Worker], name = "worker"+(i+1))
				sendTask(worker)
			}

	case (Done, time:Long, taskType: NodeType.Value) =>
		num_done+=1;
		if(taskType == NodeType.Computation){
			sumElapsedComput+=time;
			numComput+=1;
		}
		else{
			sumElapsedIO+=time;
			numIO+=1;
		}
		if(num_done == TASK_NUMBER){
			elapsedTime = System.currentTimeMillis - startTime;
			println("==================================== blocking IO =======================================================")
			println("Received "+num_done+" Shutting down... after "+elapsedTime/1000 + " seconds with "+WORKER_NUMBER+ " workers");
			println("Average elapsed time for "+numIO+" IO tasks is "+sumElapsedIO/numIO+" milliseconds.");
			println("Average elapsed time for "+numComput+" computational tasks is "+sumElapsedComput/numComput+" milliseconds.");
			context.system.shutdown();
		}

	case Ready =>
		sendTask(sender)

	case any:Any =>
		println("Invalid value recieved by master: "+ any )
	}
}

object Initializer extends App {
	var numWorkers=0;
	def toInt(s: String): Int = {
		try {
			s.toInt
		}
		catch {
			case e: Exception =>
				println("Invalid argument: "+s);
				System.exit(1)
				return 0
		}
  }

	if (args != null && args.length > 0) {
		numWorkers = toInt(args(0))
	}
	else {
		numWorkers = Runtime.getRuntime().availableProcessors() // if no argument is provided, the number of available cores on the machine is used
	}
	val system = ActorSystem("BlockingIO")

	// create the master actor to initiate the process
	val master = system.actorOf(Props(classOf[Master],numWorkers), name = "master")
	master ! Start;
}
