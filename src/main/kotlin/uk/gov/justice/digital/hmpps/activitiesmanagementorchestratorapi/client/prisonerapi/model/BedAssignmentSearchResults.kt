package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonerapi.model

data class BedAssignmentSearchResults(
  val content: List<BedAssignment>,
  val pageNumber: Int,
  val totalElements: Long,
  val totalPages: Int,
)
