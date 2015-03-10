package converter.client

import converter.client.modules.MainRouter
import japgolly.scalajs.react.React
import japgolly.scalajs.react.extra.router.BaseUrl
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
 * Created by alberto on 09/03/15.
 */
@JSExport("ScalaJSConverter")
object ScalaJSConverter extends js.JSApp {
  @JSExport
  def main(): Unit = {

    // tell React to render the router in the document body
    React.render(MainRouter.routerComponent(), dom.document.body)
  }
}
