package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model

import io.swagger.v3.oas.annotations.media.Schema

data class OffenderMergedDetails(
  @Schema(description = "The prisoner number that was removed by the merge", example = "A1234BC")
  val removedPrisonerNumber: String? = null,

  @Schema(description = "The prisoner number that was retained by the merge", example = "A1234BD")
  val prisonerNumber: String? = null,
)
