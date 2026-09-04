package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.mapping

import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.PrisonerBasicDetails
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.PrisonerBasicDetailsDto

internal fun PrisonerBasicDetails.toDto(): PrisonerBasicDetailsDto = PrisonerBasicDetailsDto(
  firstName = firstName,
  lastName = lastName,
  cellLocation = cellLocation,
)
