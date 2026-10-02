package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.api.ActivitiesApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model.EventReviewDescription
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.BedAssignment
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.BedAssignmentSearchResults
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.PrisonerBasicDetails
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
  fun `should enrich prisoner-updated events with the new and previous cell locations`() = runTest {
    val apiResponse = eventReviewSearchResultsFactory(
      content = listOf(
        eventReviewFactory(
          eventType = "prisoner-offender-search.prisoner.updated",
          bookingId = 123456,
        ),
      ),
    )
    val bedAssignmentHistory = BedAssignmentSearchResults(
      content = listOf(
        BedAssignment(description = "MDI-1-2"),
        BedAssignment(description = "MDI-1-1"),
      ),
      pageNumber = 0,
      totalElements = 2,
      totalPages = 1,
    )

    whenever(activitiesApiClient.getEventsDataForReview("MDI", date, null, null, null, 0, 10, "ascending")).thenReturn(apiResponse)
    whenever(prisonerSearchService.getBasicPrisonerDetailsMap(listOf("A1234AA"))).thenReturn(emptyMap<String, PrisonerBasicDetails>())
    whenever(prisonService.getCurrentAndPreviousBedAssignment(listOf(123456))).thenReturn(mapOf(123456 to bedAssignmentHistory))

    val result = eventReviewService.getEventsDataForReview("MDI", date, page = 0, size = 10, sortDirection = "ascending")

    assertThat(result.content[0].prisonerUpdatedDetails?.newCell).isEqualTo("MDI-1-2")
    assertThat(result.content[0].prisonerUpdatedDetails?.previousCell).isEqualTo("MDI-1-1")
    verify(prisonService).getCurrentAndPreviousBedAssignment(listOf(123456))
  }

  @Test
  fun `should only look up bed assignments for prisoner-updated events, de-duplicating booking ids`() = runTest {
    val apiResponse = eventReviewSearchResultsFactory(
      content = listOf(
        eventReviewFactory(
          eventReviewId = 1L,
          eventType = "prisoner-offender-search.prisoner.updated",
          bookingId = 123456,
        ),
        eventReviewFactory(
          eventReviewId = 2L,
          eventType = "prisoner-offender-search.prisoner.updated",
          bookingId = 123456,
        ),
        eventReviewFactory(
          eventReviewId = 3L,
          eventType = "prison-offender-events.prisoner.released",
          bookingId = 999999,
        ),
      ),
      totalElements = 3L,
    )

    whenever(activitiesApiClient.getEventsDataForReview("MDI", date, null, null, null, 0, 10, "ascending")).thenReturn(apiResponse)
    whenever(prisonerSearchService.getBasicPrisonerDetailsMap(listOf("A1234AA"))).thenReturn(emptyMap<String, PrisonerBasicDetails>())
    whenever(prisonService.getCurrentAndPreviousBedAssignment(listOf(123456))).thenReturn(emptyMap())

    eventReviewService.getEventsDataForReview("MDI", date, page = 0, size = 10, sortDirection = "ascending")

    verify(prisonService).getCurrentAndPreviousBedAssignment(listOf(123456))
  }

  @Test
  fun `should not enrich events whose event type is not a prisoner-updated event, even if a bed assignment exists for the booking id`() = runTest {
    val apiResponse = eventReviewSearchResultsFactory(
      content = listOf(
        eventReviewFactory(
          eventType = "prison-offender-events.prisoner.released",
          bookingId = 123456,
        ),
      ),
    )

    whenever(activitiesApiClient.getEventsDataForReview("MDI", date, null, null, null, 0, 10, "ascending")).thenReturn(apiResponse)
    whenever(prisonerSearchService.getBasicPrisonerDetailsMap(listOf("A1234AA"))).thenReturn(emptyMap<String, PrisonerBasicDetails>())

    val result = eventReviewService.getEventsDataForReview("MDI", date, page = 0, size = 10, sortDirection = "ascending")

    assertThat(result.content[0].prisonerUpdatedDetails).isNull()
    verifyNoInteractions(prisonService)
  }

  @Test
  fun `should leave prisonerUpdatedDetails null when no bed assignment history is found for the booking id`() = runTest {
    val apiResponse = eventReviewSearchResultsFactory(
      content = listOf(
        eventReviewFactory(
          eventType = "prisoner-offender-search.prisoner.updated",
          bookingId = 123456,
        ),
      ),
    )

    whenever(activitiesApiClient.getEventsDataForReview("MDI", date, null, null, null, 0, 10, "ascending")).thenReturn(apiResponse)
    whenever(prisonerSearchService.getBasicPrisonerDetailsMap(listOf("A1234AA"))).thenReturn(emptyMap<String, PrisonerBasicDetails>())
    whenever(prisonService.getCurrentAndPreviousBedAssignment(listOf(123456))).thenReturn(emptyMap())

    val result = eventReviewService.getEventsDataForReview("MDI", date, page = 0, size = 10, sortDirection = "ascending")

    assertThat(result.content[0].prisonerUpdatedDetails).isNull()
  }

  @Test
  fun `should leave prisonerUpdatedDetails null when a prisoner-updated event has no booking id`() = runTest {
    val apiResponse = eventReviewSearchResultsFactory(
      content = listOf(
        eventReviewFactory(
          eventType = "prisoner-offender-search.prisoner.updated",
          bookingId = null,
        ),
      ),
    )

    whenever(activitiesApiClient.getEventsDataForReview("MDI", date, null, null, null, 0, 10, "ascending")).thenReturn(apiResponse)
    whenever(prisonerSearchService.getBasicPrisonerDetailsMap(listOf("A1234AA"))).thenReturn(emptyMap<String, PrisonerBasicDetails>())

    val result = eventReviewService.getEventsDataForReview("MDI", date, page = 0, size = 10, sortDirection = "ascending")

    assertThat(result.content[0].prisonerUpdatedDetails).isNull()
    verifyNoInteractions(prisonService)
  }

  @Test
  fun `should not call prison service when there are no events to review`() = runTest {
    val emptyResponse = eventReviewSearchResultsFactory(
      content = emptyList(),
      totalElements = 0L,
      totalPages = 0,
    )

    whenever(activitiesApiClient.getEventsDataForReview("MDI", date, null, null, null, 0, 10, "ascending")).thenReturn(emptyResponse)
    whenever(prisonerSearchService.getBasicPrisonerDetailsMap(emptyList())).thenReturn(emptyMap<String, PrisonerBasicDetails>())

    eventReviewService.getEventsDataForReview("MDI", date, page = 0, size = 10, sortDirection = "ascending")

    verifyNoInteractions(prisonService)
  }
}
