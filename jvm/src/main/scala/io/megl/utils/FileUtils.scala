package io.megl.utils

import java.io._
import java.security.AccessController

import com.typesafe.scalalogging.LazyLogging
import sun.security.action.GetPropertyAction

import scala.io.Codec
import scala.language.implicitConversions
/** A wrapper around file, allowing iteration either on direct children
     or on directory tree */
class RichFile(file: File) {

  def children = new Iterable[File] {
    def iterator = if (file.isDirectory) file.listFiles.iterator else Iterator.empty
  }

  def andTree: Iterable[File] =
    Seq(file) ++ children.flatMap(child => new RichFile(child).andTree)

  def copy(destPath:File, relative:String="") {
    val dest = if(relative.isEmpty){destPath} else {new File(destPath, file.getAbsolutePath.substring(relative.length) ) }

    if(file.isFile){
      if(!dest.exists())
        new FileOutputStream(dest) getChannel() transferFrom(
          new FileInputStream(file) getChannel, 0, Long.MaxValue )
    }else{
      if(!dest.exists())
        dest.mkdirs()
    }

  }


}

/** implicitely enrich java.io.File with methods of RichFile */
object RichFile {
  implicit def toRichFile(file: File) = new RichFile(file)
}

/*
import io.utils.RichFile.toRichFile // this makes implicit toRichFile active
import java.io.File

object Test extends App {
  val root = new File("/home/user")
  for(f <- root.andTree) Console.println(f)

 // filtering comes for free
 for(f <- root.andTree; if f.getName.endsWith(".mp3")) Console.println(f)
}*
* */

object FileUtils extends LazyLogging {
  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }
//
//  def readResource(name: String, encoding:String="utf8"): Option[String] = {
//    try{
//
//      val source = io.Source.fromInputStream(getClass.getResourceAsStream(name), encoding)
//      val res = source.mkString
//      source.close
//      Some(res)
//    } catch {
//      case ex:Throwable=>
//        logger.error(s"Unable to read resource $name", ex)
//        None
//    }
//  }
//
//  def readResourceJSON(name: String, encoding:String="utf8"):Option[JsValue]=readResource(name, encoding).map(v => Json.parse(v))


  implicit private val codec = Codec.UTF8

  def writeData(file: File, data: String): Boolean = {
    writeSafe(file) { writer =>
      writer.write(data)
    }
  }

  def writeLines(file: File, lines: Iterable[String]): Boolean = {
    writeSafe(file) { writer =>
      lines.foreach { line =>
        writer.write(line)
        writer.newLine()
      }
    }
  }

  /**
   * Method that write into a temporary files, then moves it to replace to original for 'safe' write
   * @param file file to write to
   * @param writeFn wrapped function taking BufferedWriter as a parameter that actually writes to the file
   * @return true if success, false otherwise
   */
  private def writeSafe(file: File)(writeFn: (BufferedWriter) => Unit): Boolean = {
    val tmpFileName = s"${file.getPath}.tmp"
    try {
      val tmpFile = new File(tmpFileName)

      val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFileName), "UTF-8"))
      try {
        writeFn(writer)
        writer.flush()
      } finally {
        writer.close()
      }

      val status = tmpFile renameTo file
      if (!status) logger.error(s"Unable to rename $tmpFile to $file")
      status
    } catch {
      case e: Exception => {
        logger.error(s"Error when trying to write to ${file.getPath}.", e)
        false
      }
    }
  }

//  def readLines[T](file: File)(readFn: (Iterator[String]) => T): T = {
//    val source = fromFile(file)
//    readFromSource(source)(readFn)
//  }
//
//  def readLinesGzip[T](file: File)(readFn: (Iterator[String]) => T): T = {
//    val source = fromInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))))
//    readFromSource(source)(readFn)
//  }
//
////  private def readFromSource[T](s: Source)(block: (Iterator[String]) => T): T = {
////    val iteratorWithClosable = new Iterator[String] with Closable {
////      val lines = s.getLines()
////
////      override def hasNext: Boolean = lines.hasNext
////
////      override def close(): Unit = s.close()
////
////      override def next(): String = lines.next()
////    }
////
////    using(iteratorWithClosable)(block)
////  }

  // Code extracted from File.Java to determine the tmpdir on the system.
  def getTmpDirectory: File = new File(AccessController.doPrivileged(new GetPropertyAction("java.io.tmpdir")))

}