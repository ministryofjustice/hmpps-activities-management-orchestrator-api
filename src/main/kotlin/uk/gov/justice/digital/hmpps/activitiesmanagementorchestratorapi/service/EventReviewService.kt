package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.api.ActivitiesApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.model.PrisonerUpdatedDetails
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonapi.model.BedAssignmentSearchResults
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EventReviewSearchResultsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.mapping.toDto
import java.time.LocalDate

@Service
class EventReviewService(
  private val activitiesApiClient: ActivitiesApiClient,
  private val prisonerSearchService: PrisonerSearchService,
  private val prisonService: PrisonService,
) {
  suspend fun getEventsDataForReview(
    prisonCode: String,
    date: LocalDate,
    prisonerNumbers: List<String>? = null,
    filterAcknowledged: Boolean? = null,
    filterEventTypes: List<String>? = null,
    page: Int,
    size: Int,
    sortDirection: String,
  ): EventReviewSearchResultsDto {
    val targetEventForCellLocations = "prisoner-offender-search.prisoner.updated"

    val events = activitiesApiClient
      .getEventsDataForReview(
        prisonCode = prisonCode,
        date = date,
        prisonerNumbers = prisonerNumbers,
        filterAcknowledged = filterAcknowledged,
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

    val bookingIdentifiersToLookup = events.content
      .filter { it.eventType == targetEventForCellLocations }
      .mapNotNull { it.bookingId }
      .distinct()
    val cellLocationHistoryMap = if (bookingIdentifiersToLookup.isEmpty()) {
      emptyMap()
    } else {
      prisonService.getCurrentAndPreviousBedAssignment(bookingIdentifiersToLookup)
    }

//   TODO: Look into how we are going to handle prisonerDetails/prisonerNumber returning null?
//    Could the issues we occasionally see on the DLQ play into this?

    return EventReviewSearchResultsDto(
      content = events.content.map { event ->
        event.copy(
          prisonerDetails = event.prisonerNumber?.let { prisonerDetails[it] },
          prisonerUpdatedDetails = if (event.eventType == targetEventForCellLocations) {
            event.bookingId?.let { cellLocationHistoryMap[it] }?.let { translateToPrisonerUpdatedDetails(it) }
          } else {
            null
          },
        )
      },
      pageNumber = events.pageNumber,
      totalElements = events.totalElements,
      totalPages = events.totalPages,
    )
  }

  private fun translateToPrisonerUpdatedDetails(bedAssignments: BedAssignmentSearchResults): PrisonerUpdatedDetails {
    val cellDescriptions = bedAssignments.content.mapNotNull { it.description }
    return PrisonerUpdatedDetails(
    newCell = bedAssignments.content.getOrNull(0)?.description,
    previousCell = bedAssignments.content.getOrNull(1)?.description,)
  }
}
