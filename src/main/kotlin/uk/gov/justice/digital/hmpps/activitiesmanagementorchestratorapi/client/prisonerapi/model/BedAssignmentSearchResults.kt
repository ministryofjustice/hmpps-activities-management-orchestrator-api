package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonerapi.model

import com.fasterxml.jackson.annotation.JsonProperty

data class BedAssignmentSearchResults(
  val content: List<BedAssignment>,
  @JsonProperty("number")
  val pageNumber: Int,
  val totalElements: Long,
  val totalPages: Int,
)
