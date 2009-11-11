importPackage(java.util);
importPackage(net.sf.odinms.client);
importPackage(net.sf.odinms.server);
importPackage(net.sf.odinms.net.channel);
importPackage(net.sf.odinms.tools);
importPackage(net.sf.odinms.scripting.npc);

var status = 0;
function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (status >= 2 && mode == 0) {
			cm.sendOk("This is for #rMapleZtory#k only..");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
cm.getPlayer().getClient().getSession().write(MaplePacketCreator.getCharInfo(cm.getPlayer()));
			cm.sendNext("Hello #hb#!! Welcome to the world of #rMapleZtory#k Beta! Enjoy your free #bMark of Beta Hat#k!!");
		} else if (status == 1) {
			cm.sendNextPrev("To see our server rates at any time you can use @rates, our current rates are 250x/100x/5x. Remember to use #e@commands#k to see your player commands.")
		} else if (status == 2) {
			if (cm.getMeso() < 0) {
				cm.sendOk("You do not have enough mesos.")
				cm.dispose();
			} else {
				cm.sendSimple("Alright so please choose your job. You will receive a Beta Hat #b\r\n#L1#Beginner#l\r\n#L2#Warrior#l\r\n#L3#Magician#l\r\n#L4#Bowman#l\r\n#L5#Thief#l\r\n#L6#Pirate#l#k");
                                         }
                                 } else if (status == 3) {
				switch (selection) {
					case 1: // Beginners - done
                                                cm.gainItem(1002419, 1);
						cm.gainItem(1442021, 1);
						cm.gainItem(1442022, 1);
						cm.gainItem(1442023, 1);
                                                 cm.warp(809050016);
cm.getPlayer().sendNote(cm.getPlayer().getId(), "Welcome as you are probably a new member of MapleZtory! You can use @help and or @commands to look at your player commands! You can talk to the FAQ NPC by using @FAQ. You can also go to the website and vote for vote points. You can trade these in for fantastic prizes! We hope you have a good stay at our server MapleZtory!");
cm.getPlayer().showNote();
                                                 cm.dispose();
						hp = 50;
						mp = 50;
						break;
					case 2: // Warriors - done - 183 - 154 // 170 - 140
						cm.changeJobById(100);
                                                cm.gainItem(1002419, 1);
						cm.gainItem(1002044, 1);
						cm.gainItem(1092005, 1);
						cm.gainItem(1302077, 1);
                                                cm.warp(809050016);
cm.getPlayer().sendNote(cm.getPlayer().getId(), "Welcome as you are probably a new member of MapleZtory! You can use @help and or @commands to look at your player commands! You can talk to the FAQ NPC by using @FAQ. You can also go to the website and vote for vote points. You can trade these in for fantastic prizes! We hope you have a good stay at our server MapleZtory!");
cm.getPlayer().showNote();
                                                cm.dispose();
						if (cm.getChar().getGender() == 0) {
							cm.gainItem(1040038, 1);
							cm.gainItem(1060028, 1);
						} else {
							cm.gainItem(1041064, 1);
							cm.gainItem(1061023, 1);
						}
						hp = 183;
						mp = 154;
						break;
					case 3: // Magicians - done // ap = 44
						cm.changeJobById(200);
                                                cm.gainItem(1002419, 1);
						cm.gainItem(1002074, 1);
						cm.gainItem(1072045, 1);
						cm.gainItem(1372043, 1);
                                                cm.warp(809050016);
cm.getPlayer().sendNote(cm.getPlayer().getId(), "Welcome as you are probably a new member of MapleZtory! You can use @help and or @commands to look at your player commands! You can talk to the FAQ NPC by using @FAQ. You can also go to the website and vote for vote points. You can trade these in for fantastic prizes! We hope you have a good stay at our server MapleZtory!");
cm.getPlayer().showNote();
                                                cm.dispose();
						if (cm.getChar().getGender() == 0) {
							cm.gainItem(1040004, 1);
							cm.gainItem(1060012, 1);
						} else {
							cm.gainItem(1041015, 1);
							cm.gainItem(1061010, 1);
						}
						ap = 44;
						hp = 157;
						mp = 126;
						break;
					case 4: // Archers - done
						cm.changeJobById(300);
                                                cm.gainItem(1002419, 1);
						cm.gainItem(1002057, 1);
						cm.gainItem(1072059, 1);
						cm.gainItem(1452051, 1);
cm.getPlayer().sendNote(cm.getPlayer().getId(), "Welcome as you are probably a new member of MapleZtory! You can use @help and or @commands to look at your player commands! You can talk to the FAQ NPC by using @FAQ. You can also go to the website and vote for vote points. You can trade these in for fantastic prizes! We hope you have a good stay at our server MapleZtory!");
cm.getPlayer().showNote();
                                                cm.warp(809050016);
                                                cm.dispose();
						if (cm.getChar().getGender() == 0) {
							cm.gainItem(1040071, 1);
							cm.gainItem(1062004, 1);
						} else {
							cm.gainItem(1041007, 1);
							cm.gainItem(1061009, 1);
						}
						cm.gainItem(2060000, 2000);
						cm.gainItem(2061000, 2000);
						hp = 183;
						mp = 154;
						break;
					case 5: // Thiefs - done
						cm.changeJobById(400);
                                                cm.gainItem(1002419, 1);
						cm.gainItem(1002123, 1);
						cm.gainItem(1072070, 1);
						cm.gainItem(1332063, 1);
						cm.gainItem(1472061, 1);
cm.getPlayer().sendNote(cm.getPlayer().getId(), "Welcome as you are probably a new member of MapleZtory! You can use @help and or @commands to look at your player commands! You can talk to the FAQ NPC by using @FAQ. You can also go to the website and vote for vote points. You can trade these in for fantastic prizes! We hope you have a good stay at our server MapleZtory!");
cm.getPlayer().showNote();
                                                cm.warp(809050016);
                                                cm.dispose();
						if (cm.getChar().getGender() == 0) {
							cm.gainItem(1040031, 1);
							cm.gainItem(1060021, 1);
						} else {
							cm.gainItem(1041037, 1);
							cm.gainItem(1061030, 1);
						}
						cm.gainItem(2070000, 500);
						cm.gainItem(2070000, 500);
						hp = 183;
						mp = 154;
						break;
					case 6: // Pirates
						cm.changeJobById(500);
                                                cm.gainItem(1002419, 1);
						cm.gainItem(1482000, 1);
						cm.gainItem(1492000, 1);
						cm.gainItem(1002610, 1);
						cm.gainItem(1052095, 1);
cm.getPlayer().sendNote(cm.getPlayer().getId(), "Welcome as you are probably a new member of MapleZtory! You can use @help and or @commands to look at your player commands! You can talk to the FAQ NPC by using @FAQ. You can go to the website and vote for vote points. You can trade these in for fantastic prizes! We hope you have a good stay at our server MapleZtory!");
cm.getPlayer().showNote();
						cm.gainItem(2330000, 500);
						cm.gainItem(2330000, 500);
                                                cm.warp(809050016);
                                                cm.dispose();
						hp = 183;
						mp = 154;
						break;
					default:
						break;
			if (ap == 0) {
					ap = 54;
				}
 cm.getPlayer().sendNote(cm.getPlayer().getId(), "Welcome as you are probably a new member of MapleZtory! You can use @help and or @commands to look at your player commands! You can talk to the FAQ NPC by using @FAQ. You can also go to the website and vote for vote points. You can trade these in for fantastic prizes! We hope you have a good stay at our server MapleZtory!");
cm.getPlayer().showNote();
                                cm.dispose();
				cm.gainItem(1082002, 1);
				cm.gainItem(2000013, 50);
				cm.gainItem(2000014, 50);
				cm.gainMeso(100000);
				var statup = new java.util.ArrayList();
				cm.getChar().setStr(4);
				cm.getChar().setDex(4);
				cm.getChar().setInt(4);
				cm.getChar().setLuk(4);
				cm.getChar().setMaxMp(mp);
				cm.getChar().setMaxHp(hp);
				cm.getChar().setHp(hp);
				cm.getChar().setMp(mp);
				cm.getChar().setRemainingAp (ap);
				statup.add (new net.sf.odinms.tools.Pair(MapleStat.STR, java.lang.Integer.valueOf(4)));
				statup.add (new net.sf.odinms.tools.Pair(MapleStat.DEX, java.lang.Integer.valueOf(4)));
				statup.add (new net.sf.odinms.tools.Pair(MapleStat.LUK, java.lang.Integer.valueOf(4)));
				statup.add (new net.sf.odinms.tools.Pair(MapleStat.INT, java.lang.Integer.valueOf(4)));
				statup.add (new net.sf.odinms.tools.Pair(MapleStat.AVAILABLEAP, java.lang.Integer.valueOf(ap)));
				statup.add (new net.sf.odinms.tools.Pair(MapleStat.MAXHP, java.lang.Integer.valueOf(hp)));
				statup.add (new net.sf.odinms.tools.Pair(MapleStat.MAXMP, java.lang.Integer.valueOf(mp)));
				statup.add (new net.sf.odinms.tools.Pair(MapleStat.HP, java.lang.Integer.valueOf(hp)));
				statup.add (new net.sf.odinms.tools.Pair(MapleStat.MP, java.lang.Integer.valueOf(mp)));
				cm.getChar().getClient().getSession().write (net.sf.odinms.tools.MaplePacketCreator.updatePlayerStats(statup));

                                //cm.warp(809050016);
		}
	}
}
}