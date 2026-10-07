export $(cat .env | xargs)  # If you want to set or update the current shell environment

# Run the application with local profile active
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
