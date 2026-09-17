package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.mapping

import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.PrisonerBasicDetails
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EnrichedEventReviewDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EnrichedEventReviewPrisonerDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EventReviewDto

internal fun EventReviewDto.toEnrichedDto(prisoner: PrisonerBasicDetails?): EnrichedEventReviewDto =
  EnrichedEventReviewDto(
    eventReviewId = eventReviewId,
    serviceIdentifier = serviceIdentifier,
    eventType = eventType,
    eventTime = eventTime,
    prisonCode = prisonCode,
    eventData = eventData,
    acknowledgedTime = acknowledgedTime,
    acknowledgedBy = acknowledgedBy,
    eventDescription = eventDescription,
    activeAllocations = activeAllocations,
    prisoner = EnrichedEventReviewPrisonerDto(
      bookingId = bookingId,
      prisonerNumber = prisonerNumber,
      firstName = prisoner?.firstName,
      lastName = prisoner?.lastName,
      cellLocation = prisoner?.cellLocation,
    ),
  )