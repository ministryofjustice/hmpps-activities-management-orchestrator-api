package uk.gov.justice.digital.hmpps.activitiesmanagementorchestratorapi.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.auth.authorisedWebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @param:Value("\${hmpps.auth.url}") val hmppsAuthBaseUrl: String,
  @param:Value("\${activities.api.url}") val activitiesApiBaseUrl: String,
  @param:Value("\${api.health-timeout:2s}") val healthTimeout: Duration,
  @param:Value("\${api.timeout:20s}") val timeout: Duration,
) {

  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  // HMPPS Auth health ping is required if your service calls HMPPS Auth to get a token to call other services
  @Bean
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUrl, healthTimeout)

  @Bean
  fun activitiesHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(activitiesApiBaseUrl, healthTimeout)

  @Bean
  fun activitiesWebClient(authorizedClientManager: OAuth2AuthorizedClientManager, builder: WebClient.Builder): WebClient = builder
    .authorisedWebClient(authorizedClientManager, "activities-api", activitiesApiBaseUrl, timeout)
    .also { log.info("WEB CLIENT CONFIG: creating activities api web client") }
}
