package io.megl.scalajs

import io.megl.scalajs.HTML2SJS.{HTMLOptions, convertToXML}

import scala.xml.{Node, XML}

case class HTMLNode(tag:String, attributes:Seq[(String,String)]=Nil,
                    children:Seq[HTMLNode]=Nil, ident:Int=0, text:Option[String]=None) {
  def isEmpty:Boolean=children.isEmpty && text.isEmpty && (tag!="#PCDATA") && attributes.isEmpty

  def nonEmpty:Boolean = !isEmpty
}


class HtmlTreeProcessor(htmlCode: String, option:HTMLOptions=HTMLOptions()) extends ConversionUtils {

  val root = processNode(XML.loadString(convertToXML(htmlCode)),0)

  private def processNode(node: Node, ident: Int, prevLabel: String = ""): Option[HTMLNode] = {

    val label = node.label
    label match {
      case "#PCDATA" =>
        val realText = node.text.replace("\n\t\n", "")

        val children= node.child
          .flatMap(c => processNode(c, ident + 1, prevLabel = label)).filter(_.nonEmpty)

        if(realText.trim.nonEmpty || children.nonEmpty) {
          Some(HTMLNode("#PCDATA", children=children, ident=ident,
            text = if(realText.isEmpty) None else Some(realText)))
        } else None

      case _ =>
        val attributes = node.attributes.asAttrMap.flatten {
          case (name, value) =>
            name match {
              case "class" =>
                List("^.cls" -> s"""\"$value\"""")
              case "type" =>
                List("^.tpe" -> s"""\"$value\"""")
              case "style" =>
                value.split(";").map(_.trim).filter(_.nonEmpty).map {
                  st =>
                    val tokens = st.split(":").map(_.trim)
                    s"""^.style.${convertCase(tokens(0))}""" -> s"""\"${tokens.drop(1).mkString(" ")}\""""
                }
              case s: String if s.startsWith("data-") =>
                List(s"""VdomAttr("$s")""" -> s"""\"$value\"""")
              case s: String if s.startsWith("aria-") =>
                List(s"""^.aria.${convertCase(s.replace("aria-", "")).toLowerCase()}""" -> s"""\"$value\"""")
              case default =>
                List(s"""^.$default""" -> s"""\"$value\"""")
            }
        }.toSeq

        val children = node.child
          .flatMap { c =>
            processNode(c, ident + 1, prevLabel = label)
          }

        Some(HTMLNode(label, children=children, attributes = attributes, ident=ident))
    }
  }

  def body:Seq[HTMLNode]=root.get.children.filter(_.tag=="body").flatten(_.children)
}
