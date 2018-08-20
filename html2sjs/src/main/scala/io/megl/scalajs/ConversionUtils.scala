package io.megl.scalajs

import com.google.common.base.CaseFormat

trait ConversionUtils {
  val DISABLE_IDENT_TAGS = Set("i", "b", "span", "meta", "title",
    "option", "h1", "h2", "h3", "h4", "h5", "h6", "h7", "#PCDATA")
  def convertCase(name: String) = {
    CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, name)
  }

  def validateI18n(text:String):String={
    var value=text.trim
    if (value.contains("\n")) {
      value = "\"" * 3 + value + "\"" * 3
    } else {
      value = "\"" + value + "\""
    }
    value
  }

}
