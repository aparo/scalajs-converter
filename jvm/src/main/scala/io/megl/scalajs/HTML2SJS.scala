package io.megl.scalajs

import com.google.common.base.CaseFormat
import org.htmlcleaner._

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.xml.pull._


object HTML2SJS {

  case class HTMLOptions(i18n:Boolean=true)

  def processString(htmlCode: String, option:HTMLOptions=HTMLOptions()): String = {


    val xml = new XMLEventReader(Source.fromString(convertToXML(htmlCode)))

    var level = 0
    var contains = false
    val elementStack = new scala.collection.mutable.Stack[Int]
    val elementInStack = new scala.collection.mutable.Stack[Int]
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
        case EvElemStart(pre, label, attrs, _) =>
          var prefix = "  " * level
          if ((elementStack.nonEmpty && elementStack.top > 0) || (label == "li" && prevTag == "li")) {
            prefix = ", " + "\n" + prefix
          }
//          if(buf.nonEmpty && buf.last.trim.last==')')
//            prefix = ", " + "\n" + prefix

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

            if(option.i18n)
              buf += prefix + "i18n("+value +")"+ postfix
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
          buf += "\n"+prefix+ "/* " + t + " */\n"

        case _ => // ignore
      }
    }
    val noComma="""\)(\s*)<""".r
    val fontAwesome="""<\.i\(\^\.cls := "fa fa-(.*)"\)""".r

    var text=buf.mkString.split('\n').filter(_.nonEmpty).mkString("\n")
    text = noComma.replaceAllIn(text, m => s"),\n${m.group(1)}<")
    text = fontAwesome.replaceAllIn(text, m => s"FontAwesome.${m.group(1)}")
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

  def main(argv:Array[String]): Unit ={
    val html="""					<div class="profile-sidebar">
               |						<!-- PORTLET MAIN -->
               |						<div class="portlet light profile-sidebar-portlet">
               |							<!-- SIDEBAR USERPIC -->
               |							<div class="profile-userpic">
               |								<img src="../../assets/admin/pages/media/profile/profile_user.jpg" class="img-responsive" alt="">
               |							</div>
               |							<!-- END SIDEBAR USERPIC -->
               |							<!-- SIDEBAR USER TITLE -->
               |							<div class="profile-usertitle">
               |								<div class="profile-usertitle-name">
               |									 Marcus Doe
               |								</div>
               |								<div class="profile-usertitle-job">
               |									 Developer
               |								</div>
               |							</div>
               |							<!-- END SIDEBAR USER TITLE -->
               |							<!-- SIDEBAR BUTTONS -->
               |							<div class="profile-userbuttons">
               |								<button type="button" class="btn btn-circle green-haze btn-sm">Follow</button>
               |								<button type="button" class="btn btn-circle btn-danger btn-sm">Message</button>
               |							</div>
               |							<!-- END SIDEBAR BUTTONS -->
               |							<!-- SIDEBAR MENU -->
               |							<div class="profile-usermenu">
               |								<ul class="nav">
               |									<li class="active">
               |										<a href="extra_profile.html">
               |										<i class="icon-home"></i>
               |										Overview </a>
               |									</li>
               |									<li>
               |										<a href="extra_profile_account.html">
               |										<i class="icon-settings"></i>
               |										Account Settings </a>
               |									</li>
               |									<li>
               |										<a href="page_todo.html" target="_blank">
               |										<i class="icon-check"></i>
               |										Tasks </a>
               |									</li>
               |									<li>
               |										<a href="extra_profile_help.html">
               |										<i class="icon-info"></i>
               |										Help </a>
               |									</li>
               |								</ul>
               |							</div>
               |							<!-- END MENU -->
               |						</div>
               |						<!-- END PORTLET MAIN -->
               |						<!-- PORTLET MAIN -->
               |						<div class="portlet light">
               |							<!-- STAT -->
               |							<div class="row list-separated profile-stat">
               |								<div class="col-md-4 col-sm-4 col-xs-6">
               |									<div class="uppercase profile-stat-title">
               |										 37
               |									</div>
               |									<div class="uppercase profile-stat-text">
               |										 Projects
               |									</div>
               |								</div>
               |								<div class="col-md-4 col-sm-4 col-xs-6">
               |									<div class="uppercase profile-stat-title">
               |										 51
               |									</div>
               |									<div class="uppercase profile-stat-text">
               |										 Tasks
               |									</div>
               |								</div>
               |								<div class="col-md-4 col-sm-4 col-xs-6">
               |									<div class="uppercase profile-stat-title">
               |										 61
               |									</div>
               |									<div class="uppercase profile-stat-text">
               |										 Uploads
               |									</div>
               |								</div>
               |							</div>
               |							<!-- END STAT -->
               |							<div>
               |								<h4 class="profile-desc-title">About Marcus Doe</h4>
               |								<span class="profile-desc-text"> Lorem ipsum dolor sit amet diam nonummy nibh dolore. </span>
               |								<div class="margin-top-20 profile-desc-link">
               |									<i class="fa fa-globe"></i>
               |									<a href="http://www.keenthemes.com">www.keenthemes.com</a>
               |								</div>
               |								<div class="margin-top-20 profile-desc-link">
               |									<i class="fa fa-twitter"></i>
               |									<a href="http://www.twitter.com/keenthemes/">@keenthemes</a>
               |								</div>
               |								<div class="margin-top-20 profile-desc-link">
               |									<i class="fa fa-facebook"></i>
               |									<a href="http://www.facebook.com/keenthemes/">keenthemes</a>
               |								</div>
               |							</div>
               |       						</div>
               |						</div>""".stripMargin('|')

    println(processString(html))
  }
}
