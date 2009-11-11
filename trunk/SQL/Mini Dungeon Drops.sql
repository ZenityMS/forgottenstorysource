-- Custom quest for Mini Dungeons - Made by Rich / WlZET

delete from monsterdrops where itemid = '4000185';

INSERT INTO monsterdrops
(`monsterid`, `itemid`, `chance`)
VALUES
(8190003, 4000185, 3);

