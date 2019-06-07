package io.megl.scalajs

import com.google.common.base.CaseFormat
import org.htmlcleaner._

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.io.Source
import scala.xml.pull._
import better.files._
import io.megl.scalajs.HTML2SJS.HTMLOptions

import scala.xml.{Node, XML}

object HTML2SJS extends ConversionUtils {

  def main(argv: Array[String]): Unit = {
//    val destination=File("/tmp/html2sjs")
//    val destination=File("/opt/nttdata/libraries/scalajs-react-components/react-metronic/src/main/scala/react/metronic/partials")
    val destination=File("/tmp/partials")

    if(!destination.exists) destination.createDirectories()
    argv.foreach{
      entry=>
        val fileEntry=File(entry)
        if(fileEntry.isDirectory){
          fileEntry.walk()
//            .filter(_.name.endsWith("_layout.html"))
            .foreach{
              file =>

                processFile(file, destination /*file.parent*/)
            }

//          val dirDest=destination / fileEntry.name
//          if(!dirDest.exists) dirDest.toJava.mkdirs()
//
//          fileEntry.list.filter(_.name.toLowerCase.endsWith(".html"))
//            .foreach(f => processFile(f, dirDest))

        } else {
          processFile(fileEntry, destination /*file.parent*/)
        }

    }
  }

  private def processFile(srcFile:File, destDir:File): Unit ={
    try{
      val destFilename=destDir / (filenameToNode(srcFile.name)+ ".scala")
      println(s"Processing $srcFile $destFilename")
      val result=processString(srcFile.contentAsString, objectName=filenameToNode(srcFile.name))
      destFilename.write(result)

    } catch {
      case ex:Throwable =>
        println(ex)
    }
  }


  def processString(htmlCode: String, option: HTMLOptions = HTMLOptions(), objectName:String="Code"): String = {
    val processor=new HtmlTreeProcessor(htmlCode, option)
    val result:String=if(option.isFragment) {
      val nodes=processor.bodyChildren
      if(nodes.length==1)nodes.head.render(option).get
      else
        "VdomArray("+nodes.flatMap(_.render(option)).mkString(", ")+ ")"

    } else
      processor.root.get.render(option).get
    //println(result)
    //import react.icons.LineAwesome
    val imports=new ListBuffer[String]()
    imports ++= Seq(
        "japgolly.scalajs.react._",
      "japgolly.scalajs.react.vdom.html_<^._"
    )
    if(result.contains("LineAwesome")) imports += "react.icons.LineAwesome"
    if(result.contains("FontAwesome")) imports += "react.icons.FontAwesome"
    if(result.contains("FlatIcon")) imports += "react.icons.FlatIcon"

    if(result.contains("i18n")) imports += "web.I18N.i18n"


    val code = _root_.html2js.txt.fragment(
      option.pkg,
      objectName,
      imports.toList,
      result.replace("<.script(),", "")
    )

    println(code)
    org.scalafmt.Scalafmt.format(code.toString()).get

  }


  def processStringString(htmlCode: String, option: HTMLOptions = HTMLOptions()): String = {


    val xml = XML.loadString(convertToXML(htmlCode))

    def processNode(node:Node, ident:Int, prevLabel:String=""):String={
      val data=new ListBuffer[String]()

      val label=node.label
      val disableIdent=DISABLE_IDENT_TAGS.contains(label)
      var prefix=""
      if(!disableIdent && ident>0) {
        prefix="  "*ident
//        data += prefix
      }
      label match {
        case "#PCDATA" =>
          val realText=node.text.replace("\n\t\n", "")
          if(realText.trim.nonEmpty){

            if(option.i18n)
              data += s"""i18n(${validateI18n(realText)})"""
            else data += realText

          }
          val nodeMerger=if(disableIdent) ", " else ",\n"

          data += node.child
            .map(c => prefix+processNode(c, ident+ 1, prevLabel=label)).filter(_.nonEmpty).mkString(nodeMerger)

        case _ =>
          if(!DISABLE_IDENT_TAGS.contains(prevLabel)) data += "\n"+"  "*ident
          data += s"$prefix<.$label("
          val aPrefix=""
          val attributes = node.attributes.asAttrMap.flatten {
            case (name, value) =>
              name match {
                case "class" =>
                  List(aPrefix + s"""^.cls := \"$value\"""")
                case "type" =>
                  List(aPrefix + s"""^.tpe := \"$value\"""")
                case "style" =>
                  value.split(";").map(_.trim).filter(_.nonEmpty).map {
                    st =>
                      val tokens = st.split(":").map(_.trim)
                      aPrefix + s"""^.style.${convertCase(tokens(0))} := \"${tokens.drop(1).mkString(" ")}\""""
                  }
                case s: String if s.startsWith("data-") =>
                  List(aPrefix + s"""VdomAttr("$s") := \"$value\"""")
                case s: String if s.startsWith("m-") =>
                  List(aPrefix + s"""VdomAttr("$s") := \"$value\"""")
                case s: String if s.startsWith("aria-") =>
                  List(aPrefix + s"""^.aria.${convertCase(s.replace("aria-", "")).toLowerCase()} := \"$value\"""")
                case default =>
                  List(aPrefix + s"""^.$default := \"$value\"""")
              }
          }.filter(_.nonEmpty)
          if (attributes.nonEmpty) {
            data += attributes.mkString(", ")
          }

          val nodeMerger=if(disableIdent) ", " else ","

          val childrenNodes=node.child
            .map{c =>
              val result=processNode(c, ident+ 1, prevLabel=label)
              if(result.startsWith("i18n")|| result.startsWith("\"")) result
              else prefix+result
            }.filter(_.trim.nonEmpty)
          if(childrenNodes.nonEmpty){
            if(attributes.nonEmpty)
              data +=", "

            data += childrenNodes.mkString(nodeMerger)

          }

          data += ")"
      }


      data.filter(_.nonEmpty).mkString
    }




    val fontAwesome = """<\.i\(\^\.cls := "fa fa-(.*?)"\)""".r

    var text = processNode(xml, 0).trim
    println(text)
    text = fontAwesome.replaceAllIn(text, m => s"FontAwesome.${m.group(1).replace("-o", "O")}")
    text = text.substring(0, text.trim().length - 2)

    val body = "<.body("
    text = text.substring(text.indexOf(body) + body.length).trim

    text
  }


