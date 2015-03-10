package io.megl.scalajs.Jsx2SJS

import java.io.{FileReader, File}

import io.megl.utils.FileUtils
import org.mozilla.javascript.{Token, Parser, CompilerEnvirons}
import org.mozilla.javascript.ast._
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.io.Source


object BootstrapCommon {
  val mixinProperties = Map(
    "BootstrapMixin" -> List(
      ReactProperty("bsClass", "String", Some("\"\"")),
      ReactProperty("bsStyle", "String", Some("\"\"")),
      ReactProperty("bsSize", "String", Some("\"\""))
    )

  )
}

object JSX2SJS extends App {
  val SOURCEDIR="/Users/alberto/sources/scala/scalajs/studiare/react-bootstrap/lib/"
  val OUTPUTDIR="/Users/alberto/Projects/scalajs/scalajs-react-components/core/src/main/scala/chandu0101/scalajs/react/components/bootstrap"
//  new File("/Users/alberto/sources/scala/scalajs/studiare/react-bootstrap/lib/ButtonGroup.js")
  new File(SOURCEDIR).listFiles().filter(_.getAbsolutePath.endsWith(".js")).foreach{
    file=>
      val name = file.getName.replace(".js", "")
      val lines = Source.fromFile(file, "utf8").getLines()
      //  val component=new ComponentDescription(lines.toList)

      //  println(component.name)

      def typeName(`type`: Int): String = Token.typeToName(`type`).toLowerCase


      case class Variable(ast: VariableInitializer) {
        lazy val name: String = {
          ast.getTarget match {
            case name: Name =>
              name.getIdentifier
          }
        }

        lazy val inizializerString = {
          if (ast.getInitializer == null)
            ""
          else {
            ast.getInitializer.toSource(0).trim
          }

        }

        def isRequire: Boolean = inizializerString.startsWith("require(")

        def isReactCreateClass: Boolean = inizializerString.startsWith("React.createClass")

        def getReactClass: Option[ReactClass] = {
          if (!isReactCreateClass) return None
          val elements = ast.getInitializer.asInstanceOf[FunctionCall].getArguments().head.asInstanceOf[ObjectLiteral].getElements.toList
          Some(ReactClass(this.name, elements))
        }

      }


      class ExtractorVisitor extends NodeVisitor {
        val variables = new ListBuffer[Variable]()

        @Override def visit(node: AstNode): Boolean = {
          val indent = "%1$Xs".replace("X", String.valueOf(node.depth() + 1)).format("")

          node match {
            case v: VariableDeclaration =>
              println(s"$indent varDec: ${typeName(v.getType)}")
              v.getVariables.foreach {
                initializer =>
                  variables += Variable(initializer)
              }
            //          return false
            case v: VariableInitializer =>
              println(s"$indent varInit: ${typeName(v.getType)}")
            case name: Name =>
              println(s"$indent name: ${name.getIdentifier}")
            case fc: FunctionCall =>
              println(s"$indent fc: ${fc.getArguments}")
            case sl: StringLiteral =>
              println(s"$indent sl: ${sl.getValue}")
            case ol: ObjectLiteral =>
              println(s"$indent ol: ${ol.getElements}")
            case kl: KeywordLiteral =>
              println(s"$indent kl: ${kl}")
            case op: ObjectProperty =>
              println(s"$indent op: ${op}")
            case pg: PropertyGet =>
              println(s"$indent pg: ${pg.getProperty.getIdentifier} ${pg.getTarget.toSource(0)}")
            case _ =>
              println(s"$indent ukn: ${node.getClass()}")
          }
          true
        }
      }


      class Printer extends NodeVisitor {


        @Override def visit(node: AstNode): Boolean = {
          val indent = "%1$Xs".replace("X", String.valueOf(node.depth() + 1)).format("")

          node match {
            case v: VariableDeclaration =>
              println(s"$indent varDec: ${typeName(v.getType)}")
              v.getVariables
            case v: VariableInitializer =>
              println(s"$indent varInit: ${typeName(v.getType)}")
            case name: Name =>
              println(s"$indent name: ${name.getIdentifier}")
            case fc: FunctionCall =>
            case sl: StringLiteral =>
              println(s"$indent sl: ${sl.getValue}")
            case ol: ObjectLiteral =>
              println(s"$indent ol: ${ol.getElements}")
            case kl: KeywordLiteral =>
              println(s"$indent kl: ${kl}")
            case op: ObjectProperty =>
              println(s"$indent op: ${op}")
            case pg: PropertyGet =>
              println(s"$indent pg: ${pg.getProperty.getIdentifier} ${pg.getTarget}")
            case _ =>
              println(s"$indent ukn: ${node.getClass()}")
          }
          true
        }
      }

      val reader = new FileReader(file);
      try {
        val env = new CompilerEnvirons()
        env.setRecordingLocalJsDocComments(true)
        env.setAllowSharpComments(true)
        env.setRecordingComments(true)
        val node = new Parser(env).parse(reader, file.getAbsolutePath, 1)
        //      node.visitAll(new Printer())
        val extractor = new ExtractorVisitor()
        node.visitAll(extractor)
        //    println(extractor.variables)
        //    extractor.variables.foreach{
        //      variable=>
        //        println(s"${variable.name} ${variable.isRequire} ${variable.getReactClass}")
        //    }
        extractor.variables.find(_.isReactCreateClass).map(_.getReactClass match {
          case Some(rc) =>
            println(s"DisplayName: ${rc.displayName}")
            println(s"MethodNames: ${rc.methodNames}")
            println(s"Properties:")
            rc.properties.values.toList.sortBy(_.name).foreach(p => println(s" - $p"))
            FileUtils.writeData(new File(OUTPUTDIR, rc.displayName+".scala"), Template.render(rc))
//            println(Template.render(rc))
          case _ =>
        })

      } finally {
        reader.close()
      }
  }

}
