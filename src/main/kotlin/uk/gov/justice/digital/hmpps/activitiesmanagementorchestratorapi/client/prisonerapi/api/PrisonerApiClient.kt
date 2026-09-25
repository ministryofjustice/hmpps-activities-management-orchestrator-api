package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonerapi.api

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.util.context.Context
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.RetryApiService
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonerapi.model.BedAssignmentSearchResults

inline fun <reified T : Any> typeReference() = object : ParameterizedTypeReference<T>() {}

@Service
class PrisonerApiClient(
  private val prisonerApiWebClient: WebClient,
  retryApiService: RetryApiService,
  @Value("\${prisoner.api.retry.max-retries:2}") private val maxRetryAttempts: Long = 2,
  @Value("\${prisoner.api.retry.backoff-millis:250}") private val backoffMillis: Long = 250,
) {
  private val backoffSpec = retryApiService.getBackoffSpec(maxRetryAttempts, backoffMillis)

  suspend fun getBedAssignmentsHistoryByBookingId(
    bookingId: String,
    page: Int = 0,
    size: Int = 1000,
  ): BedAssignmentSearchResults? {
    if (bookingId.isEmpty()) return null

    return prisonerApiWebClient.get()
      .uri { uriBuilder ->
        uriBuilder
          .path("/api/bookings/{bookingId}/cell-history")
          .queryParam("page", page)
          .queryParam("size", size)
          .build(bookingId)
      }
      .retrieve()
      .bodyToMono<BedAssignmentSearchResults>()
      .retryWhen(backoffSpec.withRetryContext(Context.of("api", "prisoner-api", "path", "/api/bookings/{bookingId}/cell-history")))
      .awaitSingle()
  }
}
