package io.megl.scalajs.converter

import io.megl.scalajs.JSON2CC
import org.specs2.mutable.Specification
import org.specs2.mutable.Specification

class Json2caseclassSpec extends Specification {

  "json2caseclass" should {

    "works for embedded" in {
     val result= JSON2CC.convertToCC("Root",
        """
          |{
          |"indices" : {
          |    "count" : 36,
          |    "shards" : {
          |      "total" : 180,
          |      "primaries" : 180,
          |      "replication" : 0.0,
          |      "index" : {
          |        "shards" : {
          |          "min" : 5,
          |          "max" : 5,
          |          "avg" : 5.0
          |        },
          |        "primaries" : {
          |          "min" : 5,
          |          "max" : 5,
          |          "avg" : 5.0
          |        },
          |        "replication" : {
          |          "min" : 0.0,
          |          "max" : 0.0,
          |          "avg" : 0.0
          |        }
          |      }
          |    }
          |  }
          |  }
          |}
        """.stripMargin)

      result.length must_== 600
    }

  }
}
