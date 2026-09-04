package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.api.PrisonerSearchApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.PrisonerBasicDetailsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.mapping.toDto

@Service
class PrisonerSearchService(
  private val prisonerSearchApiClient: PrisonerSearchApiClient,
) {
  suspend fun getBasicPrisonerDetails(prisonerNumbers: List<String>): Map<String, PrisonerBasicDetailsDto> = prisonerSearchApiClient
    .findByPrisonerNumbersMap(prisonerNumbers)
    .mapValues { (_, prisoner) -> prisoner.toDto() }
}
