import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

import Attempt.{otherNodes, system}
import scala.concurrent.duration._

import org.json4s._
import org.json4s.jackson.JsonMethods._

class Requester extends Actor {

  def receive(): PartialFunction[Any, Unit] = {
    case "Try to shake hand!" => request()
    case _ =>
  }

  def request(): Unit = {
    otherNodes.foreach(portNode => {
      val uri = s"http://localhost:$portNode/handshakeRequest"
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = uri))
      responseFuture.onComplete {
        case Success(res) => {
          val response: Unit = println(res.entity.toStrict(1 second)(Attempt.materializer))
        }
        case Failure(_) =>
      }
    })
  }

  def parseResponseHandshake(json: String): HandshakeRequest = {
    implicit val formats: DefaultFormats.type = DefaultFormats
    parse(json).extract[HandshakeRequest]
  }
}
