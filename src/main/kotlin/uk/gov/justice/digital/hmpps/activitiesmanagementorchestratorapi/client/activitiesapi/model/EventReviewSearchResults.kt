package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model

data class EventReviewSearchResults(
  val content: List<EventReview>,
  val pageNumber: Int,
  val totalElements: Long,
  val totalPages: Int,
)
