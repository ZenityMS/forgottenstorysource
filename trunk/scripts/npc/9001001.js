function start() {
	cm.sendSimple ("Hi! your deep in Coke Mountain territory! What would you like to do?\r\n#L0#Take me to the next map!#l\r\n#L1#I want to leave... I'm scared shitless!#l\r\n");
}

function action(mode, type, selection) {
	cm.dispose();
	if (selection == 0) {
		cm.warp(211040401);
		cm.dispose();
	} else if (selection == 1) {
		cm.warp(211000000);
	} else if (selection == 2) {
		cm.sendOk("The Deepest part of Coke Mountain is under construction. Please check back tomorrrow.");
	} else {
		cm.dispose();
	}
}