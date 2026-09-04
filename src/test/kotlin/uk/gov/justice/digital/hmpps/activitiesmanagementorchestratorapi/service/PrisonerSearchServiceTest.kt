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
  fun `should return basic prisoner details as map`() = runTest {
    val prisonerNumber = "G4793VF"
    val prisoner = PrisonerSearchPrisonerFixture.instance(
      prisonerNumber = prisonerNumber,
      firstName = "JOE",
      lastName = "BLOGGS",
      cellLocation = "2-1-007",
    )

    whenever(prisonerSearchApiClient.findByPrisonerNumbersMap(listOf(prisonerNumber))).thenReturn(mapOf(prisonerNumber to prisoner))

    val result = prisonerSearchService.getBasicPrisonerDetails(listOf(prisonerNumber))
    val prisonerSummary = result[prisonerNumber]

    assertThat(result).hasSize(1)
    assertThat(prisonerSummary).isNotNull
    assertThat(prisonerSummary!!.firstName).isEqualTo("JOE")
    assertThat(prisonerSummary.lastName).isEqualTo("BLOGGS")
    assertThat(prisonerSummary.cellLocation).isEqualTo("2-1-007")
  }

  @Test
  fun `should return the basic details of multiple prisoners as a map`() = runTest {
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

    whenever(prisonerSearchApiClient.findByPrisonerNumbersMap(prisonerNumbers)).thenReturn(
      mapOf(
        "G4793VF" to firstPrisoner,
        "A1234BC" to secondPrisoner,
      ),
    )

    val result = prisonerSearchService.getBasicPrisonerDetails(prisonerNumbers)

    assertThat(result).hasSize(2)
    assertThat(result["G4793VF"]!!.firstName).isEqualTo("JOE")
    assertThat(result["G4793VF"]!!.lastName).isEqualTo("BLOGGS")
    assertThat(result["G4793VF"]!!.cellLocation).isEqualTo("2-1-007")
    assertThat(result["A1234BC"]!!.firstName).isEqualTo("JANE")
    assertThat(result["A1234BC"]!!.lastName).isEqualTo("SMITH")
    assertThat(result["A1234BC"]!!.cellLocation).isEqualTo("3-2-101")
  }
}
