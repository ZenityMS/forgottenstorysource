/* Made by Rich */

importPackage(net.sf.odinms.server);

var status = 0;


function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {

         
         if (mode == -1) {
        cm.dispose();
    
    }else if (mode == 0){
        cm.sendOk ("#eOk, talk to me when you want to Super Scroll.");
        cm.dispose();

    }else{             
        if (mode == 1)
            status++;
        else
            status--;
        
        if (status == 0) {
        reborns = cm.getChar().getReborns();
        cm.sendOk ("#b#eHello, I Super Scroll Level 110 items for users who have 5+ Reborns. \r\n\r\n\r\n #r#e You currently have "+ reborns +" reborns. \r\n This will check to see if you have enough Inventory Slots.");
        }else if (status == 1) { 
          cm.sendSimple ("Click a Level 110 Item from the list.#e#d" +
                 "#k\r\n#L0##r#v1472051# - Super Scrolls to 150 WATK" +
                 "#k\r\n#L1##r#v1472052# - Super Scrolls to 150 WATK" +
                 "#k\r\n#L2##r#v1382036# - Super Scrolls to 350 MATK" +
                 "#k\r\n#L3##r#v1332050# - Super Scrolls to 250 WATK" +
                 "#k\r\n#L4##r#v1452044# - Super Scrolls to 225 WATK" +
                 "#k\r\n#L5##r#v1322052# - Super Scrolls to 250 WATK" +
                 "#k\r\n#L6##r#v1462039# - Super Scrolls to 200 WATK" +
                 "#k\r\n#L7##r#v1312031# - Super Scrolls to 250 WATK" +
                 "#k\r\n#L8##r#v1302059# - Super Scrolls to 250 WATK" +
                 "#k\r\n#L9##r#v1402036# - Super Scrolls to 300 WATK" +
                 "#k\r\n#L10##r#v1422028# - Super Scrolls to 260 WATK" +
                 "#k\r\n#L11##r#v1412026# - Super Scrolls to 260 WATK" +
                 "#k\r\n#L12##r#v1432038# - Super Scrolls to 270 WATK" +
                 "#k\r\n#L13##r#v1442045# - Super Scrolls to 270 WATK" +
                 "#k\r\n#L14##r#v1492013# - Super Scrolls to 250 WATK" +
                 "#k\r\n#L15##r#v1482013# - Super Scrolls to 270 WATK" +
                 "#k\r\n#L16##r#v1482023# - Super Scrolls to 280 WATK");
            } else if (selection == 0) {  
            if(cm.getPlayer().getReborns() > 4 && !cm.haveItem(1472051) && cm.canHold(1472051)) {
            cm.gainReborn (-5);
            cm.gainItem (1472051);
            net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1472051, "watk", 150);
            cm.reloadChar();
            cm.dispose();
               } else {
             cm.sendOk ("You must have 5 Reborns to use me. Or you already have the item. Or you don't have enough space in your Inventory");
             cm.dispose();
             }
            } else if (selection == 1) {  
            if(cm.getPlayer().getReborns() > 4 && !cm.haveItem(1472052) && cm.canHold(1472051)) {
            cm.gainReborn (-5);
            cm.gainItem (1472052);
            net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1472052, "watk", 150);
            cm.reloadChar();
            cm.dispose();
               } else {
             cm.sendOk ("You must have 5 Reborns to use me. Or you already have the item.");
             cm.dispose();
             }
            } else if (selection == 2) {  
            if(cm.getPlayer().getReborns() > 4 && !cm.haveItem(1382036) && cm.canHold(1472051)) {
            cm.gainReborn (-5);
            cm.gainItem (1382036);
            net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1382036, "matk", 350);
            cm.reloadChar();
            cm.dispose();
               } else {
             cm.sendOk ("You must have 5 Reborns to use me. Or you already have the item. Or you don't have enough space in your Inventory");
             cm.dispose();
             }
            } else if (selection == 3) {  
            if(cm.getPlayer().getReborns() > 4 && !cm.haveItem(1332050) && cm.canHold(1472051)) {
            cm.gainReborn (-5);
            cm.gainItem (1332050);
            net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1332050, "watk", 250);
            cm.reloadChar();
            cm.dispose();
               } else {
             cm.sendOk ("You must have 5 Reborns to use me. Or you already have the item. Or you don't have enough space in your Inventory");
             cm.dispose();
             }
            } else if (selection == 4) {  
            if(cm.getPlayer().getReborns() > 4 && !cm.haveItem(1452044) && cm.canHold(1472051)) {
            cm.gainReborn (-5);
            cm.gainItem (1452044);
            net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1452044, "watk", 225);
            cm.reloadChar();
            cm.dispose();
               } else {
             cm.sendOk ("You must have 5 Reborns to use me. Or you already have the item. Or you don't have enough space in your Inventory");
             cm.dispose();
             }
            } else if (selection == 5) {  
            if(cm.getPlayer().getReborns() > 4 && !cm.haveItem(1322052) && cm.canHold(1472051)) {
            cm.gainReborn (-5);
            cm.gainItem (1322052);
            net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1322052, "watk", 250);
            cm.reloadChar();
            cm.dispose();
               } else {
             cm.sendOk ("You must have 5 Reborns to use me. Or you already have the item. Or you don't have enough space in your Inventory");
             cm.dispose();
             }
            } else if (selection == 6) {  
            if(cm.getPlayer().getReborns() > 4 && !cm.haveItem(1462039) && cm.canHold(1472051)) {
            cm.gainReborn (-5);
            cm.gainItem (1462039);
            net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1462039, "watk", 200);
            cm.reloadChar();
            cm.dispose();
               } else {
             cm.sendOk ("You must have 5 Reborns to use me. Or you already have the item. Or you don't have enough space in your Inventory");
             cm.dispose();
             }
            } else if (selection == 7) {  
            if(cm.getPlayer().getReborns() > 4 && !cm.haveItem(1312031) && cm.canHold(1472051)) {
            cm.gainReborn (-5);
            cm.gainItem (1312031);
            net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1312031, "watk", 250);
            cm.reloadChar();
            cm.dispose();
               } else {
             cm.sendOk ("You must have 5 Reborns to use me. Or you already have the item. Or you don't have enough space in your Inventory");
             cm.dispose();
             }
            } else if (selection == 8) {  
            if(cm.getPlayer().getReborns() > 4 && !cm.haveItem(1302059) && cm.canHold(1472051)) {
            cm.gainReborn (-5);
            cm.gainItem (1302059);
            net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1302059, "watk", 250);
            cm.reloadChar();
            cm.dispose();
               } else {
             cm.sendOk ("You must have 5 Reborns to use me. Or you already have the item. Or you don't have enough space in your Inventory");
             cm.dispose();
             }
            } else if (selection == 9) {  
            if(cm.getPlayer().getReborns() > 4 && !cm.haveItem(1402036) && cm.canHold(1472051)) {
            cm.gainReborn (-5);
            cm.gainItem (1402036);
            net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1402036, "watk", 300);
            cm.reloadChar();
            cm.dispose();
               } else {
             cm.sendOk ("You must have 5 Reborns to use me. Or you already have the item. Or you don't have enough space in your Inventory");
             cm.dispose();
             }
            } else if (selection == 10) {  
            if(cm.getPlayer().getReborns() > 4 && !cm.haveItem(1422028) && cm.canHold(1472051)) {
            cm.gainReborn (-5);
            cm.gainItem (1422028);
            net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1422028, "watk", 260);
            cm.reloadChar();
            cm.dispose();
               } else {
             cm.sendOk ("You must have 5 Reborns to use me. Or you already have the item. Or you don't have enough space in your Inventory");
             cm.dispose();
             }
            } else if (selection == 11) {  
            if(cm.getPlayer().getReborns() > 4 && !cm.haveItem(1412026) && cm.canHold(1472051)) {
            cm.gainReborn (-5);
            cm.gainItem (1412026);
            net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1412026, "watk", 260);
            cm.reloadChar();
            cm.dispose();
               } else {
             cm.sendOk ("You must have 5 Reborns to use me. Or you already have the item. Or you don't have enough space in your Inventory");
             cm.dispose();
             }
            } else if (selection == 12) {  
            if(cm.getPlayer().getReborns() > 4 && !cm.haveItem(1432038) && cm.canHold(1472051)) {
            cm.gainReborn (-5);
            cm.gainItem (1432038);
            net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1432038, "watk", 270);
            cm.reloadChar();
            cm.dispose();
               } else {
             cm.sendOk ("You must have 5 Reborns to use me. Or you already have the item. Or you don't have enough space in your Inventory");
             cm.dispose();
             }
            } else if (selection == 13) {  
            if(cm.getPlayer().getReborns() > 4 && !cm.haveItem(1442045) && cm.canHold(1472051)) {
            cm.gainReborn (-5);
            cm.gainItem (1442045);
            net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1442045, "watk", 270);
            cm.reloadChar();
            cm.dispose();
               } else {
             cm.sendOk ("You must have 5 Reborns to use me. Or you already have the item. Or you don't have enough space in your Inventory");
             cm.dispose();
             }
            } else if (selection == 14) {  
            if(cm.getPlayer().getReborns() > 4 && !cm.haveItem(1492013) && cm.canHold(1472051)) {
            cm.gainReborn (-5);
            cm.gainItem (1492013);
            net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1492013, "watk", 250);
            cm.reloadChar();
            cm.dispose();
               } else {
             cm.sendOk ("You must have 5 Reborns to use me. Or you already have the item. Or you don't have enough space in your Inventory");
             cm.dispose();
             }
            } else if (selection == 15) {  
            if(cm.getPlayer().getReborns() > 4 && !cm.haveItem(1482013) && cm.canHold(1472051)) {
            cm.gainReborn (-5);
            cm.gainItem (1482013);
            net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1482013, "watk", 270);
            cm.reloadChar();
            cm.dispose();
               } else {
             cm.sendOk ("You must have 5 Reborns to use me. Or you already have the item. Or you don't have enough space in your Inventory");
             cm.dispose();
             }
            } else if (selection == 16) {  
            if(cm.getPlayer().getReborns() > 4 && !cm.haveItem(1482023) && cm.canHold(1472051)) {
            cm.gainReborn (-5);
            cm.gainItem (1482023);
            net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1482023, "watk", 280);
            cm.reloadChar();
            cm.dispose();
               } else {
             cm.sendOk ("You must have 5 Reborns to use me. Or you already have the item. Or you don't have enough space in your Inventory");
             cm.dispose();
             }
}
}
}