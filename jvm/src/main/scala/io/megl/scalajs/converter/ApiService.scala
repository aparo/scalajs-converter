package io.megl.scalajs.converter

import java.util.{Date, UUID}

import converter.shared._
import io.megl.scalajs.{JSON2CC, HTML2SJS}

class ApiService extends Api {
  override def toVDOM(text: String): String = HTML2SJS.processString(text)
  override def toCaseClasses(rootName:String, jsonText: String): String = JSON2CC.convertToCC(rootName, jsonText)

}
