package io.megl.scalajs

import better.files._
import io.megl.scalajs.HTML2SJS.HTMLOptions

object MigrateTemplate extends App {

  val path=File("/opt/nttdata/sogei-awp/package/")
  path
    .walk()
    .filter(_.name.endsWith("_layout.html"))
  .foreach{
    file =>
      println(file)
      val options=HTMLOptions()
      val result=new HtmlTreeProcessor(file.contentAsString)
      result.bodyChildren.foreach{
        child =>
          println(child.render(options))
      }

  }

}
