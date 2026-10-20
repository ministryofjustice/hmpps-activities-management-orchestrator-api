package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.PrisonDetails
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.PrisonDetailsDto

class PrisonIntegrationTest : IntegrationTestBase() {

  @Test
  fun `should return 200 with prison name`() {
    val prisonCode = "LEI"
    prisonApi().stubGetPrisonName(prisonCode, PrisonDetails(description = "Leeds (HMP)"))

    val result = getPrisonName(prisonCode).success<PrisonDetailsDto>()

    assertThat(result.prisonName).isEqualTo("Leeds (HMP)")
  }

  @Test
  fun `should return 400 when prison code is blank`() {
    getPrisonName(" ").fail(HttpStatus.BAD_REQUEST)
  }

  @Test
  fun `should return 401 when not authenticated fetching prison name`() {
    getPrisonName("LEI", includeBearerAuth = false).fail(HttpStatus.UNAUTHORIZED)
  }

  @Test
  fun `should return 403 when user has incorrect role fetching prison name`() {
    getPrisonName("LEI", roles = listOf("INVALID_ROLE")).fail(HttpStatus.FORBIDDEN)
  }

  @Test
  fun `should return 500 when upstream API returns server error fetching prison name`() {
    val prisonCode = "LEI"
    prisonApi().stubGetPrisonNameServerError(prisonCode)

    getPrisonName(prisonCode).fail(HttpStatus.INTERNAL_SERVER_ERROR)
  }

  @Test
  fun `should return 404 when prison code is not found`() {
    val prisonCode = "XXX"
    prisonApi().stubGetPrisonNameNotFound(prisonCode)

    getPrisonName(prisonCode).fail(HttpStatus.NOT_FOUND)
  }

  private fun getPrisonName(
    prisonCode: String,
    roles: List<String> = listOf("VIEW_PRISONER_DATA"),
    includeBearerAuth: Boolean = true,
  ) = webTestClient.method(HttpMethod.GET)
    .uri { it.path("/prison/{prisonCode}/name").build(prisonCode) }
    .headers(if (includeBearerAuth) setAuthorisation(roles = roles) else noAuthorisation())
    .exchange()
}
