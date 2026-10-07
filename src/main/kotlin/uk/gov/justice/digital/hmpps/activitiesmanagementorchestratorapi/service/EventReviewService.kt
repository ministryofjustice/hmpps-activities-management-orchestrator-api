package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service

import jakarta.validation.ValidationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.activitiesapi.api.ActivitiesApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EventReviewDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EventReviewSearchResultsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.mapping.toDto
import java.time.LocalDate

@Service
class EventReviewService(
  private val activitiesApiClient: ActivitiesApiClient,
  private val prisonerSearchService: PrisonerSearchService,
  private val prisonService: PrisonService,
) {
  private companion object {
    val PRISON_NAME_ENRICHED_EVENT_TYPES = setOf(
      "prison-offender-events.prisoner.activities-changed",
      "prison-offender-events.prisoner.appointments-changed",
    )
  }

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
    if (prisonCode.isBlank()) {
      throw ValidationException("Prison code must be provided")
    }

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

    val hasEnrichedEvents = events.content.any { it.eventType in PRISON_NAME_ENRICHED_EVENT_TYPES }
    val prisonName = if (hasEnrichedEvents && prisonCode.isNotBlank()) {
      prisonService.getPrisonName(prisonCode).prisonName
    } else {
      null
    }

//   TODO: Look into how we are going to handle prisonerDetails/prisonerNumber returning null?
//    Could the issues we occasionally see on the DLQ play into this?

    return EventReviewSearchResultsDto(
      content = events.content.map { event ->
        event.copy(prisonerDetails = event.prisonerNumber?.let { prisonerDetails[it] })
          .enrichEventDataWithPrisonName(prisonName)
      },
      pageNumber = events.pageNumber,
      totalElements = events.totalElements,
      totalPages = events.totalPages,
    )
  }

  private fun EventReviewDto.enrichEventDataWithPrisonName(prisonName: String?): EventReviewDto {
    if (eventType !in PRISON_NAME_ENRICHED_EVENT_TYPES || prisonName == null) return this
    return copy(eventData = prisonName)
  }
}
