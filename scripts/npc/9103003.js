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
   			mode++;
			status++;
			status++;
   			return;
  		}
		if (mode == 1)
   			status++;
  		else
   			status--;
  		if (status == 0) {
			if (cm.getChar().getState() == 0) {
				cm.sendOk("Now for your #rHair color#k!");
			} else if (cm.getChar().getState() == 1) {
				cm.sendOk("Now go and choose your #rSkin color#k");
			} else if (cm.getChar().getState() == 2) {
				cm.sendOk("#rNow choose your eyes#k");
			} else if (cm.getChar().getState() == 3) {
				cm.sendOk("Now choose your eye color#k");
			} else {
				cm.sendOk("And now I will warp you to henesys to start your journy, please remember to check #r@commands#k to see your player commands.");
			}
		} else {
			if (cm.getChar().getState() == 0) {
				cm.warp(809050016);
				cm.getChar().setState(1);
				cm.dispose();
			} else if (cm.getChar().getState() == 1) {
				cm.warp(809050016);
				cm.getChar().setState(2);
				cm.dispose();
			} else if (cm.getChar().getState() == 2) {
				cm.warp(809050016);
				cm.getChar().setState(3);
				cm.dispose();
			} else if (cm.getChar().getState() == 3) {
				cm.warp(809050016);
				cm.getChar().setState(4);
				cm.dispose();
			} else {
				cm.warp(100000000);
				cm.getChar().setState(0);
				cm.dispose();
			}
		}
	}
}