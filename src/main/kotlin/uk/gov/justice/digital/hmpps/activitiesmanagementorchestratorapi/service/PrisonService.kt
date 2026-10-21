package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.api.PrisonApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.PrisonDetailsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.mapping.toDto

@Service
class PrisonService(
  private val prisonApiClient: PrisonApiClient,
) {
  suspend fun getPrisonName(prisonCode: String): PrisonDetailsDto {
    if (prisonCode.isBlank()) {
      throw ValidationException("Prison code must be provided")
    }

    return prisonApiClient.getPrisonName(prisonCode).toDto()
  }
}
