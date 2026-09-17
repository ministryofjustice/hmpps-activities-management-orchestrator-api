package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.api.PrisonerSearchApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.PrisonerBasicDetails

@Service
class PrisonerSearchService(
  private val prisonerSearchApiClient: PrisonerSearchApiClient,
) {
  suspend fun lookupPrisonerNumberByName(firstname: String, lastname: String): List<String> {
    if (firstname.isBlank() && lastname.isBlank()) {
      throw ValidationException("Either firstname or lastname must be provided")
    }

    return prisonerSearchApiClient.lookupPrisonerNumberByName(firstname, lastname)
  }

  suspend fun getBasicPrisonerDetails(prisonerNumbers: List<String>): List<PrisonerBasicDetails> {
    if (prisonerNumbers.isEmpty()) {
      throw ValidationException("Prisoner numbers must be provided")
    }

    return prisonerSearchApiClient.findByPrisonerNumbers(prisonerNumbers)
  }
}
