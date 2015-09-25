package converter.client.modules

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import spatutorial.client.components.Icon.Icon
import spatutorial.client.components._

object MainMenu {

  private val menuItems = Seq(
    MenuItem(_ => "HTML2VDOM", Icon.rotateLeft, MainRouter.html2vdomLoc),
    MenuItem(_ => "JSON 2 CaseClass", Icon.rotateRight, MainRouter.json2ccLoc)

  )
  private val MainMenu = ReactComponentB[Props]("MainMenu")
    .stateless
    .render((P, _, B) => {
      <.ul(^.className := "nav navbar-nav")(
        // build a list of menu items
        for (item <- menuItems) yield {
          <.li((P.activeLocation == item.location) ?= (^.className := "active"),
            P.router.link(item.location)(item.icon, " ", item.label(P))
          )
        }
      )
    })
    .build

  def apply(props: Props) = MainMenu(props)

  case class Props(activeLocation: MainRouter.Loc, router: MainRouter.Router)

  case class MenuItem(label: (Props) => ReactNode, icon: Icon, location: MainRouter.Loc)
}
