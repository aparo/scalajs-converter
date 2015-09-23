package io.megl.scalajs.converter

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import converter.shared.Api
import spray.http._
import spray.routing.SimpleRoutingApp
import upickle.default._
import scala.util.Properties

object Router extends autowire.Server[String, Reader, Writer] {
  def read[Result: Reader](p: String) = read[Result](p)
  def write[Result: Writer](r: Result) = write(r)
}

object Config {
  val c = ConfigFactory.load().getConfig("converter")

  val productionMode = c.getBoolean("productionMode")
}

object MainApp extends SimpleRoutingApp {
  def main(args: Array[String]): Unit = {
    // create an Actor System
    implicit val system = ActorSystem("CONVERTER")
    // use system's dispatcher as ExecutionContext for futures etc.
    implicit val context = system.dispatcher

    val port = Properties.envOrElse("CONVERTER_PORT", "8080").toInt

    val apiService = new ApiService

    startServer("0.0.0.0", port = port) {
      get {
        pathSingleSlash {
          // serve the main page
          if(Config.productionMode)
            getFromResource("web/index-full.html")
          else
            getFromResource("web/index.html")
        } ~ pathPrefix("srcmaps") {
          if(!Config.productionMode)
            getFromDirectory("../")
          else
            complete(StatusCodes.NotFound)
        } ~
          // serve other requests directly from the resource directory
          getFromResourceDirectory("web")
      } ~ post {
        path("api" / Segments) { s =>
          extract(_.request.entity.asString) { e =>
            complete {
              // handle API requests via autowire
              Router.route[Api](apiService)(
                autowire.Core.Request(s, read[Map[String, String]](e))
              )
            }
          }
        }
      }
    }
  }
}
