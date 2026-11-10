CREATE TABLE stock_movements (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 inventory_item_id BIGINT NOT NULL,
 quantity_change DECIMAL(12,2) NOT NULL,
 reason VARCHAR(255) NOT NULL,
 performed_by VARCHAR(80) NOT NULL,
 created_at DATETIME NOT NULL,
 FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id)
);
CREATE TABLE ipd_beds (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 ward VARCHAR(80) NOT NULL,
 bed_no VARCHAR(30) NOT NULL,
 occupied BOOLEAN NOT NULL DEFAULT FALSE,
 UNIQUE KEY uk_ipd_bed (ward,bed_no)
);
INSERT INTO ipd_beds(ward,bed_no) VALUES ('GENERAL','G-01'),('GENERAL','G-02'),('ICU','I-01');
