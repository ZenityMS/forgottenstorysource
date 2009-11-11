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
            cm.sendYesNo("Welcome, Unholy hero. Would you like to Super Rebirth? What will happen is that you Character will be reset and you will be given an item with Max Stats on it. It costs 100 Rebirths! So, do you want to Super Rebirth?"); 
        }else if (status == 1) { 
            if(cm.getPlayer().getReborns() < 100) { 
                cm.sendOk("Sorry, You need to have Rebirthed 100 or more times."); 
                cm.dispose(); 
            } else { 
            if (!cm.hasInventorySpace(cm.getPlayer(), net.sf.odinms.client.MapleInventoryType.EQUIP)) { 
                    cm.sendOk("#bGood-Job#k, you have qualified for a #eSuper Rebirth#n."); 
            } else { 
                cm.sendOk("You do not have enough space in your inventory. Please have at least 24 open slots."); 
                cm.dispose(); 
            }         
            } 
         }else if (status == 2) { 
        // Give item 
        cm.gainItem(1032033, 1); 

        // Meow 
            cm.unequipEverything(); 

        // Reset to default 
            cm.getPlayer().changeJob(net.sf.odinms.client.MapleJob.BEGINNER); 
        cm.getPlayer().setDex(4); 
        cm.getPlayer().setInt(4); 
        cm.getPlayer().setLuk(4); 
        cm.getPlayer().setStr(4); 
        cm.getPlayer().setHp(50); 
        cm.getPlayer().setMp(50); 
        cm.getPlayer().setMaxHp(50); 
        cm.getPlayer().setMaxMp(50); 
        cm.getPlayer().setRemainingAp(16); 
        cm.getPlayer().setExp(0); 
        cm.getPlayer().setLevel(2); 
        cm.getPlayer().setReborns(0); 
        cm.getPlayer().setKeymap(net.sf.odinms.client.MapleCharacter.getDefault(cm.getPlayer().getClient()).getKeymap()); 

        // Edit Item stats 
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032033, "str", 30000); 
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032033, "dex", 30000); 
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032033, "luk", 30000); 
        net.sf.odinms.server.MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, 1032033, "int", 30000); 

         
        // Clean up 
        cm.getPlayer().getClient().getSession().close(); 
            cm.dispose(); 

        }    
} 
}  