package converter.client.modules

import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom


object MainRouter {
  sealed trait Loc
  case object Home extends Loc
  case object JSON2CCPage extends Loc
  case object ChangeLogPage extends Loc

  val baseUrl = BaseUrl(dom.window.location.href.takeWhile(_ != '#'))
  val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._
    ( staticRoute(root,     Home)  ~> render(HTML2VDOM.component())
      | staticRoute("#json2cc", JSON2CCPage) ~> render(JSON2CC.component())
      | staticRoute("#changeLog", ChangeLogPage) ~> render(ChangeLog.component())
      )
      .notFound(_ => redirectToPage(Home)(Redirect.Push))
      .setTitle(p => s"PAGE = $p | Example App")
      .renderWith(layout)
      .onPostRender((prev, cur) =>
        Callback.log(s"Page changing from $prev to $cur."))
  }

  /**
   * Creates the basic page structure under the body tag.
   *
   * @param ic
   * @return
   */
  def layout(c: RouterCtl[Loc], r: Resolution[Loc]) = {

    <.div(
      <.nav(^.className := "navbar navbar-inverse navbar-fixed-top")(
        <.div(^.className := "container")(
          <.div(^.className := "navbar-header")(<.span(^.className := "navbar-brand")("ScalaJS Converter")),
          <.div(^.className := "collapse navbar-collapse")(
            MainMenu(MainMenu.Props(c, r)),
            <.ul(^.cls := "nav navbar-nav navbar-right",
              <.li(<.a( ^.href :="#changeLog", "v.0.1.0")),
              <.li(<.a( ^.href :="https://github.com/aparo/scalajs-converter", ^.target := "_blank", "GitHub"))
            )
          )
        )
      ),
      // currently active module is shown in this container
      <.div(^.className := "container", r.render())
    )
  }
}
