DROP TABLE IF EXISTS `odinms`.`votecontrol`;
CREATE TABLE  `odinms`.`votecontrol` (
  `name` varchar(45) COLLATE latin1_general_ci NOT NULL,
  `time` int(11) NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;



DROP TABLE IF EXISTS `xiuzsource`.`voteipcontrol`;
CREATE TABLE  `xiuzsource`.`voteipcontrol` (
  `ip` varchar(45) COLLATE latin1_general_ci NOT NULL,
  `time` int(11) NOT NULL,
  PRIMARY KEY (`ip`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `xiuzsource`.`voterewards`;
CREATE TABLE  `xiuzsource`.`voterewards` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `claimed` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=213 DEFAULT CHARSET=utf8;


ALTER TABLE `xiuzsource`.`accounts` ADD COLUMN `votepoints` INTEGER UNSIGNED NOT NULL DEFAULT 0;