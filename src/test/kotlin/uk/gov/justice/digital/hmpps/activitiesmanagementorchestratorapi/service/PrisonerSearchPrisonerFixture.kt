package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.extensions.MovementType
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.CurrentIncentive
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.IncentiveLevel
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.Prisoner
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.PrisonerAlert
import java.time.LocalDate

object PrisonerSearchPrisonerFixture {
  fun instance(
    prisonerNumber: String = "G4793VF",
    firstName: String = "Joe",
    lastName: String = "Bloggs",
    inOutStatus: Prisoner.InOutStatus = Prisoner.InOutStatus.IN,
    status: String = "ACTIVE IN",
    restrictedPatient: Boolean = false,
    bookingId: Long? = 900001,
    prisonId: String? = "MDI",
    cellLocation: String? = "1-2-3",
    currentIncentive: CurrentIncentive? = CurrentIncentive(
      level = IncentiveLevel("Basic", "BAS"),
      dateTime = "2020-07-20T10:36:53",
      nextReviewDate = LocalDate.of(2021, 7, 20),
    ),
    lastMovementType: MovementType? = null,
    releaseDate: LocalDate? = null,
    confirmedReleaseDate: LocalDate? = null,
    alerts: List<PrisonerAlert> = emptyList(),
    legalStatus: Prisoner.LegalStatus? = null,
    category: String? = "P",
  ) = Prisoner(
    prisonerNumber = prisonerNumber,
    firstName = firstName,
    lastName = lastName,
    status = status,
    restrictedPatient = restrictedPatient,
    inOutStatus = inOutStatus,
    bookingId = bookingId?.toString(),
    prisonId = prisonId,
    cellLocation = cellLocation,
    currentIncentive = currentIncentive,
    lastMovementTypeCode = lastMovementType?.nomisShortCode,
    releaseDate = releaseDate,
    confirmedReleaseDate = confirmedReleaseDate,
    alerts = alerts,
    legalStatus = legalStatus,
    category = category,
  )
}
