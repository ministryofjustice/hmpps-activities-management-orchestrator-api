package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Prison details")
data class PrisonDetailsDto(
  @Schema(description = "The prison name", example = "Leeds (HMP)")
  val prisonName: String? = null,
)
