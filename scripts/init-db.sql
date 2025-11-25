-- Bank Liquidity Management Database Initialization Script
-- This script is executed automatically when the PostgreSQL container is first created

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create schema if it doesn't exist
-- Note: PostgreSQL uses 'public' schema by default, but we can create a specific schema
-- CREATE SCHEMA IF NOT EXISTS bank_liquidity;

-- Grant necessary permissions
GRANT ALL PRIVILEGES ON DATABASE bank_liquidity TO postgres;

-- The actual tables will be created by JPA/Hibernate with ddl-auto: update
-- However, we can create some initial data or indexes here if needed

-- Optional: Create indexes for better performance (if tables already exist)
-- These will be created automatically by Hibernate, but can be pre-created here

-- Optional: Insert initial reference data
-- INSERT INTO reference_table (column1, column2) VALUES ('value1', 'value2');

-- Log completion
DO $$
BEGIN
    RAISE NOTICE 'Database initialization completed successfully';
END $$;

