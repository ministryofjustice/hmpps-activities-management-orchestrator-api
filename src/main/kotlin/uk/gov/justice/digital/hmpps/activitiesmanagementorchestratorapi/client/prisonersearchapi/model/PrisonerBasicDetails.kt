package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param prisonerNumber Prisoner Number
 * @param firstName First Name
 * @param lastName Last name
 * @param cellLocation In prison cell location
 */
data class PrisonerBasicDetails(

  @Schema(example = "A1234AA", required = true, description = "Prisoner Number")
  @get:JsonProperty("prisonerNumber", required = true) val prisonerNumber: String,

  @Schema(example = "Robert", required = true, description = "First Name")
  @get:JsonProperty("firstName", required = true) val firstName: String,

  @Schema(example = "Larsen", required = true, description = "Last name")
  @get:JsonProperty("lastName", required = true) val lastName: String,

  @Schema(example = "A-1-002", description = "In prison cell location")
  @get:JsonProperty("cellLocation") val cellLocation: String? = null,
)
