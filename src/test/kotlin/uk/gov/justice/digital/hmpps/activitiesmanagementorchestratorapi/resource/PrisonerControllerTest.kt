package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.resource

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.api.PrisonerSearchApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.PrisonerBasicDetails
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.PrisonerNumbers
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.config.ActivitiesManagementOrchestratorApiExceptionHandler
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

class PrisonerControllerTest {

  private val prisonerSearchApiClient: PrisonerSearchApiClient = mock()
  private val controller = PrisonerController(prisonerSearchApiClient)

  @Test
  fun `should return prisoner ids for matching names`() = runTest {
    val expected = listOf("A1234AA", "A1234AB")

    whenever(prisonerSearchApiClient.lookupPrisonerNumberByName("John", "Smith")).thenReturn(expected)

    val result = controller.getPrisonerNumbers("John", "Smith")

    assertThat(result).isEqualTo(expected)
    verify(prisonerSearchApiClient).lookupPrisonerNumberByName("John", "Smith")
  }

  @Test
  fun `should return empty list when no prisoner matches`() = runTest {
    whenever(prisonerSearchApiClient.lookupPrisonerNumberByName("John", "Smith")).thenReturn(emptyList())

    val result = controller.getPrisonerNumbers("John", "Smith")

    assertThat(result).isEmpty()
    verify(prisonerSearchApiClient).lookupPrisonerNumberByName("John", "Smith")
  }

  @Test
  fun `should allow when only forename is supplied`() = runTest {
    whenever(prisonerSearchApiClient.lookupPrisonerNumberByName("John", "")).thenReturn(listOf("A1234AA"))

    val result = controller.getPrisonerNumbers("John", null)

    assertThat(result).containsExactly("A1234AA")
    verify(prisonerSearchApiClient).lookupPrisonerNumberByName("John", "")
  }

  @Test
  fun `should allow when only surname is supplied`() = runTest {
    whenever(prisonerSearchApiClient.lookupPrisonerNumberByName("", "Smith")).thenReturn(listOf("A1234AA"))

    val result = controller.getPrisonerNumbers(null, "Smith")

    assertThat(result).containsExactly("A1234AA")
    verify(prisonerSearchApiClient).lookupPrisonerNumberByName("", "Smith")
  }

  @Test
  fun `should reject when both name values are empty`() = runTest {
    val exception = assertThrows<jakarta.validation.ValidationException> {
      controller.getPrisonerNumbers("", "")
    }

    assertThat(exception).hasMessage("Either prisonerForename or prisonerSurname must be provided")
  }

  @Test
  fun `should propagate exceptions from the upstream prisoner search client`() = runTest {
    whenever(prisonerSearchApiClient.lookupPrisonerNumberByName("John", "Smith")).thenThrow(RuntimeException("Upstream failure"))

    val exception = assertThrows<RuntimeException> {
      controller.getPrisonerNumbers("John", "Smith")
    }

    assertThat(exception).hasMessage("Upstream failure")
  }
}

@WebMvcTest(controllers = [PrisonerController::class])
@Import(ActivitiesManagementOrchestratorApiExceptionHandler::class)
@ContextConfiguration(classes = [PrisonerController::class, ActivitiesManagementOrchestratorApiExceptionHandler::class])
@WithMockAuthUser(roles = ["ROLE_PRISONER_SEARCH"])
class PrisonerControllerWebTest : ControllerTestBase() {

  @MockitoBean
  private lateinit var prisonerSearchApiClient: PrisonerSearchApiClient

  private lateinit var webTestClient: WebTestClient

  @BeforeEach
  fun setUp() {
    webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build()
  }

