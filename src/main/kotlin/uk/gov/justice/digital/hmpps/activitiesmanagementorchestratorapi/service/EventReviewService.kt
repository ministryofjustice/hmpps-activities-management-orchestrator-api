package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.api.ActivitiesApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EventReviewSearchResultsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.mapping.toDto
import java.time.LocalDate

@Service
class EventReviewService(
  private val activitiesApiClient: ActivitiesApiClient,
) {
  suspend fun getEventsDataForReview(
    prisonCode: String,
    date: LocalDate,
    prisonerNumber: String? = null,
    includeAcknowledged: Boolean? = null,
  ): EventReviewSearchResultsDto = activitiesApiClient.getEventsDataForReview(prisonCode, date, prisonerNumber = prisonerNumber, includeAcknowledged = includeAcknowledged).toDto()
}
