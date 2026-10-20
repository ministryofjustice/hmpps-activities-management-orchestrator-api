package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.api

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.util.context.Context
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.RetryApiService
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.PrisonDetails

@Service
class PrisonApiClient(
  private val prisonApiWebClient: WebClient,
  retryApiService: RetryApiService,
  @Value($$"${prison.api.retry.max-retries:2}") private val maxRetryAttempts: Long = 2,
  @Value($$"${prison.api.retry.backoff-millis:250}") private val backoffMillis: Long = 250,
) {
  private val backoffSpec = retryApiService.getBackoffSpec(maxRetryAttempts, backoffMillis)

  suspend fun getPrisonName(prisonCode: String): PrisonDetails {
    require(prisonCode.isNotBlank()) { "Prison code must be provided" }

    return prisonApiWebClient.get()
      .uri { uriBuilder ->
        uriBuilder
          .path("/api/agencies/{agencyId}")
          .build(prisonCode)
      }
      .retrieve()
      .bodyToMono<PrisonDetails>()
      .retryWhen(backoffSpec.withRetryContext(Context.of("api", "prison-api", "path", "/api/agencies/{agencyId}")))
      .awaitSingle()
  }
}
