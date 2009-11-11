var status = 0;
var item;
var selected;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
	if (status == 1 && mode == 0) {
		cm.dispose();
		return;
	} else if (status == 2 && mode == 0) {
		cm.sendNext("It's not easy making " + item + ". Please get the materials ready.");
		cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if (status == 0) {
	cm.sendYesNo("Hello #b" + cm.getPlayer().getName() + ",#k I'm #gT-1337#k. I sell special summonings bags that can spawn silver slimes and cube slimes. You can buy one of these summonings bags for #b10 #v4001126##k. So, would you like to buy a #bSummoning Bag?#k"); 
	} else if (status == 1) {
	cm.sendOk("Are you sure you want to spend #r10 #v4001126# to buy a #e#bspecial summoning bag?");
	} else if (status == 2) {
	if (cm.haveItem(4001126, 10)) {
	cm.gainItem(4001126, -10);
	cm.gainItem(2101072);
	cm.dispose();
	} else {
	cm.sendOk("You don't have 10 maple leaves");
	cm.dispose();
	}
	}
	}
	}