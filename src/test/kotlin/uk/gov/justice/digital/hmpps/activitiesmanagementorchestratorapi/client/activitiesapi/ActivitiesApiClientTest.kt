package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
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
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.api.ActivitiesApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.helpers.eventReviewSearchResultsFactory
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.integration.wiremock.ActivitiesApiMockServer
import java.time.LocalDate

class ActivitiesApiClientTest {
  private lateinit var activitiesApiClient: ActivitiesApiClient

  companion object {
    @JvmField
    internal val activitiesApiMockServer = ActivitiesApiMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      activitiesApiMockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      activitiesApiMockServer.stop()
    }
  }

  @BeforeEach
  fun resetStubs() {
    activitiesApiMockServer.resetAll()
    val webClient = WebClient.create("http://localhost:${activitiesApiMockServer.port()}")
    activitiesApiClient = ActivitiesApiClient(webClient, RetryApiService(3, 250))
  }

  @Test
  fun `should get events for review with all parameters`() = runTest {
    val date = LocalDate.of(2026, 8, 1)
    val expectedResult = eventReviewSearchResultsFactory()

    activitiesApiMockServer.stubGetEventsForReview("MDI", date, expectedResult)

    val result = activitiesApiClient.getEventsDataForReview(
      prisonCode = "MDI",
      date = date,
      prisonerNumber = "A1234AA",
      includeAcknowledged = true,
      page = 0,
      size = 20,
      sortDirection = "descending",
    )

    assertThat(result).isEqualTo(expectedResult)

    activitiesApiMockServer.verify(
      getRequestedFor(urlPathEqualTo("/event-review/prison/MDI"))
        .withQueryParam("date", equalTo("2026-08-01"))
        .withQueryParam("prisonerNumber", equalTo("A1234AA"))
        .withQueryParam("includeAcknowledged", equalTo("true"))
        .withQueryParam("page", equalTo("0"))
        .withQueryParam("size", equalTo("20"))
        .withQueryParam("sortDirection", equalTo("descending")),
    )
  }

  @Test
  fun `should get events for review with default parameters`() = runTest {
    val date = LocalDate.of(2026, 8, 1)
    val expectedResult = eventReviewSearchResultsFactory(
      content = emptyList(),
      totalElements = 0,
      totalPages = 0,
    )

    activitiesApiMockServer.stubGetEventsForReview("MDI", date, expectedResult)

    val result = activitiesApiClient.getEventsDataForReview(
      prisonCode = "MDI",
      date = date,
    )

    assertThat(result).isEqualTo(expectedResult)

    activitiesApiMockServer.verify(
      getRequestedFor(urlPathEqualTo("/event-review/prison/MDI"))
        .withQueryParam("date", equalTo("2026-08-01"))
        .withQueryParam("page", equalTo("0"))
        .withQueryParam("size", equalTo("10"))
        .withQueryParam("sortDirection", equalTo("ascending")),
    )
  }

  @Test
  fun `should not include prisonerNumber query param when null`() = runTest {
    val date = LocalDate.of(2026, 8, 1)
    val expectedResult = eventReviewSearchResultsFactory(
      content = emptyList(),
      totalElements = 0,
      totalPages = 0,
    )

    activitiesApiMockServer.stubGetEventsForReview("MDI", date, expectedResult)

    activitiesApiClient.getEventsDataForReview(
      prisonCode = "MDI",
      date = date,
      prisonerNumber = null,
    )

    activitiesApiMockServer.verify(
      getRequestedFor(urlPathEqualTo("/event-review/prison/MDI"))
        .withoutQueryParam("prisonerNumber"),
    )
  }

  @Test
  fun `should not include includeAcknowledged query param when null`() = runTest {
    val date = LocalDate.of(2026, 8, 1)
    val expectedResult = eventReviewSearchResultsFactory(
      content = emptyList(),
      totalElements = 0,
      totalPages = 0,
    )

    activitiesApiMockServer.stubGetEventsForReview("MDI", date, expectedResult)

    activitiesApiClient.getEventsDataForReview(
      prisonCode = "MDI",
      date = date,
      includeAcknowledged = null,
    )

    activitiesApiMockServer.verify(
      getRequestedFor(urlPathEqualTo("/event-review/prison/MDI"))
        .withoutQueryParam("includeAcknowledged"),
    )
  }

  @Test
  fun `should throw exception on 404 response`() = runTest {
    activitiesApiMockServer.stubGetEventsForReviewNotFound("XXX", LocalDate.of(2026, 8, 1))

    assertThrows<WebClientResponseException.NotFound> {
      activitiesApiClient.getEventsDataForReview(
        prisonCode = "XXX",
        date = LocalDate.of(2026, 8, 1),
      )
    }
  }

  @Test
  fun `should throw exception on 500 response`() = runTest {
    activitiesApiMockServer.stubGetEventsForReviewServerError("MDI", LocalDate.of(2026, 8, 1))

    assertThrows<WebClientResponseException.InternalServerError> {
      activitiesApiClient.getEventsDataForReview(
        prisonCode = "MDI",
        date = LocalDate.of(2026, 8, 1),
      )
    }
  }

  @Nested
  @DisplayName("Retrying failed API calls")
  inner class RetryingFailedApiCalls {
    val prisonCode = "MDI"
    val date = LocalDate.of(2026, 8, 1)
    val expectedResult = eventReviewSearchResultsFactory()

    @Test
    fun `will succeed if number of fails is less than maximum allowed`(): Unit = runTest {
      activitiesApiMockServer.stubGetEventsForReviewWithConnectionReset(prisonCode, date, expectedResult)

      val result = activitiesApiClient.getEventsDataForReview(prisonCode, date)
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `will succeed if number of fails is the maximum allowed`(): Unit = runTest {
      activitiesApiMockServer.stubGetEventsForReviewWithConnectionReset(prisonCode, date, expectedResult, 2)

      val result = activitiesApiClient.getEventsDataForReview(prisonCode, date)
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `will fail if number of fails is more than maximum allowed`(): Unit = runTest {
      activitiesApiMockServer.stubGetEventsForReviewWithConnectionReset(prisonCode, date, expectedResult, 3)

      assertThrows<WebClientRequestException> {
        activitiesApiClient.getEventsDataForReview(prisonCode, date)
      }
    }
  }
}
