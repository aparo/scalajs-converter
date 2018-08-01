package converter.client.modules

import autowire._
import converter.client.services.AjaxClient
import converter.shared.Api
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import boopickle._
import boopickle.Default._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

object JSON2CC {

  class Backend(t: BackendScope[Unit, State]) {
    def changedJSON(event: ReactEventFromInput) = {
      t.modState(_.copy(jsonCode = event.currentTarget.value))
    }

    def changedRootClassName(event: ReactEventFromInput) = {
      t.modState(_.copy(rootClassName = event.currentTarget.value))
    }

    def onClick(event: ReactEvent) = {
      val state = t.state.runNow()
      Callback.future(
        AjaxClient[Api].toCaseClasses(state.rootClassName.trim, state.jsonCode.trim).call().map { ccCode =>
          t.modState(_.copy(ccCode = ccCode))
        }
      )
    }

    def render(state: State) = {
      // get internal links
      <.div(
        <.h2("Json to CaseClass"),
        <.div(^.cls := "row",
          <.div(^.cls := "col-md-6",
            <.h3("JSON Code"),
            <.form(
              <.div(
                ^.cls := "form-group",
                <.label(
                  ^.`for` := "rootClassName", "Root Class Name"

                ),
                <.input(
                  ^.placeholder := "Enter Root Class Name",
                  ^.id := "rootClassName",
                  ^.cls := "form-control",
                  ^.tpe := "text",
                  ^.value := state.rootClassName,
                  ^.onChange ==> changedRootClassName
                )), <.div(
                ^.cls := "form-group",
                <.label(
                  ^.`for` := "jsonCode", "JSON CODE"
                ),
                <.textarea(^.cls := "col-md-12", ^.rows := 10, ^.onChange ==> changedJSON)),
              <.button(^.cls := "col-md-12", ^.onClick ==> onClick, "JSON -> Case Classes"))

          ),
          <.div(^.cls := "col-md-6",
            <.h3("Case Classes"),
            <.textarea(^.cls := "col-md-12", ^.rows := 10, ^.value := state.ccCode))
        )
      )
    }

  }


  case class State(rootClassName: String = "RootClassName", jsonCode: String = "", ccCode: String = "")

  // create the React component for Dashboard
  val component = ScalaComponent.builder[Unit]("HTML 2 VDOM")
    .initialState(State())
    .renderBackend[Backend]
    .build
}
