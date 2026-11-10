ALTER TABLE admissions ADD active_patient_key BIGINT GENERATED ALWAYS AS (CASE WHEN status='ADMITTED' THEN patient_id ELSE NULL END) STORED;
CREATE UNIQUE INDEX uk_one_active_admission_per_patient ON admissions(active_patient_key);
