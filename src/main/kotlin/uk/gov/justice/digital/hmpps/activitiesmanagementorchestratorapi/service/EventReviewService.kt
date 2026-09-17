package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.api.ActivitiesApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EventReviewSearchResultsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.mapping.toDto
import java.time.LocalDate

@Service
class EventReviewService(
  private val activitiesApiClient: ActivitiesApiClient,
  private val prisonerSearchService: PrisonerSearchService,
) {
  suspend fun getEventsDataForReview(
    prisonCode: String,
    date: LocalDate,
    prisonerNumbers: List<String>? = null,
    includeAcknowledged: Boolean? = null,
    filterEventTypes: List<String>? = null,
    page: Int,
    size: Int,
    sortDirection: String,
  ): EventReviewSearchResultsDto {
    val events = activitiesApiClient
      .getEventsDataForReview(
        prisonCode = prisonCode,
        date = date,
        prisonerNumbers = prisonerNumbers,
        includeAcknowledged = includeAcknowledged,
        filterEventTypes = filterEventTypes,
        page = page,
        size = size,
        sortDirection = sortDirection,
      )
      .toDto()

    val prisonerNumbersToLookup = events.content.mapNotNull { it.prisonerNumber }.distinct()
    val prisonerDetails = if (prisonerNumbersToLookup.isEmpty()) {
      emptyMap()
    } else {
      prisonerSearchService.getBasicPrisonerDetailsMap(prisonerNumbersToLookup)
    }

//   TODO: Look into how we are going to handle prisonerDetails/prisonerNumber returning null?
//    Could the issues we occasionally see on the DLQ play into this?

    return EventReviewSearchResultsDto(
      content = events.content.map { event ->
        event.copy(prisonerDetails = event.prisonerNumber?.let { prisonerDetails[it] })
      },
      pageNumber = events.pageNumber,
      totalElements = events.totalElements,
      totalPages = events.totalPages,
    )
  }
}
