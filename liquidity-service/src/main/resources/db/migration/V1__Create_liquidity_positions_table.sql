-- Create liquidity_positions table
CREATE TABLE IF NOT EXISTS liquidity_positions (
    id BIGSERIAL PRIMARY KEY,
    currency VARCHAR(3) NOT NULL,
    available_cash NUMERIC(15, 2) NOT NULL,
    required_reserves NUMERIC(15, 2) NOT NULL,
    net_liquidity NUMERIC(15, 2) NOT NULL,
    calculation_date TIMESTAMP NOT NULL,
    branch_code VARCHAR(10) NOT NULL,
    status VARCHAR(50),
    liquidity_ratio NUMERIC(5, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_branch_currency ON liquidity_positions(branch_code, currency);
CREATE INDEX IF NOT EXISTS idx_calculation_date ON liquidity_positions(calculation_date);
CREATE INDEX IF NOT EXISTS idx_net_liquidity ON liquidity_positions(net_liquidity);
CREATE INDEX IF NOT EXISTS idx_status ON liquidity_positions(status);

-- Add comments
COMMENT ON TABLE liquidity_positions IS 'Stores liquidity positions for bank branches';
COMMENT ON COLUMN liquidity_positions.currency IS 'ISO 4217 currency code (3 characters)';
COMMENT ON COLUMN liquidity_positions.available_cash IS 'Available cash amount';
COMMENT ON COLUMN liquidity_positions.required_reserves IS 'Required reserves amount';
COMMENT ON COLUMN liquidity_positions.net_liquidity IS 'Net liquidity (available_cash - required_reserves)';
COMMENT ON COLUMN liquidity_positions.liquidity_ratio IS 'Liquidity ratio (available_cash / required_reserves)';

