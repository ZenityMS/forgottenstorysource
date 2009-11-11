/* Made by Rich - Family Clan NPC*/

importPackage(java.util);
importPackage(net.sf.odinms.client);
importPackage(net.sf.odinms.server);
importPackage(net.sf.odinms.net.channel);
importPackage(net.sf.odinms.tools);
importPackage(net.sf.odinms.scripting.npc);

var status = 0;
var family = new Array(1, 2);


function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {

         
         if (mode == -1) {
        cm.dispose();
    
    }else if (mode == 0){
        cm.sendOk ("#eOk, talk to me when you want to Super Scroll.");
        cm.dispose();

    }else{             
        if (mode == 1)
            status++;
        else
            status--;
        
        if (status == 0) {
// cm.getPlayer().setFamily(0);
         if (cm.getPlayer().getFamily() == 0) {
            cm.sendSimple("Please pick a Family. \r\n#e CHOOSE WISELY BECAUSE YOU WILL NOT BE ABLE TO SWITCH FAMILIES#e#d" +
                 "#k\r\n#L0##b Blue Family" +
                 "#k\r\n#L1##r Red Family");
          } else {
           cm.getPlayer().getClient().getSession().write(MaplePacketCreator.startMonsterCarnival(1));
           cm.getPlayer().setTeam(1);
         //  cm.getPlayer().getClient().getSession().write(MaplePacketCreator.CPUpdate(false, cm.getPlayer().getCP(), cm.getPlayer().getTotalCP(), 1));
           cm.sendOk("I have nothing to say to you...");
            cm.dispose();
           }
            } else if (status == 1) {
            cm.getPlayer().setJob(cm.getPlayer().getJob());
           if (selection == 0) {
           cm.worldMessage("Congratulations to " + cm.getPlayer().getName() + " for they have joined the Blue family!");
           var familytext = "#b#eIt is done! You are now part of the Blue Family! Use @bluemessage to send your family messages!";
           }
           if (selection == 1) {
            cm.worldMessage("Congratulations to " + cm.getPlayer().getName() + " for they have joined the Red family!");
            var familytext ="#r#eIt is done! You are now part of the Red Family! Use @redmessage to send your family messages!";
           }
             cm.getPlayer().setFamily(family[selection]);
            cm.sendOk(familytext);
            cm.dispose();

}
}
}