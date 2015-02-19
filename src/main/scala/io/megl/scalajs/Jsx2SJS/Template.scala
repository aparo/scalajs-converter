package io.megl.scalajs.Jsx2SJS

import scala.collection.mutable.ListBuffer

/**
 * Created by alberto on 19/02/15.
 */
object Template {
  val header=
    """
      |package chandu0101.scalajs.react.components.bootstrap
      |
      |import japgolly.scalajs.react._
      |
      |import scala.scalajs.js
      |import japgolly.scalajs.react.vdom.prefix_<^._
      |
      |
    """.stripMargin

  val classHeader=
    """
      |object |NAME| {
      |
      |  case class State()
      |
      |  class Backend(t: BackendScope[Props, State]) {
      |  }
      |
      |  val component = ReactComponentB[Props](|NAME|)
      |    .initialState(State())
      |    .backend(new Backend(_))
      |    .render((P, C, S, B) => {
    """.stripMargin

  val renderEnd=
    """
      |  }
      |    )
      |    .build
    """.stripMargin


    def render(reactClass:ReactClass):String={
      val code=new ListBuffer[String]()
      code +=header
      code +=classHeader.replace("|NAME|", reactClass.displayName)
      code += reactClass.renderCode
      code += renderEnd
      code += reactClass.extraCode
      code += reactClass.propertiesCode
      code += reactClass.applyCode

      code.mkString("")
    }
}
