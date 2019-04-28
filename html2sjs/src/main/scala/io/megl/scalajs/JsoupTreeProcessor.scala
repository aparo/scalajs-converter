package io.megl.scalajs

import io.megl.scalajs.HTML2SJS.HTMLOptions
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Node}
import org.jsoup.select.NodeVisitor

class JsoupTreeProcessor(html: String, option: HTMLOptions = HTMLOptions()) extends ConversionUtils {

  val doc: Document = Jsoup.parseBodyFragment(html)

  doc.traverse(new NodeVisitor() {
    def head(node: Node, depth: Int): Unit = {
      println(s"Entering tag: ${node.nodeName} $depth")
    }

    def tail(node: Node, depth: Int): Unit = {
      println(s"Exiting tag: ${node.nodeName} $depth")
    }
  })
}
