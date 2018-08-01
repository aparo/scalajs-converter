package converter.client.modules

import autowire._
import converter.client.services.AjaxClient
import converter.shared.Api
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object HTML2VDOM {

  class Backend(t: BackendScope[Unit, State]) {
    def changedHTML(event: ReactEventFromInput) = {
      t.modState(_.copy(htmlCode = event.currentTarget.value))
    }

    def onClick(event: ReactEvent) = {
      Callback.future(
        AjaxClient[Api].toVDOM(t.state.runNow().htmlCode).call().map { vdom =>
          t.modState(_.copy(vdomCode = vdom))
        }
      )
    }

    def render(state: State) = {
      // get internal links
      <.div(
        <.h2("HTML to ScalaJS-React VDOM"),
        <.div(^.cls := "row",
          <.div(^.cls := "col-md-6",
            <.h3("HTML Code"),
            <.textarea(^.cls := "col-md-12", ^.rows := 10, ^.onChange ==> changedHTML),
            <.button(^.cls := "col-md-12", ^.onClick ==> onClick, "Convert HTML -> VDom")
          ),
          <.div(^.cls := "col-md-6",
            <.h3("VDOM"),
            <.textarea(^.cls := "col-md-12", ^.rows := 10, ^.value := state.vdomCode))
        )
      )
    }
  }


  case class State(htmlCode: String = "", vdomCode: String = "")

  // create the React component for Dashboard
  val component = ScalaComponent.builder[Unit]("HTML 2 VDOM")
    .initialState(State())
    .renderBackend[Backend]
    .build
}
