package io.megl.scalajs

import io.megl.scalajs.HTML2SJS.HTMLOptions
import org.scalatest._

import scala.collection.mutable.Stack

class HtmlTreeProcessorSpec extends FlatSpec with Matchers {



  "A HtmlTreeProcessorSpec" should "decode favicon" in {
    val source = """<h1><i class="fa fa-home"></i>User Interface Kit</h1>"""
    val options=HTMLOptions()
    val result=new HtmlTreeProcessor(source)
    println(result.body)
    result.body.render(options) should be ("""<.h1(FontAwesome.home, i18n("User Interface Kit"))""")
  }


}