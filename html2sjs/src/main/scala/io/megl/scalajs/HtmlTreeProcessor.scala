package io.megl.scalajs

import io.megl.scalajs.HTML2SJS.{HTMLOptions, convertToXML, validateI18n}

import scala.collection.mutable.ListBuffer
import scala.xml.{Node, XML}


class HtmlTreeProcessor(htmlCode: String, option:HTMLOptions=HTMLOptions()) extends ConversionUtils {



  val root = {
    val xml=convertToXML(htmlCode)
      .replace("<!--", "<comment>")
      .replace("-->", "</comment>")
    println(xml)
    processNode(XML.loadString(xml),0)
  }

  private lazy val extractIncludeComment="""\[html-partial:include:\{"file":"(.*)"\}\]""".r


  private def processNode(node: Node, ident: Int, prevLabel: String = ""): Option[HTMLNode] = {
    val label = node.label
//    println(label)
    label match {
      case "#PCDATA" =>
        val realText = node.text.replace("\n\t\n", " ").replace("\\s+", " ")

        val children= node.child
          .flatMap(c => processNode(c, ident + 1, prevLabel = label)).filter(_.nonEmpty)


        if(realText.trim.nonEmpty || children.nonEmpty) {

          Some(TextNode(realText, children=children))
        } else None
      case "comment" =>
        val realText = node.text.replace("\n\t\n", " ").replace("\\s+", " ")
        if(realText.trim.nonEmpty) {
          val includes=extractIncludeComment.findAllIn(realText).matchData.map(_.group(1)).toList
          if(includes.nonEmpty){
            var entry=filenameToNode(includes.head)
            Some(RawNode(entry+"()"))
          } else {
            Some(CommentNode(realText.trim))
          }
        } else None
      case _ =>
        val realText = node.text.replace("\n\t\n", " ").replace("\\s+", " ")
//        println(node.label)
        val attributes:Seq[(String,String)] = node.attributes.asAttrMap.flatten {
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
                    fixStyle(s"""^.style.${convertCase(tokens(0))}""")  -> s"""\"${tokens.drop(1).mkString(" ")}\""""
                }
              case s: String if s.startsWith("data-") =>
                List(s"""VdomAttr("$s")""" -> s"""\"$value\"""")
              case s: String if s.startsWith("m-") =>
                List(s"""VdomAttr("$s")""" -> s"""\"$value\"""")
              case s: String if s.startsWith("aria-") =>
                List(s"""^.aria.${fixAria(convertCase(s.replace("aria-", "")).toLowerCase())}""" -> s"""\"$value\"""")
              case "checked" =>
                List("^.checked" -> toBoolean(value))
              case default =>
                List(s"""^.$default""" -> s"""\"$value\"""")
            }
        }.toSeq

        val children = node.child
          .flatMap { c =>
            processNode(c, ident + 1, prevLabel = label)
          }

        if(children.isEmpty){
          // possible font=
          if(label=="i" && attributes.exists( _._1 == "^.cls" )) {
            val cls=attributes.find( _._1 == "^.cls" ).map(_._2).map(s=> s.substring(1, s.length-1).trim).getOrElse("").split(' ').filter(_.nonEmpty)
            if(cls.contains("fa"))
              return Some(FontAwesome(cls.toList))
            else if(cls.contains("la"))
              return Some(LineAwesome(cls.toList))
            else if(cls.exists( _.startsWith("flaticon-")))
              return Some(FlatIcon(cls.toList))
          }
        }

        Some(GenericHTMLNode(label, children=children, attributes = attributes, ident=ident))
    }
  }

  def fixAria(attr:String):String={
    attr match {
      case "haspopup" => "hasPopup"
      case _ => attr
    }
  }

  def fixStyle(attr:String):String={
    attr match {
      case "^.style.WIDTH" => "^.width"
      case "^.style.MIN_HEIGHT" => "^.minHeight"
      case "^.style.MAX_HEIGHT" => "^.maxHeight"
      case "^.style.BACKGROUND_IMAGE" => "^.backgroundImage"
      case "^.style.MARGIN_TOP" => "^.marginTop"
      case _ => attr
    }
  }

  def toBoolean(str: String):String={
    str match {
      case "true"|"false" => str
      case "checked" => "true"
      case _ => str
    }
  }


  def body:HTMLNode=root.get.children.filter(_.tag=="body").flatten(_.children).head
  def bodyChildren:Seq[HTMLNode]=root.get.children.filter(_.tag=="body").flatten(_.children)
}