  def processStringPull(htmlCode: String, option: HTMLOptions = HTMLOptions()): String = {


    val xml = new XMLEventReader(Source.fromString(convertToXML(htmlCode)))

    var level = 0
    val elementStack = new scala.collection.mutable.Stack[Int]
    val tagStack = new scala.collection.mutable.Stack[String]
    val disableIdentStack = new scala.collection.mutable.Stack[Boolean]
    val buf = ArrayBuffer[String]()

    val DISABLE_IDENT_TAGS = Set("i", "b", "span", "meta", "title", "option", "h1", "h2", "h3", "h4", "h5", "h6", "h7")
    var prevTag = ""

    def convertCase(name: String) = {
      CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, name)
    }




    for (event <- xml) {
      event match {
        case EvElemStart(pre, label, attrs, _) =>
          var prefix = "  " * level
          if ((elementStack.nonEmpty && elementStack.top > 0) || (label == "li" && prevTag == "li")) {
            prefix = ", " + "\n" + prefix
          }
          //          if(buf.nonEmpty && buf.last.trim.last==')')
          //            prefix = ", " + "\n" + prefix

          var postfix = "\n"
          disableIdentStack.push(DISABLE_IDENT_TAGS.contains(label))
          if (disableIdentStack.head)
            postfix = ""

          val tag = prefix + s"<.$label(" + postfix
          level += 1
          buf += tag
          elementStack.push(attrs.asAttrMap.size)
          tagStack.push(label)
          prefix = "  " * level
          if (disableIdentStack.head)
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
                  List(prefix + s"""VdomAttr("$s") := \"$value\"""")
                case s: String if s.startsWith("aria-") =>
                  List(prefix + s"""^.aria.${convertCase(s.replace("aria-", "")).toLowerCase()} := \"$value\"""")
                case default =>
                  List(prefix + s"""^.$default := \"$value\"""")
              }
          }.filter(_.nonEmpty)
          if (t.nonEmpty) {
            buf += t.mkString(",\n")
          }

        case EvElemEnd(pre, label) =>
          var prefix = ""
          var postfix = "\n"
          elementStack.pop()
          if (elementStack.nonEmpty && elementStack.top > 0) {
            prefix = "\n" + "  " * level
          }
          if (disableIdentStack.head) {
            prefix = ""
          }
          prevTag = tagStack.pop()
          if (tagStack.nonEmpty) {
            val disableIdent = disableIdentStack.pop()
            if (disableIdent) {
              postfix = ""
            }
          }

          val tag = prefix + ")" + postfix
          buf += tag
          level -= 1

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
            if (disableIdentStack.head) {
              prefix = ""
            }
            if (elementStack.nonEmpty && elementStack.top > 0) {
              prefix = ", "
            }

            var postfix = "\n"
            if (disableIdentStack.head) {
              postfix = ""
            }

            var value = t.trim
            if (value.contains("\n")) {
              value = "\"" * 3 + value + "\"" * 3
            } else {
              value = "\"" + value + "\""
            }

            if (option.i18n)
              buf += prefix + "i18n(" + value + ")" + postfix
            else
              buf += prefix + value + postfix
            if (elementStack.nonEmpty) {
              val top = elementStack.top
              elementStack.pop()
              elementStack.push(top + 1)
            }
          }

        case EvComment(t) =>
          val prefix = "  " * level
          buf += "\n" + prefix + "/* " + t + " */\n"

        case _ => // ignore
      }
    }
    val noComma = """\)(\s*)<""".r
    val fontAwesome = """<\.i\(\^\.cls := "fa fa-(.*?)"\)""".r

    var text = buf.mkString.split('\n').filter(_.nonEmpty).mkString("\n")
    text = noComma.replaceAllIn(text, m => s"),\n${m.group(1)}<")
    text = fontAwesome.replaceAllIn(text, m => s"FontAwesome.${m.group(1).replace("-o", "O")}")
    text = text.substring(0, text.trim().length - 2)

    val body = "<.body("
    text = text.substring(text.indexOf(body) + body.length).trim

    text
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

  case class HTMLOptions(i18n: Boolean = true, pkg:String="react.metronic.partials", isFragment:Boolean=true)
}
