package pl.edu.agh.provider

import pl.edu.agh.model._

object Subscription {

  val defaultProducts: List[String] = List("ETH-BTC", "ETH-USD", "BTC-USD", "BTC-EUR")

  // Parses the comma-separated `products` option, e.g. "BTC-USD, ETH-EUR".
  def products(option: String): List[String] =
    option.split(",").map(_.trim).filter(_.nonEmpty).toList

  // Message sent when the websocket opens; status and level2 have no subscription.
  def message(schemaTag: String, products: List[String]): String =
    schemaTag match {
      case "heartbeat" => Serializer(ProtocolMessage("subscribe", List(Channel("heartbeat", products))))
      case "ticker"    => Serializer(ProtocolMessage("subscribe", List(Channel("ticker", products))))
      case "auction"   => Serializer(ProtocolMessage("auction", List(Channel("auctionfeed", products))))
      case "status"    => ""
      case "level2"    => ""
    }
}