  @Nested
  inner class GetPrisonerNumberByName {
    @Test
    fun `should return 200 with prisoner ids in json array`() = runTest {
      whenever(prisonerSearchApiClient.lookupPrisonerNumberByName("John", "Smith")).thenReturn(listOf("A1234AA", "A1234AB"))

      webTestClient.get()
        .uri("/prisoner/prisoner-number-by-name?prisonerForename=John&prisonerSurname=Smith")
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("$[0]").isEqualTo("A1234AA")
        .jsonPath("$[1]").isEqualTo("A1234AB")

      verify(prisonerSearchApiClient).lookupPrisonerNumberByName("John", "Smith")
    }

    @Test
    fun `should return 200 with empty prisoner id list`() = runTest {
      whenever(prisonerSearchApiClient.lookupPrisonerNumberByName("John", "Smith")).thenReturn(emptyList())

      webTestClient.get()
        .uri("/prisoner/prisoner-number-by-name?prisonerForename=John&prisonerSurname=Smith")
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(0)

      verify(prisonerSearchApiClient).lookupPrisonerNumberByName("John", "Smith")
    }

    @Test
    fun `should return 200 when only forename is supplied`() = runTest {
      whenever(prisonerSearchApiClient.lookupPrisonerNumberByName("John", "")).thenReturn(listOf("A1234AA"))

      webTestClient.get()
        .uri("/prisoner/prisoner-number-by-name?prisonerForename=John")
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[0]").isEqualTo("A1234AA")

      verify(prisonerSearchApiClient).lookupPrisonerNumberByName("John", "")
    }

    @Test
    fun `should return 200 when only surname is supplied`() = runTest {
      whenever(prisonerSearchApiClient.lookupPrisonerNumberByName("", "Smith")).thenReturn(listOf("A1234AA"))

      webTestClient.get()
        .uri("/prisoner/prisoner-number-by-name?prisonerSurname=Smith")
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[0]").isEqualTo("A1234AA")

      verify(prisonerSearchApiClient).lookupPrisonerNumberByName("", "Smith")
    }

    @Test
    fun `should return 400 when both forename and surname are empty`() {
      webTestClient.get()
        .uri("/prisoner/prisoner-number-by-name?prisonerForename=&prisonerSurname=")
        .exchange()
        .expectStatus().isBadRequest

      verifyNoInteractions(prisonerSearchApiClient)
    }

    @Test
    fun `should return 400 when required surname and forename query parameter is missing`() {
      webTestClient.get()
        .uri("/prisoner/prisoner-number-by-name")
        .exchange()
        .expectStatus().isBadRequest

      verifyNoInteractions(prisonerSearchApiClient)
    }

    @Test
    @WithAnonymousUser
    fun `should return 401 when not authenticated`() {
      webTestClient.get()
        .uri("/prisoner/prisoner-number-by-name?prisonerForename=John&prisonerSurname=Smith")
        .exchange()
        .expectStatus().isUnauthorized

      verifyNoInteractions(prisonerSearchApiClient)
    }

    @Test
    @WithMockAuthUser(roles = ["WRONG_ROLE"])
    fun `should return 403 when user has incorrect role`() {
      webTestClient.get()
        .uri("/prisoner/prisoner-number-by-name?prisonerForename=John&prisonerSurname=Smith")
        .exchange()
        .expectStatus().isForbidden

      verifyNoInteractions(prisonerSearchApiClient)
    }
  }

  @Nested
  inner class PostPrisonerDetailsByNumbers {
    @Test
    fun `should return 200 with prisoner basic details by numbers`() = runTest {
      val request = PrisonerNumbers(listOf("A1234AA"))
      whenever(prisonerSearchApiClient.findByPrisonerNumbers(request.prisonerNumbers)).thenReturn(
        listOf(
          PrisonerBasicDetails(
            prisonerNumber = "A1234AA",
            firstName = "JOE",
            lastName = "BLOGGS",
            cellLocation = "2-1-007",
          ),
        ),
      )

      webTestClient.post()
        .uri("/prisoner/prisoner-details-by-numbers")
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].prisonerNumber").isEqualTo("A1234AA")
        .jsonPath("$[0].firstName").isEqualTo("JOE")
        .jsonPath("$[0].lastName").isEqualTo("BLOGGS")
        .jsonPath("$[0].cellLocation").isEqualTo("2-1-007")

      verify(prisonerSearchApiClient).findByPrisonerNumbers(request.prisonerNumbers)
    }

    @Test
    fun `should return 400 when prisoner numbers list is empty`() {
      webTestClient.post()
        .uri("/prisoner/prisoner-details-by-numbers")
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .bodyValue(PrisonerNumbers(emptyList()))
        .exchange()
        .expectStatus().isBadRequest

      verifyNoInteractions(prisonerSearchApiClient)
    }

    @Test
    fun `should return 400 when prisoner numbers body is missing`() {
      webTestClient.post()
        .uri("/prisoner/prisoner-details-by-numbers")
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest

      verifyNoInteractions(prisonerSearchApiClient)
    }

    @Test
    @WithAnonymousUser
    fun `should return 401 for prisoner details by numbers when not authenticated`() {
      webTestClient.post()
        .uri("/prisoner/prisoner-details-by-numbers")
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .bodyValue(PrisonerNumbers(listOf("A1234AA")))
        .exchange()
        .expectStatus().isUnauthorized

      verifyNoInteractions(prisonerSearchApiClient)
    }

    @Test
    @WithMockAuthUser(roles = ["WRONG_ROLE"])
    fun `should return 403 for prisoner details by numbers when user has incorrect role`() {
      webTestClient.post()
        .uri("/prisoner/prisoner-details-by-numbers")
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .bodyValue(PrisonerNumbers(listOf("A1234AA")))
        .exchange()
        .expectStatus().isForbidden

      verifyNoInteractions(prisonerSearchApiClient)
    }
  }
}
