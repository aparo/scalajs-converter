package controllers

import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

trait JsonSerializers extends autowire.Serializers[Json, Decoder, Encoder] {
  override def write[AnyClassToWrite: Encoder](obj: AnyClassToWrite): Json = obj.asJson

  override def read[AnyClassToRead](json: Json)(implicit ev: Decoder[AnyClassToRead]): AnyClassToRead = {
    val either = ev.decodeJson(json)
    if (either.isLeft) throw new Exception(either.left.get)
    either.right.get
  }
}

object AutoWireServer extends autowire.Server[Json, Decoder, Encoder] with JsonSerializers