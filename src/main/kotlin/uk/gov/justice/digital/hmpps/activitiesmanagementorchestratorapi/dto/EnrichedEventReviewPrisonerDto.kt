package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Basic prisoner details")
data class EnrichedEventReviewPrisonerDto(
  @Schema(description = "The booking ID related to this prisoner", example = "123456")
  val bookingId: Int? = null,

  @Schema(description = "The prisoner number which this event relates to", example = "G1234FF")
  val prisonerNumber: String? = null,

  @Schema(description = "The prisoner's first name", example = "JOE")
  val firstName: String? = null,

  @Schema(description = "The prisoner's last name", example = "BLOGGS")
  val lastName: String? = null,

  @Schema(description = "The prisoner's current cell location", example = "2-1-007")
  val cellLocation: String? = null,
)

