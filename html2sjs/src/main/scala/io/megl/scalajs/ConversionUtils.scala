package io.megl.scalajs

import com.google.common.base.CaseFormat

trait ConversionUtils {


  lazy val DISABLE_IDENT_TAGS = Set("i", "b", "span", "meta", "title",
    "option", "h1", "h2", "h3", "h4", "h5", "h6", "h7", "#PCDATA")
  def convertCase(name: String) = {
    CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, name)
  }

  def validateI18n(text:String):String={
    var value=text.trim.replace("\\s+",  " ").replace('\n', ' ')
    value = "\"" * 3 + value + "\"" * 3
    value
  }

  //filename fixing
  lazy val rxMinux="""-(\w)""".r
  lazy val rxUnderscore="""_(\w)""".r

  def filenameToNode(str:String):String={
    var item=str.replace(".html", "").split('/').last
    rxMinux.findAllIn(item).matchData foreach {
      m => item=item.replace("-"+m.group(1), m.group(1).toUpperCase())
    }
    rxUnderscore.findAllIn(item).matchData foreach {
      m => item=item.replace("_"+m.group(1), m.group(1).toUpperCase())
    }

    item
  }



}
