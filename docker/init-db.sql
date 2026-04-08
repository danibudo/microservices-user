-- user_service database is created by the POSTGRES_DB environment variable.
-- This script creates the auth_service database and its dedicated user.

CREATE USER auth_user WITH PASSWORD 'changeme';
CREATE DATABASE auth_service OWNER auth_user;