/* BPD by Rich */

var status = 0;
var minlvl = 100;
var maxlvl = 255;
var minplayers = 1;
var maxplayers = 6;
var time = 15;
var on = true;
var open = true;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else if (mode == 0) {
        cm.dispose();
    } else {
        if (mode == 1)
            status++;
        else
            status--;		 
        if (status == 0) {
        if (on == false) {
        cm.sendOk("I am currently not available, please check back later.");
        cm.dispose();
        } else {
            cm.sendSimple("Hello, I'm in charge of being the local nooblet. But Rich gave me another job for my poor ass :) I will be hosting the #bBoat Quest#k One of the boats in MapleZtory has been constantly being attacked by #rBalrogs.#k We need you to kill the balrogs so that we can ride the boat again. Why not buy another boat? Why do you even ask? We need more donations bitch :O Now get your ass on our site and donate. Anyways, do you want to enter the quest? \r\n#b\r\n#b#L0#Lets go!#l\r\n#L3#I'm here to trade in my #bCrimson Balrog Cards for some EXP!#k#l\r\n#L1#will there be tacos?");
	 	}
        
        } else if (status == 1) {
            var em = cm.getEventManager("BoatPQ");
            if(selection == 0) {//ENTER THE PQ
                if (!hasParty()) {//NO PARTY
                    cm.sendOk("You don't have a party with you.");
                } else if (!isLeader()) {//NOT LEADER
                    cm.sendOk("Your not the leader. Tell the leader of the party to talk to me.");
                } else if (!checkPartySize()) {//PARTY SIZE WRONG
                    cm.sendOk("Your party needs to have at least " + minplayers + " members.");
                } else if (!checkPartyLevels()) {//WRONG LEVELS
                    cm.sendOk("One of your party members has not met the level requirements of " + minlvl + "~" + maxlvl + ".");
                } else if (em == null) {//EVENT ERROR
                    cm.sendOk("There was an error starting the event. Please tell an Admin ASAP");
                } else if (!open){
                    cm.sendOk("This is closed.");
                } else {
                    em.startInstance(cm.getParty(), cm.getChar().getMap());
                    var party = cm.getChar().getEventInstance().getPlayers();
                    cm.removeFromParty(4001106, party);
                    cm.getPlayer().warning[1] = false;
                } // now to code me...
                cm.dispose();
            } else if(selection == 1) {
                cm.sendOk("I can assure you. There will be no tacos.");
                cm.dispose();
            } else if(selection == 3) {
            if (cm.haveItem(2388017, 1)) {
                cm.gainExp(400000000);
                cm.gainItem(2388017, -1);
                cm.sendOk("There you go!");
                cm.dispose();
             } else {
            cm.sendOk("You don't have enough #r Crimson Balrog Cards #k.");
            cm.dispose();
}
           }
    }
}
   
  
function getPartySize(){
    if(cm.getPlayer().getParty() == null){
        return 0;
    }else{
        return (cm.getPlayer().getParty().getMembers().size());
    }
}

function isLeader(){
    return cm.isLeader();
}

function checkPartySize(){
    var size = 0;
    if(cm.getPlayer().getParty() == null){
        size = 0;
    }else{
        size = (cm.getPlayer().getParty().getMembers().size());
    }
    if(size < minplayers || size > maxplayers){
        return false;
    }else{
        return true;
    }
}

function checkPartyLevels(){
    var pass = true;
    var party = cm.getPlayer().getParty().getMembers();
    if(cm.getPlayer().getParty() == null){
        pass = false;
    }else{
        for (var i = 0; i < party.size() && pass; i++) {
            if ((party.get(i).getLevel() < minlvl) || (party.get(i).getLevel() > maxlvl) || (party.get(i).getMapid() != cm.getMapId())) {
                pass = false;
            }
        }
    }
    return pass;
}

function hasParty(){
    if(cm.getPlayer().getParty() == null){
        return false;
    }else{
        return true;
    }
}
}
