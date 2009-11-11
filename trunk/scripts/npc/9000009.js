/*
@    Author : Snow
@
@    NPC = NAME
@    Map =  MAP
@    NPC MapId = MAPID
@    Function = Rebirth Player
@    Edited by Matt
@
*/


var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {

         
         if (mode == -1) {//ExitChat
        cm.dispose();
    
    }else if (mode == 0){//No
        cm.sendOk("Ok, talk to me when you're sure you want to be #bReborn#k.");
        cm.dispose();

    }else{            //Regular Talk
        if (mode == 1)
            status++;
        else
            status--;
        
                 if (status == 0) {
        cm.sendYesNo("Hi, I'm Mr. Pickall. I can rebirth you. Would you like to be #rRebirthed?#k");
        }else if (status == 1) {
        if(cm.getChar().getLevel() < 200){
        cm.sendOk("Sorry, You have to be level 200 to rebirth.");
        cm.dispose();
        }else{
        cm.sendOk("#bCongratulations!#k, you have been qualified for a #eRebirth#n. Make sure you have #renough space#k to unequip your items or else I'll just level you to 201, which would suck.");
        }
         }else if (status == 2) {
	wui = 1;
	var statup = new java.util.ArrayList();
	var p = cm.c.getPlayer();
        var totAp = p.getRemainingAp() + p.getStr() + p.getDex() + p.getInt() + p.getLuk();
	p.setStr(4);
	p.setDex(4);
	p.setInt(4);
	p.setLuk(4);
	p.setRemainingAp (totAp - 16);
        cm.getChar().levelUp();
        cm.unequipEverything()
        cm.changeJob(net.sf.odinms.client.MapleJob.BEGINNER);
        cm.sendNext("You have bee reborned! Good luck on your next journey.");
        cm.gain1Reborn();
	cm.setLevel(2);
	statup.add (new net.sf.odinms.tools.Pair(net.sf.odinms.client.MapleStat.LEVEL, java.lang.Integer.valueOf(1)));
        statup.add (new net.sf.odinms.tools.Pair(net.sf.odinms.client.MapleStat.STR, java.lang.Integer.valueOf(4)));
        statup.add (new net.sf.odinms.tools.Pair(net.sf.odinms.client.MapleStat.DEX, java.lang.Integer.valueOf(4)));
	statup.add (new net.sf.odinms.tools.Pair(net.sf.odinms.client.MapleStat.LUK, java.lang.Integer.valueOf(4)));
	statup.add (new net.sf.odinms.tools.Pair(net.sf.odinms.client.MapleStat.INT, java.lang.Integer.valueOf(4)));
	statup.add (new net.sf.odinms.tools.Pair(net.sf.odinms.client.MapleStat.AVAILABLEAP, java.lang.Integer.valueOf(p.getRemainingAp())));
	p.getClient().getSession().write (net.sf.odinms.tools.MaplePacketCreator.updatePlayerStats(statup));
        cm.reloadChar();
        cm.dispose();
        }            
          }
     }
