INSERT  IGNORE INTO `user` (`id`, `email`, `image_url`, `first_name`, `invite_name`, `last_name`, `phone_number`, `title`, `status`, `uid`) VALUES (10,'john.doe@innovateuk.gov.uk','image3.jpg',NULL,NULL,NULL,NULL,NULL,'ACTIVE','571bc46a-e141-48d1-96db-81def61f8859');
INSERT  IGNORE INTO `user` (`id`, `email`, `image_url`, `first_name`, `invite_name`, `last_name`, `phone_number`, `title`, `status`, `uid`) VALUES (11,'robert.johnson@innovateuk.gov.uk','image3.jpg',NULL,NULL,NULL,NULL,NULL,'ACTIVE','0aad8dad-9a45-4b06-ba4d-7a9520685e69');
INSERT  IGNORE INTO `user_role` (`user_id`, `role_id`) VALUES (10,5);
INSERT  IGNORE INTO `user_role` (`user_id`, `role_id`) VALUES (11,5);