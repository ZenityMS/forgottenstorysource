/**
Made by Rich of MapleZtory
Function: Warping to the Pumpkin Event
**/

importPackage(net.sf.odinms.server);

var status = 0;
var on = false;


function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {

         
         if (mode < -1) {
        cm.dispose();
    
    }else if (mode == 0){
        cm.sendOk ("I swear I'm coming to your house.");
        cm.dispose();
    }else{             
        if (mode == 1)
            status++;
        else
            status--;
        
        if (status == 0) {		
		cm.sendYesNo("HI #b" + cm.getPlayer().getName() + "!#k \r\n I was put in jail for killing noobs who wouldn't say yes to my questions :) .. \r\n Well there is a boss in MapleZtory named the Pumpkin Knight! He holds the #bBlack Charm#k, this charm can be put on a charm necklace to increase the stats on your necklace. Do you want to try and stop this monster?");
		} else if (status == 1) {
		cm.warp(682000304, 0);
		cm.getPlayer().dropMessage(1, "New Note: Good luck getting yourself killed. Sincerely, Tyre.");
		cm.dispose();
		}
		}
		}
		
		