var status = 0; 
var maps = Array(109040000, 280020000, 682000200, 103000900, 809050000, 101000100, 109030001, 610020000); 
var cost = Array(1, 1, 1, 1, 1, 1, 1, 1); 
var costBeginner = Array(1, 1, 1, 1, 1, 1, 1, 1); 
var selectedMap = -1; 
var job; 

importPackage(net.sf.odinms.client); 

function start() { 
    status = -1; 
    action(1, 0, 0); 
} 

function action(mode, type, selection) { 
    if (mode == -1) { 
        cm.dispose(); 
    } else { 
        if (status >= 2 && mode == 0) { 
            cm.sendOk("Alright, see you next time and have fun jqing!"); 
            cm.dispose(); 
            return; 
        } 
        if (mode == 1) 
            status++; 
        else 
            status--; 
        if (status == 0) { 
            cm.sendNext("Hi, #h #. I am incharge of jump quests. Would you like me to warp you to a jump quest? remember, every time you finish a jump quest you recive #d2#k jump points!"); 
        } else if (status == 1) { 
            cm.sendNextPrev("Alright then, I'll let you choose which jump quest.") 
        } else if (status == 2) { 
            var selStr = "Select your destination.#b"; 
            if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BEGINNER)) { 
                for (var i = 0; i < maps.length; i++) { 
                    selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + costBeginner[i] + " meso)#l"; 
                } 
            } else { 
                for (var i = 0; i < maps.length; i++) { 
                    selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + cost[i] + " meso)#l"; 
                } 
            } 
            cm.sendSimple(selStr); 
        } else if (status == 3) { 
            if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BEGINNER)) { 
                if (cm.getMeso() < costBeginner[selection]) { 
                    cm.sendOk("You do not have enough mesos.") 
                    cm.dispose(); 
                } else { 
                    cm.sendYesNo("Alright so you have nothing to do here? you want to leave #m" + maps[selection] + "#?"); 
                    selectedMap = selection; 
                } 
            } 
            else { 
                if (cm.getMeso() < cost[selection]) { 
                    cm.sendOk("You do not have enough mesos.") 
                    cm.dispose(); 
                } else { 
                    cm.sendYesNo("So you have nothing left to do here? Well suck my cock first! YAY feels good! Ok all set. Do you really wanna leave #m" + maps[selection] + "#?"); 
                    selectedMap = selection; 
                } 
            }         
        } else if (status == 4) { 
            if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BEGINNER)) { 
                cm.gainMeso(-costBeginner[selectedMap]); 
            } 
            else { 
                cm.gainMeso(-cost[selectedMap]); 
            } 
            cm.warp(maps[selectedMap], 0); 
            cm.dispose(); 
        } 
    } 
}  