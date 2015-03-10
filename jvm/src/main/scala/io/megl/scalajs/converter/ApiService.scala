package io.megl.scalajs.converter

import java.util.{Date, UUID}

import converter.shared._
import io.megl.scalajs.HTML2SJS

class ApiService extends Api {
  override def toVDOM(text: String): String = HTML2SJS.processString(text)

}
