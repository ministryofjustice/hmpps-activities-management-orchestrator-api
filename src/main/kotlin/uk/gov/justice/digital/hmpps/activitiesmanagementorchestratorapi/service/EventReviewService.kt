package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.api.ActivitiesApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.api.PrisonerSearchApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EnrichedEventReviewDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EnrichedEventReviewPrisonerDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EnrichedEventReviewSearchResultsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EventReviewSearchResultsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.mapping.toDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.mapping.toEnrichedDto
import java.time.LocalDate

@Service
class EventReviewService(
  private val activitiesApiClient: ActivitiesApiClient,
  private val prisonerSearchApiClient: PrisonerSearchApiClient
) {
  suspend fun getEventsDataForReview(
    prisonCode: String,
    date: LocalDate,
    prisonerNumber: String? = null,
    includeAcknowledged: Boolean? = null,
  ): EventReviewSearchResultsDto = activitiesApiClient.getEventsDataForReview(prisonCode, date, prisonerNumber = prisonerNumber, includeAcknowledged = includeAcknowledged).toDto()


  suspend fun getEnrichedEventsDataForReview(
    prisonCode: String,
    date: LocalDate,
    prisonerNumber: String? = null,
    includeAcknowledged: Boolean? = null,
  ): EnrichedEventReviewSearchResultsDto {
    val events = getEventsDataForReview(
      prisonCode,
      date,
      prisonerNumber = prisonerNumber,
      includeAcknowledged = includeAcknowledged,
    )

    val prisonerNumbers = events.content
      .mapNotNull { it.prisonerNumber }
      .distinct()

    val prisonerDetails = prisonerSearchApiClient.findByPrisonerNumbersMap(prisonerNumbers)

    val enrichedContent = events.content.map { event ->
      event.toEnrichedDto(prisonerDetails[event.prisonerNumber])
    }

    return EnrichedEventReviewSearchResultsDto(
      content = enrichedContent,
      pageNumber = events.pageNumber,
      totalElements = events.totalElements,
      totalPages = events.totalPages,
    )
  }
}
