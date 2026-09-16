package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.api.PrisonerSearchApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.PrisonerBasicDetails

@Service
class PrisonerSearchService(
  private val prisonerSearchApiClient: PrisonerSearchApiClient,
) {
  suspend fun lookupPrisonerNumberByName(prisonerFirstname: String, prisonerLastname: String): List<String> {
    if (prisonerFirstname.isBlank() && prisonerLastname.isBlank()) {
      throw ValidationException("Either prisonerfirstname or prisonerlastname must be provided")
    }

    return prisonerSearchApiClient.lookupPrisonerNumberByName(prisonerFirstname, prisonerLastname)
  }

  suspend fun getBasicPrisonerDetails(prisonerNumbers: List<String>): List<PrisonerBasicDetails> {
    if (prisonerNumbers.isEmpty()) {
      throw ValidationException("Prisoner numbers must be provided")
    }

    return prisonerSearchApiClient.findByPrisonerNumbers(prisonerNumbers)
  }
}
