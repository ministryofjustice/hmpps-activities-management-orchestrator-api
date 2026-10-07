package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import jakarta.validation.ValidationException
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.api.ActivitiesApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model.EventReviewDescription
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.PrisonerBasicDetails
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.PrisonDetailsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.helpers.eventReviewFactory
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.helpers.eventReviewSearchResultsFactory
import java.time.LocalDate

class EventReviewServiceTest {
  private val activitiesApiClient: ActivitiesApiClient = mock()
  private val prisonerSearchService: PrisonerSearchService = mock()
  private val prisonService: PrisonService = mock()
  private val eventReviewService = EventReviewService(activitiesApiClient, prisonerSearchService, prisonService)

  private val date = LocalDate.of(2026, 8, 1)

  @Test
  fun `should return event data based on prison code and date`() = runTest {
    val apiResponse = eventReviewSearchResultsFactory(
      content = listOf(
        eventReviewFactory(),
        eventReviewFactory(eventReviewId = 2L, eventDescription = EventReviewDescription.ACTIVITY_SUSPENDED),
      ),
      totalElements = 2L,
    )

    whenever(activitiesApiClient.getEventsDataForReview("MDI", date, null, null, null, 0, 10, "ascending")).thenReturn(apiResponse)
    whenever(prisonerSearchService.getBasicPrisonerDetailsMap(listOf("A1234AA"))).thenReturn(emptyMap<String, PrisonerBasicDetails>())

    val result = eventReviewService.getEventsDataForReview("MDI", date, page = 0, size = 10, sortDirection = "ascending")

    assertThat(result.content).hasSize(2)
    assertThat(result.totalElements).isEqualTo(2L)

    with(result.content[0]) {
      assertThat(eventReviewId).isEqualTo(1L)
      assertThat(eventDescription).isEqualTo(EventReviewDescription.RELEASED)
    }

    with(result.content[1]) {
      assertThat(eventReviewId).isEqualTo(2L)
      assertThat(eventDescription).isEqualTo(EventReviewDescription.ACTIVITY_SUSPENDED)
    }
  }

  @Test
  fun `should pass optional parameters to the API client`() = runTest {
    val apiResponse = eventReviewSearchResultsFactory()
    val prisonerNumbers = listOf("A1234AA", "B2345BB")
    val filterEventTypes = listOf("prison-offender-events.prisoner.released", "prison-offender-events.prisoner.remanded")

    whenever(
      activitiesApiClient.getEventsDataForReview(
        "MDI",
        date,
        prisonerNumbers = prisonerNumbers,
        filterAcknowledged = true,
        filterEventTypes = filterEventTypes,
        page = 2,
        size = 20,
        sortDirection = "descending",
      ),
    ).thenReturn(apiResponse)
    whenever(prisonerSearchService.getBasicPrisonerDetailsMap(listOf("A1234AA"))).thenReturn(emptyMap<String, PrisonerBasicDetails>())

    val result = eventReviewService.getEventsDataForReview(
      "MDI",
      date,
      prisonerNumbers = prisonerNumbers,
      filterAcknowledged = true,
      filterEventTypes = filterEventTypes,
      page = 2,
      size = 20,
      sortDirection = "descending",
    )

    assertThat(result.content).hasSize(1)
    assertThat(result.totalElements).isEqualTo(1L)
    assertThat(result.totalPages).isEqualTo(1)
    verify(activitiesApiClient).getEventsDataForReview(
      "MDI",
      date,
      prisonerNumbers = prisonerNumbers,
      filterAcknowledged = true,
      filterEventTypes = filterEventTypes,
      page = 2,
      size = 20,
      sortDirection = "descending",
    )
  }

  @Test
  fun `should return empty results when no events found`() = runTest {
    val emptyResponse = eventReviewSearchResultsFactory(
      content = emptyList(),
      totalElements = 0L,
      totalPages = 0,
    )

    whenever(activitiesApiClient.getEventsDataForReview("MDI", date, null, null, null, 0, 10, "ascending")).thenReturn(emptyResponse)
    whenever(prisonerSearchService.getBasicPrisonerDetailsMap(emptyList())).thenReturn(emptyMap<String, PrisonerBasicDetails>())

    val result = eventReviewService.getEventsDataForReview("MDI", date, page = 0, size = 10, sortDirection = "ascending")

    assertThat(result.content).isEmpty()
    assertThat(result.totalElements).isZero()
    assertThat(result.totalPages).isZero()
  }

