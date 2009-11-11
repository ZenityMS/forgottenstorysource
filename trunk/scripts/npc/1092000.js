importPackage(net.sf.odinms.server);
importPackage(net.sf.odinms.client);

var status = 0;
var choices = Array ("5 passion points", "50 passion points", "75 passion points", "100 passion points", "150 passion points", "200 passion points");

var point5 = Array("2070018", "2340000");
var point5amt = Array("1", "1");

var point50 = Array("1912003", "1912004", "1902008", "1902009");
var point50amt = Array("1", "1", "1", "1");

var point75 = Array("1122000", "2041200");
var point75amt = Array("1", "1");

var point100 = Array("1002736", "1092022", "1002524");
var point100amt = Array("1", "1", "1");

var point150 = Array("1122014", "1002517", "1002518");
var point150amt = Array("1", "1", "1");

var point200 = Array("1082150", "2049100");
var point200amt = Array("1", "2000");

var kind = null;
var amount = 0;

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
		cm.sendNext("Hello, I'm the #bPassion point#k NPC. You currently have #b"+cm.getChar().getPassionPoints()+"#k Passion Points, would you like to spend them?");
	} else if (status == 1) {
		var insIt = "We have a wide variety of items for you to choose from! Take your pick!#b";
			for (var i = 0; i < choices.length; i++) {
				insIt += "\r\n#L" + i + "#" + choices[i] + "#l";
			}
			cm.sendSimple(insIt);
	} else if (status == 2) {
				cm.sendYesNo("So you want to browse through items worth " + choices[selection] + "?");
				selectedItem = selection;
	} else if (status == 3) {
	var insIt = "For #r" + choices[selectedItem] + "#k, I can give you your choice of the following items:. #b";
	switch(selectedItem) {
		case 0:
			for (i = 0; i < point5.length; i++) {
				insIt += "\r\n#L" + i + "##v"+point5[i]+"# #t" + point5[i] + "# ("+point5amt[i]+") #l";
			}
			kind = point5;
			amount = 5;
			break;
		case 1:
			for (i = 0; i < point50.length; i++) {
				insIt += "\r\n#L" + i + "##v"+point50[i]+"# #t" + point50[i] + "# ("+point50amt[i]+") #l";
			}
			kind = point50;
			amount = 50;
			break;
		case 2:
			for (i = 0; i < point75.length; i++) {
				insIt += "\r\n#L" + i + "##v"+point75[i]+"# #t" + point75[i] + "# ("+point75amt[i]+") #l";
			}
			kind = point75;
			amount = 75;
			break;
		case 3:
			for (i = 0; i < point100.length; i++) {
				insIt += "\r\n#L" + i + "##v"+point100[i]+"# #t" + point100[i] + "# ("+point100amt[i]+") #l";
			}
			kind = point100;
			amount = 100;
			break;
		case 4:
			for (i = 0; i < point150.length; i++) {
				insIt += "\r\n#L" + i + "##v"+point150[i]+"# #t" + point150[i] + "# ("+point150amt[i]+") #l";
			}
			kind = point150;
			amount = 150;
			break;
		case 5:
			for (i = 0; i < point200.length; i++) {
				insIt += "\r\n#L" + i + "##v"+point200[i]+"# #t" + point200[i] + "# ("+point200amt[i]+") #l";
			}
			kind = point200;
			amount = 200;
			break;
		default:
			cm.sendOk("How did you get here?");
			cm.dispose();
			return;
	}	
	cm.sendSimple(insIt);
	} else if (status == 4) {
			selectedItem = selection;
			cm.sendYesNo("Are you sure you want to purchase #t"+kind[selectedItem]+"#?");
	} else if (status == 5) {
		if (cm.getChar().getPassionPoints() >= amount) {		
			cm.getChar().setPassionPoints(cm.getChar().getPassionPoints() - amount);
			var amt = 0;
			switch (kind) {
				case point5:
					amt = point5amt;
					break;
				case point50:
					amt = point50amt;
					break;
				case point75:
					amt = point75amt;
					break;
				case point100:
					amt = point100amt;
					break;
				case point150:
					amt = point150amt;
					break;
				case point200:
					amt = point200amt;
					break;
			}
			if (isThrowingStar(kind[selectedItem])) {
				for (i=0; i<amt[selectedItem]; i++) {
					cm.gainItem(kind[selectedItem], 1);
				}
			} else {
			     var ii = MapleItemInformationProvider.getInstance();
                 var item = ii.getEquipById(kind[selectedItem]);
                 var type = ii.getInventoryType(kind[selectedItem]);
				 if (type.equals(MapleInventoryType.EQUIP)) {
				 cm.randomizeStats(kind[selectedItem], 30);
				 } else {
				 cm.gainItem(kind[selectedItem], amt[selectedItem]);
				}
			}
			cm.sendOk("You have successfully purchased #t"+kind[selectedItem]+"#! You now have #b"+cm.getChar().getPassionPoints()+"#k Donation Points after buying your item.");
			cm.dispose();
		} else {
			cm.sendOk("You don't have enough Passion Points! This item costs "+amount+" points. If you wish to purchase an item, please consider donating!");
			cm.dispose();
		}
	}
}	
}

function isThrowingStar(id) {
	return id >= 2070000 && id <= 2070018;
}