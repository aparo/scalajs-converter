package converter.shared

trait Api {

  def toVDOM(name: String): String

  def toCaseClasses(rootName:String, jsonText: String): String

}
