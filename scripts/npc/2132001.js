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
if (cm.getPlayer().getClient().getChannelServer().neededleaves == 0) {
cm.sendYesNo ("Do you wish to start the event?");
} else {
cm.getPlayer().getClient().getChannelServer().snowballTeam1 = 100;
cm.sendOk ("The event is not ready to be started. Use #b@donate#k to help start the event.");
cm.dispose();
}
} else if (status == 1) {
if (cm.mapMobCount() < 10) {
cm.summonMobAtPosition(9400203, 1, 50000, 40, 112, 274);
cm.summonMobAtPosition(3110300, 1, 50000, 40, 112, 274);
cm.getPlayer().getClient().getChannelServer().neededleaves = 3;
cm.sendOk ("The event has started!");
cm.sendDonationNotice();
cm.dispose();
} else { 
cm.sendOk ("There are more than 10 monsters in the map right now...");
cm.dispose();
}
}
}
}