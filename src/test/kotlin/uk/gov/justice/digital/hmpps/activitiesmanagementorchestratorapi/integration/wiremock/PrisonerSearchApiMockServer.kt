package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.PrisonerBasicDetails
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.PrisonerNumbers

class PrisonerSearchApiMockServer : MockServer(8092) {

  fun stubSearchByPrisonerNumbers(vararg prisonerNumber: String) {
    stubFor(
      WireMock.post(WireMock.urlEqualTo("/prisoner-search/prisoner-numbers"))
        .withRequestBody(equalToJson(mapper.writeValueAsString(PrisonerNumbers(prisonerNumbers = prisonerNumber.asList())), true, true))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBodyFile("prisonersearchapi/prisoner-1.json")
            .withStatus(200),
        ),
    )
  }

  fun stubSearchByPrisonerNumbers(prisonerNumbers: List<String>, prisonersBasicDetails: List<PrisonerBasicDetails>) {
    stubFor(
      WireMock.post(WireMock.urlEqualTo("/prisoner-search/prisoner-numbers"))
        .withRequestBody(equalToJson(mapper.writeValueAsString(PrisonerNumbers(prisonerNumbers = prisonerNumbers)), true, true))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(mapper.writeValueAsString(prisonersBasicDetails))
            .withStatus(200),
        ),
    )
  }

  fun stubSearchByPrisonerNumbersServerError(prisonerNumbers: List<String>) {
    stubFor(
      WireMock.post(WireMock.urlEqualTo("/prisoner-search/prisoner-numbers"))
        .withRequestBody(equalToJson(mapper.writeValueAsString(PrisonerNumbers(prisonerNumbers = prisonerNumbers)), true, true))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("""{"status": 500, "errorCode": "INTERNAL_SERVER_ERROR", "userMessage": "Internal server error", "developerMessage": "Internal server error"}""")
            .withStatus(500),
        ),
    )
  }

  fun stubSearchByPrisonerNumbersWithConnectionReset(prisonerNumbers: List<String>, prisonersBasicDetails: List<PrisonerBasicDetails>, numFails: Int = 1) {
    val requestBody = equalToJson(mapper.writeValueAsString(PrisonerNumbers(prisonerNumbers = prisonerNumbers)), true, true)

    for (i in 1..numFails) {
      stubFor(
        WireMock.post(WireMock.urlEqualTo("/prisoner-search/prisoner-numbers"))
          .withRequestBody(requestBody)
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
      WireMock.post(WireMock.urlEqualTo("/prisoner-search/prisoner-numbers"))
        .withRequestBody(requestBody)
        .inScenario("Network Fail")
        .whenScenarioStateIs("Fail $numFails")
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(mapper.writeValueAsString(prisonersBasicDetails))
            .withStatus(200),
        ),
    )
  }
}
