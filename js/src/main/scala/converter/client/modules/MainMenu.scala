package converter.client.modules

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.prefix_<^._
import rx._
import rx.ops._
import converter.client.components.Icon._
import converter.client.components._
import converter.client.services._
import converter.shared.TodoItem

object MainMenu {

  case class Props(activeLocation: MainRouter.Loc, router: MainRouter.Router)

  case class MenuItem(label: (Props) => ReactNode, icon: Icon, location: MainRouter.Loc)


  private val menuItems = Seq(
    MenuItem(_ => "Dashboard", Icon.dashboard, MainRouter.dashboardLoc)
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
}
