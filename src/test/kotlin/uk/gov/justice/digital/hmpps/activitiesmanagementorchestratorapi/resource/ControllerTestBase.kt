package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.resource

import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.hmpps.kotlin.auth.HmppsResourceServerConfiguration
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.security.Principal

@Import(HmppsResourceServerConfiguration::class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockAuthUser(roles = ["ACTIVITY_ADMIN"])
abstract class ControllerTestBase {

  val user: Principal = mock { on { name } doReturn "user" }

  @Autowired
  lateinit var mockMvc: MockMvc

  @Autowired
  lateinit var mapper: ObjectMapper

  @Autowired
  private lateinit var context: WebApplicationContext

  @BeforeEach
  fun before() {
    mockMvc = MockMvcBuilders
      .webAppContextSetup(context)
      .apply<DefaultMockMvcBuilder>(springSecurity())
      .build()
  }
}
