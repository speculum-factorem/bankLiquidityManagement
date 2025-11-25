-- Create liquidity_alerts table
CREATE TABLE IF NOT EXISTS liquidity_alerts (
    id BIGSERIAL PRIMARY KEY,
    alert_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    branch_code VARCHAR(10) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    deficit_amount NUMERIC(15, 2) NOT NULL,
    liquidity_ratio NUMERIC(5, 2) NOT NULL,
    message TEXT NOT NULL,
    severity INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP,
    resolved_at TIMESTAMP,
    acknowledged_by VARCHAR(100),
    resolution_notes TEXT
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_alert_branch ON liquidity_alerts(branch_code);
CREATE INDEX IF NOT EXISTS idx_alert_status ON liquidity_alerts(status);
CREATE INDEX IF NOT EXISTS idx_alert_type ON liquidity_alerts(alert_type);
CREATE INDEX IF NOT EXISTS idx_alert_created_at ON liquidity_alerts(created_at);
CREATE INDEX IF NOT EXISTS idx_alert_severity ON liquidity_alerts(severity);

-- Add comments
COMMENT ON TABLE liquidity_alerts IS 'Stores liquidity alerts for bank branches';
COMMENT ON COLUMN liquidity_alerts.alert_type IS 'Type of alert: DEFICIT, LOW_LIQUIDITY, CRITICAL, THRESHOLD_BREACH';
COMMENT ON COLUMN liquidity_alerts.status IS 'Alert status: ACTIVE, ACKNOWLEDGED, RESOLVED';
COMMENT ON COLUMN liquidity_alerts.severity IS 'Alert severity from 1 to 10, where 10 is most severe';

