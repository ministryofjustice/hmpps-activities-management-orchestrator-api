package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import jakarta.validation.ValidationException
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.api.PrisonApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.BedAssignment
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.BedAssignmentSearchResults

class PrisonServiceTest {
  private val prisonApiClient: PrisonApiClient = mock()
  private val prisonService = PrisonService(prisonApiClient)

  @Test
  fun `should return the current and previous bed assignments for a booking id`() = runTest {
    val bookingId = "12345"
    val expected = BedAssignmentSearchResults(
      content = listOf(
        BedAssignment(description = "MDI-1-2"),
        BedAssignment(description = "MDI-1-1"),
      ),
      pageNumber = 0,
      totalElements = 2,
      totalPages = 1,
    )

    whenever(prisonApiClient.getBedAssignmentsHistoryByBookingId(bookingId, 0, 2)).thenReturn(expected)

    val result = prisonService.getCurrentAndPreviousBedAssignment(bookingId)

    assertThat(result).isEqualTo(expected)
    verify(prisonApiClient).getBedAssignmentsHistoryByBookingId(bookingId, 0, 2)
  }

  @Test
  fun `should request the first page using the configured history length as the page size`() = runTest {
    val bookingId = "12345"
    whenever(prisonApiClient.getBedAssignmentsHistoryByBookingId(bookingId, 0, 2)).thenReturn(null)

    prisonService.getCurrentAndPreviousBedAssignment(bookingId)

    verify(prisonApiClient).getBedAssignmentsHistoryByBookingId(bookingId, 0, 2)
  }

  @Test
  fun `should return null when no bed assignment history is found`() = runTest {
    val bookingId = "12345"
    whenever(prisonApiClient.getBedAssignmentsHistoryByBookingId(bookingId, 0, 2)).thenReturn(null)

    val result = prisonService.getCurrentAndPreviousBedAssignment(bookingId)

    assertThat(result).isNull()
  }

  @Test
  fun `should throw validation exception when booking id is empty`() = runTest {
    val exception = assertThrows<ValidationException> {
      prisonService.getCurrentAndPreviousBedAssignment("")
    }

    assertThat(exception).hasMessage("Booking Id must be provided")
    verifyNoInteractions(prisonApiClient)
  }

  @Test
  fun `should throw validation exception when booking id is not a valid number`() = runTest {
    val exception = assertThrows<ValidationException> {
      prisonService.getCurrentAndPreviousBedAssignment("abc123")
    }

    assertThat(exception).hasMessage("Booking ID must be a valid number")
    verifyNoInteractions(prisonApiClient)
  }

  @Test
  fun `should propagate exceptions from the upstream prisoner api client`() = runTest {
    val bookingId = "12345"
    whenever(prisonApiClient.getBedAssignmentsHistoryByBookingId(bookingId, 0, 2)).thenThrow(RuntimeException("Upstream failure"))

    val exception = assertThrows<RuntimeException> {
      prisonService.getCurrentAndPreviousBedAssignment(bookingId)
    }

    assertThat(exception).hasMessage("Upstream failure")
  }
}
