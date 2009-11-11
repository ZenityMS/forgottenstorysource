package net.sf.odinms.client.messages.commands;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import net.sf.odinms.client.Equip;
import net.sf.odinms.client.ExpTable;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.net.world.remote.WorldChannelInterface;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.messages.CommandDefinition;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.net.channel.handler.ChangeChannelHandler;
import net.sf.odinms.net.world.MapleParty;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.net.world.PartyOperation;
import net.sf.odinms.scripting.npc.NPCScriptManager;
import net.sf.odinms.server.MapleAchievements;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MapleLottery;
import net.sf.odinms.server.maps.FakeCharacter;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.server.maps.MapleMapObjectType;
import net.sf.odinms.server.maps.SavedLocationType;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.StringUtil;

public class PlayerCommands implements Command {

    public static int getOptionalIntArg(String splitted[], int position, int def) {
        if (splitted.length > position) {
            try {
                return Integer.parseInt(splitted[position]);
            } catch (NumberFormatException nfe) {
                return def;
            }
        }
        return def;
    }
    private int relationshipprompter = 0;
    public int promptrelationship;
    public String flagPromptName;

    public void addAP(MapleClient c, int stat, int amount) {
        MapleCharacter player = c.getPlayer();
        switch (stat) {
            case 1: // STR
                player.setStr(player.getStr() + amount);
                player.updateSingleStat(MapleStat.STR, player.getStr());
                break;
            case 2: // DEX
                player.setDex(player.getDex() + amount);
                player.updateSingleStat(MapleStat.DEX, player.getDex());
                break;
            case 3: // INT
                player.setInt(player.getInt() + amount);
                player.updateSingleStat(MapleStat.INT, player.getInt());
                break;
            case 4: // LUK
                player.setLuk(player.getLuk() + amount);
                player.updateSingleStat(MapleStat.LUK, player.getLuk());
                break;
            case 5: // HP
                player.setMaxHp(amount);
                player.updateSingleStat(MapleStat.MAXHP, player.getMaxHp());
                break;
            case 6: // MP
                player.setMaxMp(amount);
                player.updateSingleStat(MapleStat.MAXMP, player.getMaxMp());
                break;
        }
        if (!player.isGM()) {
            player.setRemainingAp(player.getRemainingAp() - amount);
        }
        player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
    }

