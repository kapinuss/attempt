sealed trait Message

final case class HandshakeRequest(system: String = "attempt", port: Long, message: String = "Ready") extends Message
final case class InfoMessage () extends Message
