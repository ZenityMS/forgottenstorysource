importPackage(java.util);
importPackage(net.sf.odinms.client);
importPackage(net.sf.odinms.server);
importPackage(net.sf.odinms.net.channel);
importPackage(net.sf.odinms.tools);
importPackage(net.sf.odinms.scripting.npc);

var exp = new Array (50000, 100000, 500000, 800000);
var takeaways = new Array (100, 200, 300, 500);

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
        cm.sendSimple("I am the Quest NPC for the #bSkelegon Dungeon!#k I control quests for the monsters below " +
// var lol = 1000000 * 100;
 "\r\n#L0#Bring me 100 #v4000185# for 5000000 EXP\n\#l" +
 "\r\n#L1#Bring me 200 #v4000185# for 10000000 EXP\n\#l" +
 "\r\n#L2#Bring me 300 #v4000185# for 50000000 EXP\n\#l" +
 "\r\n#L3#Bring me 500 #v4000185# for 80000000 EXP\n\#l");
} else if (status == 1) {
if (cm.haveItem(4000185, takeaways[selection])) {
cm.gainItem(4000185, -takeaways[selection]);
cm.gainExp(exp[selection] * 100);
// cm.sendTimedOk(5000, "Good luck!"); removed because of check for talking to npc's in map where the npc doesn't exist dc's you
cm.dispose();
} else {
cm.sendOk("You don't have " + takeaways[selection] + " #v4000185#.");
cm.dispose();
}
}
}
}