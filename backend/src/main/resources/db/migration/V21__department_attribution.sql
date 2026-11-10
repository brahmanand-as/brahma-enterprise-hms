INSERT INTO departments(code,name) VALUES('DEMO','DEMO'),('PHARM','Pharmacy'),('REG','Registration');
ALTER TABLE invoices ADD department_id BIGINT,ADD FOREIGN KEY(department_id) REFERENCES departments(id);
UPDATE invoices i JOIN encounters e ON e.id=i.encounter_id SET i.department_id=e.department_id;
