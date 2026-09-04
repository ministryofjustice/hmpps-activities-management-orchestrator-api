package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.extensions

enum class MovementType(val nomisShortCode: String) {
  RELEASE("REL"),
  TEMPORARY_ABSENCE("TAP"),
  TRANSFER("TRN"),
}
