package io.megl.scalajs

import io.megl.scalajs.HTML2SJS.{HTMLOptions, validateI18n}

import scala.collection.mutable.ListBuffer

trait HTMLNode {
  def isEmpty:Boolean
  def nonEmpty:Boolean = !isEmpty
  def tag:String
  def children:Seq[HTMLNode]

  def render(option:HTMLOptions):String
}



case class GenericHTMLNode(tag:String, attributes:Seq[(String,String)]=Nil,
                           children:Seq[HTMLNode]=Nil, ident:Int=0, text:Option[String]=None) extends HTMLNode {
  def isEmpty:Boolean=children.isEmpty && text.isEmpty && (tag!="#PCDATA") && attributes.isEmpty
  override def render(option:HTMLOptions): String = {
    val result=new ListBuffer[String]()
    result +=s"<.$tag("
    result += (attributes.map(a => s"${a._1} := ${a._2}") ++  children.map(_.render(option))).mkString(", ")
    result +=")"
    result.mkString("")
  }
}


case class TextNode(text:String, children:Seq[HTMLNode]=Nil) extends HTMLNode {
  def isEmpty:Boolean=children.isEmpty && text.isEmpty

  val tag: String = "#PCDATA"

  override def render(option:HTMLOptions): String = {
    val result=new ListBuffer[String]()
    if(option.i18n)
      result += s"""i18n(${validateI18n(text)})"""
    else if(text.nonEmpty)
      result+=text
    result ++= children.map(_.render(option))
    result.mkString(", ")
  }
}


trait commonFonts extends HTMLNode {
  def parameters:List[String]
  def iconPrefix:String

  lazy val sizingNames=Set(
    "lg",
    "xs",
    "sm",
    "1x",
    "2x",
    "3x",
    "4x",
    "5x",
    "6x",
    "7x",
    "8x",
    "9x",
    "10x",
    "fw",
    "ul",
    "li",
    "border",
    "pull-left",
    "pull-right",
    "spin",
    "pulse",
    "rotate-90",
    "rotate-180",
    "rotate-270",
    "flip-horizontal",
    "flip-vertical",
    "flip-horizontal.fa-flip-vertical",
    "stack",
    "stack-1x",
    "stack-2x",
    "inverse"
  )
  def children:Seq[HTMLNode]=Nil

  def isEmpty: Boolean = parameters.isEmpty

  def tag: String = "i"

  def cookName(name: String):String={
    val tokens=name.split('-')
    tokens.head + tokens.tail.map(_.capitalize).mkString
  }

  def extractName:String={
    cookName(parameters.filter(_.startsWith(iconPrefix))
      .map(_.substring(iconPrefix.length))
      .filterNot(sizingNames.contains)
      .head)
  }


}

case class FontAwesome(parameters:List[String]) extends commonFonts{
  val iconPrefix="fa-"


  def render(option: HTMLOptions): String = s"FontAwesome.${extractName}"
}

case class LineAwesome(parameters:List[String]) extends commonFonts{

  val iconPrefix="la-"

  def render(option: HTMLOptions): String = s"LineAwesome.${extractName}"
}

case class FlatIcon(parameters:List[String]) extends commonFonts{

  val iconPrefix="flaticon-"

  def render(option: HTMLOptions): String = s"FlatIcon.${extractName}"
}
