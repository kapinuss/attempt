import scala.concurrent.ExecutionContext.Implicits.global
import Attempt.{materializer, otherNodes, system}
import akka.actor.Actor

import scala.concurrent.duration._
import org.json4s._
import org.json4s.jackson.JsonMethods._

case class Connection (host: String = "0.0.0.0", port: Int, rest: Boolean, ws: Boolean, lastMessage: LocalMessage)

class ComplexActor (port: Int) extends Actor {

  var connection: Connection = Connection(port, false, false, )

  def receive(): PartialFunction[Any, Unit] = {
    case m: LocalMessage =>
    case _ =>
  }
}
