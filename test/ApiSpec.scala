import controllers.IllegalApiCallException
import controllers.IllegalApiCallException
import controllers.{IllegalApiCallException, ApiResult}
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.libs.json._
import play.api.libs.json.JsArray
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApiSpec extends Specification {
  "ApiResult" should {
    "wrap data with status on a successful response" in {
      val success = ApiResult {
        Json.obj("test" -> "value")
      }
      contentType(success) must beSome("application/json")
      status(success) must equalTo(OK)
      contentAsJson(success) \ "status" mustEqual JsString("success")
    }

    "wrap data with fail when an Api exception is thrown" in {
      val fail = ApiResult {
        if (true) throw IllegalApiCallException(Json.obj("test" -> "just testing the fail state"))
        Json.obj("never" -> "reached")
      }
      contentType(fail) must beSome("application/json")
      status(fail) must equalTo(BAD_REQUEST)
      contentAsJson(fail) \ "status" mustEqual JsString("fail")
      contentAsJson(fail) \ "data" mustEqual Json.obj("test" -> "just testing the fail state")
    }

    "return an error when something else goes wrong" in {
      val error = ApiResult {
        Json.obj("infinity" -> (1 / 0))
      }
      contentType(error) must beSome("application/json")
      status(error) must equalTo(INTERNAL_SERVER_ERROR)
      contentAsJson(error) \ "status" mustEqual JsString("error")
      contentAsJson(error) \ "message" mustEqual JsString("/ by zero")
    }
  }

  "Application" should {
    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "return a list of instances" in new WithApplication{
      val home = route(FakeRequest(GET, "/instance")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome("application/json")
      val jsonInstances = contentAsJson(home) \ "data" \ "instances"
      jsonInstances must beLike { case JsArray(_) => ok }
      jsonInstances.as[JsArray].value.length mustEqual(56)
    }
  }
}
