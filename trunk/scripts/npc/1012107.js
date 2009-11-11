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
            cm.sendYesNo("I can give you the super RB ring! Do you want it?"); 
        }else if (status == 1) { 
            if(cm.getPlayer().getReborns() < 100) { 
                cm.sendOk("Sorry, You need to have Rebirthed 100 or more times."); 
                cm.dispose(); 
            } else { 
            if (cm.getPlayer().getReborns() >= 100) { 
                    cm.sendOk("#bGood-Job#k, you have qualified for a #eSuper Rebirth#n. Check your inventory for a 30k stat item."); 
            } else { 
                cm.sendOk("LoL FaG G MeN in DA houSE"); 
                cm.dispose(); 
            }         
            } 
         }else if (status == 2) {
		 if (!cm.haveItem(1112900) && cm.getLevel() == 200) {
        cm.gainItem(1112900, 1); 
       //  cm.unequipEverything(); 
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
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1112900, "str", 30000); 
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1112900, "dex", 30000); 
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1112900, "luk", 30000); 
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1112900, "int", 30000);  
		cm.reloadChar();
		cm.getPlayer().saveToDb(true, true);
		} else {
		cm.sendOk("You already have a #v1112900# or you are not level 200.");
		}
		 cm.dispose(); 

        }    
} 
}  