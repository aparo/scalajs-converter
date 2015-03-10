package io.megl.scalajs

import java.io.File

import com.google.common.base.CaseFormat
import org.htmlcleaner._

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.xml.pull._


object HTML2SJS extends App {

  //  val filename = args(0)
  //  convertToXML(filename)
  def processString(htmlCode: String): String = {


    val xml = new XMLEventReader(Source.fromString(convertToXML(htmlCode)))

    var level = 0
    var contains = false
    val elementStack = new scala.collection.mutable.Stack[Int]
    val tagStack = new scala.collection.mutable.Stack[String]
    var disableIdent = false
    var buf = ArrayBuffer[String]()

    val DISABLE_IDENT_TAGS = Set("i", "span", "meta", "title", "option", "h1", "h2", "h3", "h4")
    var prevTag = ""

    def convertCase(name: String) = {
      CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, name)
    }


    for (event <- xml) {
      event match {
        case EvElemStart(pre, label, attrs, _) => {
          var prefix = "  " * level
          if ((elementStack.nonEmpty && elementStack.top > 0) || (label == "li" && prevTag == "li")) {
            prefix = ", " + "\n" + prefix
          }
          var postfix = "\n"
          disableIdent = DISABLE_IDENT_TAGS.contains(label)
          if (disableIdent)
            postfix = ""

          val tag = prefix + s"<.$label(" + postfix
          level += 1
          buf += tag
          elementStack.push(attrs.asAttrMap.size)
          tagStack.push(label)
          prefix = "  " * level
          if (disableIdent)
            prefix = ""

          val t = attrs.asAttrMap.flatten {
            case (name, value) =>
              name match {
                case "class" =>
                  List(prefix + s"""^.cls := \"$value\"""")
                case "type" =>
                  List(prefix + s"""^.tpe := \"$value\"""")
                case "style" =>
                  value.split(";").map(_.trim).filter(_.nonEmpty).map {
                    st =>
                      val tokens = st.split(":").map(_.trim)
                      prefix + s"""^.style.${convertCase(tokens(0))} := \"${tokens.drop(1).mkString(" ")}\""""
                  }
                case s: String if s.startsWith("data-") =>
                  List(prefix + s"""Attr("$s") := \"$value\"""")
                case s: String if s.startsWith("aria-") =>
                  List(prefix + s"""^.aria.${convertCase(s.replace("aria-", ""))} := \"$value\"""")
                case default =>
                  List(prefix + s"""^.$default := \"$value\"""")
              }
          }.filter(_.nonEmpty)
          if (t.nonEmpty) {
            buf += t.mkString(",\n")
          }
        }
        case EvElemEnd(pre, label) => {
          var prefix = ""
          var postfix = "\n"
          elementStack.pop()
          if (elementStack.nonEmpty && elementStack.top > 0) {
            prefix = "\n" + "  " * level
          }
          if (disableIdent) {
            prefix = ""
          }
          prevTag = tagStack.pop()
          if (tagStack.nonEmpty) {
            disableIdent = !DISABLE_IDENT_TAGS.contains(tagStack.top)
            if (disableIdent) {
              postfix = ""
            }
          }

          val tag = prefix + ")" + postfix
          buf += tag
          level -= 1
        }
        //      case e @ EvElemStart(_, tag, _, _) => {
        //        if (insidePage) {
        //          buf += ("<" + tag + ">")
        //        }
        //      }
        //      case e @ EvElemEnd(_, tag) => {
        //        if (insidePage) {
        //          buf += ("</" + tag + ">")
        //        }
        //      }
        case EvText(t) =>
          if (t.trim.nonEmpty) {
            var prefix = "  " * level
            if (elementStack.nonEmpty && elementStack.top > 0) {
              prefix = ", "
            } else if (disableIdent) {
              prefix = ""
            }
            var postfix = "\n"
            if (disableIdent) {
              postfix = ""
            }

            var value = t.trim
            if (value.contains("\n")) {
              value = "\"" * 3 + value + "\"" * 3
            } else {
              value = "\"" + value + "\""
            }

            buf += prefix + value + postfix
            if (elementStack.nonEmpty) {
              val top = elementStack.top
              elementStack.pop()
              elementStack.push(top + 1)
            }
          }

        case EvComment(t) =>
          buf += "\n/* " + t + " */\n"

        case _ => // ignore
      }
    }
    buf.mkString


  }


  def convertToXML(text: String): String = {
    // create an instance of HtmlCleaner
    val cleaner = new HtmlCleaner()

    // take default cleaner properties
    val props = cleaner.getProperties()

    // Clean HTML taken from simple string, file, URL, input stream,
    // input source or reader. Result is root node of created
    // tree-like structure. Single cleaner instance may be safely used
    // multiple times.
    val node = cleaner.clean(text)

    new PrettyXmlSerializer(props).getAsString(node)
  }

}
