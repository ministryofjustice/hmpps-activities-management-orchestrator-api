package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.mapping

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.helper.eventReviewFactory
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.helper.eventReviewSearchResultsFactory

class EventReviewSearchResultsMappingTest {

  @Test
  fun `should map EventReviewSearchResults to dto`() {
    val eventReviewSearchResults = eventReviewSearchResultsFactory()
    val dto = eventReviewSearchResults.toDto()

    assertThat(dto.pageNumber).isEqualTo(eventReviewSearchResults.pageNumber)
    assertThat(dto.totalElements).isEqualTo(eventReviewSearchResults.totalElements)
    assertThat(dto.totalPages).isEqualTo(eventReviewSearchResults.totalPages)
    assertThat(dto.content).hasSize(eventReviewSearchResults.content.size)
  }

  @Test
  fun `should map EventReview to dto`() {
    val eventReview = eventReviewFactory()
    val dto = eventReview.toDto()

    assertThat(dto.eventReviewId).isEqualTo(eventReview.eventReviewId)
    assertThat(dto.serviceIdentifier).isEqualTo(eventReview.serviceIdentifier)
    assertThat(dto.eventType).isEqualTo(eventReview.eventType)
    assertThat(dto.eventTime).isEqualTo(eventReview.eventTime)
    assertThat(dto.prisonCode).isEqualTo(eventReview.prisonCode)
    assertThat(dto.prisonerNumber).isEqualTo(eventReview.prisonerNumber)
    assertThat(dto.bookingId).isEqualTo(eventReview.bookingId)
    assertThat(dto.eventData).isEqualTo(eventReview.eventData)
    assertThat(dto.acknowledgedTime).isEqualTo(eventReview.acknowledgedTime)
    assertThat(dto.acknowledgedBy).isEqualTo(eventReview.acknowledgedBy)
    assertThat(dto.eventDescription).isEqualTo(eventReview.eventDescription)
    assertThat(dto.activeAllocations).isEqualTo(eventReview.activeAllocations)
  }
}
