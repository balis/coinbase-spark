package pl.edu.agh.model

import io.circe.{Decoder, Encoder}
import io.circe.parser.parse
import io.circe.syntax._
import java.sql.Timestamp
import io.circe._, io.circe.parser._
import java.time.Instant
import scala.util.Try

object Parser {

  def apply[T](
      json: String
  )(implicit decoder: Decoder[T]): Either[io.circe.Error, T] = {
    parse(json).flatMap(_.hcursor.as[T])
  }

  implicit val TimestampFormat : Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp] with Decoder[Timestamp] {
    override def apply(a: Timestamp): Json = Encoder.encodeLong.apply(a.getTime)

    override def apply(c: HCursor): Decoder.Result[Timestamp] =
      Decoder.decodeString.emapTry(s => Try(Timestamp.from(Instant.parse(s)))).apply(c)
  }
}

object Serializer {
  def apply[T](obj: T)(implicit encoder: Encoder[T]): String =
    obj.asJson.toString()
}
