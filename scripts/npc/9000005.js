

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
			cm.sendSimple("Hello #h #, Welcome to #rMaplePassion!#k\r\nWhat do you want to do?\r\n#L1##bTrade my boomer core in for 2 Maple Leaves#k#l\r\n#L2##bTrade my boomer core in for a 21 Att Brown Work Glove#l\r\n\r\n\r\n #r Credits to Asheli for this idea :) "); // \r\n#L3##bTrade my boomer core in for a 60 Att, 400 Luk Pink Adventurer Cape#k#l#k#l");
			} else if (selection == 1) {
                        if (cm.canHold(4001126) && cm.haveItem(4000391)) {
                        cm.gainItem(4000391, -1);
                        cm.gainItem(4001126, 2);
} else {
cm.sendOk ("You don't have a boomer core or you can't hold 3 more maple leaves!");
}

			} else if (selection == 2) {
        if (cm.canHold(1082149) && !cm.haveItem(1082149) && cm.haveItem(4000391)) {
        cm.gainItem(4000391, -1);
        cm.gainItem(1082149);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1082149, "watk", 21);
        cm.reloadChar();
        cm.dispose();
} else {
cm.sendOk ("You can't hold the Brown Work Gloves, you don't have a boomer core, or you already have Brown Work Gloves");
cm.dispose();
}
}
}
}

