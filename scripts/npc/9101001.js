var status = 0;

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
			cm.sendSimple("LOL hi I'm Chuck Norris! :D #k\r\nWhat do you want to do?\r\n#L50#Super Megaphones, Gachapon Tickets, Rocks, and Morphs#l");
		} else if (status == 1) {
			if (selection == 1) {
				cm.sendGetText("Who do you want to fame?");
				chicken = 1;							
			} else if (selection == 2) {
				cm.sendGetText("Who do you want to defame?");
				chicken = 2;
				}
				}
				}
				}
			