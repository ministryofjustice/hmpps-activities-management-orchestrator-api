package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 
 * @param prisonerNumbers List of prisoner numbers to search by
 */
data class PrisonerNumbers(
  @field:Schema(
    example = "[\"A1234AA\"]",
    requiredMode = Schema.RequiredMode.REQUIRED,
    description = "List of prisoner numbers to search by",
  )
  @get:JsonProperty("prisonerNumbers", required = true)
  val prisonerNumbers: List<String>,
)
