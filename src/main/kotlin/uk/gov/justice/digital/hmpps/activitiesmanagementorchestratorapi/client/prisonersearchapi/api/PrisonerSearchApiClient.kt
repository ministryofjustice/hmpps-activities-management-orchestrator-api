package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.api

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.util.context.Context
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.RetryApiService
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.PrisonerBasicDetails
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.PrisonerNumbers

inline fun <reified T : Any> typeReference() = object : ParameterizedTypeReference<T>() {}

@Service
class PrisonerSearchApiClient(
  private val prisonerSearchApiWebClient: WebClient,
  retryApiService: RetryApiService,
  @Value("\${prisoner-search.api.retry.max-retries:2}") private val maxRetryAttempts: Long = 2,
  @Value("\${prisoner-search.api.retry.backoff-millis:250}") private val backoffMillis: Long = 250,
) {
  private val backoffSpec = retryApiService.getBackoffSpec(maxRetryAttempts, backoffMillis)

  suspend fun findByPrisonerNumbers(prisonerNumbers: List<String>, batchSize: Int = 1000): List<PrisonerBasicDetails> {
    require(batchSize in 1..1000) {
      "Batch size must be between 1 and 1000"
    }
    if (prisonerNumbers.isEmpty()) return emptyList()
    return prisonerNumbers.chunked(batchSize).flatMap { chunk ->
      prisonerSearchApiWebClient.post()
        .uri("/prisoner-search/prisoner-numbers")
        .bodyValue(PrisonerNumbers(chunk))
        .retrieve()
        .bodyToMono(typeReference<List<PrisonerBasicDetails>>())
        .retryWhen(backoffSpec.withRetryContext(Context.of("api", "prisoner-search-api", "path", "/prisoner-search/prisoner-numbers")))
        .awaitSingle()
    }
  }

  suspend fun findByPrisonerNumbersMap(prisonerNumbers: List<String>): Map<String, PrisonerBasicDetails> = findByPrisonerNumbers(prisonerNumbers).associateBy { it.prisonerNumber }
}
