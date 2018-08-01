package converter.client

import converter.client.logger._
import converter.client.modules.MainRouter
import japgolly.scalajs.react.extra.router.Router
import org.scalajs.dom

import scala.scalajs.js.annotation._

object ScalaJSConverter {
  @JSExportTopLevel("ScalaJSConverter")
  protected def getInstance(): this.type = this

  @JSExport
  def main(): Unit = {
    log.warn("Application starting")

    AppCSS.load()

    val router = Router(MainRouter.baseUrl, MainRouter.routerConfig)
    // tell React to render the router in the document body
    router().renderIntoDOM(dom.document.getElementById("root"))

  }
}
