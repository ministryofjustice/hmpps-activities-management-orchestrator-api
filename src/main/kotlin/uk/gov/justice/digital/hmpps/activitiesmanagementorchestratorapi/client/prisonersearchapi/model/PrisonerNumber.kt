package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class PrisonerNumber(
  @Schema(example = "A1234AA", required = true, description = "Prisoner Number")
  @get:JsonProperty("prisonerNumber", required = true) val prisonerNumber: String,
)
