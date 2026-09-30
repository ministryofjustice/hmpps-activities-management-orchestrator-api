package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model

import io.swagger.v3.oas.annotations.media.Schema

data class PrisonerUpdatedDetails(
  @Schema(description = "The cell the prisoner was moved from", example = "MDI-1-1-001")
  val previousCell: String? = null,

  @Schema(description = "The cell the prisoner was moved to", example = "MDI-1-1-002")
  val newCell: String? = null,
)
