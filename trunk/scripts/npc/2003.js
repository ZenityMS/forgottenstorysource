/**
Rebirth Point NPC
 @author Qualitys
*/
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1){
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1){
        status++;
        } else {
        status--;
        }
        if (status == 0) {
		// cm.getPlayer().gainRebirthPoints(1);
        cm.sendSimple ("Hello, I am the Rebirth Point NPC. Every time you rebirth you gain #b1#k Rebirth Point. For 1, you can get a hat for a job you were before you #bRebirthed#k. If you did #bRebirth#k from that job before, it'll change you back to it.  \n You currently have " + cm.getPlayer().getRebirthPoints() + " Rebirth Points. \b\r\n#L0#1 Point for #bNight Lord Hat#k\n\#l\r\n #L1#1 Point for a #bShadower Hat#k \n\#l\r\n #L2#1 Point for #bHero hat#k\n\#l\r\n#L3#1 Point for #bPaladin Hat#k\n\#l\r\n#L4#1 Point for #bDragonKnight hat#k\n\#l\r\n#L5#1 Point for #bBishop Hat#k\n\#l\r\n#L6#1 Point for #bFire Poison Mage Hat#k\n\#l\r\n#L7#1 Point for #bIce Lightining Mage Hat#k\n\#l\r\n#L8#1 Point for #bBrawler Hat#k\n\#l\r\n#L9#1 Point for #bBowman Hat#k\n\#l\r\n#L10#1 Point for #bMarksMan Hat#k\n\#l\r\n\#L11#1 Point for #bCorsair Hat#k");
        } else if (status == 1) {
            switch(selection) {
                case 0:
                if (cm.getRebirthPoints() > 0) {
                    cm.gainItem(1002083);
                    cm.gainRebirthPoints(-1);
                    cm.dispose();
                } else {
                    cm.sendSimple("You don't have 5 #bRebirth Points#k.");
                    cm.dispose();
                }
                break;
                case 1:
                if (cm.getRebirthPoints() > 0) {
                    cm.gainItem(1002082);
                    cm.gainRebirthPoints(-1);
                    cm.dispose();
                } else {
                    cm.sendSimple("You don't have 5 #bRebirth Points#k.");
                    cm.dispose();
                }
                break;
                case 2:
                if (cm.getRebirthPoints() > 0) {
                    cm.gainItem(1002081);
                    cm.gainRebirthPoints(-1);
                    cm.dispose();
                } else {
                    cm.sendSimple("You don't have 5 #bRebirth Points#k.");
                    cm.dispose();
                }
                break;
                case 3:
                if (cm.getRebirthPoints() > 0) {
                    cm.gainItem(1002080);
                    cm.gainRebirthPoints(-1);
                    cm.dispose();
                } else {
                    cm.sendSimple("You don't have 5 #bRebirth Points#k.");
                    cm.dispose();
                }
                break;
                case 4:
                if (cm.getRebirthPoints() > 0) {
                    cm.gainItem(1002393);
                    cm.gainRebirthPoints(-1);
                    cm.dispose();
                } else {
                    cm.sendSimple("You don't have 5 #bRebirth Points#k.");
                    cm.dispose();
                }
                break;
                case 5:
                if (cm.getRebirthPoints() > 0) {
                    cm.gainItem(1002394);
                    cm.gainRebirthPoints(-1);
                    cm.dispose();
                } else {
                    cm.sendSimple("You don't have 5 #bRebirth Points#k.");
                    cm.dispose();
                }
                break;
                case 6:
                if (cm.getRebirthPoints() > 0) {
                    cm.gainItem(1002392);
                    cm.gainRebirthPoints(-1);
                    cm.dispose();
                } else {
                    cm.sendSimple("You don't have 5 #bRebirth Points#k.");
                    cm.dispose();
                }
                break;
                case 7:
                if (cm.getRebirthPoints() > 0) {
                    cm.gainItem(1002391);
                    cm.gainRebirthPoints(-1);
                    cm.dispose();
                } else {
                    cm.sendSimple("You don't have 5 #bRebirth Points#k.");
                    cm.dispose();
                }
                break;
                case 8:
                if (cm.getRebirthPoints() > 0) {
                    cm.gainItem(1002395);
                    cm.gainRebirthPoints(-1);
                    cm.dispose();
                } else {
                    cm.sendSimple("You don't have 5 #bRebirth Points#k.");
                    cm.dispose();
                }
                break;
                case 9:
                if (cm.getRebirthPoints() > 0) {
                    cm.gainItem(1002515);
                    cm.gainRebirthPoints(-1);
                    cm.dispose();
                } else {
                    cm.sendSimple("You don't have any #bRebirth Points#k.");
                    cm.dispose();
                }
                break;
                case 10:
                if (cm.getRebirthPoints() > 0) {
                    cm.gainItem(1002397);
                    cm.gainRebirthPoints(-1);
                    cm.dispose();
                } else {
                    cm.sendSimple("You don't have 5 #bRebirth Points#k.");
                    cm.dispose();
                }
                break;
            }
        }
    }
}  
