package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.helpers

import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model.EventReview
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model.EventReviewDescription
import java.time.LocalDateTime

internal fun eventReviewFactory(
  eventReviewId: Long = 1L,
  serviceIdentifier: String? = "SAA",
  eventType: String? = "prison-offender-events.prisoner.released",
  eventTime: LocalDateTime? = LocalDateTime.of(2026, 8, 1, 10, 0),
  prisonCode: String? = "MDI",
  prisonerNumber: String? = "A1234AA",
  bookingId: Int? = 123456,
  eventData: String? = "event data",
  acknowledgedTime: LocalDateTime? = LocalDateTime.of(2026, 8, 12, 20, 0),
  acknowledgedBy: String? = "Staff",
  eventDescription: EventReviewDescription? = EventReviewDescription.TEMPORARY_RELEASE,
  activeAllocations: List<String> = listOf("Test Allocation 1", "Test Allocation 2"),
) = EventReview(
  eventReviewId = eventReviewId,
  serviceIdentifier = serviceIdentifier,
  eventType = eventType,
  eventTime = eventTime,
  prisonCode = prisonCode,
  prisonerNumber = prisonerNumber,
  bookingId = bookingId,
  eventData = eventData,
  acknowledgedTime = acknowledgedTime,
  acknowledgedBy = acknowledgedBy,
  eventDescription = eventDescription,
  activeAllocations = activeAllocations,
)
