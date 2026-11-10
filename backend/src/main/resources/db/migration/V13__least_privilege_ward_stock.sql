INSERT INTO permissions VALUES('wardstock.read','Ward stock / Read');
INSERT INTO role_permissions VALUES('ADMIN','wardstock.read'),('NURSE','wardstock.read'),('PHARMACIST','wardstock.read');
DELETE FROM role_permissions WHERE role_name='NURSE' AND permission_code='pharmacy.read';
