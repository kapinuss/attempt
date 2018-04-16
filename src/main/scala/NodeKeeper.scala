import akka.actor._
import akka.persistence._
import scala.collection.parallel.mutable.ParHashSet

class NodeKeeper extends PersistentActor {

  val activeNodes: ParHashSet[Int] = ParHashSet.empty

  override def persistenceId = "nodeKeeper"

  val receiveRecover: Receive = {
    case command: String =>
    case _ =>
  }

  val receiveCommand: Receive = {
    case command: String =>

    case _ => println("")
  }
}
