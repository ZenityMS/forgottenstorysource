//Author: Moogra
var status = 0;
var map = Array(240010501);

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0 && status == 0)
            cm.dispose();
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendSimple("\r\n#L1# I would like to trade my #b#v4001126##k for 1 billion mesos!#l\r\n\#L2# I would like to exchange 1 billion mesos for a #b#v4001126##k\r\n\#L3# I would like to trade my  #b#v4031183##k for 100 Million Mesos!\r\n\#L4# I would like to exchange 100 Million mesos for a #b#v4031183##k!\r\n\#L5# I would like to exchange 200,000,000 for an #v4001040##l");
        } else if (status == 1) {
            if (selection == 1) {
                if(cm.haveItem(4001126) && cm.getMeso() <= 1000000000) {             
                    cm.gainMeso(1000000000);
                    cm.gainItem(4001126,-1);
                    cm.sendOk("Thank you for your mesos!");
                } else
                    cm.sendOk("Sorry, you don't have a #b#v4001126##k or you have over 1 billion mesos already.!");
                    cm.dispose();
               
            } else if (selection == 2) {
                if(cm.getMeso() >= 1000000000) {
                    cm.gainMeso(-1000000000);
                    cm.gainItem(4001126,1);
                    cm.sendOk("Thank you for your item!");
                } else {
                    cm.sendOk("Sorry, you don't have enough mesos!");
                cm.dispose();
}
            } else if (selection == 3) {
                if(cm.haveItem(4031183)) {
                    cm.gainMeso(100000000);
                    cm.gainItem(4031183,-1);
                    cm.sendOk("Thank you for your item!");
                    cm.dispose();
                } else {
                    cm.sendOk("Sorry, you don't have a #b#v4001126##k!");
                cm.dispose();
            
   }
            } else if (selection == 4) {
                if(cm.getMeso() >= 100000000) {
                    cm.gainMeso(-100000000);
                    cm.gainItem(4031183,1);
                    cm.sendOk("Thank you for your mesos!");
                } else {
                    cm.sendOk("Sorry, you don't have enough mesos!");
                cm.dispose();
}
            } else if (selection == 5) {
                cm.sendGetText("How many Lupin Erasers do you want?");
                status = 1336;
}
            } else if (status == 1337) {
               if (cm.getPlayer().getMeso() >= 200000000 * cm.getText()) {
                cm.gainMeso(-200000000 * cm.getText());
                cm.gainItem(4001040, cm.getText());
                cm.sendOk("Done! Enjoy your stay in #bMapleZtory!#k");
                cm.dispose();
               } else {
                var formula = 200000000 * cm.getText();
                cm.sendOk("You don't have " + formula + " mesos. You only have " + cm.getPlayer().getMeso() + " meso(s).");
                cm.dispose();
}
}
}
}