/* BPD by Rich */

var status = 0;
var minlvl = 100;
var maxlvl = 255;
var minplayers = 3;
var maxplayers = 6;
var time = 15;
var open = true;

function start() {
    status = -1; // and when they click lets fight make it turn to a really cool ifght song :D LOL ok like the Zakum battle song? kk and btw uhm can you add a message like after they click OK to say "Matt: Meet me near the top of the map." ? o-o in other words, a
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
            cm.sendSimple("Hello fellow noob, have you ever heard of the monster #bBigg Puff Daddy?#k He has been terrorizing #bMapleZtory#k for hundreds of years. We need strong warriors to defeat this monster so all the friendly monsters and players can live in harmony. Would you like to try and defeat Big Puff Daddy? There is no guarantee that you will return. \r\n#b\r\n#b#L0#Yes, lets go.#l\r\n#L1#no thx im scared :( btw gm pls");
	 	
        } else if (status == 1) {
            var em = cm.getEventManager("ProBosses");
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
                    cm.sendOk("The Super Horntail is #rclosed#k for now.");
                } else {
                    em.startInstance(cm.getParty(), cm.getChar().getMap());
                    var party = cm.getChar().getEventInstance().getPlayers();
                    cm.removeFromParty(4001106, party);
                    cm.getPlayer().warning[1] = false;
                } // now to code me...
                cm.dispose();
            } else if(selection == 1) {
                cm.sendOk("ur gm nao kthx we cud use u on the force ;D");
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