package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.ValidationException
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.api.PrisonerSearchApiClient
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.PrisonerBasicDetails
import uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.client.prisonersearchapi.model.PrisonerNumbers
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
@RequestMapping(value = ["/prisoner"], produces = [MediaType.APPLICATION_JSON_VALUE])
@AuthApiResponses
class PrisonerController(
  private val prisonerSearchApiClient: PrisonerSearchApiClient,
) {
  @GetMapping(value = ["/prisoner-number-by-name"])
  @PreAuthorize("hasAnyRole('ROLE_PRISONER_SEARCH')")
  @ResponseBody
  @Operation(
    summary = "Retrieve prisoner details for a prisoner by first name and/or lastname",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The prisoner details have been returned successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = PrisonerBasicDetails::class),
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
  suspend fun getPrisonerNumbers(
    @RequestParam(required = false)
    @Parameter(description = "The prisoner forename")
    prisonerForename: String?,
    @RequestParam(required = false)
    @Parameter(description = "The prisoner surname")
    prisonerSurname: String?,
  ): List<String> {
    if (prisonerForename.isNullOrBlank() && prisonerSurname.isNullOrBlank()) {
      throw ValidationException("Either prisonerForename or prisonerSurname must be provided")
    }

    return prisonerSearchApiClient.lookupPrisonerNumberByName(prisonerForename.orEmpty(), prisonerSurname.orEmpty())
  }

  @PostMapping(value = ["/prisoner-details-by-numbers"])
  @PreAuthorize("hasAnyRole('PRISONER_SEARCH')")
  @ResponseBody
  @Operation(
    summary = "Retrieve basic prisoner details by prisoner numbers",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The prisoner details have been returned successfully",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = PrisonerBasicDetails::class),
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
  suspend fun getBasicPrisonerDetails(
    @RequestBody
    @Parameter(description = "Prisoner numbers to filter by")
    prisonerNumbers: PrisonerNumbers,
  ): List<PrisonerBasicDetails> {
    if (prisonerNumbers.prisonerNumbers.isEmpty()) {
      throw ValidationException("Prisoner numbers must be provided")
    }

    return prisonerSearchApiClient.findByPrisonerNumbers(prisonerNumbers.prisonerNumbers)
  }
}
