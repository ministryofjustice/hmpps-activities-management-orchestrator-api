package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonerapi.api.PrisonerApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonerapi.model.BedAssignmentSearchResults

@Service
class PrisonerService(
  private val prisonerApiClient: PrisonerApiClient,
) {
  @Value("\${prisoner.service.bed-assignment.history-length:2}")
  private val historyLength: Int = 2

  suspend fun getCurrentAndPreviousBedAssignment(bookingId: String): BedAssignmentSearchResults? {
    if (bookingId.isEmpty()) {
      throw ValidationException("Booking Id must be provided")
    }

    return prisonerApiClient.getBedAssignmentsHistoryByBookingId(bookingId, 0, size = historyLength)
  }
}
