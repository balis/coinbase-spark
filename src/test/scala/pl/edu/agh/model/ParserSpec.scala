package pl.edu.agh.model

import java.time.Instant
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ParserSpec extends AnyFlatSpec with should.Matchers {

  private def ticker(time: String) =
    s"""{"type":"ticker","trade_id":1,"sequence":2,"time":"$time","product_id":"BTC-EUR","price":"95000.5","side":"buy","last_size":"0.01","best_bid":"95000","best_ask":"95001"}"""

  private def parsedTime(time: String): Instant =
    Parser[TickerMessage](ticker(time)).toOption.get.time.get.toInstant

  "Parser" should "read ticker times on a 24-hour clock in UTC" in {
    parsedTime("2026-10-29T00:15:00.000000Z") shouldBe Instant.parse("2026-10-29T00:15:00Z")
    parsedTime("2026-10-29T12:15:00.000000Z") shouldBe Instant.parse("2026-10-29T12:15:00Z")
    parsedTime("2026-10-29T23:15:00.000000Z") shouldBe Instant.parse("2026-10-29T23:15:00Z")
  }

  it should "keep the microseconds" in {
    parsedTime("2021-11-01T12:00:00.123123Z") shouldBe Instant.parse("2021-11-01T12:00:00.123123Z")
  }

  it should "reject a malformed time" in {
    Parser[TickerMessage](ticker("yesterday")).isLeft shouldBe true
  }
}
