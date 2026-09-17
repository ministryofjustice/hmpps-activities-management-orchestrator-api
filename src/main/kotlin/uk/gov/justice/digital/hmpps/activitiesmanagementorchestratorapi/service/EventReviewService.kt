package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.api.ActivitiesApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.api.PrisonerSearchApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EventReviewSearchResultsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.mapping.toDto
import java.time.LocalDate

@Service
class EventReviewService(
  private val activitiesApiClient: ActivitiesApiClient,
  private val prisonerSearchApiClient: PrisonerSearchApiClient,
) {
  suspend fun getEventsDataForReview(
    prisonCode: String,
    date: LocalDate,
    prisonerNumber: String? = null,
    includeAcknowledged: Boolean? = null,
  ): EventReviewSearchResultsDto {
    val events = activitiesApiClient
      .getEventsDataForReview(prisonCode, date, prisonerNumber = prisonerNumber, includeAcknowledged = includeAcknowledged)
      .toDto()

    val prisonerDetails = prisonerSearchApiClient.findByPrisonerNumbersMap(
      events.content.mapNotNull { it.prisonerNumber }.distinct(),
    )

    print(events)
    print(prisonerDetails)

    return EventReviewSearchResultsDto(
      content = events.content.map { event ->
        event.copy(prisonerDetails = prisonerDetails[event.prisonerNumber])
      },
      pageNumber = events.pageNumber,
      totalElements = events.totalElements,
      totalPages = events.totalPages,
    )
  }
}
