package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.BedAssignment
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.BedAssignmentSearchResults
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.CellLocationResultsDto

class PrisonIntegrationTest : IntegrationTestBase() {

  @Test
  fun `should return 200 with cell location history`() {
    val bookingId = "12345"
    val apiResponse = BedAssignmentSearchResults(
      content = listOf(BedAssignment(description = "MDI-1-2"), BedAssignment(description = "MDI-1-1")),
      pageNumber = 0,
      totalElements = 2,
      totalPages = 1,
    )

    prisonApi().stubGetBedAssignmentsHistoryByBookingId(bookingId, 0, 2, apiResponse)

    val result = getCellLocationHistory(bookingId).success<CellLocationResultsDto>()

    assertThat(result.cellLocations).hasSize(2)
    assertThat(result.cellLocations!![0]).isEqualTo("MDI-1-2")
    assertThat(result.cellLocations!![1]).isEqualTo("MDI-1-1")
  }

  @Test
  fun `should return 200 with empty cell location history`() {
    val bookingId = "12345"
    val apiResponse = BedAssignmentSearchResults(content = emptyList(), pageNumber = 0, totalElements = 0, totalPages = 0)

    prisonApi().stubGetBedAssignmentsHistoryByBookingId(bookingId, 0, 2, apiResponse)

    val result = getCellLocationHistory(bookingId).success<CellLocationResultsDto>()

    assertThat(result.cellLocations).isEmpty()
  }

  @Test
  fun `should return 400 when booking id is blank`() {
    getCellLocationHistory("").fail(HttpStatus.BAD_REQUEST)
  }

  @Test
  fun `should return 401 when not authenticated`() {
    getCellLocationHistory("12345", includeBearerAuth = false).fail(HttpStatus.UNAUTHORIZED)
  }

  @Test
  fun `should return 403 when user has incorrect role`() {
    getCellLocationHistory("12345", roles = listOf("ROLE_WRONG")).fail(HttpStatus.FORBIDDEN)
  }

  @Test
  fun `should return 500 when upstream API returns server error`() {
    val bookingId = "12345"
    prisonApi().stubGetBedAssignmentsHistoryByBookingIdServerError(bookingId, 0, 2)

    getCellLocationHistory(bookingId).fail(HttpStatus.INTERNAL_SERVER_ERROR)
  }

  private fun getCellLocationHistory(
    bookingId: String,
    roles: List<String> = listOf("PRISONER_SEARCH"),
    includeBearerAuth: Boolean = true,
  ) = webTestClient.method(HttpMethod.GET)
    .uri("/prison/cell-location-history")
    .contentType(MediaType.TEXT_PLAIN)
    .bodyValue(bookingId)
    .headers(if (includeBearerAuth) setAuthorisation(roles = roles) else noAuthorisation())
    .exchange()
}
