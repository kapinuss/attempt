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
    case order: String => request()
    case _ =>
  }

  def request(): Unit = {
    otherNodes.foreach(portNode => {
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = s"http://localhost:$portNode/handshakeRequest"))
      responseFuture.onComplete {
        case Success(res) => {
          val response: Unit = println(res.entity.toStrict(1 second)(Attempt.materializer))
          //println("Port - " + response.port)
        }
        case Failure(_) =>
      }
    })
  }

  def parseResponseHandshake(json: String): HandshakeRequest = {
    implicit val formats = DefaultFormats
    parse(json).extract[HandshakeRequest]
  }
}
