sealed trait LocalMessage

final case class HandshakeRequest(system: String = "attempt", port: Long, message: String = "Ready") extends LocalMessage
final case class InfoMessage (message: String) extends LocalMessage
