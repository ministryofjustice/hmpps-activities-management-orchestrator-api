package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model

import com.fasterxml.jackson.annotation.JsonProperty

data class PagedPrisoner(
  @get:JsonProperty("content", required = true) val content: List<Prisoner>,
)
