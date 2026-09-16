package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.api.PrisonerSearchApiClient

class PrisonerSearchServiceTest {
  private val prisonerSearchApiClient: PrisonerSearchApiClient = mock()
  private val prisonerSearchService = PrisonerSearchService(prisonerSearchApiClient)

  @Test
  fun `should return basic prisoner details for a single prisoner`() = runTest {
    val prisonerNumber = "G4793VF"
    val prisoner = PrisonerSearchPrisonerFixture.instance(
      prisonerNumber = prisonerNumber,
      firstName = "JOE",
      lastName = "BLOGGS",
      cellLocation = "2-1-007",
    )

    whenever(prisonerSearchApiClient.findByPrisonerNumbers(listOf(prisonerNumber))).thenReturn(listOf(prisoner))

    val result = prisonerSearchService.getBasicPrisonerDetails(listOf(prisonerNumber))

    assertThat(result).hasSize(1)
    assertThat(result[0]).isNotNull
    assertThat(result[0].firstName).isEqualTo("JOE")
    assertThat(result[0].lastName).isEqualTo("BLOGGS")
    assertThat(result[0].cellLocation).isEqualTo("2-1-007")
  }

  @Test
  fun `should return basic prisoner details for multiple prisoners`() = runTest {
    val prisonerNumbers = listOf("G4793VF", "A1234BC")
    val firstPrisoner = PrisonerSearchPrisonerFixture.instance(
      prisonerNumber = "G4793VF",
      firstName = "JOE",
      lastName = "BLOGGS",
      cellLocation = "2-1-007",
    )
    val secondPrisoner = PrisonerSearchPrisonerFixture.instance(
      prisonerNumber = "A1234BC",
      firstName = "JANE",
      lastName = "SMITH",
      cellLocation = "3-2-101",
    )

    whenever(prisonerSearchApiClient.findByPrisonerNumbers(prisonerNumbers)).thenReturn(
      listOf(firstPrisoner, secondPrisoner),
    )

    val result = prisonerSearchService.getBasicPrisonerDetails(prisonerNumbers)

    assertThat(result).hasSize(2)
    assertThat(result[0].firstName).isEqualTo("JOE")
    assertThat(result[0].lastName).isEqualTo("BLOGGS")
    assertThat(result[0].cellLocation).isEqualTo("2-1-007")
    assertThat(result[1].firstName).isEqualTo("JANE")
    assertThat(result[1].lastName).isEqualTo("SMITH")
    assertThat(result[1].cellLocation).isEqualTo("3-2-101")
  }
}
