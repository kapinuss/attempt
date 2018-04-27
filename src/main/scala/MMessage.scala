sealed trait MMessage

final case class HandshakeRequest(system: String = "attempt", port: Long, message: String = "Ready") extends MMessage
final case class InfoMessage (message: String) extends MMessage
