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
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonerapi.api.PrisonerApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonerapi.model.BedAssignment
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonerapi.model.BedAssignmentSearchResults

class PrisonerServiceTest {
  private val prisonerApiClient: PrisonerApiClient = mock()
  private val prisonerService = PrisonerService(prisonerApiClient)

  @Test
  fun `should return the current and previous bed assignments for a booking id`() = runTest {
    val bookingId = "12345"
    val expected = BedAssignmentSearchResults(
      content = listOf(
        BedAssignment(bookingId = 12345, livingUnitId = 2, agencyId = "MDI"),
        BedAssignment(bookingId = 12345, livingUnitId = 1, agencyId = "MDI"),
      ),
      pageNumber = 0,
      totalElements = 2,
      totalPages = 1,
    )

    whenever(prisonerApiClient.getBedAssignmentsHistoryByBookingId(bookingId, 0, 2)).thenReturn(expected)

    val result = prisonerService.getCurrentAndPreviousBedAssignment(bookingId)

    assertThat(result).isEqualTo(expected)
    verify(prisonerApiClient).getBedAssignmentsHistoryByBookingId(bookingId, 0, 2)
  }

  @Test
  fun `should request the first page using the configured history length as the page size`() = runTest {
    val bookingId = "12345"
    whenever(prisonerApiClient.getBedAssignmentsHistoryByBookingId(bookingId, 0, 2)).thenReturn(null)

    prisonerService.getCurrentAndPreviousBedAssignment(bookingId)

    verify(prisonerApiClient).getBedAssignmentsHistoryByBookingId(bookingId, 0, 2)
  }

  @Test
  fun `should return null when no bed assignment history is found`() = runTest {
    val bookingId = "12345"
    whenever(prisonerApiClient.getBedAssignmentsHistoryByBookingId(bookingId, 0, 2)).thenReturn(null)

    val result = prisonerService.getCurrentAndPreviousBedAssignment(bookingId)

    assertThat(result).isNull()
  }

  @Test
  fun `should throw validation exception when booking id is empty`() = runTest {
    val exception = assertThrows<ValidationException> {
      prisonerService.getCurrentAndPreviousBedAssignment("")
    }

    assertThat(exception).hasMessage("Booking Id must be provided")
    verifyNoInteractions(prisonerApiClient)
  }

  @Test
  fun `should propagate exceptions from the upstream prisoner api client`() = runTest {
    val bookingId = "12345"
    whenever(prisonerApiClient.getBedAssignmentsHistoryByBookingId(bookingId, 0, 2)).thenThrow(RuntimeException("Upstream failure"))

    val exception = assertThrows<RuntimeException> {
      prisonerService.getCurrentAndPreviousBedAssignment(bookingId)
    }

    assertThat(exception).hasMessage("Upstream failure")
  }
}
