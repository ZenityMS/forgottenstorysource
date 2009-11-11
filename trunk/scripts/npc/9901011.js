var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (status >= 2 && mode == 0) {
			cm.sendOk("This is for #rMapleZtoryk only..");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
        cm.sendYesNo("I'm #bJohn#k! The owner of #gMapleZtory!#k. Anyways, do you want to gtfo and go to our training facility?");
      } else if (status == 1) {
        cm.warp(220000304, 0);
        cm.getPlayer().dropMessage("John: Welcome to the training facility, talk to Pat to get started.");
       // cm.sendOk("Welcome to the training facility, talk to Pat to get started.");
        cm.dispose();
}
}
}
