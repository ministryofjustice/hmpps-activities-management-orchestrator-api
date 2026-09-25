package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonerapi

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.RetryApiService
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonerapi.api.PrisonerApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonerapi.model.BedAssignment
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonerapi.model.BedAssignmentSearchResults
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.integration.wiremock.PrisonerApiMockServer

class PrisonerApiClientTest {
  private lateinit var prisonerApiClient: PrisonerApiClient

  companion object {
    @JvmField
    internal val prisonerApiMockServer = PrisonerApiMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      prisonerApiMockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      prisonerApiMockServer.stop()
    }
  }

  @BeforeEach
  fun resetStubs() {
    prisonerApiMockServer.resetAll()
    val webClient = WebClient.create("http://localhost:${prisonerApiMockServer.port()}")
    prisonerApiClient = PrisonerApiClient(webClient, RetryApiService(3, 250))
  }

  @Test
  fun `getBedAssignmentsHistoryByBookingId - success`() = runTest {
    val bookingId = "12345"
    val expected = BedAssignmentSearchResults(
      content = listOf(BedAssignment(bookingId = 12345, livingUnitId = 1, agencyId = "MDI")),
      pageNumber = 0,
      totalElements = 1,
      totalPages = 1,
    )

    prisonerApiMockServer.stubGetBedAssignmentsHistoryByBookingId(bookingId, 0, 1000, expected)

    val result = prisonerApiClient.getBedAssignmentsHistoryByBookingId(bookingId)

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `getBedAssignmentsHistoryByBookingId - uses supplied page and size`() = runTest {
    val bookingId = "12345"
    val expected = BedAssignmentSearchResults(content = emptyList(), pageNumber = 2, totalElements = 0, totalPages = 0)

    prisonerApiMockServer.stubGetBedAssignmentsHistoryByBookingId(bookingId, 2, 10, expected)

    val result = prisonerApiClient.getBedAssignmentsHistoryByBookingId(bookingId, page = 2, size = 10)

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `getBedAssignmentsHistoryByBookingId - returns null and makes no call when bookingId is empty`() = runTest {
    val result = prisonerApiClient.getBedAssignmentsHistoryByBookingId("")

    assertThat(result).isNull()
    assertThat(prisonerApiMockServer.allServeEvents).isEmpty()
  }

  @Test
  fun `getBedAssignmentsHistoryByBookingId - should throw exception on 500 response`() = runTest {
    val bookingId = "12345"
    prisonerApiMockServer.stubGetBedAssignmentsHistoryByBookingIdServerError(bookingId, 0, 1000)

    assertThrows<WebClientResponseException.InternalServerError> {
      prisonerApiClient.getBedAssignmentsHistoryByBookingId(bookingId)
    }
  }

  @Nested
  @DisplayName("Retrying failed API calls")
  inner class RetryingFailedApiCalls {
    val bookingId = "12345"
    val expected = BedAssignmentSearchResults(content = emptyList(), pageNumber = 0, totalElements = 0, totalPages = 0)

    @Test
    fun `will succeed if number of fails is less than maximum allowed`(): Unit = runTest {
      prisonerApiMockServer.stubGetBedAssignmentsHistoryByBookingIdWithConnectionReset(bookingId, 0, 1000, expected)

      val result = prisonerApiClient.getBedAssignmentsHistoryByBookingId(bookingId)

      assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `will fail if number of fails is more than maximum allowed`(): Unit = runTest {
      prisonerApiMockServer.stubGetBedAssignmentsHistoryByBookingIdWithConnectionReset(bookingId, 0, 1000, expected, 3)

      assertThrows<WebClientRequestException> {
        prisonerApiClient.getBedAssignmentsHistoryByBookingId(bookingId)
      }
    }
  }
}
