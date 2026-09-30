package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "List of Cell Location")
data class CellLocationResultsDto(
  @Schema(description = "The prisoner's cell location history", example = "2-1-007, 2-1-008")
  val cellLocations: List<String>? = null,
)
