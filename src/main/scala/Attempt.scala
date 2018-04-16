import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.http.scaladsl.model.HttpMethods._
import akka.event.{LogSource, Logging}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object Attempt extends Json {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val context: ExecutionContextExecutor = system.dispatcher

  implicit val logSource: LogSource[AnyRef] = new LogSource[AnyRef] {
    def genString(o: AnyRef): String = o.getClass.getName

    override def getClazz(o: AnyRef): Class[_] = o.getClass
  }

  val requester: ActorRef = system.actorOf(Props[Requester], "requester")

  val host: String = Config.getString("http.host")
  val port: Int = Config.getInt("http.port")
  val otherNodes: Set[Int] = Set(8997, 8998, 8999).diff(Set(port))
  val log = Logging(system, this)

  system.scheduler.schedule(3 seconds, 60 seconds) {
    requester ! "Act"
  }

  def main(args: Array[String]): Unit = {
    log.info(s"Start of app on port $port.")
    val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] = Http().bind(host, port)
    val bindingFuture: Future[Http.ServerBinding] =
      serverSource.to(Sink.foreach { connection =>
        log.info("New request from " + connection.remoteAddress)
        connection handleWithSyncHandler requestHandler
      }).run()
  }

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      HttpResponse(entity = HttpEntity(
        ContentTypes.`text/html(UTF-8)`,
        "<html><body>Attempt app ver.0.1</body></html>"))
    case HttpRequest(GET, Uri.Path("/handshakeRequest"), _, _, _) =>
      HttpResponse(entity = HttpEntity(
        ContentTypes.`application/json`,
        s"""{"system": "attempt", "port": $port, "message":"Ready"}"""))
    case HttpRequest(POST, Uri.Path("/handshakeRequest"), _, _, _) =>
      HttpResponse(entity = HttpEntity(
        ContentTypes.`text/xml(UTF-8)`,
        s"""<xml><system>attempt</system><port>$port</port><message>Ready</message></xml>"""))
    case r: HttpRequest =>
      r.discardEntityBytes()
      HttpResponse(404, entity = "This endpoint is out of support.")
  }


}