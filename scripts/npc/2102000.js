var status = 0;
var minlvl = 100;
var maxlvl = 255;
var minplayers = 1;
var maxplayers = 6;
var time = 15;
var open = true;

function start() {
    status = -1; // and when they click lets fight make it turn to a really cool ifght song :D LOL ok like the Zakum battle song? kk and btw uhm can you add a message like after they click OK to say "Matt: Meet me near the top of the map." ? o-o in other words, a
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else if (mode == 0) {
        cm.sendOk("I spy a chicken :O"); // lLOL
        cm.dispose();
    } else {
        if (mode == 1)
            status++;
        else
            status--;
		
        if (status == 0) {
cm.sendYesNo("Hello #b #h ##k! Would you like to fight #rSuper Horntail?#k He is waiting :)");
} else if (status == 1) {
if (cm.getPlayer().warning[1] == false && cm.isLeader()) {
cm.getPlayer().warning[1] = true;
cm.mapMessage("On behalf of MapleZtory, please defeat Big Puff Daddy! rawrawrawr");
        var mf = cm.getPlayer().getMap().getMapFactory();
	var bossmap = mf.getMap(240030103);
	bossmap.removePortals();
	var mob = net.sf.odinms.server.life.MapleLifeFactory.getMonster(8810018);
        var mob1 = net.sf.odinms.server.life.MapleLifeFactory.getMonster(8810002);
        var mob2 = net.sf.odinms.server.life.MapleLifeFactory.getMonster(8810003);
        var mob3 = net.sf.odinms.server.life.MapleLifeFactory.getMonster(8810004);
        var mob4 = net.sf.odinms.server.life.MapleLifeFactory.getMonster(8810005);
        var mob5 = net.sf.odinms.server.life.MapleLifeFactory.getMonster(8810006);
        var mob6 = net.sf.odinms.server.life.MapleLifeFactory.getMonster(8810007);
        var mob7 = net.sf.odinms.server.life.MapleLifeFactory.getMonster(8810008);
        var mob8 = net.sf.odinms.server.life.MapleLifeFactory.getMonster(8810009);
	var overrideStats = new net.sf.odinms.server.life.MapleMonsterStats();
	overrideStats.setHp(2147000000);
	overrideStats.setExp(2147000000);
	overrideStats.setMp(mob.getMaxMp());
	// mob.setOverrideStats(overrideStats);
	mob1.setOverrideStats(overrideStats);
	mob2.setOverrideStats(overrideStats);
	mob3.setOverrideStats(overrideStats);
	mob4.setOverrideStats(overrideStats);
	mob5.setOverrideStats(overrideStats);
	mob6.setOverrideStats(overrideStats);
	mob7.setOverrideStats(overrideStats);
	mob8.setOverrideStats(overrideStats);
	mob.setHp(overrideStats.getHp());
	//eim.registerMonster(mob);
	 bossmap.spawnMonsterOnGroudBelow(mob, new java.awt.Point(-182, -178));
	 bossmap.spawnMonsterOnGroudBelow(mob1, new java.awt.Point(-182, -178));
	 bossmap.spawnMonsterOnGroudBelow(mob2, new java.awt.Point(-182, -178));
	 bossmap.spawnMonsterOnGroudBelow(mob3, new java.awt.Point(-182, -178));
	 bossmap.spawnMonsterOnGroudBelow(mob4, new java.awt.Point(-182, -178));
	 bossmap.spawnMonsterOnGroudBelow(mob5, new java.awt.Point(-182, -178));
	 bossmap.spawnMonsterOnGroudBelow(mob6, new java.awt.Point(-182, -178));
	 bossmap.spawnMonsterOnGroudBelow(mob7, new java.awt.Point(-182, -178));
	 bossmap.spawnMonsterOnGroudBelow(mob8, new java.awt.Point(-182, -178));
	// bossmap.killAllMonsters(false);
       // bossmap.killMonster(8810018); // i like that funkshun :( // this one looks pro though :Dlol ur right XD
// spawnMonster(int mobid, int HP, int MP, int level, int EXP, int boss, int undead, int amount, int x, int y);
cm.dispose();
} else {
cm.sendOk("Super Horntail has already been spawned or you are not leader!");
cm.dispose();
}
} 
}
}