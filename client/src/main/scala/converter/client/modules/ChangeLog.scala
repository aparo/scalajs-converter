package converter.client.modules

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

object ChangeLog {
  case class ChangeEntry(version:String, changes:Seq[String])

  val changes=List(ChangeEntry("0.1.0", List("Initial Release", "Added HTML 2 VDOM", "Added JSON to CaseClass (WIP)")))


  case class State()

  class Backend(t: BackendScope[MainRouter.Router, State]) {
  }

  // create the React component for Changelot
  val component = ReactComponentB[MainRouter.Router]("Changelog")
    .initialState(State())
    .backend(new Backend(_))
    .render((router, S, B) => {
    <.ul(changes.map {
      ch =>
        <.li(ch.version, <.ul(ch.changes.reverseMap(c => <.li(c))))
    })
  }
  ).build
}
