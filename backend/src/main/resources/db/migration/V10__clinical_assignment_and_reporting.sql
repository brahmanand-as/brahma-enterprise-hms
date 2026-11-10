ALTER TABLE appointments ADD doctor_employee_id BIGINT,ADD FOREIGN KEY(doctor_employee_id) REFERENCES employees(id);
ALTER TABLE encounters ADD doctor_employee_id BIGINT,ADD FOREIGN KEY(doctor_employee_id) REFERENCES employees(id);
CREATE TABLE care_quality_events(id BIGINT PRIMARY KEY AUTO_INCREMENT,encounter_id BIGINT,department VARCHAR(100) NOT NULL,event_type VARCHAR(60) NOT NULL,severity VARCHAR(20) NOT NULL,description VARCHAR(1000) NOT NULL,recorded_by VARCHAR(80) NOT NULL,created_at DATETIME DEFAULT CURRENT_TIMESTAMP);
