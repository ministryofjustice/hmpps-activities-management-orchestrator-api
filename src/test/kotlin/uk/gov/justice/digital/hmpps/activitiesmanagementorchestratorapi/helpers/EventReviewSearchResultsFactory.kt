package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.helpers

import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model.EventReview
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model.EventReviewSearchResults

internal fun eventReviewSearchResultsFactory(
  content: List<EventReview> = listOf(eventReviewFactory()),
  pageNumber: Int = 0,
  totalElements: Long = 1L,
  totalPages: Int = 1,
) = EventReviewSearchResults(
  content = content,
  pageNumber = pageNumber,
  totalElements = totalElements,
  totalPages = totalPages,
)
