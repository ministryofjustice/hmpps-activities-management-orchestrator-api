package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonerapi.model

import java.time.LocalDate
import java.time.LocalDateTime

data class BedAssignment(
  val bookingId: Long? = null,
  val livingUnitId: Long? = null,
  val assignmentDate: LocalDate? = null,
  val assignmentDateTime: LocalDateTime? = null,
  val assignmentReason: String? = null,
  val assignmentEndDate: LocalDate? = null,
  val assignmentEndDateTime: LocalDateTime? = null,
  val agencyId: String? = null,
  val description: String? = null,
  val bedAssignmentHistorySequence: Int? = null,
  val movementMadeBy: String? = null,
  val offenderNo: String? = null,
)
