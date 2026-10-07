package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.BedAssignment
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.BedAssignmentSearchResults
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.PrisonDetails
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.CellLocationResultsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.PrisonDetailsDto

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

    assertThat(result.cellLocations).containsExactly("MDI-1-2", "MDI-1-1")
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
  fun `should return 400 when booking id is not a valid number`() {
    getCellLocationHistory("abc123").fail(HttpStatus.BAD_REQUEST)
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
    roles: List<String> = listOf("VIEW_PRISONER_DATA"),
    includeBearerAuth: Boolean = true,
  ) = webTestClient.method(HttpMethod.GET)
    .uri { it.path("/prison/cell-location-history").queryParam("bookingId", bookingId).build() }
    .headers(if (includeBearerAuth) setAuthorisation(roles = roles) else noAuthorisation())
    .exchange()

  @Test
  fun `should return 200 with prison name`() {
    val prisonCode = "LEI"
    prisonApi().stubGetPrisonName(prisonCode, PrisonDetails(description = "Leeds (HMP)"))

    val result = getPrisonName(prisonCode).success<PrisonDetailsDto>()

    assertThat(result.prisonName).isEqualTo("Leeds (HMP)")
  }

  @Test
  fun `should return 400 when prison code is blank`() {
    getPrisonName(" ").fail(HttpStatus.BAD_REQUEST)
  }

  @Test
  fun `should return 401 when not authenticated fetching prison name`() {
    getPrisonName("LEI", includeBearerAuth = false).fail(HttpStatus.UNAUTHORIZED)
  }

  @Test
  fun `should return 403 when user has incorrect role fetching prison name`() {
    getPrisonName("LEI", roles = listOf("INVALID_ROLE")).fail(HttpStatus.FORBIDDEN)
  }

  @Test
  fun `should return 500 when upstream API returns server error fetching prison name`() {
    val prisonCode = "LEI"
    prisonApi().stubGetPrisonNameServerError(prisonCode)

    getPrisonName(prisonCode).fail(HttpStatus.INTERNAL_SERVER_ERROR)
  }

  @Test
  fun `should return 404 when prison code is not found`() {
    val prisonCode = "XXX"
    prisonApi().stubGetPrisonNameNotFound(prisonCode)

    getPrisonName(prisonCode).fail(HttpStatus.NOT_FOUND)
  }

  private fun getPrisonName(
    prisonCode: String,
    roles: List<String> = listOf("VIEW_PRISONER_DATA"),
    includeBearerAuth: Boolean = true,
  ) = webTestClient.method(HttpMethod.GET)
    .uri { it.path("/prison/{prisonCode}/name").build(prisonCode) }
    .headers(if (includeBearerAuth) setAuthorisation(roles = roles) else noAuthorisation())
    .exchange()
}
