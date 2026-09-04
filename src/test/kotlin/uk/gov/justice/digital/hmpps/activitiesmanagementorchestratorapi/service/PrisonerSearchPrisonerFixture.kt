package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.Prisoner

object PrisonerSearchPrisonerFixture {
  fun instance(
    prisonerNumber: String = "G4793VF",
    firstName: String = "Joe",
    lastName: String = "Bloggs",
    cellLocation: String? = "1-2-3",
  ) = Prisoner(
    prisonerNumber = prisonerNumber,
    firstName = firstName,
    lastName = lastName,
    cellLocation = cellLocation,
  )
}
