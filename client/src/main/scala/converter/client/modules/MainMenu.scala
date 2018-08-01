package converter.client.modules

import converter.client.modules.MainRouter.Loc
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import japgolly.scalajs.react.vdom.html_<^._
import converter.client.components.Icon.Icon
import converter.client.components._

object MainMenu {

  private val menuItems = Seq(
    MenuItem(_ => "HTML2VDOM", Icon.rotateLeft, MainRouter.Home),
    MenuItem(_ => "JSON 2 CaseClass", Icon.rotateRight, MainRouter.JSON2CCPage)

  )
  private val MainMenu = ScalaComponent.builder[Props]("MainMenu")
    .stateless.noBackend
    .renderP{($, props) =>
      <.ul(^.className := "nav navbar-nav")(
        // build a list of menu items
        menuItems.toTagMod{
          item =>
          <.li((^.className := "active").when(props.r.page == item.location),
          props.c.link(item.location)(item.icon, " ", item.label(props))
          )
        }
      )
    }
    .build

  def apply(props: Props) = MainMenu(props)

  case class Props(c: RouterCtl[Loc], r: Resolution[Loc])

  case class MenuItem(label: Props => VdomNode, icon: Icon, location: Loc)
}
