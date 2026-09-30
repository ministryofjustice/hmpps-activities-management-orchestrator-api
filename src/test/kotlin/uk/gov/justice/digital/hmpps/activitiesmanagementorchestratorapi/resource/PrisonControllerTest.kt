package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.resource

import jakarta.validation.ValidationException
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.BedAssignment
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.BedAssignmentSearchResults
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.config.ActivitiesManagementOrchestratorApiExceptionHandler
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.CellLocationResultsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service.PrisonService
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

class PrisonControllerTest {

  private val prisonService: PrisonService = mock()
  private val controller = PrisonController(prisonService)

  @Test
  fun `should return cell location history for a booking id`() = runTest {
    val expected = BedAssignmentSearchResults(
      content = listOf(BedAssignment(description = "MDI-1-2"), BedAssignment(description = "MDI-1-1")),
      pageNumber = 0,
      totalElements = 2,
      totalPages = 1,
    )

    whenever(prisonService.getCurrentAndPreviousBedAssignment("12345")).thenReturn(expected)

    val result = controller.getCellLocationHistory("12345")

    assertThat(result).isEqualTo(CellLocationResultsDto(cellLocations = listOf("MDI-1-2", "MDI-1-1")))
    verify(prisonService).getCurrentAndPreviousBedAssignment("12345")
  }

  @Test
  fun `should return null when no cell location history is found`() = runTest {
    whenever(prisonService.getCurrentAndPreviousBedAssignment("12345")).thenReturn(null)

    val result = controller.getCellLocationHistory("12345")

    assertThat(result).isNull()
    verify(prisonService).getCurrentAndPreviousBedAssignment("12345")
  }

  @Test
  fun `should propagate validation exception from the service`() = runTest {
    whenever(prisonService.getCurrentAndPreviousBedAssignment(""))
      .thenThrow(ValidationException("Booking Id must be provided"))

    val exception = assertThrows<ValidationException> {
      controller.getCellLocationHistory("")
    }

    assertThat(exception).hasMessage("Booking Id must be provided")
  }

  @Test
  fun `should propagate exceptions from the upstream prison service`() = runTest {
    whenever(prisonService.getCurrentAndPreviousBedAssignment("12345")).thenThrow(RuntimeException("Upstream failure"))

    val exception = assertThrows<RuntimeException> {
      controller.getCellLocationHistory("12345")
    }

    assertThat(exception).hasMessage("Upstream failure")
  }
}

@WebMvcTest(controllers = [PrisonController::class])
@Import(ActivitiesManagementOrchestratorApiExceptionHandler::class)
@ContextConfiguration(classes = [PrisonController::class, ActivitiesManagementOrchestratorApiExceptionHandler::class])
@WithMockAuthUser(roles = ["ROLE_PRISONER_SEARCH"])
class PrisonControllerWebTest : ControllerTestBase() {

  @MockitoBean
  private lateinit var prisonService: PrisonService

  private lateinit var webTestClient: WebTestClient

  @BeforeEach
  fun setUp() {
    webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build()
  }

  @Test
  fun `should return 200 with cell location history`() = runTest {
    val expected = BedAssignmentSearchResults(
      content = listOf(BedAssignment(description = "MDI-1-2"), BedAssignment(description = "MDI-1-1")),
      pageNumber = 0,
      totalElements = 2,
      totalPages = 1,
    )

    whenever(prisonService.getCurrentAndPreviousBedAssignment("12345")).thenReturn(expected)

    webTestClient.method(HttpMethod.GET)
      .uri("/prison/cell-location-history")
      .contentType(MediaType.TEXT_PLAIN)
      .bodyValue("12345")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.cellLocations.length()").isEqualTo(2)
      .jsonPath("$.cellLocations[0]").isEqualTo("MDI-1-2")
      .jsonPath("$.cellLocations[1]").isEqualTo("MDI-1-1")

    verify(prisonService).getCurrentAndPreviousBedAssignment("12345")
  }

  @Test
  fun `should return 200 with empty cell location history`() = runTest {
    whenever(prisonService.getCurrentAndPreviousBedAssignment("12345")).thenReturn(null)

    webTestClient.method(HttpMethod.GET)
      .uri("/prison/cell-location-history")
      .contentType(MediaType.TEXT_PLAIN)
      .bodyValue("12345")
      .exchange()
      .expectStatus().isOk
      .expectBody().isEmpty

    verify(prisonService).getCurrentAndPreviousBedAssignment("12345")
  }

  @Test
  fun `should return 400 when booking id is blank`() = runTest {
    webTestClient.method(HttpMethod.GET)
      .uri("/prison/cell-location-history")
      .contentType(MediaType.TEXT_PLAIN)
      .bodyValue("")
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  @WithAnonymousUser
  fun `should return 401 when not authenticated`() {
    webTestClient.method(HttpMethod.GET)
      .uri("/prison/cell-location-history")
      .contentType(MediaType.TEXT_PLAIN)
      .bodyValue("12345")
      .exchange()
      .expectStatus().isUnauthorized

    verifyNoInteractions(prisonService)
  }

  @Test
  @WithMockAuthUser(roles = ["WRONG_ROLE"])
  fun `should return 403 when user has incorrect role`() {
    webTestClient.method(HttpMethod.GET)
      .uri("/prison/cell-location-history")
      .contentType(MediaType.TEXT_PLAIN)
      .bodyValue("12345")
      .exchange()
      .expectStatus().isForbidden

    verifyNoInteractions(prisonService)
  }
}
