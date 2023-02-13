#!/bin/bash
set -e

POSTGRES="psql --username ${POSTGRES_USER}"

echo "Creating database: ${DB_APP_NAME}"
echo "Creating test database: ${DB_APP_NAME}-test"

$POSTGRES <<EOSQL
CREATE DATABASE "${DB_APP_NAME}" OWNER "${DB_APP_USER}";
CREATE DATABASE "${DB_APP_NAME}-test" OWNER "${DB_APP_USER}-test";
EOSQL