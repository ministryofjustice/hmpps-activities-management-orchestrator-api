package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model

import java.time.LocalDateTime

data class EventReview(
  val eventReviewId: Long,
  val serviceIdentifier: String? = null,
  val eventType: String? = null,
  val eventTime: LocalDateTime? = null,
  val prisonCode: String? = null,
  val prisonerNumber: String? = null,
  val bookingId: Int? = null,
  val eventData: String? = null,
  val acknowledgedTime: LocalDateTime? = null,
  val acknowledgedBy: String? = null,
  val eventDescription: EventReviewDescription? = null,
  val activeAllocations: List<String> = emptyList(),
  val alertDetails: AlertsUpdatedDetails? = null,
  val incentiveDetails: IncentiveLevelsChangedDetails? = null,
  val prisonerUpdatedDetails: PrisonerUpdatedDetails? = null,
  val mergeDetails: OffenderMergedDetails? = null,
)

enum class EventReviewDescription {
  ACTIVITY_SUSPENDED,
  ACTIVITY_ENDED,
  RELEASED,
  PERMANENT_RELEASE,
  TEMPORARY_RELEASE,
  ALERT_ADDED,
  ALERT_CLOSED,
  ALERTS_ADDED_AND_CLOSED,
  TRANSFER_OUT,
  ARRIVAL_OR_RETURN,
  NON_ASSOCIATION,
  CELL_MOVE,
  INCENTIVE_LEVEL_CHANGED,
  PRISONER_MERGED,
  APPOINTMENTS_CANCELLED,
  APPOINTMENTS_KEPT,
}
