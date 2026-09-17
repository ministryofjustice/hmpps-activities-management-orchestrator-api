package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import jakarta.validation.ValidationException
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.api.PrisonerSearchApiClient

class PrisonerSearchServiceTest {
  private val prisonerSearchApiClient: PrisonerSearchApiClient = mock()
  private val prisonerSearchService = PrisonerSearchService(prisonerSearchApiClient)

  private val firstPrisoner = PrisonerSearchPrisonerFixture.instance(
    prisonerNumber = "G4793VF",
    firstName = "JOE",
    lastName = "BLOGGS",
    cellLocation = "2-1-007",
  )
  private val secondPrisoner = PrisonerSearchPrisonerFixture.instance(
    prisonerNumber = "A1234BC",
    firstName = "JANE",
    lastName = "SMITH",
    cellLocation = "3-2-101",
  )

  @Test
  fun `should return basic prisoner details for a single prisoner`() = runTest {
    whenever(prisonerSearchApiClient.findByPrisonerNumbers(listOf(firstPrisoner.prisonerNumber))).thenReturn(listOf(firstPrisoner))

    val result = prisonerSearchService.getBasicPrisonerDetails(listOf(firstPrisoner.prisonerNumber))

    assertThat(result).hasSize(1)
    assertThat(result[0]).isNotNull
    assertThat(result[0].firstName).isEqualTo("JOE")
    assertThat(result[0].lastName).isEqualTo("BLOGGS")
    assertThat(result[0].cellLocation).isEqualTo("2-1-007")
  }

  @Test
  fun `should return basic prisoner details map for multiple prisoners`() = runTest {
    val prisonerNumbers = listOf(firstPrisoner.prisonerNumber, secondPrisoner.prisonerNumber)

    whenever(prisonerSearchApiClient.findByPrisonerNumbersMap(prisonerNumbers)).thenReturn(
      mapOf(
        "G4793VF" to firstPrisoner,
        "A1234BC" to secondPrisoner,
      ),
    )

    val result = prisonerSearchService.getBasicPrisonerDetailsMap(prisonerNumbers)

    assertThat(result).containsExactlyInAnyOrderEntriesOf(
      mapOf(
        "G4793VF" to firstPrisoner,
        "A1234BC" to secondPrisoner,
      ),
    )
  }

  @Test
  fun `should throw validation exception when prisoner numbers are empty for map lookup`() = runTest {
    val exception = assertThrows<ValidationException> {
      prisonerSearchService.getBasicPrisonerDetailsMap(emptyList())
    }

    assertThat(exception).hasMessage("Prisoner numbers must be provided")
  }

  @Test
  fun `should return basic prisoner details for multiple prisoners`() = runTest {
    val prisonerNumbers = listOf(firstPrisoner.prisonerNumber, secondPrisoner.prisonerNumber)

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

  @Test
  fun `should return prisoner numbers when first name and last name are provided`() = runTest {
    whenever(prisonerSearchApiClient.lookupPrisonerNumberByName("JOE", "BLOGGS")).thenReturn(
      listOf("G4793VF", "G4793VG"),
    )

    val result = prisonerSearchService.lookupPrisonerNumberByName("JOE", "BLOGGS")

    assertThat(result).containsExactly("G4793VF", "G4793VG")
  }

  @Test
  fun `should throw validation exception when first name and last name are blank`() = runTest {
    val exception = assertThrows<ValidationException> {
      prisonerSearchService.lookupPrisonerNumberByName("", "")
    }

    assertThat(exception).hasMessage("Either firstname or lastname must be provided")
  }
}
