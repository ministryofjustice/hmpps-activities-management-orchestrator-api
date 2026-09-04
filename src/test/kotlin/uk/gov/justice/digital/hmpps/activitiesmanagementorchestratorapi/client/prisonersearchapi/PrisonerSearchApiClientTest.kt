package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi

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
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.api.PrisonerSearchApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.integration.wiremock.PrisonerSearchApiMockServer
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service.PrisonerSearchPrisonerFixture
class PrisonerSearchApiClientTest {
  private lateinit var prisonerSearchApiClient: PrisonerSearchApiClient

  companion object {
    @JvmField
    internal val prisonerSearchApiMockServer = PrisonerSearchApiMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      prisonerSearchApiMockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      prisonerSearchApiMockServer.stop()
    }
  }

  @BeforeEach
  fun resetStubs() {
    prisonerSearchApiMockServer.resetAll()
    val webClient = WebClient.create("http://localhost:${prisonerSearchApiMockServer.port()}")
    prisonerSearchApiClient = PrisonerSearchApiClient(webClient, RetryApiService(3, 250))
  }

  @Test
  fun `findByPrisonerNumbers - success`() = runTest {
    val prisonerNumber = "G4793VF"

    prisonerSearchApiMockServer.stubSearchByPrisonerNumbers(prisonerNumber)
    val prisoners = prisonerSearchApiClient.findByPrisonerNumbers(listOf(prisonerNumber))

    assertThat(prisoners).hasSize(1)
    assertThat(prisoners.first().prisonerNumber).isEqualTo(prisonerNumber)
  }

  @Test
  fun `findByPrisonerNumbers no numbers - success`() = runTest {
    val prisoners = prisonerSearchApiClient.findByPrisonerNumbers(emptyList())

    assertThat(prisoners).hasSize(0)
  }

  @Test
  fun `findByPrisonerNumbers batch requests - success`() = runTest {
    val prisonerNumbers = listOf("A1234BC", "B2345CD", "C3456DE", "D4567EF", "E5678FG")

    val batch1 = listOf(
      PrisonerSearchPrisonerFixture.instance(prisonerNumber = "A1234BC", bookingId = 1),
      PrisonerSearchPrisonerFixture.instance(prisonerNumber = "B2345CD", bookingId = 2),
    )
    prisonerSearchApiMockServer.stubSearchByPrisonerNumbers(batch1.map { it.prisonerNumber }, batch1)
    val batch2 = listOf(
      PrisonerSearchPrisonerFixture.instance(prisonerNumber = "C3456DE", bookingId = 3),
      PrisonerSearchPrisonerFixture.instance(prisonerNumber = "D4567EF", bookingId = 4),
    )
    prisonerSearchApiMockServer.stubSearchByPrisonerNumbers(batch2.map { it.prisonerNumber }, batch2)
    val batch3 = listOf(
      PrisonerSearchPrisonerFixture.instance(prisonerNumber = "E5678FG", bookingId = 5),
    )
    prisonerSearchApiMockServer.stubSearchByPrisonerNumbers(batch3.map { it.prisonerNumber }, batch3)

    val batchSize = 2

    val prisoners = prisonerSearchApiClient.findByPrisonerNumbers(prisonerNumbers, batchSize)

    assertThat(prisoners).hasSize(5)
    assertThat(prisoners.map { it.prisonerNumber }).isEqualTo(prisonerNumbers)
    assertThat(prisoners.map { it.bookingId }).isEqualTo(listOf("1", "2", "3", "4", "5"))
  }

  @Test
  fun `findByPrisonerNumbers batch size must be greater than zero`() = runTest {
    val exception = try {
      prisonerSearchApiClient.findByPrisonerNumbers(emptyList(), 0)
      null
    } catch (e: IllegalArgumentException) {
      e
    }

    assertThat(exception).isNotNull
    assertThat(exception).hasMessage("Batch size must be between 1 and 1000")
  }

  @Test
  fun `findByPrisonerNumbers batch size must be less than 1001`() = runTest {
    val exception = try {
      prisonerSearchApiClient.findByPrisonerNumbers(emptyList(), 1001)
      null
    } catch (e: IllegalArgumentException) {
      e
    }

    assertThat(exception).isNotNull
    assertThat(exception).hasMessage("Batch size must be between 1 and 1000")
  }

  @Test
  fun `findByPrisonerNumbersMap omits those not found by prisoner search api`() = runTest {
    val prisonerNumbers = listOf("A1234BC", "B2345CD")
    val foundPrisoner = PrisonerSearchPrisonerFixture.instance(prisonerNumber = "A1234BC")

    prisonerSearchApiMockServer.stubSearchByPrisonerNumbers(prisonerNumbers, listOf(foundPrisoner))

    val prisonerMap = prisonerSearchApiClient.findByPrisonerNumbersMap(prisonerNumbers)

    assertThat(prisonerMap).hasSize(1)
    assertThat(prisonerMap).containsKey("A1234BC")
    assertThat(prisonerMap).doesNotContainKey("B2345CD")
  }

  @Test
  fun `should throw exception on 500 response`() = runTest {
    val prisonerNumber = "G4793VF"
    prisonerSearchApiMockServer.stubSearchByPrisonerNumbersServerError(listOf(prisonerNumber))

    assertThrows<WebClientResponseException.InternalServerError> {
      prisonerSearchApiClient.findByPrisonerNumbers(listOf(prisonerNumber))
    }
  }

  @Nested
  @DisplayName("Retrying failed API calls")
  inner class RetryingFailedApiCalls {
    val prisonerNumber = "G4793VF"
    val prisonerNumbers = listOf(prisonerNumber)
    val expectedPrisoner = PrisonerSearchPrisonerFixture.instance(prisonerNumber = prisonerNumber)

    @Test
    fun `will succeed if number of fails is less than maximum allowed`(): Unit = runTest {
      prisonerSearchApiMockServer.stubSearchByPrisonerNumbersWithConnectionReset(prisonerNumbers, listOf(expectedPrisoner))

      val result = prisonerSearchApiClient.findByPrisonerNumbers(prisonerNumbers)
      assertThat(result).containsExactly(expectedPrisoner)
    }

    @Test
    fun `will fail if number of fails is more than maximum allowed`(): Unit = runTest {
      prisonerSearchApiMockServer.stubSearchByPrisonerNumbersWithConnectionReset(prisonerNumbers, listOf(expectedPrisoner), 3)

      assertThrows<WebClientRequestException> {
        prisonerSearchApiClient.findByPrisonerNumbers(prisonerNumbers)
      }
    }
  }
}
