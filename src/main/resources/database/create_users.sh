#!/bin/bash
set -e

POSTGRES="psql --username ${POSTGRES_USER}"

echo "Creating database role: ${DB_APP_USER}"
echo "Creating database role: ${DB_APP_USER}-test"

$POSTGRES <<EOSQL
CREATE USER "${DB_APP_USER}" WITH CREATEDB PASSWORD '${DB_APP_PASS}';
CREATE USER "${DB_APP_USER}-test" WITH CREATEDB PASSWORD '${DB_APP_USER}-test';
EOSQL