  @Test
  fun `should return event data enriched with the prisoner's basic details`() = runTest {
    val apiResponse = eventReviewSearchResultsFactory(
      content = listOf(
        eventReviewFactory(prisonerNumber = "G4793VF"),
        eventReviewFactory(eventReviewId = 2L, eventDescription = EventReviewDescription.RELEASED),
      ),
      totalElements = 2L,
    )

    whenever(activitiesApiClient.getEventsDataForReview("MDI", date, null, null, null, 0, 10, "ascending")).thenReturn(apiResponse)

    val prisonerNumbers = listOf("G4793VF", "A1234AA")
    val firstPrisoner = PrisonerSearchPrisonerFixture.instance(
      prisonerNumber = "G4793VF",
      firstName = "JOE",
      lastName = "BLOGGS",
      cellLocation = "2-1-007",
    )

    val secondPrisoner = PrisonerSearchPrisonerFixture.instance(
      prisonerNumber = "A1234AA",
      firstName = "JANE",
      lastName = "SMITH",
      cellLocation = "1-2-003",
    )

    whenever(prisonerSearchService.getBasicPrisonerDetailsMap(prisonerNumbers)).thenReturn(
      mapOf(
        "G4793VF" to firstPrisoner,
        "A1234AA" to secondPrisoner,
      ),
    )

    val result = eventReviewService.getEventsDataForReview("MDI", date, null, null, null, 0, 10, "ascending")

    assertThat(result.content).isNotEmpty()
    assertThat(result.content[0].eventReviewId).isEqualTo(1L)
    assertThat(result.content[0].eventDescription).isEqualTo(EventReviewDescription.RELEASED)
    assertThat(result.content[0].prisonerDetails?.prisonerNumber).isEqualTo("G4793VF")
    assertThat(result.content[0].prisonerDetails?.firstName).isEqualTo("JOE")
    assertThat(result.content[0].prisonerDetails?.lastName).isEqualTo("BLOGGS")
    assertThat(result.content[0].prisonerDetails?.cellLocation).isEqualTo("2-1-007")

    assertThat(result.content[1].eventReviewId).isEqualTo(2L)
    assertThat(result.content[1].eventDescription).isEqualTo(EventReviewDescription.RELEASED)
    assertThat(result.content[1].prisonerDetails?.prisonerNumber).isEqualTo("A1234AA")
    assertThat(result.content[1].prisonerDetails?.firstName).isEqualTo("JANE")
    assertThat(result.content[1].prisonerDetails?.lastName).isEqualTo("SMITH")
    assertThat(result.content[1].prisonerDetails?.cellLocation).isEqualTo("1-2-003")
  }

  @Test
  fun `should enrich eventData with the prison name for activities-changed and appointments-changed events`() = runTest {
    val apiResponse = eventReviewSearchResultsFactory(
      content = listOf(
        eventReviewFactory(
          eventReviewId = 1L,
          eventType = "prison-offender-events.prisoner.activities-changed",
          prisonCode = "MDI",
          eventData = "event data",
        ),
        eventReviewFactory(
          eventReviewId = 2L,
          eventType = "prison-offender-events.prisoner.appointments-changed",
          prisonCode = "MDI",
          eventData = "event data",
        ),
        eventReviewFactory(
          eventReviewId = 3L,
          eventType = "prison-offender-events.prisoner.released",
          prisonCode = "MDI",
          eventData = "event data",
        ),
      ),
      totalElements = 3L,
    )

    whenever(activitiesApiClient.getEventsDataForReview("MDI", date, null, null, null, 0, 10, "ascending")).thenReturn(apiResponse)
    whenever(prisonerSearchService.getBasicPrisonerDetailsMap(listOf("A1234AA"))).thenReturn(emptyMap<String, PrisonerBasicDetails>())
    whenever(prisonService.getPrisonName("MDI")).thenReturn(PrisonDetailsDto(prisonName = "Moorland (HMP)"))

    val result = eventReviewService.getEventsDataForReview("MDI", date, page = 0, size = 10, sortDirection = "ascending")

    assertThat(result.content[0].eventData).isEqualTo("Moorland (HMP)")
    assertThat(result.content[1].eventData).isEqualTo("Moorland (HMP)")
    assertThat(result.content[2].eventData).isEqualTo("event data")
    verify(prisonService).getPrisonName("MDI")
  }

  @Test
  fun `should throw validation exception and make no calls when prison code is blank`() = runTest {
    val exception = assertThrows<ValidationException> {
      eventReviewService.getEventsDataForReview(" ", date, page = 0, size = 10, sortDirection = "ascending")
    }

    assertThat(exception).hasMessage("Prison code must be provided")
    verifyNoInteractions(activitiesApiClient)
    verifyNoInteractions(prisonerSearchService)
    verifyNoInteractions(prisonService)
  }
}
