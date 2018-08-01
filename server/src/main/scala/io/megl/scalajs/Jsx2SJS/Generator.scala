package io.megl.scalajs.Jsx2SJS

/**
 * Code taken from reactjs-scalajs by chandu0101
 */
object Generator {

  val propTypesMap = Map(
    "React.PropTypes.array" -> "js.UndefOr[Array] = js.undefined",
    "React.PropTypes.bool" -> "js.UndefOr[Boolean] = js.undefined",
    "React.PropTypes.func" -> "js.UndefOr[js.Any] = js.undefined",
    "React.PropTypes.number" -> "js.UndefOr[Double] = js.undefined",
    "React.PropTypes.object" -> "js.UndefOr[Any] = js.undefined",
    "React.PropTypes.string" -> "js.UndefOr[String] = js.undefined",
    "React.PropTypes.node" -> "js.UndefOr[String] = js.undefined",
    "React.PropTypes.element" -> "js.UndefOr[VdomElement] = js.undefined",
    "React.PropTypes.array.isRequired" -> "js.UndefOr[Array]",
    "React.PropTypes.bool.isRequired" -> "js.UndefOr[Boolean]",
    "React.PropTypes.func.isRequired" -> "js.UndefOr[js.Any]",
    "React.PropTypes.number.isRequired" -> "js.UndefOr[Double]",
    "React.PropTypes.object.isRequired" -> "js.UndefOr[Any]",
    "React.PropTypes.string.isRequired" -> "js.UndefOr[String]",
    "React.PropTypes.node.isRequired" -> "js.UndefOr[String]",
    "React.PropTypes.element.isRequired" -> "js.UndefOr[VdomElement]",
    "React.PropTypes.any.isRequired" -> "js.UndefOr[js.Any]",
    "React.PropTypes.any" -> "js.UndefOr[js.Any]=js.undefined",
    "React.PropTypes.comp" -> "js.UndefOr[VdomElement]=js.undefined"
  )


  val propTypesToScala:Map[String,(String, Option[String])] = Map(
    "React.PropTypes.bool" -> ("Boolean", Some("false")),
    "React.PropTypes.func" -> ("REventIAny", Some("null")),
    "React.PropTypes.number" -> ("Int", Some("0")),
    "React.PropTypes.object" -> ("Any", Some("null")),
    "React.PropTypes.string" -> ("String", Some("\"\"")),
    "React.PropTypes.node" -> ("VdomElement", Some("null")),
    "React.PropTypes.element" -> ("VdomElement", Some("null")),
    "React.PropTypes.comp" -> ("TagMod", Some("null")),
    "React.PropTypes.any" -> ("Any", Some("null")),
    "React.PropTypes.css" -> ("CssClassType", Some("Map()")),
    "React.PropTypes.key" -> ("js.Any",Some("{}")),
    "React.PropTypes.ref" -> ("js.UndefOr[String]", Some("\"\"")),
    "React.PropTypes.array" -> ("Vector[_]", None),
    "React.PropTypes.arrayOf(React.PropTypes.number)" -> ("Vector[Int]",Some("Vector.empty[Int]")),
    "React.PropTypes.arrayOf(React.PropTypes.number).isRequired" -> ("Vector[Int]", None),
    "React.PropTypes.array.isRequired" -> ("Vector", None),
    "React.PropTypes.bool.isRequired" -> ("Boolean", None),
    "React.PropTypes.func.isRequired" -> ("REventIUnit", None),
    "React.PropTypes.number.isRequired" -> ("Int", None),
    "React.PropTypes.object.isRequired" -> ("Any", None),
    "React.PropTypes.string.isRequired" -> ("String", None),
    "React.PropTypes.node.isRequired" -> ("VdomElement", None),
    "React.PropTypes.element.isRequired" -> ("VdomElement", None),
    "React.PropTypes.comp.isRequired" -> (" TagMod", None),
    "React.PropTypes.any.isRequired" -> ("Any", None)
  )

  def guessTypeByValue(value:String):String={
    value match {
      case "true"|"false"=>"Boolean"
      case "null"=>"js.UndefOr[Any]"
      case s:String if value.startsWith("'")&&value.endsWith("'")=>
        "String"
      case s:String if value.startsWith("\"")&&value.endsWith("\"")=>
        "String"
      case s:String if s.forall(_.isDigit)=>
        "Int"
      case s:String =>
        "Double"
    }
  }
}
