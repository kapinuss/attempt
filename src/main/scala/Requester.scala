import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

import Attempt.system

class Requester extends Actor {
  def receive() = {
    case order: String =>
    case _ =>
  }

  def request(): Unit = {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://akka.io"))

    responseFuture.onComplete {
      case Success(res) => println(res)
      case Failure(_) => sys.error("something wrong")
    }


  }

}
