package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "The result of an event review search")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class EventReviewSearchResultsDto(
  @Schema(description = "The matching records")
  val content: List<EventReviewDto>,

  @Schema(description = "The current page number", example = "1")
  val pageNumber: Int,

  @Schema(description = "The total number of elements", example = "20")
  val totalElements: Long,

  @Schema(description = "The total number of pages", example = "5")
  val totalPages: Int,
)
