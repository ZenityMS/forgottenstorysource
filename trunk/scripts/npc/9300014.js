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
            cm.sendYesNo("Yo, wuttup b? I'm Matt. lololol. anyway homes, ill do a lil favor for you. You just press yes and ill see if you can get a super RB, k?"); 
        }else if (status == 1) { 
            if(cm.getPlayer().getReborns() < 100) { 
                cm.sendOk("Sorry, You need to have Rebirthed 100 or more times."); 
                cm.dispose(); 
            } else { 
            if (cm.getPlayer().getReborns() >= 100) { 
                    cm.sendOk("#bGood-Job#k, you have qualified for a #eSuper Rebirth#n. The first White Undershirt in your inventory will be godly."); 
            } else { 
                cm.sendOk("LoL FaG G MeN in DA houSE"); 
                cm.dispose(); 
            }         
            } 
         }else if (status == 2) {
		 if (!cm.haveItem(1032033) && cm.getLevel() == 200) {
        cm.gainItem(1032033, 1); 
        // cm.unequipEverything(); 
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
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032033, "str", 30000); 
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032033, "dex", 30000); 
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032033, "luk", 30000); 
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032033, "int", 30000);  
		// cm.getPlayer().getClient().getSession().close(); // this is the only way that the item will save!!
		 cm.getPlayer().saveToDb(true, true);
		 cm.reloadChar();
		 } else {
		 cm.sendOk("You already have a #v1032033# or you are not level 200.");
		 }
		 cm.dispose(); 

        }    
} 
}  