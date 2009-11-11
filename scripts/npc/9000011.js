/*
BossQuest NPC Starter
*/

var status = 0;
var minLevel = 50;
var maxLevel = 255;
var minPlayers = 1;
var maxPlayers = 6;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (cm.getParty() == null) { // no party
                cm.sendOk("Welcome,#g #h ##k, to the waiting room for the Boss Quest. I hope you have trained well.\r\n\\r\n\You may do this solo, or form a party, and tell the leader to talk to me.\r\n\\r\n\#rMake sure you're between level 50 and 250. GMs accompanying you may be level 255.#k");
                cm.dispose();
                                return;
            }
            if (!cm.isLeader()) { // not party leader
                cm.sendOk("If you want to try the quest, tell your leader to talk to me.");
                cm.dispose();
                        }
            else {
                // check if all party members are within 50-200 range, etc.
                var party = cm.getParty().getMembers();
                var mapId = cm.getChar().getMapId();
                var next = true;
                var levelValid = 0;
                var inMap = 0;
                // Temp removal for testing
                if (party.size() < minPlayers || party.size() > maxPlayers) 
                    next = false;
                else {
                    for (var i = 0; i < party.size() && next; i++) {
                        if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
                            levelValid += 1;
                        if (party.get(i).getMapid() == mapId)
                            inMap += 1;
                    }
                    if (levelValid < minPlayers || inMap < minPlayers)
                        next = false;
                }
                if (next) {
                    cm.sendOk("Okay, good luck.");
                    var em = cm.getEventManager("BossQuest");
                    if (em == null) {
                        cm.sendOk("This PQ is currently disabled by David. Please be patient.");
                    }
                    else {
                        em.startInstance(cm.getParty(),cm.getChar().getMap());
                        party = cm.getChar().getEventInstance().getPlayers();
                    }
                    cm.dispose();
                }
                else {
                    cm.sendOk("You don't have a party of at least 1. Please make sure all your members are present and qualified to participate in this quest.  I see #b" + levelValid.toString() + " #kmembers are in the right level range, and #b" + inMap.toString() + "#k are here. If this seems wrong, #blog out and log back in,#k or reform the party.");
                    cm.dispose();
                }
            }
        }
        else {
            cm.sendOk("I'm broken.");
            cm.dispose();
        }
    }
}
