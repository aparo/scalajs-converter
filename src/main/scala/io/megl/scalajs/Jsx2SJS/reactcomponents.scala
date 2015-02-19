package io.megl.scalajs.Jsx2SJS

import org.mozilla.javascript.ast._
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

case class ReactProperty(name: String, `type`: String, default: Option[String] = None) {
}

case class ReactClass(name: String, astMethods: List[ObjectProperty]) {
  var properties = Map.empty[String, ReactProperty]
  buildProperties()

  def displayName: String = {
    astMethods.find(_.toSource().startsWith("displayName")) match {
      case Some(node) =>
        node.getRight.asInstanceOf[StringLiteral].getValue
      case None =>
        this.name
    }
  }

  def methodNames = astMethods.map(_.getLeft.asInstanceOf[Name].getIdentifier)

  lazy val methodsMap: Map[String, AstNode] = astMethods.map(mth => (mth.getLeft.asInstanceOf[Name].getIdentifier -> mth.getRight)).toMap

  /* we retrieve the mixins properties*/
  private def mixinsProperties: List[ReactProperty] = methodsMap.get("mixins") match {
    case Some(mixinNode) =>
      mixinNode.asInstanceOf[ArrayLiteral].getElements.flatMap {
        node =>
          val name = node.asInstanceOf[Name].getIdentifier
          BootstrapCommon.mixinProperties.getOrElse(name, Nil)
      }.toList
    case None =>
      Nil
  }

  private def propTypesProperties: List[ReactProperty] = methodsMap.get("propTypes") match {
    case Some(propTypeNode) =>
      propTypeNode.asInstanceOf[ObjectLiteral].getElements.map {
        propNode =>
          val name = propNode.getLeft.asInstanceOf[Name].getIdentifier
          val target = propNode.getRight.toSource.trim
          Generator.propTypesToScala.get(target) match {
            case Some(res) =>
              val (typ, default) = res
              ReactProperty(name, typ, default)
            case None =>
              throw new RuntimeException(s"Invalid $target")
          }
      }.toList
    case _ => Nil

  }

  private def getDefaultProperties: List[ReactProperty] = methodsMap.get("getDefaultProps") match {
    case Some(propTypeNode) =>
      propTypeNode.asInstanceOf[FunctionNode].getBody.asInstanceOf[Block].head.
        asInstanceOf[ReturnStatement].getReturnValue.asInstanceOf[ObjectLiteral].getElements.map {
        propNode =>
          val name = propNode.getLeft.asInstanceOf[Name].getIdentifier
          val target = propNode.getRight.toSource.trim
          ReactProperty(name, Generator.guessTypeByValue(target), Some(target.replace("'", "\"")))
      }.toList
    case _ => Nil

  }


  /* we build the properties */
  private def buildProperties(): Unit = {
    var properties = mixinsProperties.map(p => p.name -> p).toMap ++ propTypesProperties.map(p => p.name -> p).toMap
    val defaults = getDefaultProperties
    defaults.foreach {
      prop =>
        if (properties.contains(prop.name)) {
          val nProperty=properties(prop.name).copy(default = prop.default)
          properties += (prop.name -> nProperty)
        } else {
          properties += (prop.name -> prop)
        }
    }
    this.properties = properties.toMap
  }


  def renderCode:String={
    val code=new ListBuffer[String]()
    code += "/*"

    code += "*/"
    code.mkString("\n")
  }
}
