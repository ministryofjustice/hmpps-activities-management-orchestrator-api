package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.api.ActivitiesApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model.EventReviewDescription
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.helpers.eventReviewFactory
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.helpers.eventReviewSearchResultsFactory
import java.time.LocalDate

class EventReviewServiceTest {
  private val activitiesApiClient: ActivitiesApiClient = mock()
  private val eventReviewService = EventReviewService(activitiesApiClient)

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
}
