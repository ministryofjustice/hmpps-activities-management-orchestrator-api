package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.api.PrisonApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.BedAssignmentSearchResults
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.PrisonDetailsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.mapping.toDto

@Service
class PrisonService(
  private val prisonApiClient: PrisonApiClient,
) {
  @Value("\${prison.service.bed-assignment.history-length:2}")
  private val historyLength: Int = 2

  suspend fun getCurrentAndPreviousBedAssignment(bookingId: String): BedAssignmentSearchResults? {
    if (bookingId.isBlank()) {
      throw ValidationException("Booking Id must be provided")
    }

    if (bookingId.toLongOrNull() !is Long) {
      throw ValidationException("Booking ID must be a valid number")
    }

    return prisonApiClient.getBedAssignmentsHistoryByBookingId(bookingId, 0, size = historyLength)
  }

  suspend fun getPrisonName(prisonCode: String): PrisonDetailsDto {
    if (prisonCode.isBlank()) {
      throw ValidationException("Prison code must be provided")
    }

    return prisonApiClient.getPrisonName(prisonCode).toDto()
  }
}
