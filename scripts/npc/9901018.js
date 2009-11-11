var status = 0;
function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (status >= 1 && mode == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
                cm.sendOk("Hi I'm Cheeeese :D");
                } else if (status == 1) {
                cm.sendYesNo("Do you want to go to the player hangout room?");
                } else if (status == 2) {
                cm.warp(110000000, 0);
                cm.dispose();
}
}
}

