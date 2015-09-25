package converter.client.modules

import autowire._
import converter.client.services.AjaxClient
import converter.shared.Api
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import boopickle.Default._
object HTML2VDOM {

  class Backend(t: BackendScope[MainRouter.Router, State]) {
    def changedHTML(event: ReactEventI): Unit = {
        t.modState(_.copy(htmlCode = event.currentTarget.value))
    }

    def onClick(event: ReactEvent): Unit = {
      AjaxClient[Api].toVDOM(t.state.htmlCode).call().foreach { vdom =>
        t.modState(_.copy(vdomCode = vdom))
      }
    }
  }


  case class State(htmlCode:String="", vdomCode:String="")
  // create the React component for Dashboard
  val component = ReactComponentB[MainRouter.Router]("HTML 2 VDOM")
    .initialState(State())
    .backend(new Backend(_))
    .render((router,S,B) => {
    // get internal links
    <.div(
      <.h2("HTML to ScalaJS-React VDOM"),
    <.div(^.cls:="row",
      <.div(^.cls:="col-md-6",
      <.h3("HTML Code"),
      <.textarea(^.cls:="col-md-12", ^.rows:=10, ^.onChange ==> B.changedHTML),
        <.button(^.cls:="col-md-12", ^.onClick ==> B.onClick, "Convert HTML -> VDom")
      ),
      <.div(^.cls:="col-md-6",
        <.h3("VDOM"),
        <.textarea(^.cls:="col-md-12", ^.rows:=10, ^.value := S.vdomCode))
    )
    )
  }).build
}