package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.PrisonDetails

class PrisonApiMockServer : MockServer(8093) {

  fun stubGetPrisonName(prisonCode: String, response: PrisonDetails) {
    stubFor(
      WireMock.get(WireMock.urlEqualTo("/api/agencies/$prisonCode"))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(mapper.writeValueAsString(response))
            .withStatus(200),
        ),
    )
  }

  fun stubGetPrisonNameWithConnectionReset(prisonCode: String, response: PrisonDetails, numFails: Int = 1) {
    val url = WireMock.urlEqualTo("/api/agencies/$prisonCode")

    for (i in 1..numFails) {
      stubFor(
        WireMock.get(url)
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
      WireMock.get(url)
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

  fun stubGetPrisonNameServerError(prisonCode: String) {
    stubFor(
      WireMock.get(WireMock.urlEqualTo("/api/agencies/$prisonCode"))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("""{"status": 500, "errorCode": "INTERNAL_SERVER_ERROR", "userMessage": "Internal server error", "developerMessage": "Internal server error"}""")
            .withStatus(500),
        ),
    )
  }

  fun stubGetPrisonNameNotFound(prisonCode: String) {
    stubFor(
      WireMock.get(WireMock.urlEqualTo("/api/agencies/$prisonCode"))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("""{"status": 404, "errorCode": "NOT_FOUND", "userMessage": "Not found", "developerMessage": "Not found"}""")
            .withStatus(404),
        ),
    )
  }
}

class PrisonApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val prisonApiServer = PrisonApiMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    prisonApiServer.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    prisonApiServer.resetAll()
  }

  override fun afterAll(context: ExtensionContext) {
    prisonApiServer.stop()
  }
}
