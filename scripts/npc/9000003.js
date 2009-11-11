var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (status >= 0 && mode == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
if (cm.getPlayer().blueteam == true && cm.getPlayer().hasflag == true) {
if (cm.getPlayer().getPosition().x > 2753) {
if (!cm.getPlayer().getCheatTracker().Spam(5000, 21)) {
cm.warp(1010000, 0);
cm.getPlayer().gainBlueTeamWin();
cm.dropFlagAtPos(1302065, -62, 274);
cm.getPlayer().removeAll(1302065);
cm.getPlayer().mapNotice("The Blue Team has gained a point! They now have " + cm.getPlayer().blueteamwins + " points!");
cm.dispose();
if (cm.getPlayer().blueteamwins >= 10) {
    cm.getPlayer().mapNotice("Congratulations Blue Team! You have won!");
 //   cm.getPlayer().resetFlagMap(true);
   // cm.getPlayer().screwFlags(true);
   // cm.warp(100000000, 0);
     cm.getPlayer().warpOut();
}
} else {
cm.dispose();
}
} else {
cm.sendOk ("You need be past me to get a point!");
cm.dispose();
}
} else {
cm.sendOk("You are not on the #b Blue Team #k or you don't have a flag.");
cm.dispose();
}
}
}
}

