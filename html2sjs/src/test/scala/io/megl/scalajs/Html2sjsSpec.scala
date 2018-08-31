package io.megl.scalajs

import org.scalatest._

class Html2sjsSpec extends FlatSpec with Matchers {


  "A html2sjs" should "decode favicon" in {
    val source = """<h1><i class="fa fa-home"></i>User Interface Kit</h1>"""
    val result=HTML2SJS.processString(source)
    result should be ("""<.h1(FontAwesome.home, i18n("User Interface Kit"))""")
  }

//  it should "throw NoSuchElementException if an empty stack is popped" in {
  //    val emptyStack = new Stack[Int]
  //    a [NoSuchElementException] should be thrownBy {
  //      emptyStack.pop()
  //    }
  //  }
}