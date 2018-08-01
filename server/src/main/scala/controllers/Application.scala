package controllers


import converter.shared.Api
import io.circe.Json
import play.api.mvc._
import services.ApiService
import io.circe.Json
import io.circe.java8.time.TimeInstances
import io.circe.parser._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import javax.inject._

@Singleton
class Application @Inject() (cc: ControllerComponents)
  extends AbstractController(cc) {
  val apiService = new ApiService()

  def index = Action {
    Ok(views.html.index("ScalaJS - Converter"))
  }


  private val procedureCallRouter: autowire.Core.Request[Json] => Future[Result] = AutoWireServer
    .route[Api](apiService)(_)
    .map(_.noSpaces).map(Ok(_))

  def autowireApi(path: String) = Action.async(parse.byteString) {
    implicit request =>
      println(s"Request path: $path")

      decode[Map[String, Json]](request.body.utf8String) match {
        case Right(s) =>
          val procedureCallRequest = autowire.Core.Request(path.split('/'), s)
          procedureCallRouter(procedureCallRequest)
        case Left(v) =>
          println("Error" + v)
          Future.successful(Ok("Request failed: " + v).as("application/json"))
      }
  }

  def logging = Action(parse.anyContent) {
    implicit request =>
      request.body.asJson.foreach { msg =>
        println(s"CLIENT - $msg")
      }
      Ok("")
  }
}
