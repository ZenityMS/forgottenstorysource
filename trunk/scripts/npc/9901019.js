
importPackage(java.util);
importPackage(net.sf.odinms.client);
importPackage(net.sf.odinms.server);
importPackage(net.sf.odinms.net.channel);
importPackage(net.sf.odinms.tools);
importPackage(net.sf.odinms.scripting.npc);

var status = 0;
var chicken = 0;
var famecost = 700000;
var defamecost = 800000;
var warpcost = 500000;
var killcost = 5000000;
var sendcost = 500000;
var itemcost = 1000000;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendSimple("LOL hi I'm Chuck Norris! :D #k\r\nWhat do you want to do?\r\n#L1##bFame Someone (" + famecost + " Mesos)#k#l\r\n#L2##bDefame Someone (" + defamecost + " Mesos)#k#l\r\n#L3##rKill someone (" + killcost + ") Mesos#k#l\r\n#L4##rWarp to someone (" +warpcost + ") Mesos#k#l\r\n#L5##rSend an item to someone (" +itemcost + ") Mesos#l"); // \r\n#b#L5##rSend a message to someone offline (" +sendcost + ") Mesos#k#l");
		} else if (status == 1) {
			if (selection == 1) {
				cm.sendGetText("Who do you want to fame?");
				chicken = 1;							
			} else if (selection == 2) {
				cm.sendGetText("Who do you want to defame?");
				chicken = 2;
			} else if (selection == 3) {
				cm.sendGetText("Who do you want to kill?");
				chicken = 3;
			} else if (selection == 4) {
                                cm.sendGetText("Who do you want to warp too?");
                                chicken = 4;
			
			} else if (selection == 5) {
                  var toSend = "I can send the #r FIRST EQUIP ITEM IN YOUR INVENTORY#k WHICH IS #v" + cm.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(1).getItemId() + "# to another character Who shall receive the item in your first equip inventory slot?#b";
              //  var iter = cm.getPlayer().getMap().getCharacters().iterator();
                  var iter = cm.getClient().getChannelServer().getPlayerStorage().getAllCharacters().iterator();
                var i = 0;
                targets = new Array();
                while (iter.hasNext()) {
                    var curChar = iter.next();
                    toSend += "\r\n#L" + i + "#" + curChar.getName() + "#l";
                    targets[i] = curChar;
                    i++;

                }
                toSend += "#k";
                cm.sendSimple(toSend);
                chicken = 5;
			/* } else if (selection == 5) {
                                cm.sendGetText("Who do you want to send the message too?");
                                chicken = 5; */
			}
		} else if (status == 2) {
			if (chicken == 1) {
				if (cm.getMeso() >= famecost) {
					victim = cm.getPlayer().getClient().getChannelServer().getPlayerStorage().getCharacterByName(cm.getText());
					if (victim != null) {
						victim.setFame(victim.getFame() +1);
						victim.updateSingleStat(MapleStat.FAME, victim.getFame());
						victim.getClient().getSession().write(MaplePacketCreator.serverNotice(6, "You have gained 1 fame."));
						cm.gainMeso(-famecost);
					} else
						cm.sendOk("I can't find him/her\r\nare you sure you spelled the name correctly?");
					cm.dispose();
				} else {
					cm.sendOk("You don't have enough #bMesos#k noob.\r\nDon't scam me or I will use this on you! #s9001001#");
					cm.dispose();
				}
			} else if (chicken == 2) {
				if (cm.getMeso() >= defamecost) {
					victim = cm.getPlayer().getClient().getChannelServer().getPlayerStorage().getCharacterByName(cm.getText());
					if (victim != null) {
						victim.setFame(victim.getFame() -1);
						victim.updateSingleStat(MapleStat.FAME, victim.getFame());
						victim.getClient().getSession().write(MaplePacketCreator.serverNotice(6, "You have lost 1 fame"));
						cm.gainMeso(-defamecost);
					} else
						cm.sendOk("I can't find him/her\r\nare you sure you spelled the name correctly?");
					cm.dispose();
				} else {
    					cm.getPlayer().setHp(0);
    					cm.getPlayer().setMp(0);
    					cm.getPlayer().updateSingleStat(MapleStat.HP, 0);
    					cm.getPlayer().updateSingleStat(MapleStat.MP, 0);
					cm.sendOk("You don't have enough #bMesos#k\r\nDon't scam me or I will use this on you! #s9001001#");
					cm.dispose();
				}
			} else if (chicken == 3) {
 victim = cm.getPlayer().getClient().getChannelServer().getPlayerStorage().getCharacterByName(cm.getText());
				if (cm.getMeso() >= killcost && victim != null) {
					cm.gainMeso(-killcost);
                                	victim = cm.getPlayer().getClient().getChannelServer().getPlayerStorage().getCharacterByName(cm.getText());
					victim.getClient().getSession().write(MaplePacketCreator.serverNotice(6, "Hey, " + cm.getPlayer().getName() + " has killed you, I think he hates you!"));
					// cm.spawnMob(100100, 100, 100, 1, 5, 0, 0, 5, 341, 34);
    					victim.setHp(0);
    					victim.setMp(0);
    					victim.updateSingleStat(MapleStat.HP, 0);
    					victim.updateSingleStat(MapleStat.MP, 0);
					cm.dispose();

				} else {
					cm.sendOk("You don't have enough #bMesos#k or you are trying to kill someone that is not online.\r\nDon't scam me or I will use this on you! #s9001001#");
					cm.dispose();
				}

			} else if (chicken == 4) {
                               
 victim = cm.getPlayer().getClient().getChannelServer().getPlayerStorage().getCharacterByName(cm.getText());
				if (cm.getMeso() >= warpcost && victim != null) {
                                if (victim.isGM() == false) {
								if (victim.getMapId() != 912000000) {
                                        cm.gainMeso(-warpcost);
                                        victim = cm.getPlayer().getClient().getChannelServer().getPlayerStorage().getCharacterByName(cm.getText());
                                        var blackpussy = cm.getText();
                                        cm.WarpTo(blackpussy);
					victim.getClient().getSession().write(MaplePacketCreator.serverNotice(6, "Hey, " + cm.getPlayer().getName() + " has warped to you through a NPC in the Free Market"));
                                        cm.sendOk("You know what makes thunder? When my balls drop!");
										} else {
										cm.sendOk("You cannot warp to the donator map");
										cm.dispose();
										}
                                } else {
                                        cm.sendOk("You can not warp to a GM!");
                                        cm.dispose();
}
                                } else {
                                        cm.sendOk ("You don't have enough mesos to warp to someone, you are not in the same channel as the person you want to warp too, or the person you want to warp too is not online.");
                                        cm.dispose();
                              } 
		/*	} else if (chicken == 5) {
                                        victim = cm.getPlayer().getClient().getChannelServer().getPlayerStorage().getCharacterByName(cm.getText());
                                        var blackpussy = cm.getText();
                                        cm.sendGetText("What do you want to say?");
                                        chicken = 7;

					} else if (chicken == 7) {
 victim = cm.getPlayer().getClient().getChannelServer().getPlayerStorage().getCharacterByName(cm.getText());
				if (cm.getMeso() >= sendcost) {
                                        cm.gainMeso(-sendcost);
                                        victim = cm.getPlayer().getClient().getChannelServer().getPlayerStorage().getCharacterByName(cm.getText());
                                        var blackpussy2 = cm.getText();
                                        cm.sendNote(victim.getId(), blackpussy2);
                                        cm.sendOk("There you go!");
                                        cm.dispose();
                                } else {
                                        cm.sendOk ("You don't have enough mesos to send a mesage to someone offline"); 
                                      
			} */


			} else if (chicken == 5) {
                sendTarget = targets[selection];
              if (sendTarget.canHold(1002083)) {
            if (cm.getMeso() >= itemcost) {
                var item = cm.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(1).copy();
                MapleInventoryManipulator.removeFromSlot(cm.getC(), MapleInventoryType.EQUIP, 1, 1, true);
                MapleInventoryManipulator.addFromDrop(sendTarget.getClient(), item, false, cm.getPlayer().getName()); // "Sent to " + sendTarget.getName() + "using Tess");
                sendTarget.getClient().getSession().write(MaplePacketCreator.serverNotice(6, "Hey, " + cm.getPlayer().getName() + " has sent you an item!"));
                cm.gainMeso(-itemcost);
                cm.sendOk(sendTarget.getName() + " has received the item. See you next time.");
                cm.dispose();
              } else {
                cm.sendOk ("You don't have enough mesos to send an item!");
                cm.dispose();
}
            } else {
                cm.sendOk ("The person you are trying to send too cannot hold the item in their inventory!");
                cm.dispose();
 }
		} 
	}
}
}