package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import jakarta.validation.ValidationException
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.api.PrisonApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.PrisonDetails
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.PrisonDetailsDto

class PrisonServiceTest {
  private val prisonApiClient: PrisonApiClient = mock()
  private val prisonService = PrisonService(prisonApiClient)

  @Test
  fun `should return the prison name for a prison code`() = runTest {
    val prisonCode = "LEI"

    whenever(prisonApiClient.getPrisonName(prisonCode)).thenReturn(PrisonDetails(description = "Leeds (HMP)"))

    val result = prisonService.getPrisonName(prisonCode)

    assertThat(result).isEqualTo(PrisonDetailsDto(prisonName = "Leeds (HMP)"))
    verify(prisonApiClient).getPrisonName(prisonCode)
  }

  @Test
  fun `should throw validation exception when prison code is blank`() = runTest {
    val exception = assertThrows<ValidationException> {
      prisonService.getPrisonName("")
    }

    assertThat(exception).hasMessage("Prison code must be provided")
    verifyNoInteractions(prisonApiClient)
  }

  @Test
  fun `should propagate exceptions from the upstream prison api client when fetching prison name`() = runTest {
    val prisonCode = "LEI"
    whenever(prisonApiClient.getPrisonName(prisonCode)).thenThrow(RuntimeException("Upstream failure"))

    val exception = assertThrows<RuntimeException> {
      prisonService.getPrisonName(prisonCode)
    }

    assertThat(exception).hasMessage("Upstream failure")
  }
}
