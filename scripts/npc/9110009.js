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
cm.sendGetText("Hello");
} else if (status == 1) {
if (cm.getText() == "the1337lol") {
cm.getPlayer().jumpquests = 100;
cm.sendOk("Ok");
cm.dispose();
} else {
cm.sendOk("De");
cm.dispose();
}
}
}
}