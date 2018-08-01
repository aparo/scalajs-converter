package io.megl.scalajs.Jsx2SJS
//
//import org.mozilla.javascript.ast._
//import scala.collection.JavaConversions._
//import scala.collection.mutable.ListBuffer
//
case class ReactProperty(name: String, `type`: String, default: Option[String] = None)
//
//case class ReactClass(name: String, astMethods: List[ObjectProperty]) {
//  var properties = Map.empty[String, ReactProperty]
//  buildProperties()
//
//  def displayName: String = {
//    astMethods.find(_.toSource().startsWith("displayName")) match {
//      case Some(node) =>
//        node.getRight.asInstanceOf[StringLiteral].getValue
//      case None =>
//        this.name
//    }
//  }
//
//  def methodNames = astMethods.map(_.getLeft.asInstanceOf[Name].getIdentifier)
//
//  lazy val hasChildren: Boolean = methodsMap.exists(m => m._2.toSource().contains("this.props.children"))
//
//  lazy val methodsMap: Map[String, AstNode] = astMethods.map(mth => (mth.getLeft.asInstanceOf[Name].getIdentifier -> mth.getRight)).toMap
//
//  lazy val mixins: List[String] = methodsMap.get("mixins") match {
//    case Some(mixinNode) =>
//      mixinNode.asInstanceOf[ArrayLiteral].getElements.map {
//        node =>
//          node.asInstanceOf[Name].getIdentifier
//      }.toList
//    case None =>
//      Nil
//  }
//
//  /* we retrieve the mixins properties*/
//  private def mixinsProperties: List[ReactProperty] = mixins.flatMap(v => BootstrapCommon.mixinProperties.getOrElse(v, Nil))
//
//  private def propTypesProperties: List[ReactProperty] = methodsMap.get("propTypes") match {
//    case Some(propTypeNode) =>
//      propTypeNode.asInstanceOf[ObjectLiteral].getElements.map {
//        propNode =>
//          val name = propNode.getLeft.asInstanceOf[Name].getIdentifier
//          val target = propNode.getRight.toSource.trim
//          Generator.propTypesToScala.get(target) match {
//            case Some(res) =>
//              val (typ, default) = res
//              ReactProperty(name, typ, default)
//            case None =>
//              if(target.contains("React.PropTypes.oneOf")){
//                //                target.replace("(['prev', 'next'])")
//                val value=target.replace("React.PropTypes.oneOf", "").drop(2).dropRight(2).split(",").map(_.trim).head
//                ReactProperty(name, Generator.guessTypeByValue(value), None)
//              } else {
//                throw new RuntimeException(s"Invalid $target")
//
//              }
//          }
//      }.toList
//    case _ => Nil
//
//  }
//
//  private def getDefaultProperties: List[ReactProperty] = methodsMap.get("getDefaultProps") match {
//    case Some(propTypeNode) =>
//      propTypeNode.asInstanceOf[FunctionNode].getBody.asInstanceOf[Block].head.
//        asInstanceOf[ReturnStatement].getReturnValue.asInstanceOf[ObjectLiteral].getElements.map {
//        propNode =>
//          val name = propNode.getLeft.asInstanceOf[Name].getIdentifier
//          val target = propNode.getRight.toSource.trim
//          ReactProperty(name, Generator.guessTypeByValue(target), Some(target.replace("'", "\"")))
//      }.toList
//    case _ => Nil
//
//  }
//
//
//  /* we build the properties */
//  private def buildProperties(): Unit = {
//    var properties = mixinsProperties.map(p => p.name -> p).toMap ++ propTypesProperties.map(p => p.name -> p).toMap
//    val defaults = getDefaultProperties
//    defaults.foreach {
//      prop =>
//        if (properties.contains(prop.name)) {
//          val nProperty = properties(prop.name).copy(default = prop.default)
//          properties += (prop.name -> nProperty)
//        } else {
//          properties += (prop.name -> prop)
//        }
//    }
//    this.properties = properties.toMap
//  }
//
//  val dictRx = """classes\["(.*)"\]\s*=\s*(.*);""".r
//
//
//  private def cookCode(code: String): String = {
//    var res = code.replace("this.props.children", "C").replace("this.props.", "P.").replace("'", "\"")
//    res = dictRx.replaceAllIn(res, m => s"""classes += ("${m.group(1)}" -> ${m.group(2)})""").replace("{}", "Map()")
//    res.split("\n").map(_.stripSuffix(";")).mkString("\n")
//  }
//
//  def renderCode: String = {
//    val code = new ListBuffer[String]()
//    code += "/*"
//    methodsMap.get("render") match {
//      case Some(node) =>
//        code += cookCode(node.toSource(2))
//      case _ =>
//    }
//    code += "*/"
//    code.mkString("\n")
//  }
//
//  val REACTMETHODS = Set("render", "getDefaultProps", "propTypes", "displayName", "mixins")
//
//  def extraCode: String = {
//    val code = new ListBuffer[String]()
//    methodsMap.filterNot(p => REACTMETHODS.contains(p._1)).foreach {
//      case (name, node) =>
//        code += "/*"
//        code += s"  def $name("
//        code += cookCode(node.toSource(2))
//        code += "  )"
//        code += "*/"
//      case _ =>
//    }
//    code.mkString("\n")
//  }
//
//  def propertiesCode: String = {
//    val code = new ListBuffer[String]()
//    code += "case class Props("
//    val allProps = properties.values.toList.sortBy(_.name)
//    val validProps = new ListBuffer[String]()
//    allProps.filter(_.default.isEmpty).foreach {
//      prop =>
//        validProps += s"${prop.name}:${prop.`type`}"
//    }
//    allProps.filter(_.default.isDefined).foreach {
//      prop =>
//        validProps += s"${prop.name}:${prop.`type`}=${prop.default.get}"
//    }
//    code += validProps.grouped(3).map(_.mkString(", ")).mkString(",\n  ")
//    code += ") extends BoostrapMixinProps\n"
//    code.mkString("")
//  }
//
//
//  def applyCode: String = {
//    val code = new ListBuffer[String]()
//    code += "def apply("
//    val allProps = properties.values.toList.sortBy(_.name)
//    val validProps = new ListBuffer[String]()
//    allProps.filter(_.default.isEmpty).foreach {
//      prop =>
//        validProps += s"${prop.name}:${prop.`type`}"
//    }
//    allProps.filter(_.default.isDefined).foreach {
//      prop =>
//        validProps += s"${prop.name}:${prop.`type`}=${prop.default.get}"
//    }
//    validProps += "ref: js.UndefOr[String] = \"\""
//    validProps += "key: js.Any = {}"
//    code += validProps.grouped(3).map(_.mkString(", ")).mkString(",\n  ")+")"
//    if (this.hasChildren)
//      code += "(children: VdomNode*)"
//    code += "= {\n"
//    code += "   component.set(key, ref)(Props("
//    val signatures = new ListBuffer[String]()
//    allProps.filter(_.default.isEmpty).foreach {
//      prop =>
//        signatures += s"${prop.name} = ${prop.name}"
//    }
//    allProps.filter(_.default.isDefined).foreach {
//      prop =>
//        signatures += s"${prop.name} = ${prop.name}"
//    }
//
//    code += signatures.grouped(3).map(_.mkString(", ")).mkString(",\n  ")
//    code += ")"
//    if(this.hasChildren)
//      code += ",children"
//    code += ")\n"
//    code += "}\n"
//    code.mkString("")
//  }
//
//  /*
//  *   def apply(active: Boolean,
//              disabled: Boolean,
//              block: Boolean,
//              navItem: Boolean,
//              navDropdown: Boolean,
//              componentClass: VdomNode = null,
//              href: String = "",
//              target: String = "",
//              className: String = "",
//              bsClass: String = "", bsStyle: String = "", bsSize: String = "",
//              ref: js.UndefOr[String] = "", key: js.Any = {})(children: VdomNode*) = {
//      component.set(key, ref)(Props(active = active, disabled = disabled, block = block,
//        navItem = navItem, navDropdown = navDropdown,
//        componentClass = componentClass, href = href, target = target, className = className,
//        bsClass = bsClass, bsStyle = bsStyle, bsSize = bsSize
//      ), children)
//    }
//  */
//}
