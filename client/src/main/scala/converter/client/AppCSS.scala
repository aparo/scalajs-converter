package converter.client

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scalacss.ProdDefaults._
import scalacss.ScalaCssReact._
import scalacss.internal.mutable.GlobalRegistry

object AppCSS {
  @JSImport("bootstrap/dist/css/bootstrap.min.css", JSImport.Default )
  @js.native
  object BootstrapCSS extends js.Object


  def load(): Unit = {
    /* touch objects to force namespace import */
    BootstrapCSS

    GlobalRegistry.addToDocumentOnRegistration()
  }
}
