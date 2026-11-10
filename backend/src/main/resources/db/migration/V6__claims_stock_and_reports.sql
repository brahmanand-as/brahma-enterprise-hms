ALTER TABLE insurance_claims ADD admission_id BIGINT NULL,ADD claim_type VARCHAR(30) NOT NULL DEFAULT 'CASHLESS',ADD non_payable DECIMAL(12,2) NOT NULL DEFAULT 0,ADD settled_amount DECIMAL(12,2) NOT NULL DEFAULT 0;
CREATE TABLE insurance_events(id BIGINT PRIMARY KEY AUTO_INCREMENT,claim_id BIGINT NOT NULL,status VARCHAR(30) NOT NULL,amount DECIMAL(12,2),notes VARCHAR(500),performed_by VARCHAR(80) NOT NULL,created_at DATETIME DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(claim_id) REFERENCES insurance_claims(id));
ALTER TABLE inventory_items ADD batch_no VARCHAR(80),ADD expires_on DATE;
ALTER TABLE radiology_orders ADD verified_by VARCHAR(80),ADD verified_at DATETIME;
CREATE TABLE doctor_slot_locks(doctor_name VARCHAR(120) PRIMARY KEY);
