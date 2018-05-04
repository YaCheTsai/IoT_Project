CREATE TABLE `userlnfo` (
  `USER_ID` varchar(45) NOT NULL,
  `USER_PASSWORD` varchar(45) DEFAULT NULL,
  `USER_EMAIL` varchar(45) DEFAULT NULL,
  `USER_NAME` varchar(45) DEFAULT NULL,
  `PUBLICKEY` varchar(256) DEFAULT NULL,
  `ROLE` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

CREATE TABLE `rel_gwayuserid` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `USER_ID` varchar(45) DEFAULT NULL,
  `GATEWAY_ID` varchar(45) DEFAULT NULL,
  `ROLE_ID` varchar(45) DEFAULT NULL,
  `startime` datetime(6) DEFAULT NULL,
  `deadline` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8

CREATE TABLE `rel_rolename` (
  `ROLE_ID` varchar(45) NOT NULL,
  `ROLE_NAME` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`ROLE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

CREATE TABLE `device_value` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `DeviceId` varchar(192) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  `TimeStamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `DataType` int(11) NOT NULL,
  `Value` double NOT NULL DEFAULT '0',
  `name` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  `reportid` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=67 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci

CREATE TABLE `server_sensor` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `DeviceId` varchar(192) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  `TimeStamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `DataType` int(11) NOT NULL,
  `Value` double NOT NULL DEFAULT '0',
  `name` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  `reportid` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=90 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci

