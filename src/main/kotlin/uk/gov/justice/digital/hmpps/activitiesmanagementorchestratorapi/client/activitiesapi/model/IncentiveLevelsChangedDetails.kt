package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model

import io.swagger.v3.oas.annotations.media.Schema

data class IncentiveLevelsChangedDetails(
  @Schema(description = "The new incentive level", example = "STD")
  val newLevel: String? = null,

  @Schema(description = "The previous incentive level", example = "BAS")
  val previousLevel: String? = null,
)
