package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonerapi.model.BedAssignmentSearchResults

class PrisonerApiMockServer : MockServer(8093) {

  fun stubGetBedAssignmentsHistoryByBookingId(bookingId: String, page: Int, size: Int, results: BedAssignmentSearchResults) {
    stubFor(
      WireMock.get(WireMock.urlEqualTo("/api/bookings/$bookingId/cell-history?page=$page&size=$size"))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(mapper.writeValueAsString(results))
            .withStatus(200),
        ),
    )
  }

  fun stubGetBedAssignmentsHistoryByBookingIdServerError(bookingId: String, page: Int, size: Int) {
    stubFor(
      WireMock.get(WireMock.urlEqualTo("/api/bookings/$bookingId/cell-history?page=$page&size=$size"))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("""{"status": 500, "errorCode": "INTERNAL_SERVER_ERROR", "userMessage": "Internal server error", "developerMessage": "Internal server error"}""")
            .withStatus(500),
        ),
    )
  }

  fun stubGetBedAssignmentsHistoryByBookingIdWithConnectionReset(bookingId: String, page: Int, size: Int, results: BedAssignmentSearchResults, numFails: Int = 1) {
    val url = WireMock.urlEqualTo("/api/bookings/$bookingId/cell-history?page=$page&size=$size")

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
            .withBody(mapper.writeValueAsString(results))
            .withStatus(200),
        ),
    )
  }
}
