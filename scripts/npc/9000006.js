/* NPC made by Rich */

importPackage(java.util);
importPackage(net.sf.odinms.client);
importPackage(net.sf.odinms.server);
importPackage(net.sf.odinms.net.channel);
importPackage(net.sf.odinms.tools);
importPackage(net.sf.odinms.scripting.npc);

var status = 0;

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
                cm.sendYesNo("I am the Exit NPC for #bCapture the Flag.#k Would you like to exit this map?");
            } else if (status == 1) {
                cm.getPlayer().exitFLagMap();
var eim = cm.getPlayer().getEventInstance();
                cm.warp(100000000, 0);
                if (eim != null) {
                    eim.unregisterPlayer(cm.getPlayer());
}
                cm.dispose();
}
}
}
