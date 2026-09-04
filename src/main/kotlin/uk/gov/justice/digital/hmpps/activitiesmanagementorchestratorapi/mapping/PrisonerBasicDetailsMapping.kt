package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.mapping

import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.Prisoner
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.PrisonerBasicDetailsDto

internal fun Prisoner.toDto(): PrisonerBasicDetailsDto = PrisonerBasicDetailsDto(
  firstName = firstName,
  lastName = lastName,
  cellLocation = cellLocation,
)
