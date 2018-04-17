import akka.{Done, NotUsed}
import akka.http.scaladsl.Http
import akka.stream.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._

import scala.concurrent.Future
import Attempt.{materializer, system}
import akka.actor.Actor
import system.dispatcher

class WSRequester extends Actor  {

  val printSink: Sink[Message, Future[Done]] = Sink.foreach {
    case message: TextMessage.Strict => println(message.text)
  }

  val helloSource: Source[Message, NotUsed] = Source.single(TextMessage("hello world!"))

  val flow: Flow[Message, Message, Future[Done]] = Flow.fromSinkAndSourceMat(printSink, helloSource)(Keep.left)

  def receive(): PartialFunction[Any, Unit] = {
    case message: String => sendViaWS(message)
    case _ =>
  }

  def sendViaWS(message: String): Unit = {
    val (upgradeResponse, closed) = Http().singleWebSocketRequest(WebSocketRequest("ws://0.0.0.0:8998/ws"), flow)

    val connected = upgradeResponse.map { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) Done
      else throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
    }

    connected.onComplete(println)
    closed.foreach(_ => println("closed"))
  }
}