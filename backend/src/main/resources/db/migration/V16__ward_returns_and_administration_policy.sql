ALTER TABLE return_credit_requests MODIFY dispense_id BIGINT NULL;
CREATE TABLE consumable_returns(id BIGINT PRIMARY KEY AUTO_INCREMENT,movement_id BIGINT NOT NULL,quantity DECIMAL(12,2) NOT NULL,reason VARCHAR(500) NOT NULL,performed_by VARCHAR(80) NOT NULL,created_at DATETIME DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(movement_id) REFERENCES batch_movements(id));
INSERT INTO hospital_settings VALUES('early_medication_minutes','60');
