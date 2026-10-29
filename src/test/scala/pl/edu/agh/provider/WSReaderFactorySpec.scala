package pl.edu.agh.provider

import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.time.Instant
import org.apache.spark.SparkConf
import org.apache.spark.serializer.JavaSerializer
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.PartitionReaderFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import pl.edu.agh.model.TickerMessage

import scala.collection.mutable.ListBuffer

class WSReaderFactorySpec extends AnyFlatSpec with should.Matchers {

  // The serializer Spark uses to send tasks to executors
  private val serializer = new JavaSerializer(new SparkConf()).newInstance()

  private def ticker(productId: String, price: Double, time: String) =
    TickerMessage(Some("ticker"), Some(1L), Some(2L), Some(Timestamp.from(Instant.parse(time))),
      Some(productId), Some(price), Some("buy"), Some(0.01), Some(price - 1), Some(price + 1))

  private val streamer =
    WSMicroBatchStreamer[TickerMessage](2, "ticker", "ws://localhost:1", List("BTC-USD"))

  "createReaderFactory" should "return a factory that serializes without the streamer" in {
    val buffer = serializer.serialize(streamer.createReaderFactory())
    val bytes = new Array[Byte](buffer.remaining())
    buffer.get(bytes)
    new String(bytes, StandardCharsets.ISO_8859_1) should not include "WSMicroBatchStreamer"
  }

  it should "read the messages of a partition as rows" in {
    val factory = serializer.deserialize[PartitionReaderFactory](serializer.serialize(streamer.createReaderFactory()))
    val partition = WSInputPartition(ListBuffer(
      (ticker("BTC-USD", 80000.5, "2026-10-29T12:15:00.123456Z"), 0L),
      (ticker("BTC-EUR", 72000.25, "2026-10-29T12:15:01Z"), 1L)))
    val reader = factory.createReader(partition)
    val rows = ListBuffer[InternalRow]()
    while (reader.next()) rows.append(reader.get().copy())

    rows.map(_.getString(4)) shouldBe List("BTC-USD", "BTC-EUR")
    rows.map(_.getDouble(5)) shouldBe List(80000.5, 72000.25)
    rows.head.getLong(3) shouldBe Instant.parse("2026-10-29T12:15:00.123456Z").toEpochMilli * 1000 + 456
  }
}
