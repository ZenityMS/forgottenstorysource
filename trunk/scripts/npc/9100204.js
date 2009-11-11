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
        cm.sendOk("Ok, talk to me when your sure you want to #bSuper Rebirth#k."); 
        cm.dispose(); 

    }else{             
        if (mode == 1) 
            status++; 
        else 
            status--; 
         
        if (status == 0) { 
            cm.sendYesNo("I can give you the super RB glove! Do you want it?"); 
        }else if (status == 1) { 
            if(cm.getPlayer().getReborns() < 100) { 
                cm.sendOk("Sorry, You need to have Rebirthed 100 or more times."); 
                cm.dispose(); 
            } else { 
            if (cm.getPlayer().getReborns() >= 100) { 
                    cm.sendOk("#bCongratulations,#k you have qualified for the #rSuper Rebirth#k, please click Ok to continue.."); 
            } else { 
                cm.sendOk("You have not met the requirement of 100 reborns."); 
                cm.dispose(); 
            }         
            } // lets clean this up
         }else if (status == 2) {
       if (!cm.haveItem(1082233) && cm.getLevel() == 200) {
        cm.gainItem(1082233, 1); 
        // cm.unequipEverything(); could bug character
        cm.getPlayer().changeJob(net.sf.odinms.client.MapleJob.BEGINNER); 
        cm.getPlayer().setDex(4); 
        cm.getPlayer().setInt(4); 
        cm.getPlayer().setLuk(4);
        cm.getPlayer().setStr(4); 
        cm.getPlayer().setHp(30000); 
        cm.getPlayer().setMp(30000); 
        cm.getPlayer().setMaxHp(30000); 
        cm.getPlayer().setMaxMp(30000); 
        cm.getPlayer().setExp(0); 
        cm.getPlayer().setLevel(2); 
        cm.getPlayer().setReborns(cm.getPlayer().getReborns() - 100); 
		// cm.gainReborns(-100); i added an npc command called cm.change
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1082233, "str", 30000); //can i d
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1082233, "dex", 30000); 
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1082233, "luk", 30000); 
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1082233, "int", 30000);  
		// cm.getPlayer().getClient().getSession().close(); // this is the only way that the item will save!!
		 cm.reloadChar(); 
		 cm.getPlayer().saveToDb(true, true);  
		 cm.dispose(); 
		 } else {
		 cm.sendOk("You already have a #v1082233# or you are not level 200.");
		 cm.dispose();
}
        }    
} 
}  