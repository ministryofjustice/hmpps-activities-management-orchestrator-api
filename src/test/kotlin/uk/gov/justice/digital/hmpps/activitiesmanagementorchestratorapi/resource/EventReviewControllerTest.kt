package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.resource

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model.EventReviewDescription
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EventReviewDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EventReviewSearchResultsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service.EventReviewService
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(controllers = [EventReviewController::class])
@ContextConfiguration(classes = [EventReviewController::class])
class EventReviewControllerTest : ControllerTestBase() {

  @MockitoBean
  private lateinit var eventReviewService: EventReviewService

  private lateinit var webTestClient: WebTestClient

  private val date = LocalDate.of(2026, 8, 1)

  private val expectedDto = EventReviewSearchResultsDto(
    content = listOf(
      EventReviewDto(
        eventReviewId = 1L,
        serviceIdentifier = "SAA",
        eventType = "prison-offender-events.prisoner.released",
        eventTime = LocalDateTime.of(2026, 8, 1, 10, 0),
        prisonCode = "MDI",
        prisonerNumber = "A1234AA",
        bookingId = 123456,
        eventDescription = EventReviewDescription.TEMPORARY_RELEASE,
      ),
    ),
    pageNumber = 0,
    totalElements = 1,
    totalPages = 1,
  )

  @BeforeEach
  fun setUp() {
    webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build()
  }

  @Test
  fun `should return 200 with event review data`() {
    runTest {
      whenever(eventReviewService.getEventsDataForReview(eq("MDI"), eq(date), anyOrNull(), anyOrNull())).thenReturn(
        expectedDto,
      )
      webTestClient.get().uri("/event-review/prison/MDI?date=2026-08-01")
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.pageNumber").isEqualTo(0)
        .jsonPath("$.totalElements").isEqualTo(1)
        .jsonPath("$.totalPages").isEqualTo(1)
        .jsonPath("$.content.length()").isEqualTo(1)
        .jsonPath("$.content[0].eventReviewId").isEqualTo(1)
        .jsonPath("$.content[0].prisonCode").isEqualTo("MDI")
        .jsonPath("$.content[0].prisonerNumber").isEqualTo("A1234AA")

      verify(eventReviewService).getEventsDataForReview(eq("MDI"), eq(date), anyOrNull(), anyOrNull())
    }
  }

  @Test
  fun `should return 200 with optional parameters`() {
    runTest {
      whenever(eventReviewService.getEventsDataForReview(eq("MDI"), eq(date), eq("A1234AA"), eq(true))).thenReturn(
        expectedDto,
      )

      webTestClient.get()
        .uri("/event-review/prison/MDI?date=2026-08-01&prisonerNumber=A1234AA&includeAcknowledged=true")
        .exchange()
        .expectStatus().isOk

      verify(eventReviewService).getEventsDataForReview(eq("MDI"), eq(date), eq("A1234AA"), eq(true))
    }
  }

  @Test
  fun `should return 200 with empty results`() {
    val emptyDto = EventReviewSearchResultsDto(
      content = emptyList(),
      pageNumber = 0,
      totalElements = 0,
      totalPages = 0,
    )

    runTest {
      whenever(eventReviewService.getEventsDataForReview(eq("MDI"), eq(date), anyOrNull(), anyOrNull())).thenReturn(emptyDto)
    }

    webTestClient.get().uri("/event-review/prison/MDI?date=2026-08-01")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.content.length()").isEqualTo(0)
      .jsonPath("$.totalElements").isEqualTo(0)
  }

  @Test
  fun `should return 400 when date parameter is missing`() {
    webTestClient.get().uri("/event-review/prison/MDI")
      .exchange()
      .expectStatus().isBadRequest
    verifyNoInteractions(eventReviewService)
  }

  @Test
  fun `should return 400 when date parameter is invalid`() {
    webTestClient.get().uri("/event-review/prison/MDI?date=invalid-date")
      .exchange()
      .expectStatus().isBadRequest
    verifyNoInteractions(eventReviewService)
  }

  @Test
  @WithAnonymousUser
  fun `should return 401 when not authenticated`() {
    webTestClient.get().uri("/event-review/prison/MDI?date=2026-08-01")
      .exchange()
      .expectStatus().isUnauthorized

    verifyNoInteractions(eventReviewService)
  }

  @Test
  @WithMockAuthUser(roles = ["WRONG_ROLE"])
  fun `should return 403 when user has incorrect role`() {
    webTestClient.get().uri("/event-review/prison/MDI?date=2026-08-01")
      .exchange()
      .expectStatus().isForbidden

    verifyNoInteractions(eventReviewService)
  }
}
