importPackage(net.sf.odinms.net.channel);
importPackage(net.sf.odinms.server.maps.pvp);

var status = 0;
var prizes = Array(1050127, 1050127, 1051140, 1092035, 1072239, 1002511, 1002574, 1102084, 1102085, 1102086, 1122002, 1122005, 1442023, 1302063);
var cost = Array(10, 20, 20, 30, 40, 50, 60, 70, 70, 70, 80, 80, 90, 100);
var prize;
var mapid;

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
        }
        if (cm.getPlayer().getMapId() != 910000000 && cm.getPlayer().getMapId() != 910000006) {
            cm.dispose();
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
           // if (cm.getPlayer().getMapId() == 910000000) {
                cm.sendSimple("Yo, what's up?#b\r\n#L0#PQ!#l\r\n#L1#PvP!#l\r\n#L5#Guild Wars#l");
            } else if (cm.getPlayer().getMapId() == 910000006) {
                cm.sendSimple("Congrats on winning the Guild Wars event! What do you want?\r\n#L9##bSummon Zakum#k#l\r\n#L10##bSummon Papulatus#k#l\r\n#L12##bSummon PQ monsters#k#l\r\n#L11##bKill Monsters#k#l\r\nIf you do not feel this is enough, it is temporary. Feel free to make a suggestion.");
        } else if (status == 1) {
            if (selection == 0) {
                cm.sendSimple("Select a PQ.#b\r\n#L0#WorldPQ(51-100)#l#k");
                status = 49;
            } else if (selection == 1) {
                cm.sendSimple("There are three PvP arena's currently for play.\r\n#bSingle Deathmatch#k: You can attack anyone at any time in the map!\r\n#bTeam Deathmatch#k: You can attack someone who isn't in a party, or isn't in yours.\r\n#bGuild Deathmatch#k: You can attack anyone who isn't in a guild, or isn't in yours.\r\nWhat's your choice?!\r\n#L2##bSingle Deathmatch#k#l\r\n#L3##bParty Deathmatch#k#l\r\n#L4##bGuild Deathmatch#k#l\r\n#L12##bI'd like to redeem my kills and deaths.#k#l\r\nChoose wisely!!");
                status = 9;
            } else if (selection == 5) {
                cm.sendSimple("Ah, Guild Wars? It's a new system only for  (Like most of the things I provide) Anyways, it's kinda like Field of Judgement where you need to register in the event to go in, but it has twists to it! You'll find out how it's like when you enter! If you want to join or if you want the prize (only if you have won!) talk to me! As it's a system for guilds, if you want to join, it will cost 10000000 mesos. Are you ready?!\r\n#L6##bEnter guild into Guild Wars#k#l\r\n#L7##bJoin guild already in Guild Wars#k#l\r\n#L8##bI won, gimme mah prize.#k#l");
                status = 29;
            } else if (selection == 9) {
                if (cm.getBossLog('ZAKUMR') < 5 && cm.grabCastle() >= 1) {
                    if (cm.mapMobCount() == 0) {
                        cm.summonMobAtPosition(8800000,1,45,-206);
                        cm.summonMobAtPosition(8800003,1,45,-206);
                        cm.summonMobAtPosition(8800004,1,45,-206);
                        cm.summonMobAtPosition(8800005,1,45,-206);
                        cm.summonMobAtPosition(8800006,1,45,-206);
                        cm.summonMobAtPosition(8800007,1,45,-206);
                        cm.summonMobAtPosition(8800008,1,45,-206);
                        cm.summonMobAtPosition(8800009,1,45,-206);
                        cm.summonMobAtPosition(8800010,1,45,-206);
                        cm.setBossLog('ZAKUMR');
                        cm.dispose();
                    } else {
                        cm.sendOk("You can only spawn one thing at a time.");
                        cm.dispose();
                    }
                } else {
                    cm.sendOk("Sorry but you already used this function 5 times or more or you do not own castle.");
                    cm.dispose();
                }
            } else if (selection == 10) {
                if (cm.getBossLog('PAPR') < 5 && cm.grabCastle() >= 1) {
                    if (cm.mapMobCount() == 0) {
                        cm.summonMobAtPosition(8500000,1,45,-206);
                        cm.setBossLog('PAPR');
                        cm.dispose();
                    } else {
                        cm.sendOk("You can only spawn one thing at a time.");
                        cm.dispose();
                    }
                } else {
                    cm.sendOk("Sorry but you already used this function 5 times or more or you do not own castle.");
                    cm.dispose();
                }
            } else if (selection == 11) {
                cm.killAllMobs();
                cm.killAllMobs();
                cm.killAllMobs();
                cm.dispose();
            } else if (selection == 12) {
                if (cm.getBossLog('PQMON') < 5 && cm.grabCastle() >= 1) {
                    if (cm.mapMobCount() == 0) {
                        cm.summonMobAtPosition(8220000,50000000,3000000,1,45,-206);
                        cm.summonMobAtPosition(8220001,30000000,1500000,1,45,-206);
                        cm.summonMobAtPosition(9410015,30000000,1500000,1,45,-206);
                        cm.setBossLog('PQMON');
                        cm.dispose();
                    } else {
                        cm.sendOk("You can only spawn one thing at a time.");
                        cm.dispose();
                    }
                } else {
                    cm.sendOk("Sorry but you already used this function 5 times or more or you do not own castle.");
                    cm.dispose();
                }
            }
        } else if (status == 10) {
            if (selection == 2) {
                cm.warp(910000003, 0);
                cm.dispose();
            } else if (selection == 3) {
                if (cm.getParty() != null) {
                    cm.warp(910000004, 0);
                    cm.dispose();
                } else {
                    cm.sendOk("Can't enter Party Deathmatch without being in a Party. Sorry.");
                    cm.dispose();
                }
            } else if (selection == 4) {
                if (cm.getPlayer().getGuildId() > 0) {
                    cm.warp(910000005, 0);
                    cm.dispose();
                } else {
                    cm.sendOk("Can't enter Guild Deathmatch without being in a Guild. Sorry.");
                    cm.dispose();
                }
            } else if (selection == 12) {
                cm.sendYesNo("Redeem your kills #eand#n deaths? Hmm, let's make it this way. Your deaths will always remain the same(as they do modify your exp..), but I can redeem your kills. Your kills are #r" + cm.getPvpKills() + "#k. Would you like to redeem them?");
                status = 39;
            } else {
                cm.sendOk("Eh, something's wrong.");
                cm.dispose();
            }
        } else if (status == 20) {
            cm.dispose();
        } else if (status == 30) {
            if (selection == 6) {
                var em = cm.getEventManager("guildwars");
                var guildId = cm.getPlayer().getGuildId();
                var eim = getEimForGuild(em, guildId);
                //var eimg = em.newInstance(guildId);
                if (em == null) {
                    cm.sendOk("Sorry but #gGuild Wars#k is currently not available.");
                    cm.dispose();
                } else if (em.getProperty("entryPossible").equals("false")) {
                    cm.sendOk("Sorry, but #rGuild Wars#k is currently closed.");
                    cm.dispose();
                } else if (cm.getMeso() < 10000000) {
                    cm.sendOk("You don't have enough mesos. You need 10000000 to get inside.");
                    cm.dispose();
                } else if (cm.getPlayer().getGuildId() == 0 || cm.getPlayer().getGuildRank() >= 3) {
                    cm.sendOk("You must be inside a guild to enter, and be a Jr. Master or Master.");
                    cm.dispose();
                } else if (eim != null) {
                    cm.sendOk("Your guild is already entered in the Guild Wars.");
                    cm.dispose();
                } else {
                    cm.gainMeso(-10000000);
                    em.newInstance(guildId);
                    //eimg.registerPlayer(cm.getPlayer());
                    em.getInstance("guildwars").registerPlayer(cm.getPlayer());
                    cm.guildMessage("The guild has been entered into the Guild Wars. Please report to Harry at the Free Market Entrance on channel 2.");
                    cm.dispose();
                }
            } else if (selection == 7) {
                var em = cm.getEventManager("guildwars");
                var eim = getEimForGuild(em, cm.getPlayer().getGuildId());
                if (em == null) {
                    cm.sendOk("Sorry but #gGuild Wars#k is currently not available.");
                } else if (em.getProperty("entryPossible").equals("false")) {
                    cm.sendOk("Sorry, but #rGuild Wars#k is currently closed or your Guild has gone on without you.");
                    cm.dispose();
                } else if (cm.getPlayer().getGuildId() == 0) {
                    cm.sendOk("Without a guild, you may not enter Guild Wars.");
                    cm.dispose();
                } else if (eim == null) {
                    cm.sendOk("Your guild is not registered in the Guild Wars.");
                    cm.dispose();
                } else {
                    em.getInstance("guildwars").registerPlayer(cm.getPlayer());
                    cm.dispose();
                }
            } else if (selection == 8) {
                if (cm.grabCastle() >= 1 && cm.getBossLog('CASTLE') < 5) {
                    cm.setBossLog('CASTLE');
                    cm.warp(910000006,0);
                    cm.dispose();
                } else {
                    cm.sendOk("Sorry but you either didn't win Guild Wars or are trying to go inside 5 or more times.");
                    cm.dispose();
                }
            } else {
                cm.sendOk("Eh, something's wrong.");
                cm.dispose();
            }
        } else if (status == 40) {
            var select = "Select your prize.#gMore prizes will be added soon, feel free to make a suggestion#k#r"
            for (var i = 0; i < prizes.length; i++) {
                if (i != 0) {
                    select += "\r\n#L" + i + "##v" + prizes[i] + "##t" + prizes[i] + "# (" + cost[i] + " kills)#l";
                } else {
                    select += "\r\n#L0#1000000 mesos (10 kills)#l";
                }
            }
            select += "#k";
            cm.sendSimple(select);
        } else if (status == 41) {
            prize = selection;
            if (cm.getPvpKills() < cost[selection]) {
                cm.sendOk("You do not have enough kills.");
                cm.dispose();
            } else {
                if (selection != 0) {
                    cm.sendYesNo("Do you want to buy #v" + prizes[selection] + "##t" + prizes[selection] + "# for " + cost[selection] + " kills?");
                } else {
                    cm.sendYesNo("Do you want to redeem 10 kills for 1000000 mesos?");
                }
            }
        } else if (status == 42) {
            cm.gainPvpKills(-cost[prize]);
            if (prize != 0) {
                cm.gainItemRand(prizes[prize], 1);
            } else {
                cm.gainMeso(1000000);
            }
            cm.dispose();
        } else if (status == 50) {
            if (selection == 0) {
                cm.warp(109060001);
            } else if (selection == 1) {
                cm.warp(109010000);
            }
            cm.dispose();
        } else if (status == 60) {
            mapid = 910000008 + selection;
            cm.sendSimple("Okay, and what would you like to do?#b\r\n#L0#Start/join the match#l\r\n#L1#Spectate#l#k");
        } else if (status == 61) {
            var ch = cm.getC().getChannel();
            var ctf = cm.isMapleCapture(ch, mapid);
            if (selection == 0) {
                if (!ctf) {
                    if (cm.getParty() == null) {
                        cm.sendOk("You do not have a party..!");
                    } else {
                        var newctf = new MapleCapture(cm.getPlayer());
                        var iter = cm.getParty().getMembers().iterator();
                        while (iter.hasNext()) {
                            iter.next().getClient().getPlayer().changeMap(mapid);
                        }
                    }
                    cm.dispose();
                } else {
                    ctf = cm.getMapleCapture(ch, mapid);
                    if (ctf.getChallenger() != null) {
                        cm.sendOk("There is already a match in the map.");	
                    } else if (cm.getParty() == null) {
                        cm.sendOk("You do not have a party..!");
                    } else {
                        ctf.newChallenger(cm.getPlayer());
                        cm.sendOk("Please wait for the other party leader to accept! You may withdraw using !pvp cancel.");
                    }
                    cm.dispose();
                }
            } else {
                if (ctf) {
                    ctf = cm.getMapleCapture(ch, mapid);
                    if (ctf.getWaiting() != null && ctf.getChallenger() != null) {
                        cm.warp(mapid);
                        ctf.newSpectator(cm.getPlayer());
                    } else {
                        cm.sendOk("Sorry but there is no match started in the map, so you can't spectate.");
                    }
                    cm.dispose();
                } else {
                    cm.sendOk("Sorry but there is no match in the map.");
                    cm.dispose();
                }
            }
        } else {
            cm.dispose();
        }
    }
}

function getEimForGuild(em, id) {
    var stringId = "" + id;
    return em.getInstance(stringId);
}