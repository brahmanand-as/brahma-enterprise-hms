ALTER TABLE appointment_slots DROP INDEX uk_doctor_start,ADD UNIQUE KEY uk_doctor_start_id(doctor_employee_id,starts_at);
INSERT INTO hospital_settings VALUES('consultation_slot_minutes','30');
