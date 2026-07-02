[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/hmpps-activities-management-orchestrator-api/badge?style=flat)](https://github-community.service.justice.gov.uk/repository-standards/hmpps-activities-management-orchestrator-api)
[![Docker Repository on ghcr](https://img.shields.io/badge/ghcr.io-repository-2496ED.svg?logo=docker)](https://ghcr.io/ministryofjustice/hmpps-activities-management-orchestrator-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://activities-management-orchestrator-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html)

# hmpps-activities-management-orchestrator-api

This service serves as the backend for the [Activities Management UI](https://github.com/ministryofjustice/hmpps-activities-management), retrieving data from the [Activities Management API](https://github.com/ministryofjustice/hmpps-activities-management-api) and aggregating the responses.

## Building the project

Tools required:
* JDK v25
* Kotlin v2.4

Useful tools that can be installed, using [Homebrew](https://brew.sh/), but are not essential:
* [kubectl](https://kubernetes.io/docs/reference/kubectl/) - not essential for building the project but will be needed for other tasks.
* [k9s](https://k9scli.io/) - a terminal-based UI to interact with your Kubernetes clusters.

## Install gradle and build the project
```
./gradlew
```

```
./gradlew clean build
```

## Running the service locally
Add a local `.env` file to the root of the project:

#### Set up the local environment variables
```
SYSTEM_CLIENT_ID=<system.client.id>
SYSTEM_CLIENT_SECRET=<system.client.secret>
HMPPS_AUTH_URL= https://sign-in-dev.hmpps.service.justice.gov.uk/auth
ACTIVITIES_API_URL=https://activities-api-dev.prison.service.justice.gov.uk
PRISONER_SEARCH_API_URL=https://prisoner-search-dev.prison.service.justice.gov.uk
```

- You **must** escape any '\$' characters with '\\$'
- `SYSTEM_CLIENT_ID` and `SYSTEM_CLIENT_SECRET` can be extracted from the Kubernetes secrets for the `DEV` environment.

#### Run the service
```
./run-local.sh
```

Or, to use default port and properties
```
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

## Running tests

### Unit

```
./gradlew test 
 ```

## Common gradle tasks

To list project dependencies, run:

```bash
./gradlew dependencies
``` 

To check for dependency updates, run:
```bash
./gradlew dependencyUpdates --warning-mode all
```

#### KtLint

To run Ktlint check:
```bash
./gradlew ktlintCheck
```

To run Ktlint format:
```bash
./gradlew ktlintFormat
```
