package converter.client.services

import org.scalajs.dom
import upickle._
import upickle.default._
import autowire._
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

object AjaxClient extends autowire.Client[String, Reader, Writer] {
  override def doCall(req: Request): Future[String] = {
    dom.ext.Ajax.post(
      url = "/api/" + req.path.mkString("/"),
      data = write(req.args)
    ).map(_.responseText)
  }

  def read[Result: Reader](p: String) = read[Result](p)
  def write[Result: Writer](r: Result) = write(r)
}
