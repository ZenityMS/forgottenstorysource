/*
Stat Trading
Russellon
Made by Rich
 */
importPackage(net.sf.odinms.client);
importPackage(net.sf.odinms.server);
importPackage(net.sf.odinms.net);

var status = 0;
var wui = 0;
var jobName;
var job;

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
// cm.getPlayer().getClient().getChannelServer().xtremeEvent = false;
	cm.sendSimple ("Pick a Category#e#d " + cm.getPlayer().getClient().getChannelServer().isXtremeEvent() + " " +
                 "#k\r\n#L80##r15 Rebirth Shop" +
                 "#k\r\n#L81##r5 Rebirth Shop#l \r\n\r\n #k#e You currently have " +cm.getPlayer().getReborns() + " Reborns\r\n\r\n#b#e NPC Made by Rich. More will be added soon :)");
            
            } else if (selection == 80) { 
                cm.sendSimple ("Hi, what would you like to buy from me? \r\n #r#eYou will not lose rebirths when buying! Make sure you have enough room in your Inventory!#e#d" +
                 "#k\r\n#L0##bReg GM Job. (Not SuperGM) - 10 #v4001126#" +
                 "#k\r\n#L1##b3K stat Earrings - 3 #v4001126# " +
                 "#k\r\n#L2##b5K stat Earrings - 5 #v4001126# " +
                 "#k\r\n\r\n#L3##rChange back to beginner from GM Job");

            } else if (selection == 0) {    
                if (cm.getPlayer().getReborns() > 14 && cm.haveItem(4001126, 10)) {
                    cm.gainItem (4001126, -10);
                    cm.changeJob(MapleJob.GM);
                    cm.sendOk ("You now have Reg. GM Skills!!");
                    cm.dispose();
                } else {
                    cm.sendOk ("#r#eYou don't have enough #v4001126# or you don't have enough reborns!!");
                    cm.dispose(); 
                    }
            } else if (selection == 1) {  
                if (cm.getPlayer().getReborns() > 14 && !cm.haveItem(1032034) && cm.haveItem(4001126, 3)) {
         cm.gainItem (1032036);
         cm.gainItem (4001126, -3);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032036, "str", 3000);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032036, "dex", 3000);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032036, "luk", 3000);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032036, "int", 3000);  
        cm.reloadChar();
        cm.dispose();
         } else {
        cm.sendOk ("You don't have the Required ammount of Reborns, you already have the Item, or you don't have 3 #v4001126#");
        cm.dispose();
}
            } else if (selection == 2) {  
                if (cm.getPlayer().getReborns() > 14 && !cm.haveItem(1032034) && cm.haveItem(4001126, 5)) {
         cm.gainItem (4001126, -5);
         cm.gainItem (1032036);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032036, "str", 5000);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032036, "dex", 5000);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032036, "luk", 5000);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032036, "int", 5000);  
        cm.reloadChar();
        cm.dispose();
         } else {
        cm.sendOk ("You don't have the Required ammount of Reborns, you already have the Item, or you don't have 5 #v4001126#");
        cm.dispose();
}
            } else if (selection == 81) { 
                cm.sendSimple ("Hi, what would you like to buy from me? \r\n #r#eYou will not lose rebirths when buying! Make sure you have enough room in your Inventory!#e#d" +
                 "#k\r\n#L4##bBuy a 500 Stat Ring - 1 #v4001126#" +
                 "#k\r\n#L5##b1K stat Earrings - 2 #v4001126# " +
                 "#k\r\n#L6##b3K stat Earrings - 3 #v4001126#");
            } else if (selection == 4) { 
                if (cm.getPlayer().getReborns() > 4 && !cm.haveItem(1032038) && cm.haveItem(4001126, 1)) {
         cm.gainItem (4001126, -1);
         cm.gainItem (1032038);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032038, "str", 500);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032038, "dex", 500);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032038, "luk", 500);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032038, "int", 500);  
        cm.reloadChar();
        cm.dispose();
         } else {
        cm.sendOk ("You don't have the Required ammount of Reborns, you already have the Item, or you don't have 3 #v4001126#");
        cm.dispose();
}
            } else if (selection == 5) {
                if (cm.getPlayer().getReborns() > 4 && !cm.haveItem(1032039) && cm.haveItem(4001126, 2)) {
         cm.gainItem (4001126, -2);
         cm.gainItem (1032039);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032039, "str", 1000);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032039, "dex", 1000);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032039, "luk", 1000);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032039, "int", 1000);  
        cm.reloadChar();
        cm.dispose();
         } else {
        cm.sendOk ("You don't have the Required ammount of Reborns, you already have the Item, or you don't have 2 #v4001126#");
        cm.dispose();
}
            } else if (selection == 6) {
                if (cm.getPlayer().getReborns() > 4 && !cm.haveItem(1032027) && cm.haveItem(4001126, 3)) {
         cm.gainItem (4001126, -3);
         cm.gainItem (1032027);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032027, "str", 3000);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032027, "dex", 3000);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032027, "luk", 3000);
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032027, "int", 3000);   
        cm.reloadChar();
        cm.dispose();
         } else {
        cm.sendOk ("You don't have the Required ammount of Reborns, you already have the Item, or you don't have 3 #v4001126#");
        cm.dispose();
}

            } else if (selection == 3) {  
           if (cm.getJob().equals(net.sf.odinms.client.MapleJob.GM)) {
                    cm.changeJob(MapleJob.BEGINNER);
                    cm.sendOk ("You are now a beginner again!");     
                    cm.dispose();
                  } else {
                    cm.sendOk ("You do not have the GM Job..");
                    cm.dispose();
}
}
}
}




