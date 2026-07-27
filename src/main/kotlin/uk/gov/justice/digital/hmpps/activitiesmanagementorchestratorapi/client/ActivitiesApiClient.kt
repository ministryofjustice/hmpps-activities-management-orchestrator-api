package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class ActivitiesApiClient(private val activitiesWebClient: WebClient) {
  suspend fun getEventReviewData()

}