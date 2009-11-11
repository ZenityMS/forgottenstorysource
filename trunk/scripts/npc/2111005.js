/*@author Jvlaple
  *Crystal of Roots
  */

var status = 0;
var PQItems = Array(4001087, 4001088, 4001089, 4001090, 4001091, 4001092, 4001093);

importPackage(net.sf.odinms.client);

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status >= 0 && mode == 0) {
            cm.sendOk("Ok, keep preservering!");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
            if (status == 0 ) {
                cm.sendYesNo("Hello I'm the Dungeon Exit NPC. Do you wish to go out from here?");
            } else if (status == 1) {
                var eim = cm.getPlayer().getEventInstance();
                cm.warp(100000000, 0);
                if (eim != null) {
                    eim.unregisterPlayer(cm.getPlayer());
                }cm.dispose();
            
        }
    }
}	