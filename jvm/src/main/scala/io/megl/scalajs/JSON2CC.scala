package io.megl.scalajs

import com.google.common.base.CaseFormat
import play.api.libs.json._

import scala.collection.mutable.ListBuffer
import scala.io.Source

case class Variable(name: String, `type`: String, default: Option[String] = None, multiple:Boolean=false){
  def code:String={
    if(multiple)
      s"$nameVar:List[$typeVar]=Nil"
    else
      s"$nameVar:$typeVar=${getDefault}"
  }

  def getDefault:String={
    `type` match {
      case "Boolean" => "true"
      case "String" | "text" | "enum" | "time" | "duration" => "\"\""
      case "Int" => "0"
      case "Integer" => "0"
      case "Any" => "\"\""
      case "jobject" => "JsObject"
      case "Long" => "0"
      case "Number" => "0.0"
      case "Float" => "0.0"
      case "Double" => "0.0"
      case default => default

    }
  }

  def isBase=Variable.BaseType.contains(`type`)

  def nameVar=CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name)

  def typeVar=if(isBase)`type` else CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, `type`)

  def extractCode:String={
    if(!multiple){
      if(isBase)
        s"""json.as[${`type`}]("$name", $getDefault)"""
      else {
        s"""$typeVar.fromJsObj(json.as[js.Dictionary[Any]]("$name", js.Dictionary[Any]()))"""
      }
    }else {
      if(isBase)
        s"""json.asList("$name").map(_.asInstanceOf[${`type`}])"""
      else {
        s"""json.asListDict("$name").map($typeVar.fromJsObj).toList"""
      }
    }
  }
}

object Variable {
  val BaseType=Set("Boolean", "Int", "String", "Long", "Double", "Integer", "Float")
}

case class CClass(name: String, variables: List[Variable]){
  def code:String=
    s"case class $nameClass(${variables.map(_.code).mkString(", ")})"

  def codeObject:String={
    val code=new ListBuffer[String]()
    code += s"object $nameClass {\n"

    code += s"  def fromJsObj(json: js.Dictionary[Any]): $nameClass = {\n"
    code += s"    new $nameClass(\n"
    code += variables.map{
      variable =>
        s"      ${variable.nameVar} = ${variable.extractCode}"
    }.mkString(",\n")+"\n"
    code += s"    )\n"
    code += s"  }\n"

//      new Document(
//        string = json.as[String]("string", ""),
//        lang = json.as[String]("lang", ""),
//        originalText = json.as[String]("original_text", json.as[String]("string", "")),
//        sections = json.as[js.Array[js.Dictionary[Any]]]("sections", js.Array[js.Dictionary[Any]]()).map(sectionFromJson).toList,
//        entities = json.as[js.Array[js.Dictionary[Any]]]("entities", js.Array[js.Dictionary[Any]]()).map(entityFromJson).toList,
//        names = json.as[js.Array[js.Dictionary[Any]]]("names", js.Array[js.Dictionary[Any]]()).map(tokenFromJson).toList
//      )
//    }


    code += "\n}"
    code.mkString("")
  }

  def nameClass=CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name)

}

object JSON2CC extends App {

  val filename = args(0)
  val json = Json.parse(Source.fromFile(filename, "utf8").getLines().mkString)
  val classes = new ListBuffer[(String, CClass)]()

  def extractType(name:String, jsValue:JsValue):(String, Boolean)={
    jsValue match {
      case JsNull =>
        "Any" -> false
      case j: JsBoolean =>
        "Boolean" -> false
      case j: JsString =>
        "String"-> false
      case JsNumber(n) =>
        if(n.toString().contains(".")){
          "Double" -> false
        } else
          "Int" -> false
      case JsArray(values) =>
        if(values.isEmpty)
          "Any" -> true
        else
          extractType(name, values.head)._1 -> true
      case jo:JsObject =>
        val cls=extractClass(name, jo)
        classes += (name -> cls)
         cls.name -> true
    }
  }

  def extractClass(name: String, js: JsObject): CClass = {
    val variables = js.fields
      .flatMap {
      case (name, jsvalue) =>
        if(name=="mappings") None else{
          val (typ, multiple) = extractType(name, jsvalue)

          Some(Variable(name, typ, multiple = multiple))

        }
    }

    CClass(name, variables = variables.toList)
  }


  val res=extractClass("ClusterState", json.as[JsObject])
  classes += ("ClusterState" -> res)
  println(res)
  println(classes)

  classes.foreach{
    cc =>
      println(cc._2.code)
      println(cc._2.codeObject)
      println()
  }
}
