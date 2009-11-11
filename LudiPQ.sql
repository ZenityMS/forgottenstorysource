/*Check*/
DELETE FROM `monsterdrops` where monsterid = '9300006';
DELETE FROM `monsterdrops` where monsterid = '9300005';
DELETE FROM `monsterdrops` where monsterid = '9300007';
DELETE FROM `monsterdrops` where monsterid = '9300014';
DELETE FROM `monsterdrops` where monsterid = '9300008';
DELETE FROM `monsterdrops` where monsterid = '9300012';
DELETE FROM `monsterdrops` where monsterid = '9300010';
DELETE FROM `monsterdrops` where monsterid = '3230302';
DELETE FROM `monsterdrops` where monsterid = '9300169';
DELETE FROM `monsterdrops` where monsterid = '9300170';
DELETE FROM `monsterdrops` where monsterid = '9300171';

DELETE FROM `reactordrops` where reactorid = '2202003';
DELETE FROM `reactordrops` where reactorid = '2201000';





/* Monster Drops */
INSERT INTO `monsterdrops` (`monsterid`, `itemid`, `chance`) VALUES (9300006, 4001022, 1);
INSERT INTO `monsterdrops` (`monsterid`, `itemid`, `chance`) VALUES (9300005, 4001022, 1);
INSERT INTO `monsterdrops` (`monsterid`, `itemid`, `chance`) VALUES (9300007, 4001022, 1);
INSERT INTO `monsterdrops` (`monsterid`, `itemid`, `chance`) VALUES (9300014, 4001022, 1);
INSERT INTO `monsterdrops` (`monsterid`, `itemid`, `chance`) VALUES (9300008, 4001022, 1);
INSERT INTO `monsterdrops` (`monsterid`, `itemid`, `chance`) VALUES (9300012, 4001023, 1);
INSERT INTO `monsterdrops` (`monsterid`, `itemid`, `chance`) VALUES (9300010, 4001022, 1);
INSERT INTO `monsterdrops` (`monsterid`, `itemid`, `chance`) VALUES (9300169, 4001156, 1);
INSERT INTO `monsterdrops` (`monsterid`, `itemid`, `chance`) VALUES (9300170, 4001156, 1);
INSERT INTO `monsterdrops` (`monsterid`, `itemid`, `chance`) VALUES (9300171, 4001156, 1);

/* Regular bloctopus Drop */
INSERT INTO `monsterdrops` (`monsterid`, `itemid`, `chance`) VALUES (3230302, 4001022, 1);

/* Reactor Drops */
INSERT INTO `reactordrops` (`reactorid`, `itemid`, `chance`) VALUES (2202003, 4001022, 1);
INSERT INTO `reactordrops` (`reactorid`, `itemid`, `chance`) VALUES (2201000, 4001022, 1);