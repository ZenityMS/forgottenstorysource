var status = 0;
var choices = Array("Timeless Weapons", "Raven Weapons");

var timeless = Array("1302081", "1312037", "1322060", "1322046", "1412033", "1422037", "1482023");
var tprices = Array("1", "1", "1", "1", "1", "1", "1");

var raven = Array("1332077", "1332078", "1332079", "1332080", "1402048", "1402049", "1402050", "1402051", "1462052", "1462053", "1462054", "1462055", "1472072", "1472073", "1472074", "1472075");
var rprices = Array("1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1");

var kind = null;
var amt = null;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
if (mode == -1) {
	cm.dispose();
} else {
	if (mode == 0 && status >= 2) {
		cm.sendOk("Come back soon!");
		cm.dispose();
		return;
	}
if (mode == 1)
	status++;
else
	cm.dispose();

	if (status == 0) {
                if(cm.getPlayer().getStrikePrompt() != true) {
		if (cm.getChar().getJumpPoints() < 1) {
			cm.sendOk("You should probably get some #bJump Points#k before talking to me...");
			cm.dispose();
		} else {
			cm.sendOk("Wow, I'm impressed! You have #b"+cm.getChar().getJumpPoints()+"#k jump points! You know what you can do with those? You can exhange them for stuff.");
		}
} else {
cm.sendOk ("You now have #r" + cm.getPlayer().getStrike() + "#k strikes. I would cease what you are doing. \r\n #r#e After three strikes you will be banned.");
cm.getPlayer().setStrikePrompt(false); // So they don't see the NPC unless they get another strike
cm.disose();
}
	} else if (status == 1) {
		var insIt = "What type of item are you interested in? #b";
		for (var i = 0; i < choices.length; i++) {
			insIt += "\r\n#L" + i + "#" + choices[i] + "#l";
		}
		cm.sendSimple(insIt);
	} else if (status == 2) {
		cm.sendYesNo("So you want to browse through "+ choices[selection] +"?");
		selectedItem = selection;
	} else if (status == 3) {
	var insIt = "Well, this is what we have in stock. #b";
	switch(selectedItem) {
		case 0:
			for (i = 0; i < timeless.length; i++) {
				insIt += "\r\n#L" + i + "##v"+timeless[i]+"# #t" + timeless[i] + "# ("+tprices[i]+" points) #l";
			}
			kind = timeless;
			break;
		case 1:
			for (i = 0; i < raven.length; i++) {
				insIt += "\r\n#L" + i + "##v"+raven[i]+"# #t" + raven[i] + "# ("+rprices[i]+" points) #l";
			}
			kind = raven;
			break;
		default:
			cm.sendOk("How did you get here?");
			cm.dispose();
			return;
	}	
	cm.sendSimple(insIt);
	} else if (status == 4) {
		selectedItem = selection;
		switch (kind) {
			case timeless:
				amt = tprices;
				break;
			case raven:
				amt = rprices;
				break;
		}
		cm.sendYesNo("Are you sure you want to purchase #t"+kind[selectedItem]+"#?");
	} else if (status == 5) {
		if (cm.getChar().getJumpPoints() >= amt[selectedItem]) {		
			cm.getChar().setJumpPoints(cm.getChar().getJumpPoints() - amt[selectedItem]);			
			cm.gainItem(kind[selectedItem], amt[selectedItem]);			
			cm.sendOk("You have successfully purchased #t"+kind[selectedItem]+"#! You have "+cm.getChar().getJumpPoints()+" points left.");
			cm.dispose();
		} else {
			cm.sendOk("Sorry, you can't afford the goods.");
			cm.dispose();
		}
	}
}	
}