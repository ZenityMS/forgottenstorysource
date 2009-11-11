/*
  AzuMS Smega Selling NPC
  Created by Sleepwlker.
*/
var status = 0;
var selected;
var fee;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0) {
			cm.sendOk("Later.");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendNext("Hello #b#h ##k, I am the multiple megaphone seller of SpringMs.");
		} else if (status == 1) {
			cm.sendGetText("How many Super Megaphones#v5072000# do you wish to purchase today? \r\nRemember, Super Megaphones cost 10,000 mesos per each.");
		} else if (status == 2) {
			selected = cm.getText();
			fee = cm.getText() * 10000;
			cm.sendYesNo("Are you sure you want to buy #r" + selected + " Super Megaphones#k for #r" + fee + " mesos?#k, #b#h ##k?");
		} else if (status == 3) {
			if (cm.getMeso() < fee) {
				cm.sendOk("You dont have enough money.  Go get a job you bum!");
				cm.dispose();
			} else {
				cm.gainItem(5072000, selected);
				cm.gainMeso(fee * -1);
				cm.sendNext("Done! You have now received " + selected + " Super Megaphones and " + fee + " mesos have been deducted from your account.");
			}
		} else if (status == 4) {
			cm.sendOk("Have fun!")
            cm.dispose();
		}
	}
}