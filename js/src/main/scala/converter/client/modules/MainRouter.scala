package converter.client.modules

import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._

// define a trait to access all application routes
trait AppLinks {
  def dashboard(content: TagMod*): ReactTag
}

object MainRouter extends RoutingRules {
  // register the modules and store locations
  val dashboardLoc = register(rootLocation(Dashboard.component))

  def appLinks(router: Router): AppLinks = new AppLinks {
    override def dashboard(content: TagMod*) = router.link(dashboardLoc)(content)
  }

  // redirect all invalid routes to dashboard
  override protected val notFound = redirect(dashboardLoc, Redirect.Replace)

  /**
   * Creates the basic page structure under the body tag.
   *
   * @param ic
   * @return
   */
  override protected def interceptRender(ic: InterceptionR) = {
    <.div(
      <.nav(^.className := "navbar navbar-inverse navbar-fixed-top")(
        <.div(^.className := "container")(
          <.div(^.className := "navbar-header")(<.span(^.className := "navbar-brand")("ScalaJS Converter")),
          <.div(^.className := "collapse navbar-collapse")(
            MainMenu(MainMenu.Props(ic.loc, ic.router))
          )
        )
      ),
      // currently active module is shown in this container
      <.div(^.className := "container")(ic.element)
    )
  }
}
