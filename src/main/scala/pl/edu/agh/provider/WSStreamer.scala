package pl.edu.agh.provider

import java.util

import org.apache.spark.sql.connector.catalog.{
  SupportsRead,
  Table,
  TableCapability
}
import org.apache.spark.sql.connector.read.streaming.MicroBatchStream
import org.apache.spark.sql.connector.read.{Scan, ScanBuilder}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import scala.jdk.CollectionConverters._
import pl.edu.agh.model._

class WSStreamer(override val schema: StructType, numPartitions: Int, schemaTag: String)
    extends Table
    with SupportsRead {

  override def name(): String = "ws://..."

  override def capabilities(): util.Set[TableCapability] = {
    Set(
      TableCapability.MICRO_BATCH_READ
    ).asJava
  }

  override def newScanBuilder(options: CaseInsensitiveStringMap): ScanBuilder =
    () => {
      val url = options.getOrDefault("url", "wss://ws-feed.exchange.coinbase.com")
      val products = Subscription.products(
        options.getOrDefault("products", Subscription.defaultProducts.mkString(","))
      )
      val wsMicroBatchStreamer = schemaTag match {
        case "heartbeat" => WSMicroBatchStreamer[HeartbeatMessage](numPartitions, schemaTag, url, products)
        case "status" => WSMicroBatchStreamer[StatusMessage](numPartitions, schemaTag, url, products)
        case "ticker" => WSMicroBatchStreamer[TickerMessage](numPartitions, schemaTag, url, products)
        case "level2" => WSMicroBatchStreamer[Level2Message](numPartitions, schemaTag, url, products)
        case "auction" => WSMicroBatchStreamer[AuctionMessage](numPartitions, schemaTag, url, products)
      }
      new Scan {
        override def readSchema(): StructType = schema

        override def toMicroBatchStream(
            checkpointLocation: String
        ): MicroBatchStream = 
          wsMicroBatchStreamer
      }
    }
}
