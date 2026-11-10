ALTER TABLE payments ADD verified_reference_key VARCHAR(160) GENERATED ALWAYS AS (CASE WHEN method IN ('CARD','UPI','BANK') AND reference_no IS NOT NULL AND reference_no<>'' THEN CONCAT(method,':',reference_no) ELSE NULL END) STORED;
CREATE UNIQUE INDEX uk_verified_payment_reference ON payments(verified_reference_key);