    private ResultSet pvpRanking(boolean gm) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps;
            if (!gm) {
                ps = con.prepareStatement("SELECT pvpkills, level, name, job FROM characters WHERE gm < 3 ORDER BY pvpkills desc LIMIT 10");
            } else {
                ps = con.prepareStatement("SELECT name, gm FROM characters WHERE gm >= 3");
            }
            return ps.executeQuery();
        } catch (SQLException ex) {
            return null;
        }
    }

    private ResultSet bossCounterRanking(boolean gm) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps;
            if (!gm) {
                ps = con.prepareStatement("SELECT bosscounter, level, name, job FROM characters WHERE gm < 3 ORDER BY bosscounter desc LIMIT 10");
            } else {
                ps = con.prepareStatement("SELECT name, gm FROM characters WHERE gm >= 3");
            }
            return ps.executeQuery();
        } catch (SQLException ex) {
            return null;
        }
    }

    private ResultSet monsterKillRanking(boolean gm) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps;
            if (!gm) {
                ps = con.prepareStatement("SELECT mkills, level, name, job FROM characters WHERE gm < 3 ORDER BY mkills desc LIMIT 10");
            } else {
                ps = con.prepareStatement("SELECT name, gm FROM characters WHERE gm >= 3");
            }
            return ps.executeQuery();
        } catch (SQLException ex) {
            return null;
        }
    }

    private ResultSet ranking(boolean gm) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps;
            if (!gm) {
                ps = con.prepareStatement("SELECT reborns, level, name, job FROM characters WHERE gm < 3 ORDER BY reborns desc LIMIT 10");
            } else {
                ps = con.prepareStatement("SELECT name, gm FROM characters WHERE gm >= 3");
            }
            return ps.executeQuery();
        } catch (SQLException ex) {
            return null;
        }
    }

    private ResultSet rbRanking(boolean gm) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps;
            if (!gm) {
                ps = con.prepareStatement("SELECT reborns, level, name, job FROM characters WHERE gm < 3 ORDER BY reborns desc LIMIT 10");
            } else {
                ps = con.prepareStatement("SELECT name, gm FROM characters WHERE gm >= 3");
            }
            return ps.executeQuery();
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception {
        splitted[0] = splitted[0].toLowerCase();
        MapleCharacter player = c.getPlayer();
        ChannelServer cserv = c.getChannelServer();
        if (player.getMapId() != 200090300) {
            if (player.getMapId() != 1010000) {
                Collection<ChannelServer> cservs = ChannelServer.getAllInstances();
                if (splitted[0].equalsIgnoreCase("@commands") || splitted[0].equalsIgnoreCase("@help")) {
                    mc.dropMessage("================================================================");
                    mc.dropMessage("                  " + c.getChannelServer().getServerName() + " Commands");
                    mc.dropMessage("================================================================");
                    mc.dropMessage("@str/@dex/@int/@luk <number> ~ with these commands you will never have to add AP the slow way.");
                    mc.dropMessage("@buynx <number> ~ NX currency is currently 5000 for 1 nx point.");
                    mc.dropMessage("@save ~ Basically saves.");
                    mc.dropMessage("@rank ~ shows top 10 people.");
                    mc.dropMessage("@dispose ~ Turn off seeing smegas / Turn on.");
                    mc.dropMessage("@togglesmega ~ Turn off seeing smegas / Turn on.");
                    mc.dropMessage("@decieve ~ Sets your fame to -1337");
                    mc.dropMessage("@expfix ~ Got negative EXP ? Type this");
                    mc.dropMessage("@gm <message> ~ Call a gm for help (: ?");
                    mc.dropMessage("@checkstat ~ Allows you to check what your stats are. Including rebirths");
                    mc.dropMessage("@go ~ try it out, type @go");
                    mc.dropMessage("@bosscounter ~ shows your boss count :D");
                    mc.dropMessage("@revive ~ revives anyone on the channel besides yourself.");
                    mc.dropMessage("@cody,reward,reward1,storage,kin,nimakin,pot");
                    mc.dropMessage("@achievements shows you what achievements you have done.");
                    mc.dropMessage("@relationship ~ @relationship *charname* - Sends a relationship request to a partner");
                    mc.dropMessage("@getrelationship ~ @getrelationship *charname* - Checks if the character is in a relationship.");
                    mc.dropMessage("@pvpon ~ Turns your PVP Setting on for 10 minutes.");
                    mc.dropMessage("@pvpoff ~ Turns your PVP setting off");
                    mc.dropMessage("@president ~ Makes you the president of a channel for 50m Mesos");
                    mc.dropMessage("@presnotice ~ Makes a president's notice ~ Requirement: You must be president");
                    mc.dropMessage("@news ~ Shows you the news for MapleZtory");
                    mc.dropMessage("@godmode ~ Makes it where monsters do not follow you and you do not lose HP or MP for 200M mesos ands lasts 10 minutes");
                    mc.dropMessage("@multiplyme ~ Multiplies the mesos that drop from monsters by two for ten minutes - 50 Million Mesos");
                    mc.dropMessage("@territory ~ Opens the Guild Territory NPC");
                    mc.dropMessage("@charm ~ Opens the Charm Necklace NPC");
                    mc.dropMessage("@donator ~ Messages the GM's that you would like to be a donator");

                } else if (splitted[0].equalsIgnoreCase("@news")) {
                    mc.dropMessage("News of MapleZtory:");
                    for (String i : c.getPlayer().getNews()) {
                        mc.dropMessage(i);
                    }

                } else if (splitted[0].equalsIgnoreCase("@bosscounter")) {
                    mc.dropMessage("Your current boss counter: " + player.getBossCount());

                } else if (splitted[0].equalsIgnoreCase("@achievements") || splitted[0].equalsIgnoreCase("@goals")) {
                    mc.dropMessage("Your finished achievements:");
                    for (Integer i : c.getPlayer().getFinishedAchievements()) {
                        mc.dropMessage(MapleAchievements.getInstance().getById(i).getName() + " - " + MapleAchievements.getInstance().getById(i).getReward() + " NX.");
                    }

                    //} else if (player.getMapId() != 980000404) {
                } else if (splitted[0].equals("@str") || splitted[0].equals("@dex") || splitted[0].equals("@int") || splitted[0].equals("@luk") || splitted[0].equals("@hp") || splitted[0].equals("@mp")) {
                    if (splitted.length != 2) {
                        mc.dropMessage("Syntax: @<Stat> <amount>");
                        mc.dropMessage("Stat: <STR> <DEX> <INT> <LUK> <HP> <MP>");
                        return;
                    }
                    int x = Integer.parseInt(splitted[1]), max = 30000;
                    if (x > 0 && x <= player.getRemainingAp() && x < Short.MAX_VALUE) {
                        if (splitted[0].equals("@str") && x + player.getStr() < max) {
                            addAP(c, 1, x);
                        } else if (splitted[0].equals("@dex") && x + player.getDex() < max) {
                            addAP(c, 2, x);
                        } else if (splitted[0].equals("@int") && x + player.getInt() < max) {
                            addAP(c, 3, x);
                        } else if (splitted[0].equals("@luk") && x + player.getLuk() < max) {
                            addAP(c, 4, x);
                        } else if (splitted[0].equals("@hp") && x + player.getMaxHp() < max) {
                            addAP(c, 5, x);
                        } else if (splitted[0].equals("@mp") && x + player.getMaxMp() < max) {
                            addAP(c, 6, x);
                        } else {
                            mc.dropMessage("Make sure the stat you are trying to raise will not be over 30000.");
                        }
                    } else {
                        mc.dropMessage("Please make sure your AP is valid.");
                    }

                    //  } else if (splitted[0].equalsIgnoreCase("@allgoals")) {
                    //  player.dropMessage("All goals / achievements you can get:");
                    //player.dropMessage("" + MapleAchievements.getInstance().getAllAchievements()); // You have to have a string before you can put a list.

                } else if (splitted[0].equalsIgnoreCase("@finishedgoals")) {
                    mc.dropMessage("All of " + player.getName() + "'s finished goals:");
                    for (Integer i : c.getPlayer().getFinishedAchievements()) {
                        mc.dropMessage(MapleAchievements.getInstance().getById(i).getName() + " - " + MapleAchievements.getInstance().getById(i).getReward() + " NX, " + MapleAchievements.getInstance().getById(i).getReward2() + " Passion Points.");
                    }

                } else if (splitted[0].equalsIgnoreCase("@checkstats")) {
                    mc.dropMessage("Your stats are:");
                    mc.dropMessage("Str: " + player.getStr());
                    mc.dropMessage("Dex: " + player.getDex());
                    mc.dropMessage("Int: " + player.getInt());
                    mc.dropMessage("Luk: " + player.getLuk());
                    mc.dropMessage("Available AP: " + player.getRemainingAp());
                    mc.dropMessage("Rebirths: " + player.getReborns());
                } else if (splitted[0].equalsIgnoreCase("@buynx")) {
                    mc.dropMessage("Vote for vote points and use them to buy NX");

                } else if (splitted[0].equalsIgnoreCase("@save")) {
                    if (!player.getCheatTracker().Spam(900000, 0)) { // 15 minutes
                        player.saveToDB(true, false);
                        mc.dropMessageYellow("Saved, please have a fun time in MapleZtory!");
                    } else {
                        mc.dropMessageYellow("You cannot save more than once every 15 minutes.");
                    }
                } else if (splitted[0].equalsIgnoreCase("@expfix")) {
                    player.setExp(0);
                    player.updateSingleStat(MapleStat.EXP, player.getExp());
                } else if (splitted[0].equalsIgnoreCase("@go")) {
                    HashMap<String, Integer> maps = new HashMap<String, Integer>();
                    maps.put("fm", 910000000);
                    maps.put("henesys", 100000000);
                    maps.put("ellinia", 101000000);
                    maps.put("perion", 102000000);
                    maps.put("kerning", 103000000);
                    maps.put("lith", 104000000);
                    maps.put("sleepywood", 105040300);
                    maps.put("florina", 110000000);
                    maps.put("orbis", 200000000);
                    maps.put("happy", 209000000);
                    maps.put("elnath", 211000000);
                    maps.put("ludi", 220000000);
                    maps.put("aqua", 230000000);
                    maps.put("leafre", 240000000);
                    maps.put("mulung", 250000000);
                    maps.put("herb", 251000000);
                    maps.put("omega", 221000000);
                    maps.put("korean", 222000000);
                    maps.put("nlc", 600000000);
                    maps.put("excavation", 990000000);
                    maps.put("mushmom", 100000005);
                    maps.put("griffey", 240020101);
                    maps.put("manon", 240020401);
                    maps.put("horseman", 682000001);
                    maps.put("balrog", 105090900);
                    maps.put("showa", 801000000);
                    maps.put("guild", 200000301);
                    maps.put("shrine", 800000000);
                    maps.put("skelegon", 104040001);
                    maps.put("mall", 910000022);
                    maps.put("patshideout", 3);

                    if (splitted.length != 2) {
                        StringBuilder builder = new StringBuilder("Syntax: @go <mapname>");
                        int i = 0;
                        for (String mapss : maps.keySet()) {
                            if (1 % 10 == 0) {// 10 maps per line
                                mc.dropMessage(builder.toString());
                            } else {
                                builder.append(mapss + ", ");
                            }
                        }
                        mc.dropMessage(builder.toString());
                    } else if (maps.containsKey(splitted[1])) {
                        int map = maps.get(splitted[1]);
                        if (map == 910000000) {
                            player.saveLocation(SavedLocationType.FREE_MARKET);
                        }
                        player.changeMap(map);
                        mc.dropMessage("Please feel free to suggest any more locations");
                    } else {
                        mc.dropMessage("I could not find the map that you requested, go get an eye test.");
                    }
                    maps.clear();

                } else if (splitted[0].equalsIgnoreCase("@lottotest")) {
                    MapleLottery.buyTicket(player, 5000000);
                    mc.dropMessage("If you see this it worked.");
                } else if (splitted[0].equalsIgnoreCase("@gm")) {
                    if (splitted.length < 2) {
                        return;
                    }
                    if (!player.getCheatTracker().Spam(300000, 1)) { // 5 minutes.
                        try {
                            c.getChannelServer().getWorldInterface().broadcastGMMessage(null, MaplePacketCreator.serverNotice(6, "Channel: " + c.getChannel() + "  " + player.getName() + ": " + StringUtil.joinStringFrom(splitted, 1)).getBytes());
                        } catch (RemoteException ex) {
                            c.getChannelServer().reconnectWorld();
                        }
                    } else {
                        player.dropMessage(1, "Please don't flood GMs with your messages");
                    }

                } else if (splitted[0].equalsIgnoreCase("@donator")) {
                    if (!player.getCheatTracker().Spam(300000, 1)) { // 5 minutes.
                        try {
                            c.getChannelServer().getWorldInterface().broadcastGMMessage(null, MaplePacketCreator.serverNotice(5, player.getName() + " wants to be a donator! Please warp to them and tell them about it!!").getBytes());
                            player.dropMessage("A GM should be with you shortly");
                        } catch (RemoteException e) {
                            c.getChannelServer().reconnectWorld();
                        }
                    } else {
                        player.dropMessage(1, "I know you want to talk to the GM's but please don't spam them.");
                    }

                } else if (splitted[0].equalsIgnoreCase("@gm")) {
                    if (splitted.length < 2) {
                        return;
                    }
                    if (!player.getCheatTracker().Spam(1000, 5)) { // 5 minutes.
                        try {
                            c.getChannelServer().getWorldInterface().broadcastGMMessage(null, MaplePacketCreator.serverNotice(6, "Channel: " + c.getChannel() + "  " + player.getName() + ": " + StringUtil.joinStringFrom(splitted, 1)).getBytes());
                        } catch (RemoteException ex) {
                            c.getChannelServer().reconnectWorld();
                        }
                    } else {
                        player.dropMessage(1, "Please don't spam your family.");
                    }

                } else if (splitted[0].equalsIgnoreCase("@bluemessage")) {
                    if (player.getFamily() == 1) {
                        if (splitted.length < 2) {
                            return;
                        }
                        if (!player.getCheatTracker().Spam(1000, 5)) { // 5 minutes.
                            try {
                                c.getChannelServer().getWorldInterface().broadcastBlueFamilyMessage(null, MaplePacketCreator.serverNotice(5, "Family: " + player.getName() + ": " + StringUtil.joinStringFrom(splitted, 1)).getBytes());
                            } catch (RemoteException ex) {
                                c.getChannelServer().reconnectWorld();
                            }
                        } else {
                            player.dropMessage(1, "Please don't spam your family.");
                        }
                    } else {
                        player.dropMessage("You are not in the Blue Family!");
                    }

                } else if (splitted[0].equalsIgnoreCase("@redmessage")) {
                    if (player.getFamily() == 2) {
                        if (splitted.length < 2) {
                            return;
                        }
                        if (!player.getCheatTracker().Spam(1000, 5)) { // 5 minutes.
                            try {
                                c.getChannelServer().getWorldInterface().broadcastRedFamilyMessage(null, MaplePacketCreator.serverNotice(5, "Family: " + player.getName() + ": " + StringUtil.joinStringFrom(splitted, 1)).getBytes());
                            } catch (RemoteException ex) {
                                c.getChannelServer().reconnectWorld();
                            }
                        } else {
                            player.dropMessage(1, "Please don't spam your family.");
                        }
                    } else {
                        player.dropMessage("You are not in the Red Family!");
                    }

                } else if (splitted[0].equalsIgnoreCase("@togglesmega")) {
                    if (player.getMeso() >= 10000000) {
                        player.setSmegaEnabled(!player.getSmegaEnabled());
                        String text = (!player.getSmegaEnabled() ? "I blinded you with enchanted dog shit :) ! Now you may not see Avatar smega's" : "You magically grew ears that can see smegas T___T oh god that line is lame.");
                        mc.dropMessage(text);
                        player.gainMeso(-10000000, true);
                    } else {
                        mc.dropMessage("Wheres the mesos you idiot ! I want 10,000,000 meso");
                    }
                } else if (splitted[0].equalsIgnoreCase("@dispose")) {
                    NPCScriptManager.getInstance().dispose(c);
                    mc.dropMessage("You have been disposed.");
                } else if (splitted[0].equalsIgnoreCase("@rank")) {
                    ResultSet rs = ranking(false);
                    mc.dropMessageYellow("Top 10 Players: ");
                    int i = 1;
                    while (rs.next()) { // hey no clue what just happened
                        String job; // Should i make it so it shows the actual job ?
                        if (rs.getInt("job") >= 400 && rs.getInt("job") <= 422) {
                            job = "Thief";
                        } else if (rs.getInt("job") >= 300 && rs.getInt("job") <= 322) {
                            job = "Archer";
                        } else if (rs.getInt("job") >= 200 && rs.getInt("job") <= 232) {
                            job = "Mage";
                        } else if (rs.getInt("job") >= 100 && rs.getInt("job") <= 132) {
                            job = "Warrior";
                        } else if (rs.getInt("job") >= 500 && rs.getInt("job") <= 532) {
                            job = "Pirate";
                        } else {
                            job = "Beginner";
                        }
                        mc.dropMessageYellow(i + ". " + rs.getString("name") + "  ||  Job: " + job + "  ||  Rebirths: " + rs.getInt("reborns") + "  ||  Level: " + rs.getInt("level"));
                        i++;
                    }

                } else if (splitted[0].equalsIgnoreCase("@pvpranking")) {
                    mc.dropMessageYellow("Top 10 killers mah faw: ");
                    ResultSet rs = pvpRanking(false);
                    int i = 1;
                    while (rs.next()) {
                        mc.dropMessageYellow(i + ". " + rs.getString("name") + ": PVP Kills: " + rs.getInt("pvpkills"));
                        i++;
                    }

                } else if (splitted[0].equalsIgnoreCase("@rbranking")) {
                    mc.dropMessageYellow("Top 10 rebirthers: ");
                    ResultSet rs = rbRanking(false);
                    int i = 1;
                    while (rs.next()) {
                        mc.dropMessageYellow(i + ". " + rs.getString("name") + ": Rebirths: " + rs.getInt("reborns"));
                        i++;
                    }

                } else if (splitted[0].equalsIgnoreCase("@killranking")) {
                    mc.dropMessageYellow("Top 10 monster killers: ");
                    ResultSet rs = monsterKillRanking(false);
                    int i = 1;
                    while (rs.next()) {
                        mc.dropMessageYellow(i + ". " + rs.getString("name") + ": Monster Kills: " + rs.getInt("mkills"));
                        i++;
                    }

                } else if (splitted[0].equalsIgnoreCase("@bossranking")) {
                    mc.dropMessageYellow("Top 10 Boss Counters: ");
                    ResultSet rs = bossCounterRanking(false);
                    int i = 1;
                    while (rs.next()) {
                        mc.dropMessageYellow(i + ". " + rs.getString("name") + ": Boss Kills: " + rs.getInt("bosscounter"));
                        i++;
                    }

                } else if (splitted[0].equalsIgnoreCase("@rank")) {
                    ResultSet rs = ranking(false);
                    mc.dropMessageYellow("Top 10 Players: ");
                    int i = 1;
                    while (rs.next()) { // hey no clue what just happened
                        String job; // Should i make it so it shows the actual job ?
                        if (rs.getInt("job") >= 400 && rs.getInt("job") <= 422) {
                            job = "Thief";
                        } else if (rs.getInt("job") >= 300 && rs.getInt("job") <= 322) {
                            job = "Archer";
                        } else if (rs.getInt("job") >= 200 && rs.getInt("job") <= 232) {
                            job = "Mage";
                        } else if (rs.getInt("job") >= 100 && rs.getInt("job") <= 132) {
                            job = "Warrior";
                        } else if (rs.getInt("job") >= 500 && rs.getInt("job") <= 532) {
                            job = "Pirate";
                        } else {
                            job = "Beginner";
                        }
                        mc.dropMessageYellow(i + ". " + rs.getString("name") + "  ||  Job: " + job + "  ||  Rebirths: " + rs.getInt("reborns") + "  ||  Level: " + rs.getInt("level"));
                        i++;
                    }

                } else if (splitted[0].equalsIgnoreCase("@servergms")) {
                    ResultSet rs = ranking(true);
                    String gmType;
                    while (rs.next()) {
                        int gmLevl = rs.getInt("gm");
                        if (gmLevl == 3) {
                            gmType = "GM";
                        } else if (gmLevl >= 4) {
                            gmType = "Administrator";
                        } else {
                            gmType = "Error";
                        }
                        mc.dropMessage(rs.getString("name") + "  :  " + gmType);
                    }
                    rs.close();
                } else if (splitted[0].equalsIgnoreCase("@revive")) {
                    if (splitted.length == 2) {
                        MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                        if (player != victim) {
                            if (player.getMeso() >= 50000000) { // 50 mil
                                if (victim != null) {
                                    if (!victim.isAlive()) {
                                        victim.setHp(1);
                                        player.gainMeso(-50000000);
                                        victim.updateSingleStat(MapleStat.HP, 1);
                                        mc.dropMessage("You have revived " + victim.getName() + ".");
                                    } else {
                                        mc.dropMessage(victim.getName() + "is not dead.");
                                    }
                                } else {
                                    mc.dropMessage("The player is not online.");
                                }
                            } else {
                                mc.dropMessage("You need 50 million mesos to do this.");
                            }
                        } else {
                            mc.dropMessage("You can't revive yourself.");
                        }
                    } else {
                        mc.dropMessage("Syntax: @revive <player name>");
                    }
                } else if (splitted[0].equalsIgnoreCase("@banme")) {
                    c.getSession().write(MaplePacketCreator.sendGMPolice(0, "Being cool.", 1000000));
                } else if (splitted[0].equalsIgnoreCase("@clan")) {
                    NPCScriptManager.getInstance().start(c, 9201061, "ClanNPC", null);
                } else if (splitted[0].equalsIgnoreCase("@afk")) {
                    if (splitted.length >= 2) {
                        String name = splitted[1];
                        MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
                        if (victim == null) {
                            try {
                                WorldChannelInterface wci = c.getChannelServer().getWorldInterface();
                                int channel = wci.find(name);
                                if (channel == -1) {
                                    mc.dropMessage("This player is not online.");
                                    return;
                                }
                                victim = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(name);
                            } catch (RemoteException re) {
                                c.getChannelServer().reconnectWorld();
                            }
                        }
                        long blahblah = System.currentTimeMillis() - victim.getAfkTime();
                        if (Math.floor(blahblah / 60000) == 0) { // less than a minute
                            mc.dropMessage("Player has not been afk !");
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append(victim.getName());
                            sb.append(" has been afk for");
                            compareTime(sb, blahblah);
                            mc.dropMessage(sb.toString());
                        }
                    } else {
                        mc.dropMessage("Incorrect syntax");
                    }
                } else if (splitted[0].equalsIgnoreCase("@onlinetime")) {
                    if (splitted.length >= 2) {
                        String name = splitted[1];
                        MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
                        if (victim == null) {
                            try {
                                WorldChannelInterface wci = c.getChannelServer().getWorldInterface();
                                int channel = wci.find(name);
                                if (channel == -1) {
                                    mc.dropMessage("This player is not online.");
                                    return;
                                }
                                victim = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(name);
                            } catch (RemoteException re) {
                                c.getChannelServer().reconnectWorld();
                            }
                        }
                        long blahblah = System.currentTimeMillis() - victim.getLastLogin();
                        StringBuilder sb = new StringBuilder();
                        sb.append(victim.getName());
                        sb.append(" has been online for");
                        compareTime(sb, blahblah);
                        mc.dropMessage(sb.toString());
                    } else {
                        mc.dropMessage("Incorrect syntax");
                    }
                } else if (splitted[0].equals("@emo")) {
                    player.setHp(0);
                    player.updateSingleStat(MapleStat.HP, 0);
                } else if (splitted[0].equals("@rebirth") || splitted[0].equals("@rg")) {
                    if (player.getLevel() >= 200) {
                        player.doReborn();
                    } else {
                        mc.dropMessage("You must be at least level 200.");
                    }

                } else if (splitted[0].equals("@cloneme")) {
                    if (c.getPlayer().getMeso() >= 5000000) {
                        int clones = 1;
                        try {
                            clones = getOptionalIntArg(splitted, 1, 1);
                        } catch (NumberFormatException asdasd) {
                            clones = 1;
                        }
                        if (player.getFakeChars().size() >= 1) {
                            mc.dropMessage("You are not allowed to clone yourself more than once.");
                        } else {
                            for (int i = 0; i < clones && i + player.getFakeChars().size() <= 2; i++) {
                                FakeCharacter fc = new FakeCharacter(player, player.getId() + player.getFakeChars().size() + clones + i);
                                player.getFakeChars().add(fc);
                                c.getChannelServer().addClone(fc);
                            }
                            mc.dropMessage("The clone has now been created, hit a monster for 2x the damage you would normally do.");
                            player.gainMeso(-5000000, true);
                        }
                    } else {
                        mc.dropMessage("You do not have 5 million mesos.");
                    }
                } else if (splitted[0].equalsIgnoreCase("@autojob")) {
                    if (player.autojobon == false) {
                        player.autojobon = true;
                        player.dropMessage("Auto Job is now on!");
                    } else {
                        player.autojobon = false;
                        player.dropMessage("Auto Job is now off!");
                    }

                } else if (splitted[0].equals("@removeclones")) {
                    for (FakeCharacter fc : player.getFakeChars()) {
                        if (fc.getFakeChar().getMap() == player.getMap()) {
                            c.getChannelServer().getAllClones().remove(fc);
                            player.getMap().removePlayer(fc.getFakeChar());
                        }
                    }
                    player.getFakeChars().clear();
                    mc.dropMessage("All your clones in the map removed.");
                } else if (splitted[0].equals("@follow")) {
                    int slot = Integer.parseInt(splitted[1]);
                    FakeCharacter fc = player.getFakeChars().get(slot);
                    if (fc == null) {
                        mc.dropMessage("Clone does not exist.");
                    } else {
                        fc.setFollow(true);
                    }


                } else if (splitted[0].equals("@leaf")) {
                    if (player.getMeso() >= 1000000000) {
                        player.gainMeso(-1000000000);
                        MapleInventoryManipulator.addById(c, 4001126, (short) 1);
                    }
                } else if (splitted[0].equals("@monsterpoints")) { // wait look at this ;d l
                    mc.dropMessage("You have " + player.getMkills() + " monsterpoints.");
                } else if (splitted[0].equalsIgnoreCase("@fakerelog") || splitted[0].equalsIgnoreCase("!fakerelog")) {
                    c.getSession().write(MaplePacketCreator.getCharInfo(player));
                    player.getMap().removePlayer(player);
                    player.getMap().addPlayer(player);

                } else if (splitted[0].equalsIgnoreCase("@fm")) {
                    player.changeMap(910000000, 0);

                } else if (splitted[0].equalsIgnoreCase("@cody")) {
                    NPCScriptManager.getInstance().start(c, 9200000);

                } else if (splitted[0].equalsIgnoreCase("@charm")) {
                    NPCScriptManager.getInstance().start(c, 9201052);

                } else if (splitted[0].equalsIgnoreCase("@territory")) {
                    NPCScriptManager.getInstance().start(c, 9010001);

                } else if (splitted[0].equalsIgnoreCase("@family")) {
                    NPCScriptManager.getInstance().start(c, 9901315);

                } else if (splitted[0].equalsIgnoreCase("@storage")) {
                    player.getStorage().sendStorage(c, 2080005);
                } else if (splitted[0].equalsIgnoreCase("@news")) {
                    NPCScriptManager.getInstance().start(c, 9040011);
                } else if (splitted[0].equalsIgnoreCase("@kin")) {
                    NPCScriptManager.getInstance().start(c, 9900000);
                } else if (splitted[0].equalsIgnoreCase("@nimakin")) {
                    NPCScriptManager.getInstance().start(c, 9900001);
                } else if (splitted[0].equalsIgnoreCase("@reward")) {
                    NPCScriptManager.getInstance().start(c, 2050019);
                } else if (splitted[0].equalsIgnoreCase("@reward1")) {
                    NPCScriptManager.getInstance().start(c, 2020004);
                } else if (splitted[0].equalsIgnoreCase("@fredrick")) {
                    NPCScriptManager.getInstance().start(c, 9030000);
                } else if (splitted[0].equalsIgnoreCase("@spinel")) {
                    NPCScriptManager.getInstance().start(c, 9000020);
                } else if (splitted[0].equalsIgnoreCase("@goafk")) {
                    player.setChalkboard("I'm afk ! drop me a message <3");
                } else if (splitted[0].equalsIgnoreCase("@checkexp")) {
                    MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                    if (victim == null) {
                        mc.dropMessage("Player was not found in this channel.");
                    } else {
                        mc.dropMessage("EXP: " + victim.getExp() + "   " + victim.getExp() / ExpTable.getExpNeededForLevel(victim.getLevel() + 1) * 100 + " %");
                    }

                } else if (splitted[0].equals("@flaginfo")) {
                    player.dropMessage("Try to get the flag to the NPC on the other side. The leader will automatically be the first person to get the flag and if he gets hit it will be dropped and other party members will be able to loot it. If you get hit 5 times then you will be warped back to your team spawn point.");

                } else if (splitted[0].equalsIgnoreCase("@dropgame") || splitted[0].equalsIgnoreCase("@dg")) {
                    if (player.getMapId() >= 109010000 && player.getMapId() <= 109090000) {
                        return;
                    }
                    NPCScriptManager.getInstance().start(c, 2112010, null, null);
                } else if (splitted[0].equalsIgnoreCase("@boyhair")) {
                    if (player.getMapId() >= 109010000 && player.getMapId() <= 109090000) {
                        return;
                    }
                    NPCScriptManager.getInstance().start(c, 9900000, null, null);
                } else if (splitted[0].equalsIgnoreCase("@girlhair")) {
                    if (player.getMapId() >= 109010000 && player.getMapId() <= 109090000) {
                        return;
                    }
                    NPCScriptManager.getInstance().start(c, 9900001, null, null);
                } else if (splitted[0].equalsIgnoreCase("@shop")) {
                    if (player.getMapId() >= 109010000 && player.getMapId() <= 109090000) {
                        return;
                    }
                    NPCScriptManager.getInstance().start(c, 9270038, null, null);

                } else if (splitted[0].equalsIgnoreCase("@faq")) {
                    if (player.getMapId() >= 109010000 && player.getMapId() <= 109090000) {
                        return;
                    }
                    NPCScriptManager.getInstance().start(c, 9201092, null, null);

                } else if (splitted[0].equals("@accept")) {
                    MapleCharacter victim = cserv.getPlayerStorage().getCharacterById(relationshipprompter);
                    //    if (c.getPlayer().getAllianceInvited() != null) {
                    //     c.getPlayer().getAllianceInvited().addGuild2(c, c.getPlayer().getGuildId());
                    //   c.getPlayer().setAllianceInvited(null);
                    victim.setRelationship(player.getId());
                    player.setRelationship(relationshipprompter);
                    mc.dropMessage("Done! " + victim.getName() + " is now your mate.");
                    victim.dropMessage(player.getName() + " has accepted your request and is now your mate!");
                    relationshipprompter = 0;

                } else if (splitted[0].equals("@breakup")) {
                    MapleCharacter victim = cserv.getPlayerStorage().getCharacterById(player.getRelationship());
                    if (victim != null) {
                        victim.dropMessage(player.getName() + " has broken up with you.");
                        player.dropMessage("You have now broke up with " + MapleCharacter.getNameById(player.getRelationship(), 0));
                        victim.sendNote(player.getRelationship(), player.getName() + " has left the relationship.");
                        player.setRelationship(0);
                        victim.setRelationship(0);
                    } else {
                        player.dropMessage("The player is offline, use @leaverelationship to leave without notifying your partner.");
                    }
                } else if (splitted[0].equalsIgnoreCase("@rates") || splitted[0].equalsIgnoreCase("!rates")) {
                    c.getSession().write(MaplePacketCreator.sendYellowTip("EXP Rate: " + c.getChannelServer().getExpRate()));
                    c.getSession().write(MaplePacketCreator.sendYellowTip("Meso Rate: " + c.getChannelServer().getMesoRate()));
                    c.getSession().write(MaplePacketCreator.sendYellowTip("Drop Rate: " + c.getChannelServer().getDropRate()));
                } else if (splitted[0].equals("@leaverelationship")) {
                    MapleCharacter victim = cserv.getPlayerStorage().getCharacterById(player.getRelationship());
                    if (player.getRelationship() > 0) {
                        //        MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(player.getRelationship());
                        //  victim.dropMessage(player.getRelationship() + " has broken up with you.");
                        c.getPlayer().sendNote(player.getRelationship(), player.getName() + " has left the relationship. You have not left your relationship yet since you were secretly told this by me. To leave your relationship use @leaverelationship");
                        player.dropMessage("You have now left.");
                        player.setRelationship(0);
                    } else {
                        player.dropMessage("You can't leave a relationship if you are not in one.");
                    }
                } else if (splitted[0].equals("@spousechat")) {
                    MapleCharacter victim = cserv.getPlayerStorage().getCharacterById(player.getRelationship());
                    if (victim == null) {
                        if (player.getRelationship() < 1) {
                            //   if (!player.getRelationship().equals (null)) {
                            player.dropMessage("You are not in a relationship.");
                        } else {
                            player.dropMessage("Your partner is not online!");
                        }
                    } else {
                        victim.dropMessage("[Spouse]" + StringUtil.joinStringFrom(splitted, 1));
                        player.dropMessage("[Spouse]" + StringUtil.joinStringFrom(splitted, 1));
                    }

                } else if (splitted[0].equalsIgnoreCase("@gmlist")) {
                    ResultSet rs = ranking(true);
                    String gmType;
                    while (rs.next()) {
                        int gmLevl = rs.getInt("gm");
                        if (gmLevl == 3) {
                            gmType = "Game Master";
                        } else if (gmLevl == 4) {
                            gmType = "Administrator.";
                        } else if (gmLevl >= 5) {
                            gmType = "Owner.";
                        } else {
                            gmType = "Error";
                        }
                        mc.dropMessage(rs.getString("name") + "  :  " + gmType);
                    }

                } else if (splitted[0].equals("@getrelationship")) {
                    MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                    //    if (!victim.getRelationship().equals (null)) {
                    if (victim == null || victim.getRelationship() == 0) {
                        player.dropMessage("Syntax: -charname- or the character you are searhing for is not online. Or the character is not in a relationship.");
                    } else {
                        player.dropMessage(victim.getName() + "'s relationship partner is " + MapleCharacter.getNameById(victim.getRelationship(), 0));
                        // victim.dropMessage(player.getName() + " just tried to see if you were in a relationship, I think he/she/it likes you ;D"); kind of for the LOL
                    }

                } else if (splitted[0].equals("@decline")) {
                    MapleCharacter victim = cserv.getPlayerStorage().getCharacterById(relationshipprompter);
                    //   if (c.getPlayer().getAllianceInvited() != null) {
                    //     c.getPlayer().setAllianceInvited(null);
                    mc.dropMessage("Done.");
                    relationshipprompter = 0;
                    victim.dropMessage(player.getName() + " has denied your request. Please try again later.");

                } else if (splitted[0].equalsIgnoreCase("@presinfo")) {
                    player.dropMessage("This makes you president of a channel for the cost of 50 million mesos. When you are president you receieve 3X more EXP from monsters. Use @president to try it out.");

                } else if (splitted[0].equalsIgnoreCase("@president")) {
                    if (player.warned2 == false) {
                        player.dropMessage("You may want to use @presinfo before using this. Use this command again if you are sure.");
                        player.warned2 = true;
                    } else {
                        if (!ChannelServer.getInstance(player.getClient().getChannel()).president.equalsIgnoreCase(player.getName())) {
                            if (!ChannelServer.getInstance(player.getClient().getChannel()).president.equalsIgnoreCase("None")) {
                                if (player.getMeso() > 50000000) {
                                    player.gainMeso(-50000000);
                                    cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, player.getName() + " is now the president of  Channel " + c.getChannel() + " and has kicked " + player.getClient().getChannelServer().president + " out of office!").getBytes());
                                    ChannelServer.getInstance(player.getClient().getChannel()).president = player.getName();
                                } else {
                                    player.dropMessage("You don't have 50 Million mesos!");
                                }
                            } else {
                                if (player.getMeso() > 50000000) {
                                    player.gainMeso(-50000000);
                                    cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, player.getName() + " is now the president of Channel " + c.getChannel()).getBytes());
                                    player.getClient().getChannelServer().president = player.getName();
                                } else {
                                    player.dropMessage("You don't have 50 Million mesos!");
                                }
                            }
                        } else {
                            player.dropMessage("You are already the president of this channel");

                        }
                    }

                } else if (splitted[0].equalsIgnoreCase("@relationship")) {
                    MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                    if (!player.getCheatTracker().Spam(10000, 12)) {
                        if (victim != null) { // victim.getRelationship().equalsIgnoreCase (null)) {
                            if (victim.getRelationship() == 0 || player.getRelationship() == 0) {
                                if (relationshipprompter == 0) {
                                    if (splitted[1] != null) {
                                        // if (victim.getRelationship().equals (null)) {
                                        promptrelationship = 1;
                                        relationshipprompter = player.getId();
                                        victim.dropMessage(player.getName() + " wants to be your boyfriend/girlfriend/biosexual mate. Use @accept to accept the request, use @decline to decline the request.");
                                        player.dropMessage("Sent, the request is pending...");
                                    } else {
                                        player.dropMessage("Syntax: relationship -Charname-");
                                    }
                                } else {
                                    player.dropMessage("A relationship request is already pending. Just use @decline to stop the request.");
                                }
                            } else {
                                player.dropMessage("The character is already in a relationship or you are already in a relationship");
                            }
                        } else {
                            player.dropMessage("Syntax: relationship -Charname- or the Character is offline.");
                        }
                    } else {
                        player.dropMessage("You can only send a request every 10 seconds.");
                    }

                } else if (splitted[0].equalsIgnoreCase("@playernotice")) {
                    if (!player.getCheatTracker().Spam(10000, 13) && player.getReborns() >= 3) { // 10 minutes.
                        if (splitted.length > 1) {
                            try {
                                cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, " [Player: " + player.getName() + "] " + StringUtil.joinStringFrom(splitted, 1)).getBytes());
                            } catch (RemoteException e) {
                                cserv.reconnectWorld();
                            }
                        } else {
                            mc.dropMessage("Syntax: @notice <message>");
                        }
                    } else {
                        mc.dropMessage("You don't have 3 rebirths or you have already done this within the last 10 seconds.");
                    }

                } else if (splitted[0].equalsIgnoreCase("@presnotice")) {
                    if (!player.getCheatTracker().Spam(10000, 14) && player.getClient().getChannelServer().president.equalsIgnoreCase(player.getName())) { // 10 minutes.
                        if (splitted.length > 1) {
                            try {
                                cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, " [President of Channel " + c.getChannel() + ": " + player.getName() + "] " + StringUtil.joinStringFrom(splitted, 1)).getBytes());
                            } catch (RemoteException e) {
                                cserv.reconnectWorld();
                            }
                        } else {
                            mc.dropMessage("Syntax: @presnotice <message>");
                        }
                    } else {
                        mc.dropMessage("You are not the president of channel " + c.getChannel() + " or you have already done this within the last 10 seconds.");
                    }

                } else if (splitted[0].equalsIgnoreCase("@donate")) {
                    if (player.warned == false) {
                        player.dropMessage("This is the donation command made for MapleZtory. You are now being warned, are you sure you want to spend one maple leaf on the donations needed to start the event? Use the command if you want to do so. The current needed leaves is " + player.getClient().getChannelServer().neededleaves + ". Please note that all channels are separated in this.");
                        player.warned = true;
                    } else {
                        if (player.getClient().getChannelServer().neededleaves != 0) {
                            if (player.haveItem(4001126, 5, false, true)) {
                                player.finishAchievement(29);
                                MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4001126, 5, false, false);
                                player.getClient().getChannelServer().neededleaves = player.getClient().getChannelServer().neededleaves - 1;
                                if (player.getClient().getChannelServer().neededleaves == 0) {
                                    cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, player.getName() + " has donated the final leaf needed to start the event in Channel " + c.getChannel() + "! To start it click Lord in Henesys.").getBytes());
                                } else {
                                    cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, "A donation has been made by " + player.getName() + ". We need " + player.getClient().getChannelServer().neededleaves + " more leaves to start the event in Channel " + c.getChannel() + "!").getBytes());
                                }
                            } else {
                                player.dropMessage("You don't have a maple leaf to donate!");
                            }
                        } else {
                            player.dropMessage("The event can already be started! Click Lord in Henesys");
                        }
                    }

                } else if (splitted[0].equals("@godmode")) {
                    if (player.getMeso() >= 100000000) {
                        player.startAndEndInvincibility();
                        player.gainMeso(-100000000);
                    } else {
                        player.dropMessage("You don't have 100 Million mesos. Good luck in farming some more! P.S. beg and spam pat for mesos.");
                    }
                } else if (splitted[0].equals("@multiplyme")) {
                    if (player.getMeso() >= 50000000) {
                        player.mesoMultiplier();
                        player.gainMeso(-50000000);
                    } else {
                        player.dropMessage("You don't have 50 Million mesos. Good luck in farming some more! P.S. beg and spam pat for mesos.");
                    }

                } else if (splitted[0].equalsIgnoreCase("@pvpon")) {
                    if (player.getMeso() >= 50000000) {
                        if (!player.getPvpSetting()) {
                            player.gainMeso(-50000000);
                            player.setPvpPlayerSettingOn();
                            player.dropMessage("Your PVP setting is set to on!");
                            player.dropMessage("If you change channels or DC then your PVP will be set back to off, there will be no refunds so use carefully! Unless of course it is your birthday ;D");
                        } else {
                            player.dropMessage("Your PVP setting is already on. I don't want to steal your mes0rz D:");
                        }
                    } else {
                        player.dropMessage("You do not have 10 million mesos! You currently only have " + player.getMeso() + ".");
                    }

                } else if (splitted[0].equalsIgnoreCase("@pvpoff")) {
                    if (player.warned == false) {
                        if (player.getPvpSetting()) {
                            player.warned = true;
                            player.dropMessage("Are you sure you want to end your PVP time early? Use this command again if you are 100% sure!");
                        } else {
                            player.dropMessage("Your PVP setting isn't on!");
                        }
                    } else {
                        player.setPvpSetting(false);
                        player.warned = false;
                        // TimerManager.getInstance().stop();
                        player.dropMessage("Very well, you were warned, your PVP setting is now set to off.");
                    }

                } else if (splitted[0].equalsIgnoreCase("@votepoints") || splitted[0].equalsIgnoreCase("@votepoint")) {
                    Connection con = DatabaseConnection.getConnection();
                    PreparedStatement ps = (PreparedStatement) con.prepareStatement("SELECT * FROM voterewards WHERE name = ?");
                    ps.setString(1, c.getAccountName());
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        player.addVotepoint(player.votesNotClaimed()); // New method should count the votepoints
                        mc.dropMessage("You have gained " + player.votesNotClaimed() + " votepoint(s)");
                    } else {
                        mc.dropMessage("You don't have any vote points that are unclaimed, to get vote points go to our website.");
                    }
                    rs.close();
                    ps.close();
                    PreparedStatement pse = (PreparedStatement) con.prepareStatement("DELETE FROM voterewards WHERE name = ?");
                    pse.setString(1, c.getAccountName());
                    pse.executeUpdate();
                    pse.close();

                } else if (splitted[0].equalsIgnoreCase("@addbuddyroom") || splitted[0].equalsIgnoreCase("!addbuddyroom")) {
                    if (c.getPlayer().getMeso() >= 10000000) {
                        c.getPlayer().setBuddyCapacity(100);
                        c.getPlayer().gainMeso(-1000000, true);
                    }
                } else if (splitted[0].equals("@unstuck")) {
                    java.sql.Connection con = DatabaseConnection.getConnection();
                    PreparedStatement ps;
                    try {
                        ps = con.prepareStatement("UPDATE `accounts` SET loggedin = 0 WHERE loggedin = 2");
                        ps.executeUpdate();
                        mc.dropMessage("Account unsticked.");
                    } catch (SQLException e) {
                        mc.dropMessageYellow("The account is NOT stuck!uiz : " + e);
                    }
                } else if (splitted[0].equalsIgnoreCase("@changegender")) {
                    if (c.getPlayer().getMeso() >= 5000000) {
                        MapleCharacter f = c.getPlayer();
                        f.setGender(f.getGender() == 1 ? 0 : 1);
                        f.getClient().getSession().write(MaplePacketCreator.getCharInfo(f));
                        f.getMap().removePlayer(f);
                        f.getMap().addPlayer(f);
                        mc.dropMessageYellow("Your gender has been changed");
                        player.gainMeso(-5000000, true);
                    }

                } else if (splitted[0].equals("@votetest")) {
                    c.getSession().write(MaplePacketCreator.sendLinky("http://www.gtop100.com/in.php?site=37246")); // I doubt this will work.
                    mc.dropMessageYellow("Thanks for voting MapleZtory!!");

                } else if (splitted[0].equalsIgnoreCase("@note")) {
                    if (c.getPlayer().getMeso() >= 5000000) {
                        MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                        int accid = MapleCharacter.getAccIdFromCNAME(splitted[1]); // to check if the character exists
                        if (accid != -1) {
                            if (victim.getNoteCount() <= 5) {
                                player.gainMeso(-5000000, true);
                                c.getPlayer().sendNote2(MapleCharacter.getIdByName(splitted[1], 0), StringUtil.joinStringFrom(splitted, 2));
                                player.dropMessage("You have sent a message too " + splitted[1] + ".");
                                player.dropMessage("The player has received your message. Even if the target was offline he will receive it when he logs in.");
                                if (victim != null) {
                                    victim.showNote();
                                }
                            } else {
                                player.dropMessage("The player's inbox is currently full, please check back later.");
                            }
                        } else {
                            mc.dropMessage("Syntax: @note 'Character Name' 'Message'");
                        }
                    } else {
                        mc.dropMessage("Make sure you have 5 Million Mesos!");
                    }

                } else if (splitted[0].equalsIgnoreCase("@partyfix")) {
                    WorldChannelInterface wci = c.getChannelServer().getWorldInterface();
                    MapleParty party = player.getParty();
                    MaplePartyCharacter partyplayer = new MaplePartyCharacter(player);
                    party = wci.getParty(player.getPartyId());
                    if (partyplayer.equals(party.getLeader())) { // disband
                        wci.updateParty(party.getId(), PartyOperation.DISBAND, partyplayer);
                        if (player.getEventInstance() != null) {
                            player.getEventInstance().disbandParty();
                        }
                    } else {
                        player.dropMessage("You are not the leader of the party.");
                    }

                } else if (splitted[0].equalsIgnoreCase("@multiplyme")) {
                    if (player.getMeso() >= 50000000) {
                        player.mesoMultiplier();
                        player.dropMessage("Your mesos are now being multiplied by two from the monsters you kill. If you log off or change channels this will be turned off.");
                    } else {
                        player.dropMessage("You don't have 50 Million Mesos");
                    }


                } else if (splitted[0].equalsIgnoreCase("@clearitems")) {
                    if (player.getMapId() == 910000006 || player.getMapId() == 221000000) {
                        MapleMap map = player.getMap();
                        double range = Double.POSITIVE_INFINITY;
                        java.util.List<MapleMapObject> items = map.getMapObjectsInRange(player.getPosition(), range, Arrays.asList(MapleMapObjectType.ITEM));
                        for (MapleMapObject itemmo : items) {
                            map.removeMapObject(itemmo);
                            map.broadcastMessage(MaplePacketCreator.removeItemFromMap(itemmo.getObjectId(), 0, player.getId()));
                        }
                        mc.dropMessage("You have destroyed " + items.size() + " items on the ground.");
                    } else {
                        player.dropMessage("You can only use this in FM6 or the BossHunterPQ Map");
                    }

                } else if (splitted[0].equalsIgnoreCase("@cc")) {
                    if (!player.banned) {
                        if (!player.getCheatTracker().Spam(3000, 23)) { // 3 seconds
                        if (player.getClient().getChannel() != Integer.parseInt(splitted[1])) {
                            try {
                                ChangeChannelHandler.changeChannel(Integer.parseInt(splitted[1]), c);
                                mc.dropMessageYellow("You are now changing channels, please wait.");
                            } catch (Exception e) {
                                player.dropMessage("Please select a channel to change too");
                            }
                        } else {
                            player.dropMessage("You can't change to the channel you are already in");
                        }
                        } else {
                            mc.dropMessageYellow("Hello " + c.getPlayer().getName() + " wait 3 seconds before trying to change channels again.");
                        }
                    } else {
                        return;
                    }


                } else if (splitted[0].equalsIgnoreCase("@beta")) {
                    int itemid;
                    short multiply;
                    try {
                        itemid = 1012076;
                        multiply = 80;
                    } catch (NumberFormatException e) {
                        return;
                    }
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    IItem item = ii.getEquipById(itemid);
                    if (player.isBetaChar() && !player.haveItem(itemid, 1, true, true)) {
                        MapleInventoryManipulator.addFromDrop(c, ii.hardcoreItem((Equip) item, multiply));
                        mc.dropMessageYellow("Thanks for participating in MapleZtory Beta!");
                    } else {
                        mc.dropMessageYellow("Your character was not in MapleZtory Beta or you already have the mask! Sorry!");
                    }

                } else if (splitted[0].equalsIgnoreCase("@info")) {
                    mc.dropMessageYellow("Your have:");
                    mc.dropMessageYellow("Rebirth points: " + player.getRebirthPoints());
                    mc.dropMessageYellow("Passion Points: " + player.getPassionPoints());
                    mc.dropMessageYellow("Jump Points: " + player.getJumpPoints());
                    mc.dropMessageYellow("PVP Kills: " + player.getPvpKills());
                    mc.dropMessageYellow("Rebirths: " + player.getReborns());
                    mc.dropMessageYellow("Donator Level: " + player.getDonatorLevel());

                } else if (splitted[0].equals("@rstars")) {
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    for (IItem stars : c.getPlayer().getInventory(MapleInventoryType.USE).list()) {
                        if (ii.isThrowingStar(stars.getItemId())) {
                            stars.setQuantity(ii.getSlotMax(c, stars.getItemId()));
                            c.getSession().write(MaplePacketCreator.updateInventorySlot(MapleInventoryType.USE, (Item) stars));
                        }
                        mc.dropMessageYellow("Your stars have been recharged.");
                    }

                } else if (splitted[0].equalsIgnoreCase("@monsterkills")) {
                    player.dropMessage("You have " + player.getMkills() + " Monster Kills");

                } else if (splitted[0].equalsIgnoreCase("@snowball")) {
                    if (player.getClient().getChannelServer().snowballOn) {
                        int map = 109060000;
                        if (player.getClient().getChannelServer().snowballTeam0 < player.getClient().getChannelServer().snowballTeam1) {
                            player.changeMap(map, 0);
                            player.getClient().getChannelServer().snowballTeam0 = player.getClient().getChannelServer().snowballTeam0 + 1;
                        } else {
                            if (player.getClient().getChannelServer().snowballTeam1 == player.getClient().getChannelServer().snowballTeam1) {
                                player.changeMap(map, 1);
                                player.getClient().getChannelServer().snowballTeam1 = player.getClient().getChannelServer().snowballTeam1 + 1;
                            } else {
                                if (player.getClient().getChannelServer().snowballTeam0 > player.getClient().getChannelServer().snowballTeam1) {
                                    player.changeMap(map, 0);
                                    player.getClient().getChannelServer().snowballTeam1 = player.getClient().getChannelServer().snowballTeam1 + 1;
                                }
                            }
                        }
                    } else {
                        player.dropMessage("The snowball event has not started yet.");
                    }


                } else if (splitted[0].equals("@fmnpc") || splitted[0].equals("@allnpc")) {
                    if (player.getMapId() >= 109010000 && player.getMapId() <= 109090000) {
                        return;
                    }
                    NPCScriptManager.getInstance().start(c, 9110000, null, null);

                } else if (splitted[0].equalsIgnoreCase("@token")) {
                    if (player.getMeso() >= 50000000 && player.getDonatorLevel() >= 1) {
                        player.gainMeso(-50000000);
                        player.gainItem(4031874, (short) 1, false, false);
                        player.dropMessage("You have gained a donation token.");
                    } else {
                        mc.dropMessageYellow("You do not have 50,000,000 mesos or you are not a donator.");
                    }
                } else if (splitted[0].equalsIgnoreCase("@joinevent")) {
                    NPCScriptManager.getInstance().start(c, 2101);
                    mc.dropMessage("If this doesn't work you need to be at the same channel as the event!");

                } else if (splitted[0].equalsIgnoreCase("@giftplayernx")) {
                    // Made by Rich of MapleZtory
                    MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                    if (victim != null) {
                        int amount;
                        try {
                            amount = Integer.parseInt(splitted[2]);
                        } catch (NumberFormatException e) {
                            return;
                        }
                        int type = getOptionalIntArg(splitted, 3, 1);
                        if (player.getCSPoints(type) >= amount) {
                            if (amount > 0) {
                                if (victim.getId() != player.getId()) {
                                    victim.modifyCSPoints(type, amount);
                                    player.modifyCSPoints(type, -amount);
                                    victim.dropMessage(5, player.getName() + " has gifted you " + amount + " NX points.");
                                    mc.dropMessage("You have gifted " + victim + " " + amount + " NX");
                                    mc.dropMessage("You have lost " + amount + " NX");
                                } else {
                                    player.dropMessage("You can't gift yourself NX...");
                                }
                            } else {
                                player.dropMessage("Please don't be a pansy");
                            }
                        } else {
                            player.dropMessage("You don't have the ammount of NX you are trying to gift, you currently have " + player.getCSPoints(type) + " NX");
                        }
                    } else {
                        mc.dropMessage("The player is not found, try switching to the same channel as him/her");
                    }

                } else if (splitted[0].equalsIgnoreCase("@pointnpc")) {
                    if (player.getMapId() >= 109010000 && player.getMapId() <= 109090000) {
                        return;
                    }
                    NPCScriptManager.getInstance().start(c, 2131006, null, null);

                } else {
                    mc.dropMessage(splitted[0] + " is not a valid command.");
                }
            } else {
                player.dropMessage("You cannot exit this way, click the NPC in the middle of the field to get out.");
            }
        } else {
            player.dropMessage("You cannot use these commands in jail.");
        }
    }

    private void compareTime(StringBuilder sb, long timeDiff) {
        double secondsAway = timeDiff / 1000;
        double minutesAway = 0;
        double hoursAway = 0;

        while (secondsAway > 60) {
            minutesAway++;
            secondsAway -= 60;
        }
        while (minutesAway > 60) {
            hoursAway++;
            minutesAway -= 60;
        }
        boolean hours = false;
        boolean minutes = false;
        if (hoursAway > 0) {
            sb.append(" ");
            sb.append((int) hoursAway);
            sb.append(" hours");
            hours = true;
        }
        if (minutesAway > 0) {
            if (hours) {
                sb.append(" -");
            }
            sb.append(" ");
            sb.append((int) minutesAway);
            sb.append(" minutes");
            minutes = true;
        }
        if (secondsAway > 0) {
            if (minutes) {
                sb.append(" and");
            }
            sb.append(" ");
            sb.append((int) secondsAway);
            sb.append(" seconds !");
        }
    }

    @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[]{
                    new CommandDefinition("str", 0),
                    new CommandDefinition("dex", 0),
                    new CommandDefinition("int", 0),
                    new CommandDefinition("luk", 0),
                    new CommandDefinition("hp", 0),
                    new CommandDefinition("mp", 0),
                    new CommandDefinition("checkstats", 0),
                    new CommandDefinition("commands", 0),
                    new CommandDefinition("buynx", 0),
                    new CommandDefinition("save", 0),
                    new CommandDefinition("expfix", 0),
                    new CommandDefinition("go", 0),
                    new CommandDefinition("gm", 0),
                    new CommandDefinition("togglesmega", 0),
                    new CommandDefinition("deceive", 0),
                    new CommandDefinition("monsterpoints", 0),
                    new CommandDefinition("info", 0),
                    new CommandDefinition("dispose", 0),
                    new CommandDefinition("rank", 0),
                    new CommandDefinition("servergms", 0),
                    new CommandDefinition("revive", 0),
                    new CommandDefinition("banme", 0),
                    new CommandDefinition("clan", 0),
                    new CommandDefinition("afk", 0),
                    new CommandDefinition("onlinetime", 0),
                    new CommandDefinition("emo", 0),
                    new CommandDefinition("rebirth", 0),
                    new CommandDefinition("reborn", 0),
                    new CommandDefinition("leaf", 0),
                    new CommandDefinition("kin", 0),
                    new CommandDefinition("nimakin", 0),
                    new CommandDefinition("reward", 0),
                    new CommandDefinition("reward1", 0),
                    new CommandDefinition("cody", 0),
                    new CommandDefinition("storage", 0),
                    new CommandDefinition("fakerelog", 0),
                    new CommandDefinition("fredrick", 0),
                    new CommandDefinition("news", 0),
                    new CommandDefinition("spinel", 0),
                    new CommandDefinition("news", 0),
                    new CommandDefinition("goafk", 0),
                    new CommandDefinition("achievements", 0),
                    new CommandDefinition("boyhair", 0),
                    new CommandDefinition("girlhair", 0),
                    new CommandDefinition("allnpc", 0),
                    new CommandDefinition("fmnpc", 0),
                    new CommandDefinition("help", 0),
                    new CommandDefinition("clearinv", 0),
                    new CommandDefinition("go", 0),
                    new CommandDefinition("emo", 0),
                    new CommandDefinition("evilemo", 0),
                    new CommandDefinition("rebirth", 0),
                    new CommandDefinition("achievements", 0),
                    new CommandDefinition("cody", 0),
                    new CommandDefinition("shop", 0),
                    new CommandDefinition("accept", 0),
                    new CommandDefinition("territory", 0),
                    new CommandDefinition("decline", 0),
                    new CommandDefinition("dropgame", 0),
                    new CommandDefinition("dg", 0),
                    new CommandDefinition("note", 0),
                    new CommandDefinition("playernotice", 0),
                    new CommandDefinition("giftplayernx", 0),
                    new CommandDefinition("autojob", 0),
                    new CommandDefinition("pvpon", 0),
                    new CommandDefinition("pvpoff", 0),
                    new CommandDefinition("relationship", 0),
                    new CommandDefinition("getrelationship", 0),
                    new CommandDefinition("breakup", 0),
                    new CommandDefinition("spousechat", 0),
                    new CommandDefinition("leaverelationship", 0),
                    new CommandDefinition("snowball", 0),
                    new CommandDefinition("spinel", 0),
                    new CommandDefinition("votepoints", 0),
                    new CommandDefinition("votepoint", 0),
                    new CommandDefinition("donate", 0),
                    new CommandDefinition("faq", 0),
                    new CommandDefinition("pointnpc", 0),
                    new CommandDefinition("president", 0),
                    new CommandDefinition("presinfo", 0),
                    new CommandDefinition("presnotice", 0),
                    new CommandDefinition("no", 0),
                    new CommandDefinition("join", 0),
                    new CommandDefinition("flaginfo", 0),
                    new CommandDefinition("removeclones", 0),
                    new CommandDefinition("follow", 0),
                    new CommandDefinition("stance", 0),
                    new CommandDefinition("news", 0),
                    new CommandDefinition("partyfix", 0),
                    new CommandDefinition("gmlist", 0),
                    new CommandDefinition("rates", 0),
                    new CommandDefinition("clearitems", 0),
                    new CommandDefinition("cloneme", 0),
                    new CommandDefinition("info", 0),
                    new CommandDefinition("addbuddyroom", 0),
                    new CommandDefinition("unstuck", 0),
                    new CommandDefinition("changegender", 0),
                    new CommandDefinition("joinevent", 0),
                    new CommandDefinition("rbranking", 0),
                    new CommandDefinition("pvpranking", 0),
                    new CommandDefinition("killranking", 0),
                    new CommandDefinition("godmode", 0),
                    new CommandDefinition("multiplyme", 0),
                    new CommandDefinition("monsterkills", 0),
                    new CommandDefinition("bluemessage", 0),
                    new CommandDefinition("logoff", 0),
                    new CommandDefinition("autorebirth", 0),
                    new CommandDefinition("bosscounter", 0),
                    new CommandDefinition("redmessage", 0),
                    // new CommandDefinition("family", 0),
                    new CommandDefinition("fm", 0),
                    new CommandDefinition("lottotest", 0),
                    new CommandDefinition("bossranking", 0),
                    new CommandDefinition("finishedgoals", 0),
                    new CommandDefinition("allgoals", 0),
                    new CommandDefinition("goals", 0),
                    new CommandDefinition("votetest", 0),
                    new CommandDefinition("beta", 0),
                    new CommandDefinition("token", 0),
                    new CommandDefinition("donator", 0),
                    new CommandDefinition("charm", 0),
                    new CommandDefinition("cc", 0),
                    new CommandDefinition("checkexp", 0)
                };
    }
}
