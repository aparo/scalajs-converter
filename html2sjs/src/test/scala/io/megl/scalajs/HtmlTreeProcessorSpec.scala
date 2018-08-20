package io.megl.scalajs

import org.scalatest._

import scala.collection.mutable.Stack

class HtmlTreeProcessorSpec extends FlatSpec with Matchers {


  "A HtmlTreeProcessorSpec" should "decode favicon" in {
    val source = """<h1><i class="fa fa-home"></i>User Interface Kit</h1>"""

    val result=new HtmlTreeProcessor(source)
    println(result.body)
    result should be ("""<.h1(FontAwesome.home, i18n("User Interface Kit"))""")
  }


}