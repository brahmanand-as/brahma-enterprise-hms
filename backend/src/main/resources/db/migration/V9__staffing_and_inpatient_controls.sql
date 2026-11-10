ALTER TABLE ipd_beds ADD reserved_patient_id BIGINT,ADD daily_rate DECIMAL(12,2) NOT NULL DEFAULT 0;
ALTER TABLE duty_roster ADD ends_on DATE,ADD replacement_for BIGINT;
UPDATE duty_roster SET ends_on=shift_date;
CREATE TABLE shift_requests(id BIGINT PRIMARY KEY AUTO_INCREMENT,roster_id BIGINT NOT NULL,requested_by VARCHAR(80) NOT NULL,replacement_employee_id BIGINT NOT NULL,reason VARCHAR(500) NOT NULL,status VARCHAR(30) NOT NULL DEFAULT 'PENDING',reviewed_by VARCHAR(80),reviewed_at DATETIME,created_at DATETIME DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(roster_id) REFERENCES duty_roster(id),FOREIGN KEY(replacement_employee_id) REFERENCES employees(id));
CREATE TABLE department_heads(department VARCHAR(100) PRIMARY KEY,username VARCHAR(80) NOT NULL,FOREIGN KEY(username) REFERENCES staff_accounts(username));
CREATE TABLE attendance(id BIGINT PRIMARY KEY AUTO_INCREMENT,employee_id BIGINT NOT NULL,work_date DATE NOT NULL,status VARCHAR(30) NOT NULL,hours_worked DECIMAL(6,2) NOT NULL,UNIQUE KEY uk_attendance(employee_id,work_date),FOREIGN KEY(employee_id) REFERENCES employees(id));
ALTER TABLE salary_slips ADD bank_reference VARCHAR(120),ADD reconciled_by VARCHAR(80);
INSERT INTO hospital_settings VALUES('payroll_pf_percent','12'),('payroll_professional_tax','200');
CREATE TABLE patient_identity_locks(mobile VARCHAR(20) PRIMARY KEY);
