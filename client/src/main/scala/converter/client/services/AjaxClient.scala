package converter.client.services

import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import org.scalajs.dom

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue


class AjaxClient extends autowire.Client[Json, Decoder, Encoder] {

  override def doCall(req: Request): Future[Json] = {
    dom.ext.Ajax.post(
      url = "/api/" + req.path.mkString("/"),
      data = {
        val d: String = Json.fromFields(req.args).noSpaces
        d
      }
    ).map(r => {
      parse(r.responseText).right.get
    })
  }

  override def write[AnyClassToWrite: Encoder](obj: AnyClassToWrite): Json = {
    val w = obj.asJson
    w
  }

  override def read[AnyClassToRead: Decoder](json: Json): AnyClassToRead = {
    val e = json.as[AnyClassToRead]
    val r = e.right.get
    r
  }

}

object AjaxClient {
  def apply[Trait] = new AjaxClient()[Trait]
}