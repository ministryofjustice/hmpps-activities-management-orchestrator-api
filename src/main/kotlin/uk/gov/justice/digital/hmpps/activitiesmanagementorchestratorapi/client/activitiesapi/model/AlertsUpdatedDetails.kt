package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model

import io.swagger.v3.oas.annotations.media.Schema

data class AlertsUpdatedDetails(
  @Schema(description = "The alert codes that were added", example = "[\"A1\", \"A2\"]")
  val alertsAdded: List<String> = emptyList(),

  @Schema(description = "The alert codes that were removed", example = "[\"C1\", \"C2\"]")
  val alertsClosed: List<String> = emptyList(),
)
