package converter.client

import converter.client.components.GlobalStyles
import converter.client.modules.MainRouter
import japgolly.scalajs.react.extra.router.Router
import org.scalajs.dom
import logger._
import scalacss.ProdDefaults._
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

object ScalaJSConverter {
  @JSExportTopLevel("ScalaJSConverter")
  protected def getInstance(): this.type = this

  @JSExport
  def main(): Unit = {
    log.warn("Application starting")

    import scalacss.ScalaCssReact._

    GlobalStyles.addToDocument()

    val router = Router(MainRouter.baseUrl, MainRouter.routerConfig)
    // tell React to render the router in the document body
    router().renderIntoDOM(dom.document.body)

  }
}
