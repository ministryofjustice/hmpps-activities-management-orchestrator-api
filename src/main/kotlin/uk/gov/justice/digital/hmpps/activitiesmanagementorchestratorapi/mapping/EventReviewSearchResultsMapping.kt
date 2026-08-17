package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.mapping

import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model.EventReview
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model.EventReviewSearchResults
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EventReviewDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EventReviewSearchResultsDto

internal fun EventReviewSearchResults.toDto(): EventReviewSearchResultsDto = EventReviewSearchResultsDto(
  content = content.map { it.toDto() },
  pageNumber = pageNumber,
  totalElements = totalElements,
  totalPages = totalPages,
)

internal fun EventReview.toDto(): EventReviewDto = EventReviewDto(
  eventReviewId = eventReviewId,
  serviceIdentifier = serviceIdentifier,
  eventType = eventType,
  eventTime = eventTime,
  prisonCode = prisonCode,
  prisonerNumber = prisonerNumber,
  bookingId = bookingId,
  eventData = eventData,
  acknowledgedTime = acknowledgedTime,
  acknowledgedBy = acknowledgedBy,
  eventDescription = eventDescription,
  activeAllocations = activeAllocations,
)
