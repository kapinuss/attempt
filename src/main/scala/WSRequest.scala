import akka.{Done, NotUsed}
import akka.http.scaladsl.Http
import akka.stream.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._

import scala.concurrent.Future
import Attempt.{system, materializer}


object WSRequest {
  def main1(args: Array[String]) = {
    import system.dispatcher

    val printSink: Sink[Message, Future[Done]] = Sink.foreach {
      case message: TextMessage.Strict => println(message.text)
    }

    val helloSource: Source[Message, NotUsed] = Source.single(TextMessage("hello world!"))

    val flow: Flow[Message, Message, Future[Done]] = Flow.fromSinkAndSourceMat(printSink, helloSource)(Keep.left)

    val (upgradeResponse, closed) = Http().singleWebSocketRequest(WebSocketRequest("ws://echo.websocket.org"), flow)

    val connected = upgradeResponse.map { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) Done
      else throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
    }

    connected.onComplete(println)
    closed.foreach(_ => println("closed"))
  }
}