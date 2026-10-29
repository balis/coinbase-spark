package pl.edu.agh.provider

import io.circe.parser.parse
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class SubscriptionSpec extends AnyFlatSpec with should.Matchers {

  "products" should "split a comma-separated list and ignore spaces and empty entries" in {
    Subscription.products(" BTC-USD,ETH-EUR ,, SOL-USD") shouldBe List("BTC-USD", "ETH-EUR", "SOL-USD")
  }

  "defaultProducts" should "survive the round trip through the option string" in {
    Subscription.products(Subscription.defaultProducts.mkString(",")) shouldBe Subscription.defaultProducts
  }

  "message" should "subscribe the ticker channel to the given products" in {
    val json = parse(Subscription.message("ticker", List("BTC-USD", "ETH-EUR"))).toOption.get
    json shouldBe parse(
      """{"type": "subscribe", "channels": [{"name": "ticker", "product_ids": ["BTC-USD", "ETH-EUR"]}]}"""
    ).toOption.get
  }

  it should "subscribe the heartbeat channel to the given products" in {
    val json = parse(Subscription.message("heartbeat", List("BTC-USD"))).toOption.get
    json.hcursor.get[String]("type") shouldBe Right("subscribe")
    json.hcursor.downField("channels").downArray.get[String]("name") shouldBe Right("heartbeat")
    json.hcursor.downField("channels").downArray.get[List[String]]("product_ids") shouldBe Right(List("BTC-USD"))
  }

  it should "send nothing for the status and level2 schemas" in {
    Subscription.message("status", List("BTC-USD")) shouldBe ""
    Subscription.message("level2", List("BTC-USD")) shouldBe ""
  }
}
