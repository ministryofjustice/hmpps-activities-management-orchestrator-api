package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.api

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.util.context.Context
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.RetryApiService
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model.EventReviewSearchResults
import java.time.LocalDate
import java.util.Optional

@Component
class ActivitiesApiClient(
  private val activitiesWebClient: WebClient,
  retryApiService: RetryApiService,
  @Value("\${activities.api.retry.max-retries:2}") private val maxRetryAttempts: Long = 2,
  @Value("\${activities.api.retry.backoff-millis:250}") private val backoffMillis: Long = 250,
) {
  private val backoffSpec = retryApiService.getBackoffSpec(maxRetryAttempts, backoffMillis)

  suspend fun getEventsDataForReview(
    prisonCode: String,
    date: LocalDate,
    prisonerNumber: String? = null,
    includeAcknowledged: Boolean? = false,
    page: Int = 0,
    size: Int = 10,
    sortDirection: String = "ascending",
  ): EventReviewSearchResults = activitiesWebClient
    .get()
    .uri { uriBuilder ->
      uriBuilder
        .path("/event-review/prison/{prisonCode}")
        .queryParam("date", date)
        .queryParamIfPresent("prisonerNumber", Optional.ofNullable(prisonerNumber))
        .queryParamIfPresent("includeAcknowledged", Optional.ofNullable(includeAcknowledged))
        .queryParam("page", page)
        .queryParam("size", size)
        .queryParam("sortDirection", sortDirection)
        .build(prisonCode)
    }
    .retrieve()
    .bodyToMono<EventReviewSearchResults>()
    .retryWhen(backoffSpec.withRetryContext(Context.of("api", "activities-api", "path", "/event-review/prison/{prisonCode}")))
    .awaitSingle()
}
