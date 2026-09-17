package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.api.ActivitiesApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model.EventReviewDescription
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.api.PrisonerSearchApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.helpers.eventReviewFactory
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.helpers.eventReviewSearchResultsFactory
import java.time.LocalDate

class EventReviewServiceTest {
  private val activitiesApiClient: ActivitiesApiClient = mock()
  private val prisonerSearchApiClient: PrisonerSearchApiClient = mock()
  private val eventReviewService = EventReviewService(activitiesApiClient, prisonerSearchApiClient)
  private val prisonerSearchService = PrisonerSearchService(prisonerSearchApiClient)

  private val date = LocalDate.of(2026, 8, 1)

  @Test
  fun `should return event data based on prison code and date`() = runTest {
    val apiResponse = eventReviewSearchResultsFactory(
      content = listOf(
        eventReviewFactory(),
        eventReviewFactory(eventReviewId = 2L, eventDescription = EventReviewDescription.RELEASED),
      ),
      totalElements = 2L,
    )

    whenever(activitiesApiClient.getEventsDataForReview("MDI", date, null, null)).thenReturn(apiResponse)

    val result = eventReviewService.getEventsDataForReview("MDI", date)

    assertThat(result.content).hasSize(2)
    assertThat(result.totalElements).isEqualTo(2L)

    with(result.content[0]) {
      assertThat(eventReviewId).isEqualTo(1L)
      assertThat(eventDescription).isEqualTo(EventReviewDescription.TEMPORARY_RELEASE)
    }

    with(result.content[1]) {
      assertThat(eventReviewId).isEqualTo(2L)
      assertThat(eventDescription).isEqualTo(EventReviewDescription.RELEASED)
    }
  }

  @Test
  fun `should pass optional parameters to the API client`() = runTest {
    val apiResponse = eventReviewSearchResultsFactory()

    whenever(
      activitiesApiClient.getEventsDataForReview("MDI", date, prisonerNumber = "A1234AA", includeAcknowledged = true),
    ).thenReturn(apiResponse)

    val result = eventReviewService.getEventsDataForReview("MDI", date, prisonerNumber = "A1234AA", includeAcknowledged = true)

    assertThat(result.content).hasSize(1)
    assertThat(result.totalElements).isEqualTo(1L)
    assertThat(result.totalPages).isEqualTo(1)
    verify(activitiesApiClient).getEventsDataForReview("MDI", date, prisonerNumber = "A1234AA", includeAcknowledged = true)
  }

  @Test
  fun `should return empty results when no events found`() = runTest {
    val emptyResponse = eventReviewSearchResultsFactory(
      content = emptyList(),
      totalElements = 0L,
      totalPages = 0,
    )

    whenever(activitiesApiClient.getEventsDataForReview("MDI", date, null, null)).thenReturn(emptyResponse)

    val result = eventReviewService.getEventsDataForReview("MDI", date)

    assertThat(result.content).isEmpty()
    assertThat(result.totalElements).isZero()
    assertThat(result.totalPages).isZero()
  }

  @Test
  fun `should return set of event data enriched with the prisoner's basic details`() = runTest {

    val apiResponse = eventReviewSearchResultsFactory(
      content = listOf(
        eventReviewFactory(prisonerNumber = "G4793VF"),
        eventReviewFactory(eventReviewId = 2L, eventDescription = EventReviewDescription.RELEASED),
      ),
      totalElements = 2L,
    )

    whenever(activitiesApiClient.getEventsDataForReview("MDI", date, null, null)).thenReturn(apiResponse)

    val result = eventReviewService.getEventsDataForReview("MDI", date)

    with(result.content[0]) {
      assertThat(eventReviewId).isEqualTo(1L)
      assertThat(eventDescription).isEqualTo(EventReviewDescription.TEMPORARY_RELEASE)
    }

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

    whenever(prisonerSearchApiClient.findByPrisonerNumbersMap(prisonerNumbers)).thenReturn(
      mapOf(
        "G4793VF" to firstPrisoner,
        "A1234AA" to secondPrisoner,
      ),
    )

    val result1 = prisonerSearchService.getBasicPrisonerDetailsMap(prisonerNumbers)

    assertThat(result1).isNotEmpty()
    assertThat(result1["G4793VF"]?.prisonerNumber).isEqualTo("G4793VF")
    assertThat(result1["G4793VF"]?.firstName).isEqualTo("JOE")
    assertThat(result1["G4793VF"]?.lastName).isEqualTo("BLOGGS")
    assertThat(result1["G4793VF"]?.cellLocation).isEqualTo("2-1-007")

    val result2 = eventReviewService.getEnrichedEventsDataForReview("MDI", date, null, null)

    assertThat(result2.content).isNotEmpty()
    assertThat(result2.content[0].eventReviewId).isEqualTo(1L)
    assertThat(result2.content[0].eventDescription).isEqualTo(EventReviewDescription.TEMPORARY_RELEASE)
    assertThat(result2.content[0].prisoner?.prisonerNumber).isEqualTo("G4793VF")
    assertThat(result2.content[0].prisoner?.firstName).isEqualTo("JOE")
    assertThat(result2.content[0].prisoner?.lastName).isEqualTo("BLOGGS")
    assertThat(result2.content[0].prisoner?.cellLocation).isEqualTo("2-1-007")

    assertThat(result2.content[1].eventReviewId).isEqualTo(2L)
    assertThat(result2.content[1].eventDescription).isEqualTo(EventReviewDescription.RELEASED)
    assertThat(result2.content[1].prisoner?.prisonerNumber).isEqualTo("A1234AA")
    assertThat(result2.content[1].prisoner?.firstName).isEqualTo("JANE")
    assertThat(result2.content[1].prisoner?.lastName).isEqualTo("SMITH")
    assertThat(result2.content[1].prisoner?.cellLocation).isEqualTo("1-2-003")
  }
}
