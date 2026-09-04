package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.integration.wiremock

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class PrisonerSearchApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val prisonerSearchApiServer = PrisonerSearchApiMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    prisonerSearchApiServer.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    prisonerSearchApiServer.resetAll()
  }

  override fun afterAll(context: ExtensionContext) {
    prisonerSearchApiServer.stop()
  }
}
