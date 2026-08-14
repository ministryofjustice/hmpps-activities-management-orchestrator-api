package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.EventReviewSearchResultsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service.EventReviewService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.LocalDate

@RestController
@RequestMapping(value = ["/event-review"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class EventReviewController(
  private val eventReviewService: EventReviewService,
) {
  @GetMapping(value = ["/prison/{prisonCode}"])
  @PreAuthorize("hasAnyRole('ACTIVITY_HUB', 'ACTIVITY_ADMIN')")
  @ResponseBody
  @Operation(
    summary = "Retrieve events for a prison to indicate that a change of circumstances affecting allocations has occurred",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The event data has been returned successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = EventReviewSearchResultsDto::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid Request",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  suspend fun getEventsForReview(
    @PathVariable
    @Parameter(description = "The prison code")
    prisonCode: String,
    @RequestParam
    @Parameter(description = "The date for which to retrieve events")
    date: LocalDate,
    @RequestParam(required = false)
    @Parameter(description = "The prisoner number to filter by")
    prisonerNumber: String? = null,
    @RequestParam(required = false)
    @Parameter(description = "Whether to include acknowledged events")
    includeAcknowledged: Boolean? = false,
  ): EventReviewSearchResultsDto = eventReviewService.getEventsDataForReview(prisonCode, date, prisonerNumber, includeAcknowledged)
}
