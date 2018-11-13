package simulations.jui

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.reform.jui.performance.util.Environment
//import io.gatling.jdbc.Predef._

class JUIBasicPerf extends Simulation {

  val idamBaseUrl = scala.util.Properties.envOrElse("IDAM_URL", Environment.IDAM_WEB_URL).toLowerCase()

  val BaseUrl = scala.util.Properties.envOrElse("URL_TO_TEST", Environment.URL_TO_TEST).toLowerCase()
  val JUIUsername = scala.util.Properties.envOrElse("process.env.TEST_EMAIL", Environment.JUIUsername).toLowerCase()
  val JUIPassword = scala.util.Properties.envOrElse("process.env.TEST_PASSWORD", Environment.JUIPassword)


  /*val usernameFeeder = Array(
    Map("username" -> "mytestuser5@gmail.com"),
    Map("username" -> "mytestuser7@gmail.com"),
    Map("username" -> "mytestuser9@gmail.com")
  ).random*/

  val httpProtocol = http
    .baseURL(BaseUrl)
    .proxy(Proxy("proxyout.reform.hmcts.net", 8080).httpsPort(8080))
    .inferHtmlResources(BlackList("""https://www.google.com*""", """https://www.googletagmanager.com.*""", """https://www.google-analytics.com.*""", """.*\css""", """.*\.js""", """.*\.ico""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.woff""", """.*\.(t|o)tf""", """.*\.png"""), WhiteList())
    .doNotTrackHeader("1")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")

  val headers_0 = Map("Upgrade-Insecure-Requests" -> "1")

