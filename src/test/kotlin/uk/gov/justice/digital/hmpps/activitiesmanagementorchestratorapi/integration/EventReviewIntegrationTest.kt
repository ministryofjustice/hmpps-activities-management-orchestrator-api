package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model.EventReviewDescription
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.PrisonerBasicDetails
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EventReviewSearchResultsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.helpers.eventReviewFactory
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.helpers.eventReviewSearchResultsFactory
import java.time.LocalDate

class EventReviewIntegrationTest : IntegrationTestBase() {

  private val date = LocalDate.of(2026, 8, 1)

  @Test
  fun `should return 200 with event review data`() {
    val apiResponse = eventReviewSearchResultsFactory(
      content = listOf(
        eventReviewFactory(),
        eventReviewFactory(eventReviewId = 2L, eventDescription = EventReviewDescription.RELEASED),
      ),
      totalElements = 2L,
    )

    activitiesApi().stubGetEventsForReview("MDI", date, apiResponse)
    prisonerSearchApi().stubSearchByPrisonerNumbers(
      listOf("A1234AA"),
      listOf(
        PrisonerBasicDetails(
          prisonerNumber = "A1234AA",
          firstName = "JOHN",
          lastName = "SMITH",
          cellLocation = "1-2-003",
        ),
      ),
    )

    val result = getEventsDataForReview("MDI", date).success<EventReviewSearchResultsDto>()

    assertThat(result.content).hasSize(2)
    assertThat(result.totalElements).isEqualTo(2L)
    assertThat(result.content[0].prisonerNumber).isEqualTo("A1234AA")
    assertThat(result.content[0].eventDescription).isEqualTo(EventReviewDescription.TEMPORARY_RELEASE)
    assertThat(result.content[0].prisonerDetails?.prisonerNumber).isEqualTo("A1234AA")
    assertThat(result.content[0].prisonerDetails?.firstName).isEqualTo("JOHN")
    assertThat(result.content[0].prisonerDetails?.lastName).isEqualTo("SMITH")
    assertThat(result.content[0].prisonerDetails?.cellLocation).isEqualTo("1-2-003")
  }

  @Test
  fun `should return 200 with empty results`() {
    val emptyResponse = eventReviewSearchResultsFactory(
      content = emptyList(),
      totalElements = 0L,
      totalPages = 0,
    )

    activitiesApi().stubGetEventsForReview("MDI", date, emptyResponse)
    prisonerSearchApi().stubSearchByPrisonerNumbers(emptyList(), emptyList())

    val result = getEventsDataForReview("MDI", date).success<EventReviewSearchResultsDto>()

    assertThat(result.content).isEmpty()
    assertThat(result.totalElements).isZero()
  }

  @Test
  fun `should return 401 when not authenticated`() {
    getEventsDataForReview("MDI", date, includeBearerAuth = false).fail(HttpStatus.UNAUTHORIZED)
  }

  @Test
  fun `should return 403 when user has incorrect role`() {
    getEventsDataForReview("MDI", date, roles = listOf("ROLE_WRONG")).fail(HttpStatus.FORBIDDEN)
  }

  @Test
  fun `should return 404 when prison code is not found`() {
    activitiesApi().stubGetEventsForReviewNotFound("XXX", date)

    getEventsDataForReview("XXX", date, roles = listOf("ROLE_ACTIVITY_ADMIN")).fail(HttpStatus.NOT_FOUND)
  }

  @Test
  fun `should return 400 when date parameter is missing`() {
    webTestClient.get()
      .uri("/event-review/prison/MDI")
      .headers(setAuthorisation(roles = listOf("ROLE_ACTIVITY_ADMIN")))
      .exchange()
      .fail(HttpStatus.BAD_REQUEST)
  }

  @Test
  fun `should return 500 when upstream API returns server error`() {
    activitiesApi().stubGetEventsForReviewServerError("MDI", date)

    getEventsDataForReview("MDI", date, roles = listOf("ROLE_ACTIVITY_ADMIN")).fail(HttpStatus.INTERNAL_SERVER_ERROR)
  }

  private fun getEventsDataForReview(
    prisonCode: String,
    date: LocalDate,
    roles: List<String> = listOf("ACTIVITY_HUB", "ACTIVITY_ADMIN"),
    includeBearerAuth: Boolean = true,
  ) = webTestClient.get()
    .uri { uriBuilder ->
      uriBuilder
        .path("/event-review/prison/{prisonCode}")
        .queryParam("date", date)
        .queryParam("page", 0)
        .queryParam("size", 10)
        .queryParam("sortDirection", "ascending")
        .build(prisonCode)
    }
    .accept(MediaType.APPLICATION_JSON)
    .headers(if (includeBearerAuth) setAuthorisation(roles = roles) else noAuthorisation())
    .exchange()
}
