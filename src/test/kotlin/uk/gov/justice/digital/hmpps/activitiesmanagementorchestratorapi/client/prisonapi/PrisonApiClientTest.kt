package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi

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
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.api.PrisonApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.PrisonDetails
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.integration.wiremock.PrisonApiMockServer

class PrisonApiClientTest {
  private lateinit var prisonApiClient: PrisonApiClient

  companion object {
    @JvmField
    internal val prisonApiMockServer = PrisonApiMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      prisonApiMockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      prisonApiMockServer.stop()
    }
  }

  @BeforeEach
  fun resetStubs() {
    prisonApiMockServer.resetAll()
    val webClient = WebClient.create("http://localhost:${prisonApiMockServer.port()}")
    prisonApiClient = PrisonApiClient(webClient, RetryApiService(3, 250))
  }

  @Test
  fun `getPrisonName - success`() = runTest {
    val prisonCode = "LEI"
    val expected = PrisonDetails(
      description = "Leeds (HMP)",
    )

    prisonApiMockServer.stubGetPrisonName(prisonCode, expected)

    val result = prisonApiClient.getPrisonName(prisonCode)

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `getPrisonName - throws exception and makes no call when prisonCode is empty`() = runTest {
    assertThrows<IllegalArgumentException> {
      prisonApiClient.getPrisonName("")
    }

    assertThat(prisonApiMockServer.allServeEvents).isEmpty()
  }

  @Test
  fun `getPrisonName - should throw exception on 404 response`() = runTest {
    val prisonCode = "XXX"
    prisonApiMockServer.stubGetPrisonNameNotFound(prisonCode)

    assertThrows<WebClientResponseException.NotFound> {
      prisonApiClient.getPrisonName(prisonCode)
    }
  }

  @Test
  fun `getPrisonName - should throw exception on 500 response`() = runTest {
    val prisonCode = "LEI"
    prisonApiMockServer.stubGetPrisonNameServerError(prisonCode)

    assertThrows<WebClientResponseException.InternalServerError> {
      prisonApiClient.getPrisonName(prisonCode)
    }
  }

  @Nested
  @DisplayName("Retrying failed API calls")
  inner class RetryingFailedApiCalls {
    val prisonCode = "LEI"
    val expected = PrisonDetails(description = "Leeds (HMP)")

    @Test
    fun `getPrisonName will fail if number of fails is more than maximum allowed`(): Unit = runTest {
      prisonApiMockServer.stubGetPrisonNameWithConnectionReset(prisonCode, expected, 3)

      assertThrows<WebClientRequestException> {
        prisonApiClient.getPrisonName(prisonCode)
      }
    }

    @Test
    fun `getPrisonName will succeed if number of fails is the maximum allowed`(): Unit = runTest {
      prisonApiMockServer.stubGetPrisonNameWithConnectionReset(prisonCode, expected, 2)

      val result = prisonApiClient.getPrisonName(prisonCode)
      assertThat(result).isEqualTo(expected)
    }
  }
}
