#!/bin/bash

echo "Parking Counter Application Startup Script"
echo "========================================="

# Function to run with local profile (H2)
run_local() {

    echo "Ensure project dependencies"
    mvn clean install -DskipTests
    echo "Starting application with LOCAL profile (H2 Database)..."
    mvn spring-boot:run -Dspring-boot.run.profiles=local
}

# Function to run with production profile (SQL Server)
run_prod() {
    echo "Ensure project dependencies"
    mvn clean install -DskipTests
    echo "Starting application with PRODUCTION profile (SQL Server)..."
    echo "Make sure your SQL Server credentials are set!"

    # You can set the password as environment variable
    # export DB_PASSWORD=your_actual_password

    mvn spring-boot:run -Dspring-boot.run.profiles=prod
}

# Function to run with custom profile
run_custom() {
    echo "Starting application with profile: $1"
    mvn spring-boot:run -Dspring-boot.run.profiles=$1
}

# Main menu
if [ $# -eq 0 ]; then
    echo "Usage: ./run-app.sh [local|prod|profile-name]"
    echo ""
    echo "Examples:"
    echo "  ./run-app.sh local    - Run with H2 database"
    echo "  ./run-app.sh prod     - Run with SQL Server"
    echo ""
    read -p "Enter profile (local/prod): " profile

    case $profile in
        local)
            run_local
            ;;
        prod)
            run_prod
            ;;
        *)
            run_custom $profile
            ;;
    esac
else
    case $1 in
        local)
            run_local
            ;;
        prod)
            run_prod
            ;;
        *)
            run_custom $1
            ;;
    esac
fi