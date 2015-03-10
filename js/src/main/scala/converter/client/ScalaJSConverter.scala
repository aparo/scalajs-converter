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
    // build a baseUrl, this method works for both local and server addresses (assuming you use #)
    val baseUrl = BaseUrl(dom.window.location.href.takeWhile(_ != '#'))
    val router = MainRouter.router(baseUrl)

    // tell React to render the router in the document body
    React.render(router(), dom.document.body)
  }
}
