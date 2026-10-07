package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.mapping

import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.PrisonDetails
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.PrisonDetailsDto

internal fun PrisonDetails.toDto(): PrisonDetailsDto = PrisonDetailsDto(
  prisonName = this.description,
)
