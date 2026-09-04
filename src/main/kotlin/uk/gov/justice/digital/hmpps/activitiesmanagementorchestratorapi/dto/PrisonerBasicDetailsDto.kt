package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Basic prisoner details")
data class PrisonerBasicDetailsDto(
  @Schema(description = "The prisoner's first name", example = "JOE")
  val firstName: String,
  @Schema(description = "The prisoner's last name", example = "BLOGGS")
  val lastName: String,
  @Schema(description = "The prisoner's current cell location", example = "2-1-007")
  val cellLocation: String? = null,
)
