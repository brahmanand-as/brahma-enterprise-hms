-- Forward-only additions; existing employees and staff accounts are preserved.
CREATE TABLE employee_number_sequence (id INT PRIMARY KEY, next_value BIGINT NOT NULL);
INSERT INTO employee_number_sequence (id,next_value) SELECT 1, GREATEST(COALESCE(MAX(id),0)+1,1) FROM employees;
CREATE TABLE doctor_profiles (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 employee_id BIGINT NOT NULL UNIQUE,
 specialty VARCHAR(120) NOT NULL,
 qualification VARCHAR(250),
 experience_years INT,
 registration_number VARCHAR(100),
 consultation_fee DECIMAL(12,2),
 photo_url VARCHAR(500),
 biography TEXT,
 availability_notes VARCHAR(500),
 CONSTRAINT fk_doctor_profile_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);
