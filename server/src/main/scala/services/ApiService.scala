package services

import converter.shared._
import io.megl.scalajs.{HTML2SJS, JSON2CC}

class ApiService extends Api {
  override def toVDOM(text: String): String = HTML2SJS.processString(text)
  override def toCaseClasses(rootName:String, jsonText: String): String = JSON2CC.convertToCC(rootName, jsonText)

}
