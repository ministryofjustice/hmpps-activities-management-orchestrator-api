package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model.EventReviewSearchResults
import java.time.LocalDate

class ActivitiesApiMockServer : MockServer(8091) {

  fun stubGetEventsForReview(prisonCode: String, date: LocalDate, response: EventReviewSearchResults) {
    stubFor(
      WireMock.get(WireMock.urlPathEqualTo("/event-review/prison/$prisonCode"))
        .withQueryParam("date", WireMock.equalTo(date.toString()))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(mapper.writeValueAsString(response))
            .withStatus(200),
        ),
    )
  }

  fun stubGetEventsForReviewServerError(prisonCode: String, date: LocalDate) {
    stubFor(
      WireMock.get(WireMock.urlPathEqualTo("/event-review/prison/$prisonCode"))
        .withQueryParam("date", WireMock.equalTo(date.toString()))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("""{"status": 500, "errorCode": "INTERNAL_SERVER_ERROR", "userMessage": "Internal server error", "developerMessage": "Internal server error"}""")
            .withStatus(500),
        ),
    )
  }

  fun stubGetEventsForReviewNotFound(prisonCode: String, date: LocalDate) {
    stubFor(
      WireMock.get(WireMock.urlPathEqualTo("/event-review/prison/$prisonCode"))
        .withQueryParam("date", WireMock.equalTo(date.toString()))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("""{"status": 404, "errorCode": "NOT_FOUND", "userMessage": "Not found", "developerMessage": "Not found"}""")
            .withStatus(404),
        ),
    )
  }

  fun stubGetEventsForReviewWithConnectionReset(prisonCode: String, date: LocalDate, response: EventReviewSearchResults, numFails: Int = 1) {
    val url = "/event-review/prison/$prisonCode"

    for (i in 1..numFails) {
      stubFor(
        WireMock.get(WireMock.urlPathEqualTo(url))
          .withQueryParam("date", WireMock.equalTo(date.toString()))
          .inScenario("Network Fail")
          .whenScenarioStateIs(if (i == 1) STARTED else "Fail ${i - 1}")
          .willReturn(
            WireMock.aResponse()
              .withFault(Fault.CONNECTION_RESET_BY_PEER),
          )
          .willSetStateTo("Fail $i"),
      )
    }

    stubFor(
      WireMock.get(WireMock.urlPathEqualTo(url))
        .withQueryParam("date", WireMock.equalTo(date.toString()))
        .inScenario("Network Fail")
        .whenScenarioStateIs("Fail $numFails")
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(mapper.writeValueAsString(response))
            .withStatus(200),
        ),
    )
  }
}

class ActivitiesApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val activitiesApiServer = ActivitiesApiMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    activitiesApiServer.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    activitiesApiServer.resetAll()
  }

  override fun afterAll(context: ExtensionContext) {
    activitiesApiServer.stop()
  }
}
