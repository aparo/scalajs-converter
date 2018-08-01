package converter.client.modules

import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom


object MainRouter extends RoutingRules {
  val baseUrl = BaseUrl(dom.window.location.href.takeWhile(_ != '#'))

  // register the modules and store locations
  val html2vdomLoc = register(rootLocation(HTML2VDOM.component))
  val json2ccLoc = register(location("#json2cc", JSON2CC.component))
  val changeLogLoc = register(location("#changeLog", ChangeLog.component))

  def dashboard(content: TagMod*) = router.link(html2vdomLoc)(content)

  // initialize router and its React component
  val router = routingEngine(baseUrl)
  val routerComponent = Router.component(router)


  // redirect all invalid routes to dashboard
  override protected val notFound = redirect(html2vdomLoc, Redirect.Replace)

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
            MainMenu(MainMenu.Props(ic.loc, ic.router)),
            <.ul(^.cls := "nav navbar-nav navbar-right",
              <.li(<.a( ^.href :="#changeLog", "v.0.1.0")),
              <.li(<.a( ^.href :="https://github.com/aparo/scalajs-converter", ^.target := "_blank", "GitHub"))
            )
          )
        )
      ),
      // currently active module is shown in this container
      <.div(^.className := "container")(ic.element)
    )
  }
}
