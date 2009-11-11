/* Author: Rich
   Function: Charm Necklaces
*/
importPackage(java.util);
importPackage(net.sf.odinms.client);
importPackage(net.sf.odinms.server);
importPackage(net.sf.odinms.net.channel);
importPackage(net.sf.odinms.tools);
importPackage(net.sf.odinms.scripting.npc);

var status = 0;
var stats = new Array(25, 25, 25, 25, 50);
var types2 = new Array(1, 2, 3, 4, 5);
var items = new Array (4000008, 4000093, 4000261, 4031059, 4032031, 4140903);

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
		   cm.sendSimple("Hello #b" + cm.getPlayer().getName() + ",#k I'm Professor Foxwit! \r\n Recently I have invented a  new necklace. It's called a #rCharm Necklace#k Each charm can add a special effect to your necklace so that you will be stronger! The charms can increase things like Luk, Int, Str, Dex, and even weapon attack. You can obtain these charms by doing certain things in #bMapleZtory#k! Some players may be able to help you find out how to get the charms. So what do you want me to do for you?" +
                 "#k\r\n#L0#Please start me off with a regular necklace." +
                 "#k\r\n#L1#What charms are there?" +
				 "#k\r\n#L2#I'm here for you to put charms on my necklace.");
				 } else if (selection == 0) {
				if (!cm.haveItem(1122007)) { // we might need to add a column in the database. or maybe we will allow users to make multiple necklaces?
				 cm.gainItem(1122007, 1);
				 cm.sendOk("You have been started out with my friend Spingelman's necklace. It looks like this: #v1122007# \r\n Please take good care of it. When you find a charm in the game you can bring it to me to put it on your necklace.");
				 } else {
				 cm.sendOk("You already have a necklace! Please don't try to steal from me!");
				 }
				 } else if (selection == 1) {
				 cm.sendOk("The following charms can be found:#b\r\n #v4000008# - 100 Str " +
				 "\r\n#v4000093# - 100 Dex" +
				 "\r\n#v4000261# - 100 Int" +
				 "\r\n#v4031059# - 100 Luk" +
				 "\r\n#v4032031# - 50 Weapon Attack" +
				 "\r\n#v4140903# - 50 Magic Attack" +
				 "\r\n #rPlease note that more charms may be added in the future");
				} else if (selection == 2) {
				cm.sendSimple("#rPUT YOUR NECKLACE YOU WANT TO USE AS THE FIRST EQUIP IN YOUR INVENTORY.\r\n Please pick a charm: #k\r\n#L10##v4000008# - 100 Str " +
				 "#k\r\n#L11##v4000093# - 100 Dex" +
				 "#k\r\n#L12##v4000261# - 100 Int" +
				 "#k\r\n#L13##v4031059# - 100 Luk" +
				 "#k\r\n#L14##v4032031# - 50 Weapon Attack" +
				 "#k\r\n#L15##v4140903# - 50 Magic Attack");
				 status = 1000;
				 
				 } else if(status == 1001) {
				 var formula = selection;
				 var items = new Array (4000008, 4000093, 4000261, 4031059, 4032031, 4140903);
				 var stats = new Array(100, 100, 100, 100, 50, 50);
				 var maxes = new Array(400, 400, 400, 400, 200, 200);
                 var types2 = new Array(1, 2, 3, 4, 5);
			     var lol = cm.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(1);
                 var types = new Array(lol.getStr(), lol.getDex(), lol.getInt(), lol.getLuk(), lol.getWatk(), lol.getMatk());
				 var types3 = new Array ("str", "dex", "luk", "int", "watk", "matk");
				 if (lol.getItemId() == 1122007 && types[selection - 10] < maxes[selection - 10] && cm.haveItem(items[selection - 10], 1)) { // does anybody know a way to get the slot by itemid?
				 cm.gainItem(items[selection - 10], -1);
				 var slot = 1;
				// cm.changeStat(slot, 1, 1);
				// net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1122007, types3, stats[selection - 10]);
				net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1122007, types3[selection - 10], types[selection - 10] + stats[selection - 10]);
				 cm.reloadChar();
				 cm.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(1).setOwner(cm.getPlayer().getName());
				 cm.sendOk("Done. Please enjoy!");		
				 cm.gainItem(items[selection - 10], -1);				 
				 } else {
				 cm.sendOk("Please make sure that your necklace is the first equip in your inventory and that you have the #v" + items[selection - 10] + "# charm. Or you already have this charm on your necklace.");
				 } 
				 }
				 }
				 }