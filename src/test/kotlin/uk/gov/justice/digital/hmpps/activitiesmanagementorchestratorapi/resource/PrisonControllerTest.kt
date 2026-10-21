package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.resource

import jakarta.validation.ValidationException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.config.ActivitiesManagementOrchestratorApiExceptionHandler
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.PrisonDetailsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service.PrisonService
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

@WebMvcTest(controllers = [PrisonController::class])
@Import(ActivitiesManagementOrchestratorApiExceptionHandler::class)
@ContextConfiguration(classes = [PrisonController::class, ActivitiesManagementOrchestratorApiExceptionHandler::class])
@WithMockAuthUser(roles = ["ROLE_VIEW_PRISONER_DATA"])
class PrisonControllerTest : ControllerTestBase() {

  @MockitoBean
  private lateinit var prisonService: PrisonService

  private lateinit var webTestClient: WebTestClient

  @BeforeEach
  fun setUp() {
    webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build()
  }

  @Test
  fun `should return 200 with prison name`() = runTest {
    whenever(prisonService.getPrisonName("LEI")).thenReturn(PrisonDetailsDto(prisonName = "Leeds (HMP)"))

    webTestClient.method(HttpMethod.GET)
      .uri { it.path("/prison/{prisonCode}/name").build("LEI") }
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.prisonName").isEqualTo("Leeds (HMP)")

    verify(prisonService).getPrisonName("LEI")
  }

  @Test
  fun `should return 400 when prison code is blank`() = runTest {
    whenever(prisonService.getPrisonName(" "))
      .thenThrow(ValidationException("Prison code must be provided"))

    webTestClient.method(HttpMethod.GET)
      .uri { it.path("/prison/{prisonCode}/name").build(" ") }
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should return 404 when prison code is not found`() = runTest {
    whenever(prisonService.getPrisonName("XXX"))
      .thenThrow(WebClientResponseException.create(404, "Not Found", HttpHeaders.EMPTY, ByteArray(0), null))

    webTestClient.method(HttpMethod.GET)
      .uri { it.path("/prison/{prisonCode}/name").build("XXX") }
      .exchange()
      .expectStatus().isNotFound

    verify(prisonService).getPrisonName("XXX")
  }

  @Test
  @WithAnonymousUser
  fun `should return 401 when not authenticated fetching prison name`() {
    webTestClient.method(HttpMethod.GET)
      .uri { it.path("/prison/{prisonCode}/name").build("LEI") }
      .exchange()
      .expectStatus().isUnauthorized

    verifyNoInteractions(prisonService)
  }

  @Test
  @WithMockAuthUser(roles = ["WRONG_ROLE"])
  fun `should return 403 when user has incorrect role fetching prison name`() {
    webTestClient.method(HttpMethod.GET)
      .uri { it.path("/prison/{prisonCode}/name").build("LEI") }
      .exchange()
      .expectStatus().isForbidden

    verifyNoInteractions(prisonService)
  }
}
