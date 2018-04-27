import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.http.scaladsl.model.HttpMethods._
import akka.event.{LogSource, Logging}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage, UpgradeToWebSocket}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._

object Attempt extends Json {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val context: ExecutionContextExecutor = system.dispatcher

  implicit val logSource: LogSource[AnyRef] = new LogSource[AnyRef] {
    def genString(o: AnyRef): String = o.getClass.getName

    override def getClazz(o: AnyRef): Class[_] = o.getClass
  }

  val handshaker: ActorRef = system.actorOf(Props[Handshaker], "handshaker")
  val wsRequester: ActorRef = system.actorOf(Props[WSRequester], "wsRequester")
  val nodeKeeper: ActorRef = system.actorOf(Props[WSRequester], "nodeKeeper")

  //val complexActor: ActorRef = system.actorOf(Props(classOf[ComplexActor], 8999), "complexActor")

  val host: String = Config.getString("http.host")
  val port: Int = Config.getInt("http.port")
  val otherNodes: Set[Int] = Set(8997, 8998, 8999).diff(Set(port))
  val complexActors: mutable.Buffer[ActorRef] = otherNodes.toBuffer[Int].map(
    node => system.actorOf(Props(classOf[ComplexActor], node), s"complexActor$node")
  )
  val log = Logging(system, this)

  system.scheduler.schedule(30000 seconds, 60 seconds) {
    handshaker ! "Try to shake hand!"
    wsRequester ! "wsRequest!"
  }

  def main(args: Array[String]): Unit = {
    log.info(s"Start of app on port $port.")
    complexActors.foreach(actor => system.log.info(actor.toString()))
    val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] = Http().bind(host, port)
    val bindingFuture: Future[Http.ServerBinding] =
      serverSource.to(Sink.foreach { connection =>
        log.info("New request from " + connection.remoteAddress)
        connection handleWithSyncHandler requestHandler
      }).run()
  }

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) => HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
      "<html><body>Attempt app ver.0.1</body></html>"))
    case HttpRequest(GET, Uri.Path("/handshakeRequest"), _, _, _) => HttpResponse(entity = HttpEntity(
        ContentTypes.`application/json`, s"""{"system": "attempt", "port": $port, "message":"Ready"}"""))
    case HttpRequest(POST, Uri.Path("/handshakeRequest"), _, _, _) =>
      HttpResponse(entity = HttpEntity(ContentTypes.`text/xml(UTF-8)`,
        s"""<xml><system>attempt</system><port>$port</port><message>Ready</message></xml>"""))
    case request @ HttpRequest(GET, Uri.Path("/ws"), _, _, _) =>
      request.header[UpgradeToWebSocket] match {
        case Some(upgrade) => upgrade.handleMessages(webSocketService)
        case None => HttpResponse(400, entity = "Not a valid websocket request!")
      }
    case r: HttpRequest =>
      r.discardEntityBytes()
      HttpResponse(404, entity = "This endpoint is out of support.")
  }

  val webSocketService: Flow[Message, TextMessage, NotUsed] = Flow[Message].mapConcat {
    case tm: TextMessage => { log.info("Received message via WS"); TextMessage(Source.single("Hello ") ++ tm.textStream) :: Nil }
    case bm: BinaryMessage => bm.dataStream.runWith(Sink.ignore)
      Nil
  }


}