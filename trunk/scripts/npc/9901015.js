importPackage(java.util);
importPackage(net.sf.odinms.client);
importPackage(net.sf.odinms.server);
importPackage(net.sf.odinms.net.channel);
importPackage(net.sf.odinms.tools);
importPackage(net.sf.odinms.scripting.npc);

var status = 0;
var chicken = 0;
var famecost = 7000000;
var defamecost = 8000000;
var warpcost = 5000000;
var killcost = 50000000;
var sendcost = 5000000;
var itemcost = 10000000;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
        cm.sendSimple ("Hello! I can warp you to various custom Mini Dungeons where you can train! Pick a map!" + "\r\n#L1##b#eSkele Dungeon - Advised to be at least level 70#k\n\#l" +
"\r\n#L2##bHeadless Horsemen - Advised to be at least level 120!\n\#l" +
"\r\n#L3##bBunny Dungeon - Any level, the monsters have high HP though!\n\#l");
} else if (selection == 1) {
cm.warp(105040301, 0);
// cm.sendTimedOk(5000, "Good luck!"); removed because of check for talking to npc's in map where the npc doesn't exist dc's you
cm.dispose();
} else if (selection == 2) {
cm.warp(105040302, 0);
// cm.sendTimedOk(5000, "Good luck!"); removed because of check for talking to npc's in map where the npc doesn't exist dc's you
cm.dispose();
} else if (selection == 3) {
cm.warp(105040303, 0);
cm.getPlayer().dropMessage("You are advised to talk to Mad Bunny for a quest you can do here");
}
}
}