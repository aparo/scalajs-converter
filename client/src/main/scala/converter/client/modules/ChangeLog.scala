package converter.client.modules

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object ChangeLog {
  case class ChangeEntry(version:String, changes:Seq[String])

  val changes=List(ChangeEntry("0.1.0", List("Initial Release", "Added HTML 2 VDOM", "Added JSON to CaseClass (WIP)")))


  // create the React component for Changelot
  val component = ScalaComponent.builder[Unit]("Changelog")
    .stateless
    .render(_ => {
    <.ul(changes.toTagMod {
      ch =>
        <.li(ch.version, <.ul(ch.changes.reverseMap(c => <.li(c)).toTagMod))
    })
  }
  ).build
}
