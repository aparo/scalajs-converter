package io.megl.scalajs

import better.files._
import io.megl.scalajs.HTML2SJS.HTMLOptions

object MigrateTemplate extends App {

  HTML2SJS.main(Array("/opt/nttdata/sogei-awp/package"))
//  val path=File("/opt/nttdata/sogei-awp/package/")
//  path
//    .walk()
//    .filter(_.name.endsWith(".html"))
//  .foreach{
//    file =>
//      try{
//        println(file)
//        val options=HTMLOptions()
//        val result=new HtmlTreeProcessor(file.contentAsString)
//        result.bodyChildren
//
//      } catch {
//        case _ =>
//      }
////      val result=new HtmlTreeProcessor(file.contentAsString)
////      result.bodyChildren.foreach{
////        child =>
////          println(child.render(options))
////      }
//
//  }

}
