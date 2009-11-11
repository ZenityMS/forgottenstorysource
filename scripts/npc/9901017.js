importPackage(net.sf.odinms.tools);

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
              if (cm.isLeader()) { // party leader 
                    cm.sendYesNo("Due to the fact that you need 6 people to do this PQ, while needing only one person. You have the option to skip this stage. Would you like to do this?");
                   } else {
                    cm.sendOk("Please get your party leader to talk to me.");
                    cm.dispose();
                   }
                 } else if (status == 1) {
                    cm.warpParty(922010900);
                    cm.dispose();
}
}
}