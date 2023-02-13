#!/bin/bash
set -e

echo "Creating tables..."

PGPASSWORD=${DB_APP_PASS} psql --username ${DB_APP_USER} ${DB_APP_NAME} --file src/main/resources/database/database.sql