ALTER TABLE `token` ADD COLUMN `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE `token` ADD COLUMN `updated` timestamp DEFAULT NULL;