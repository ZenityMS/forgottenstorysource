// Made by Rich

var status = 0;
var minlvl = 10;
var maxlvl = 255;
var minplayers = 1;
var maxplayers = 6;
var time = 15;
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
            cm.sendSimple("Hey, want to create a Capture the Flag match?\r\n#b\r\n#b#L0#Ok, lets bounce#l\r\n#L1#I want to join a friend's match");
	 	
          } else if (status == 101) { 
            var chr = cm.getCharByName(cm.getText());
            if (chr != null) {
                    var eim = chr.getEventInstance();
           if (eim != null) {           
            if (chr.isLeader()) {
                    eim.registerPlayer(cm.getPlayer());                     
                    chr.warpParty(1010000, 0);
                    chr.setBlueTeam();
                    chr.setCanPickup(false);
                    chr.hasflag = true;
                    chr.removeAll(4001025);
                    chr.flagPromptName2 = cm.getPlayer().getName();
                    chr.gainItem(4001025, 1, false, false);
                    cm.getPlayer().setRedTeam();
                    cm.getPlayer().setCanPickup(false);
                    cm.getPlayer().hasflag = true;
                    cm.getPlayer().removeAll(4001025);
                    cm.getPlayer().flagPromptName2 = cm.getPlayer().getName();
                    cm.getPlayer().gainItem(4001025, 1, false, false);
                    cm.getPlayer().warpPartyTimed(1010000, 5, 3000); 
                    cm.getPlayer().joinPartyEventInstance();
                    cm.mapMessage(5, "Try to get to the other side! If the leader gets hit he will lose the flag!");
} else {
cm.sendOk("The person you type in has to be the leader of the party.");
cm.dispose();          
}        
} else {
cm.sendOk("That user is not in Capture the Flag");
cm.dispose();
}
            } else
                cm.sendOk("Player was not found");
                cm.dispose();

        } else if (status == 1) {
            var em = cm.getEventManager("CaptureTheFlag");
            if(selection == 0) {//ENTER THE PQ
                if (!hasParty()) {//NO PARTY
                    cm.sendOk("#eYou don't have a party with you.");
                } else if (!isLeader()) {//NOT LEADER
                    cm.sendOk("#eYour not the leader. Tell the leader of the party to talk to me.");
                } else if (!checkPartySize()) {//PARTY SIZE WRONG
                    cm.sendOk("#eYour party needs to have at least " + minplayers + " members.");
                } else if (!checkPartyLevels()) {//WRONG LEVELS
                    cm.sendOk("#eOne of your party members has not met the level requirements of " + minlvl + "~" + maxlvl + ".");
                } else if (em == null) {//EVENT ERROR
                    cm.sendOk("ERROR IN EVENT");
                } else if (!open){
                    cm.sendOk("The PQ is #rclosed#k for now.");
                } else {
                    cm.mapMessage(5, "Please wait for somebody to challenge you.");
                    em.startInstance(cm.getParty(), cm.getChar().getMap());
                    // var party = cm.getChar().getEventInstance().getPlayers();
                   // cm.removeFromParty(4001106, party);
                }
               cm.dispose();

            } else if(selection == 1) {
            cm.sendGetText("Type in the Player who is hosting the Capture the Flag match");
            status = 100;
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