package converter.client.modules

import autowire._
import converter.client.services.AjaxClient
import converter.shared.Api
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import boopickle.Default._

object JSON2CC {

  class Backend(t: BackendScope[MainRouter.Router, State]) {
    def changedJSON(event: ReactEventFromInput): Unit = {
      t.modState(_.copy(jsonCode = event.currentTarget.value))
    }

    def changedRootClassName(event: ReactEventFromInput): Unit = {
      t.modState(_.copy(rootClassName = event.currentTarget.value))
    }

    def onClick(event: ReactEvent): Unit = {
      AjaxClient[Api].toCaseClasses(t.state.rootClassName.trim, t.state.jsonCode.trim).call().foreach { ccCode =>
        t.modState(_.copy(ccCode = ccCode))
      }
    }
  }


  case class State(rootClassName: String = "RootClassName", jsonCode: String = "", ccCode: String = "")

  // create the React component for Dashboard
  val component = ScalaComponent.builder[MainRouter.Router]("HTML 2 VDOM")
    .initialState(State())
    .backend(new Backend(_))
    .render((router, S, B) => {
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
                ^.value := S.rootClassName,
                ^.onChange ==> B.changedRootClassName
              )), <.div(
              ^.cls := "form-group",
              <.label(
                ^.`for` := "jsonCode", "JSON CODE"
              ),
              <.textarea(^.cls := "col-md-12", ^.rows := 10, ^.onChange ==> B.changedJSON)),
            <.button(^.cls := "col-md-12", ^.onClick ==> B.onClick, "JSON -> Case Classes"))

        ),
        <.div(^.cls := "col-md-6",
          <.h3("Case Classes"),
          <.textarea(^.cls := "col-md-12", ^.rows := 10, ^.value := S.ccCode))
      )
    )
  }).build
}
