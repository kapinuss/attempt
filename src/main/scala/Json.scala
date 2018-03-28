import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import akka.http.scaladsl.server.Directives

trait Json extends SprayJsonSupport with DefaultJsonProtocol {

  final case class HandshakeRequest(system: String = "attempt", port: Long, message: String = "Ready")
  final case class Order(id: String)

  implicit val handshakeRequestFormat: RootJsonFormat[HandshakeRequest] = jsonFormat3(HandshakeRequest)
  implicit val orderFormat: RootJsonFormat[Order] = jsonFormat1(Order)
}
