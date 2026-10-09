package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import jakarta.validation.ValidationException
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.api.PrisonApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.BedAssignment
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.BedAssignmentSearchResults
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.PrisonDetails
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.PrisonDetailsDto

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
  fun `should return a map of booking id to bed assignment history for a list of booking ids`() = runTest {
    val firstAssignment = BedAssignmentSearchResults(
      content = listOf(BedAssignment(description = "MDI-1-1")),
      pageNumber = 0,
      totalElements = 1,
      totalPages = 1,
    )
    val secondAssignment = BedAssignmentSearchResults(
      content = listOf(BedAssignment(description = "MDI-2-1")),
      pageNumber = 0,
      totalElements = 1,
      totalPages = 1,
    )

    whenever(prisonApiClient.getBedAssignmentsHistoryByBookingId("111", 0, 2)).thenReturn(firstAssignment)
    whenever(prisonApiClient.getBedAssignmentsHistoryByBookingId("222", 0, 2)).thenReturn(secondAssignment)

    val result = prisonService.getCurrentAndPreviousBedAssignment(listOf(111, 222))

    assertThat(result).isEqualTo(mapOf(111 to firstAssignment, 222 to secondAssignment))
  }

  @Test
  fun `should omit booking ids from the map when no bed assignment history is found`() = runTest {
    val assignment = BedAssignmentSearchResults(
      content = listOf(BedAssignment(description = "MDI-1-1")),
      pageNumber = 0,
      totalElements = 1,
      totalPages = 1,
    )

    whenever(prisonApiClient.getBedAssignmentsHistoryByBookingId("111", 0, 2)).thenReturn(assignment)
    whenever(prisonApiClient.getBedAssignmentsHistoryByBookingId("222", 0, 2)).thenReturn(null)

    val result = prisonService.getCurrentAndPreviousBedAssignment(listOf(111, 222))

    assertThat(result).isEqualTo(mapOf(111 to assignment))
  }

  @Test
  fun `should return an empty map when no booking ids are supplied`() = runTest {
    val result = prisonService.getCurrentAndPreviousBedAssignment(emptyList())

    assertThat(result).isEmpty()
    verifyNoInteractions(prisonApiClient)
  }

  @Test
  fun `should only look up each distinct booking id once`() = runTest {
    val assignment = BedAssignmentSearchResults(
      content = listOf(BedAssignment(description = "MDI-1-1")),
      pageNumber = 0,
      totalElements = 1,
      totalPages = 1,
    )

    whenever(prisonApiClient.getBedAssignmentsHistoryByBookingId("111", 0, 2)).thenReturn(assignment)

    val result = prisonService.getCurrentAndPreviousBedAssignment(listOf(111, 111, 111))

    assertThat(result).isEqualTo(mapOf(111 to assignment))
    verify(prisonApiClient, times(1)).getBedAssignmentsHistoryByBookingId("111", 0, 2)
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

  @Test
  fun `should propagate exceptions from the upstream prisoner api client for a list of booking ids`() = runTest {
    whenever(prisonApiClient.getBedAssignmentsHistoryByBookingId("111", 0, 2)).thenThrow(RuntimeException("Upstream failure"))

    val exception = assertThrows<RuntimeException> {
      prisonService.getCurrentAndPreviousBedAssignment(listOf(111))
    }

    assertThat(exception).hasMessage("Upstream failure")
  }

  @Test
  fun `should return the prison name for a prison code`() = runTest {
    val prisonCode = "LEI"

    whenever(prisonApiClient.getPrisonName(prisonCode)).thenReturn(PrisonDetails(description = "Leeds (HMP)"))

    val result = prisonService.getPrisonName(prisonCode)

    assertThat(result).isEqualTo(PrisonDetailsDto(prisonName = "Leeds (HMP)"))
    verify(prisonApiClient).getPrisonName(prisonCode)
  }

  @Test
  fun `should throw validation exception when prison code is blank`() = runTest {
    val exception = assertThrows<ValidationException> {
      prisonService.getPrisonName("")
    }

    assertThat(exception).hasMessage("Prison code must be provided")
    verifyNoInteractions(prisonApiClient)
  }

  @Test
  fun `should propagate exceptions from the upstream prison api client when fetching prison name`() = runTest {
    val prisonCode = "LEI"
    whenever(prisonApiClient.getPrisonName(prisonCode)).thenThrow(RuntimeException("Upstream failure"))

    val exception = assertThrows<RuntimeException> {
      prisonService.getPrisonName(prisonCode)
    }

    assertThat(exception).hasMessage("Upstream failure")
  }
}