  val headers_9 = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
    "Accept-Encoding" -> "gzip, deflate, br",
    "Accept-Language" -> "en,en-GB;q=0.9",
    "Cache-Control" -> "max-age=0",
    "Connection" -> "keep-alive",
    "Origin" -> "https://idam.preprod.ccidam.reform.hmcts.net",
    "Upgrade-Insecure-Requests" -> "1")

  val headers_21 = Map(
    "Accept" -> "application/json, text/plain, */*",
    "Accept-Encoding" -> "gzip, deflate, br",
    "Accept-Language" -> "en,en-GB;q=0.9",
    "Connection" -> "keep-alive")

  val headers_25 = Map(
    "Accept" -> "application/json, text/plain, */*",
    "Accept-Encoding" -> "gzip, deflate, br",
    "Accept-Language" -> "en,en-GB;q=0.9",
    "Connection" -> "keep-alive",
    "Content-Type" -> "application/json",
    "Origin" -> "https://jui-webapp-aat.service.core-compute-aat.internal")

  val headers_26 = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
    "Accept-Encoding" -> "gzip, deflate, br",
    "Accept-Language" -> "en,en-GB;q=0.9",
    "Connection" -> "keep-alive",
    "Upgrade-Insecure-Requests" -> "1")



  val pauseMin = 0
  val pauseMax = 5

  val scn = scenario("JUIBasic")

    .exec(juiPerf.juiHome)
    .exec(juiPerf.juiLogin)
    .exec(juiPerf.juiSelectCase)
    .exec(juiPerf.viewCaseFiles)
    .exec(juiPerf.juiDash)
    .exec(juiPerf.juiLogout)


  object juiPerf {


    val juiHome =

      exec(http("JUI_HOME")
        .get("/")
        .headers(headers_0)
        .check(css(".form-group>input[name='_csrf']", "value").saveAs("csrf"))
        .check(css(".form-group>input[name='upliftToken']", "value").saveAs("upliftToken"))
        .check(css(".form-group>input[name='response_type']", "value").saveAs("response_type"))
        .check(css(".form-group>input[name='redirect_uri']", "value").saveAs("redirect_uri"))
        .check(css(".form-group>input[name='client_id']", "value").saveAs("client_id"))
        .check(css(".form-group>input[name='scope']", "value").saveAs("scope"))
        .check(css(".form-group>input[name='state']", "value").saveAs("state"))
        .check(css(".form-group>input[name='continue']", "value").saveAs("continue"))
      )

    val juiLogin =

      pause(pauseMax * 3)

        .exec(http("JUI_LOGIN")
          .post(idamBaseUrl + "/login?response_type=code&client_id=${client_id}&redirect_uri=${redirect_uri}")
          .headers(headers_9)
          //    .formParam("username", "${username}")
          //    .formParam("password", "1234567")
          .formParam("username", JUIUsername)
          .formParam("password", JUIPassword)
          .formParam("continue", "${continue}")
          .formParam("upliftToken", "")
          .formParam("response_type", "${response_type}")
          .formParam("_csrf", "${csrf}")
          .formParam("redirect_uri", "${redirect_uri}")
          .formParam("client_id", "juiwebapp")
          .formParam("scope", "")
          .formParam("state", "")
          .check(regex ("""href="/case/(.+?)/summary""").findAll.optional.saveAs("P_case"))
          //      .check(regex ("""href="/case/(.+?)/summary""").findRandom.optional.saveAs("P_case")) // only for Gatling 3.0 and upwards
          //    .check(regex("""href="/case/(.+?)/(.+?)/(.+?)/summary""").ofType[(String, String, String)].optional.saveAs("PA_case_details"))
          .check(currentLocation.is(BaseUrl + "/"))
        )


    val juiSelectCase =

      pause(pauseMax * 2)

        .doIf("${P_case.exists()}") {

          //    NOTE the check for current location should include the case reference but as currently selected randomly I need
          //    to add a way to save the random param

          //    exec(session => session.set("P_caseSelected", ${P_case.random()}))

          //  SELECT A CASE
          exec(http("JUI_SELECT_CASE")
            .get("/api/case/${P_case.random()}")
            //      .get("/api/case/${P_caseSelected}")
            .headers(headers_21)
            .check(regex("""id":".{0,12}","name":"(.{0,12})","type":"page""").findAll.optional.saveAs("P_pagesA"))
            .check(regex("""id";:";.{0,12}";,";name";:";.({0,12})";,";type";:";page""").findAll.optional.saveAs("P_pagesB"))
            .check(regex("""document_binary_url":".+?/documents/(.+?)","id":"(.+?)"}]}""").ofType[(String, String)].findAll.optional.saveAs("PA_docs"))
            //      .check(currentLocation.is(baseURL + "/case/${P_caseSelected}/summary"))
          )

        }

    val viewCaseFiles =

    // no network activity for clicking parties timeline (or summary 2nd time) since all information was in case file JSON.
    // clicking case file will only generate network activity if there are documents.

    //    val viewCaseFile =
      doIf("${PA_docs.exists()}"){
        exec( session => {
          println( "Found Case File(s)" )
          session
        })

        repeat("${PA_docs.size()}", "n") {

          pause(pauseMax * 3)

            .exec(http("JUI_VIEW_CASE_FILE_G")
              .get("/api/documents/${PA_docs(n)._2}")
              .headers(headers_0)
              .resources(http("JUI_VIEW_CASE_FILE_R")
                .get("/api/documents/${PA_docs(n)._1}"))
              //        .check(currentLocation.is(baseURL + "/case/${P_caseSelected}/casefile/${PA_docs(n)._2}"))
            )
        }
      }

    val juiDash =

      pause(pauseMax * 1)

        //  Navigate back to home

        .exec(http("JUI_DASHBOARD")
        .get("/")
        .headers(headers_26)
        .check(currentLocation.is(BaseUrl + "/")))

    val juiLogout =

      pause(pauseMax * 1)

        .exec(http("JUI_LOGOUT")
          .get("/logout?redirect=${redirect_uri}%3Fresponse_type%3D${response_type}%26client_id%3D${client_id}%26redirect_uri%3D${redirect_uri}")
          .headers(headers_26))



  }

    setUp(scn.inject(atOnceUsers(1)).protocols(httpProtocol))




  //  setUp(scn.inject(rampUsers(10) over  (120 seconds)).protocols(httpProtocol))

}
