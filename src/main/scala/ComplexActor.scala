import scala.concurrent.ExecutionContext.Implicits.global
import Attempt._
import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}

case class Socket (port: Int, host: String = "0.0.0.0")
case class Connection (socket: Socket, rest: Boolean, ws: Boolean, lastMessage: MMessage)

class ComplexActor (port: Int) extends Actor {

  system.scheduler.schedule(3 seconds, 20 seconds) {
    self ! InfoMessage("tick")
  }

  implicit var connection: Connection = Connection(Socket(port), rest = false, ws = false, lastMessage = InfoMessage(""))

  def receive(): PartialFunction[Any, Unit] = {
    case InfoMessage("tick") if connection.rest && !connection.ws => messageViaWS
    case InfoMessage("tick") if connection.rest => wsConnect
    case InfoMessage("tick") if !connection.rest => restConnect
    case m: MMessage =>
    case _ =>
  }

  def restConnect(implicit connection: Connection): Unit = {
    val responseFuture: Future[HttpResponse] = Http()
      .singleRequest(HttpRequest(uri = s"http://localhost:${connection.socket.port}/handshakeRequest"))
    responseFuture.onComplete {
      case Success(res) => {
        system.log.info(res.entity.toStrict(1 second)(materializer).toString)
        this.connection = connection.copy(rest = true)
      }
      case Failure(_) =>
    }
  }

  def wsConnect(implicit connection: Connection): Unit = {
    println("helloWS")
  }

  def messageViaWS(implicit connection: Connection): Unit = {
    println("messageViaWS")
  }
}
