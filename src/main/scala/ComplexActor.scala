import scala.concurrent.ExecutionContext.Implicits.global
import Attempt.{materializer, otherNodes, system}
import akka.actor.Actor

import scala.concurrent.duration._
import org.json4s._
import org.json4s.jackson.JsonMethods._

case class Socket (port: Int, host: String = "0.0.0.0")
case class Connection (socket: Socket, rest: Boolean, ws: Boolean, lastMessage: LocalMessage)

class ComplexActor (port: Int) extends Actor {

  var connection: Connection = Connection(Socket(port), false, false, InfoMessage(""))

  def receive(): PartialFunction[Any, Unit] = {
    case m: LocalMessage =>
    case _ =>
  }
}
