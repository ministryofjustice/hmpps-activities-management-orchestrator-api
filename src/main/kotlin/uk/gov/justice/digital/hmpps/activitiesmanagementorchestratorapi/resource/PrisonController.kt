package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto.CellLocationResultsDto
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.service.PrisonService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@RequestMapping(value = ["/prison"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class PrisonController(
  private val prisonService: PrisonService,
) {
  @GetMapping(value = ["/cell-location-history"])
  @PreAuthorize("hasRole('VIEW_PRISONER_DATA')")
  @ResponseBody
  @Operation(
    summary = "Retrieve basic prisoner details by prisoner numbers",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The cell location history has been returned successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = CellLocationResultsDto::class),
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
  suspend fun getCellLocationHistory(
    @RequestParam(required = true)
    @Parameter(description = "Booking identiier")
    bookingId: String,
  ): CellLocationResultsDto? = prisonService.getCurrentAndPreviousBedAssignment(bookingId = bookingId)
    ?.content
    ?.mapNotNull { it.description }
    ?.let { CellLocationResultsDto(cellLocations = it) }
}
