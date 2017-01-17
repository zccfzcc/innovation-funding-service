CREATE TABLE `public_content` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `competition_id` bigint(20) NOT NULL,
  `publish_date` datetime,
  `short_description` varchar(225),
  `project_funding_range`varchar(225),
  `eligibility_summary` varchar(225),
  `summary` longtext,
  `funding_type` varchar(50),
  `project_size` varchar(225),
  PRIMARY KEY (`id`),
  KEY `FK_public_content_to_competition` (`competition_id`),
  CONSTRAINT `FK_public_content_to_competition` FOREIGN KEY (`competition_id`) REFERENCES `competition` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `content_keyword` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `public_content_id` bigint(20) NOT NULL,
  `keyword` varchar(50),
  PRIMARY KEY (`id`),
  KEY `FK_competition_keyword_to_public_content` (`public_content_id`),
  CONSTRAINT `FK_competition_keyword_to_public_content` FOREIGN KEY (`public_content_id`) REFERENCES `public_content` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `content_repeatable_section` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `public_content_id` bigint(20) NOT NULL,
  `section` varchar(50),
  `heading` varchar(225),
  `content` longtext,
  `file_entry_id` bigint(20),
  PRIMARY KEY (`id`),
  KEY `FK_content_section_to_public_content` (`public_content_id`),
  KEY `FK_content_section_to_file_entry` (`file_entry_id`),
  CONSTRAINT `FK_content_section_to_public_content` FOREIGN KEY (`public_content_id`) REFERENCES `public_content` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_content_section_to_file_entry` FOREIGN KEY (`file_entry_id`) REFERENCES `file_entry` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `content_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `public_content_id` bigint(20) NOT NULL,
  `date` datetime,
  `content` longtext,
  PRIMARY KEY (`id`),
  KEY `FK_content_event_to_public_content` (`public_content_id`),
  CONSTRAINT `FK_content_event_to_public_content` FOREIGN KEY (`public_content_id`) REFERENCES `public_content` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `content_setup_status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `public_content_id` bigint(20) NOT NULL,
  `section` varchar(50),
  `status` tinyint(1),
  PRIMARY KEY (`id`),
  KEY `FK_content_setup_status_to_public_content` (`public_content_id`),
  CONSTRAINT `FK_content_setup_status_to_public_content` FOREIGN KEY (`public_content_id`) REFERENCES `public_content` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;