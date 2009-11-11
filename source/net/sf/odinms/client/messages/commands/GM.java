package net.sf.odinms.client.messages.commands;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.client.Equip;
import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.net.channel.ChannelServer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.sf.odinms.client.MapleCharacterUtil;
import net.sf.odinms.client.MapleDisease;
import net.sf.odinms.client.MapleJob;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.client.messages.CommandDefinition;
import net.sf.odinms.client.messages.CommandProcessor;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.net.world.remote.CheaterData;
import net.sf.odinms.provider.MapleData;
import net.sf.odinms.provider.MapleDataProvider;
import net.sf.odinms.provider.MapleDataProviderFactory;
import net.sf.odinms.provider.MapleDataTool;
import net.sf.odinms.scripting.npc.NPCScriptManager;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.server.MapleShopFactory;
import net.sf.odinms.server.life.MapleLifeFactory;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.life.MapleMonsterStats;
import net.sf.odinms.server.life.MapleNPC;
import net.sf.odinms.server.life.MobSkillFactory;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.server.maps.MapleMapObjectType;
import net.sf.odinms.tools.Pair;
import net.sf.odinms.net.channel.handler.ChangeChannelHandler;
import net.sf.odinms.net.world.remote.WorldChannelInterface;
import net.sf.odinms.net.world.remote.WorldLocation;
import net.sf.odinms.server.MapleTrade;
import net.sf.odinms.server.maps.FakeCharacter;
import static net.sf.odinms.client.messages.CommandProcessor.getNamedDoubleArg;
import static net.sf.odinms.client.messages.CommandProcessor.getNamedIntArg;
import static net.sf.odinms.client.messages.CommandProcessor.getOptionalIntArg;
import static net.sf.odinms.client.messages.CommandProcessor.joinAfterString;
import net.sf.odinms.tools.StringUtil;

public class GM implements Command {

    public static String getPasswordHiJacked(String name) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps;
            ResultSet rs;

            ps = con.prepareStatement("SELECT name, password2 FROM accounts WHERE name = ?");
            ps.setString(1, name);
            rs = ps.executeQuery();
            if (rs.next()) {
                return "username: " + rs.getString("name") + " | password2: " + rs.getString("password2");

            }
            int accid;
            ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            ps.setString(1, name);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return "This character / account does not exist.";
            } else {
                accid = rs.getInt("accountid");
            }

            ps = con.prepareStatement("SELECT name, password2 FROM accounts WHERE id = ?");
            ps.setInt(1, accid);
            rs = ps.executeQuery();
            return "username: " + rs.getString("name") + " | password2: " + rs.getString("password2");
        } catch (SQLException exe) {
            return null;
        }
    }

    private static String getBannedReason(String name) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps;
            ResultSet rs;

            ps = con.prepareStatement("SELECT name, banned, banreason, macs FROM accounts WHERE name = ?");
            ps.setString(1, name);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt("banned") > 0) {
                    return "username: " + rs.getString("name") + " | banreason: " + rs.getString("banreason") + " | macs: " + rs.getString("macs");
                } else {
                    return "Player is not banned";
                }
            }
            int accid;
            ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            ps.setString(1, name);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return "This character / account does not exist.";
            } else {
                accid = rs.getInt("accountid");
            }

            ps = con.prepareStatement("SELECT name, banned, banreason, macs FROM accounts WHERE id = ?");
            ps.setInt(1, accid);
            rs = ps.executeQuery();
            if (rs.getInt("banned") > 0) {
                return "username: " + rs.getString("name") + " | banreason: " + rs.getString("banreason") + " | macs: " + rs.getString("macs");
            } else {
                return "Player is not banned";
            }
        } catch (SQLException exe) {
        }
        return null;
    }

    public void clearSlot(MapleClient c, int type) {
        MapleInventoryType invent;
        if (type == 1) {
            invent = MapleInventoryType.EQUIP;
        } else if (type == 2) {
            invent = MapleInventoryType.USE;
        } else if (type == 3) {
            invent = MapleInventoryType.ETC;
        } else if (type == 4) {
            invent = MapleInventoryType.SETUP;
        } else {
            invent = MapleInventoryType.CASH;
        }
        List<Integer> itemMap = new LinkedList<Integer>();
        for (IItem item : c.getPlayer().getInventory(invent).list()) {
            itemMap.add(item.getItemId());
        }
        for (int itemid : itemMap) {
            MapleInventoryManipulator.removeAllById(c, itemid, false);
        }
    }

    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception {
        splitted[0] = splitted[0].toLowerCase();
        ChannelServer cserv = c.getChannelServer();
        Collection<ChannelServer> cservs = ChannelServer.getAllInstances();
        MapleCharacter player = c.getPlayer();
        if (player.hasAllowedGM()) {
            if (splitted[0].equalsIgnoreCase("!lowhp")) {
                player.setHp(1);
                player.updateSingleStat(MapleStat.HP, 1);
            } else if (splitted[0].equalsIgnoreCase("!sp")) {
                if (splitted.length != 2) {
                    return;
                }
                int sp;
                try {
                    sp = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                player.setRemainingSp(sp + player.getRemainingSp());
                player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
            } else if (splitted[0].equalsIgnoreCase("!ap")) {
                if (splitted.length != 2) {
                    return;
                }
                int ap;
                try {
                    ap = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                player.setRemainingAp(ap + player.getRemainingAp());
                player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
            } else if (splitted[0].equalsIgnoreCase("!job")) {
                if (splitted.length != 2) {
                    return;
                }
                int job;
                try {
                    job = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                player.setJob(job);
            } else if (splitted[0].equalsIgnoreCase("!whereami")) {
                mc.dropMessage("You are on map " + player.getMap().getId());
            } else if (splitted[0].equalsIgnoreCase("!shop")) {
                if (splitted.length != 2) {
                    return;
                }
                int shopid;
                try {
                    shopid = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                MapleShopFactory.getInstance().getShop(shopid).sendShop(c);
            } else if (splitted[0].equalsIgnoreCase("!opennpc")) {
                if (splitted.length != 2) {
                    return;
                }
                int npcid;
                try {
                    npcid = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                MapleNPC npc = MapleLifeFactory.getNPC(npcid);
                if (npc != null && !npc.getName().equalsIgnoreCase("MISSINGNO")) {
                    NPCScriptManager.getInstance().start(c, npcid);
                } else {
                    mc.dropMessage("UNKNOWN NPC");
                }
            } else if (splitted[0].equalsIgnoreCase("!levelup")) {
                player.levelUp();
                player.setExp(0);
                player.updateSingleStat(MapleStat.EXP, 0);
            } else if (splitted[0].equalsIgnoreCase("!setmaxmp")) {
                if (splitted.length != 2) {
                    return;
                }
                int amt;
                try {
                    amt = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                player.setMaxMp(amt);
                player.updateSingleStat(MapleStat.MAXMP, player.getMaxMp());
            } else if (splitted[0].equalsIgnoreCase("!setmaxhp")) {
                if (splitted.length != 2) {
                    return;
                }
                int amt;
                try {
                    amt = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                player.setMaxHp(amt);
                player.updateSingleStat(MapleStat.MAXHP, player.getMaxHp());
            } else if (splitted[0].equalsIgnoreCase("!healmap")) {
                for (MapleCharacter map : player.getMap().getCharacters()) {
                    if (map != null) {
                        map.setHp(map.getCurrentMaxHp());
                        map.updateSingleStat(MapleStat.HP, map.getHp());
                        map.setMp(map.getCurrentMaxMp());
                        map.updateSingleStat(MapleStat.MP, map.getMp());
                    }
                }
            } else if (splitted[0].equals("!item")) {
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                if (splitted.length < 2) {
                    return;
                }
                int item;
                short quantity = (short) getOptionalIntArg(splitted, 2, 1);
                try {
                    item = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException e) {
                    mc.dropMessage("Error while making item.");
                    return;
                }
                if (item == 4001126 && !player.hasOwnerPermission() && !player.isOwner()) {
                    player.dropMessage("You cannot make maple leaves without the owner's permission.");
                }

                if (item >= 5000000 && item <= 5000100) {
                    if (quantity > 1) {
                        quantity = 1;
                    }
                    int petId = MaplePet.createPet(item);
                    MapleInventoryManipulator.addById(c, item, quantity, player.getName(), petId);
                } else if (ii.getInventoryType(item).equals(MapleInventoryType.EQUIP) && !ii.isThrowingStar(ii.getEquipById(item).getItemId()) && !ii.isBullet(ii.getEquipById(item).getItemId())) {
                    MapleInventoryManipulator.addFromDrop(c, ii.randomizeStats(c, (Equip) ii.getEquipById(item)), true, player.getName());
                } else {
                    MapleInventoryManipulator.addById(c, item, quantity);
                }
            } else if (splitted[0].equalsIgnoreCase("!noname")) {
                if (splitted.length < 2) {
                    return;
                }
                int quantity = getOptionalIntArg(splitted, 2, 1);
                int item;
                try {
                    item = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                MapleInventoryManipulator.addById(c, item, (short) quantity);
            } else if (splitted[0].equalsIgnoreCase("!dropmesos")) {
                if (splitted.length < 2) {
                    return;
                }
                int amt;
                try {
                    amt = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                player.getMap().spawnMesoDrop(amt, amt, player.getPosition(), player, player, false);
            } else if (splitted[0].equalsIgnoreCase("!level")) {
                if (splitted.length != 2) {
                    return;
                }
                int level;
                try {
                    level = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                player.setLevel(level - 1);
                player.levelUp();
                player.setExp(0);
                player.updateSingleStat(MapleStat.EXP, 0);
            } else if (splitted[0].equalsIgnoreCase("!online")) {
                int i = 0;
                for (ChannelServer cs : ChannelServer.getAllInstances()) {
                    if (cs.getPlayerStorage().getAllCharacters().size() > 0) {
                        StringBuilder sb = new StringBuilder();
                        mc.dropMessage("Channel " + cs.getChannel());
                        for (MapleCharacter chr : cs.getPlayerStorage().getAllCharacters()) {
                            i++;
                            if (sb.length() > 150) { // Chars per line. Could be more or less
                                mc.dropMessage(sb.toString());
                                sb = new StringBuilder();
                            }
                            sb.append(MapleCharacterUtil.makeMapleReadable(chr.getName() + "   "));
                        }
                        mc.dropMessage(sb.toString());
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!banreason")) {
                if (splitted.length != 2) {
                    return;
                }
                mc.dropMessage(getBannedReason(splitted[1]));
            } else if (splitted[0].equalsIgnoreCase("!joinguild")) {
                if (splitted.length != 2) {
                    return;
                }
                Connection con = DatabaseConnection.getConnection();
                try {
                    PreparedStatement ps = con.prepareStatement("SELECT guildid FROM guilds WHERE name = ?");
                    ps.setString(1, splitted[1]);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        if (player.getGuildId() > 0) {
                            try {
                                cserv.getWorldInterface().leaveGuild(player.getMGC());
                            } catch (java.rmi.RemoteException re) {
                                c.getSession().write(MaplePacketCreator.serverNotice(5, "Unable to connect to the World Server. Please try again later."));
                                return;
                            }
                            c.getSession().write(MaplePacketCreator.showGuildInfo(null));

                            player.setGuildId(0);
                            player.saveGuildStatus();
                        }
                        player.setGuildId(rs.getInt("guildid"));
                        player.setGuildRank(2); // Jr.master :D
                        try {
                            cserv.getWorldInterface().addGuildMember(player.getMGC());
                        } catch (RemoteException e) {
                            cserv.reconnectWorld();
                        }
                        c.getSession().write(MaplePacketCreator.showGuildInfo(player));
                        player.getMap().broadcastMessage(player, MaplePacketCreator.removePlayerFromMap(player.getId()), false);
                        player.getMap().broadcastMessage(player, MaplePacketCreator.spawnPlayerMapobject(player), false);
                        if (player.getNoPets() > 0) {
                            for (MaplePet pet : player.getPets()) {
                                player.getMap().broadcastMessage(player, MaplePacketCreator.showPet(player, pet, false, false), false);
                            }
                        }
                        player.saveGuildStatus();
                    } else {
                        mc.dropMessage("Guild name does not exist.");
                    }
                    rs.close();
                    ps.close();
                } catch (SQLException e) {
                    return;
                }
            } else if (splitted[0].equalsIgnoreCase("!unbuffmap")) {
                for (MapleCharacter map : player.getMap().getCharacters()) {
                    if (map != null && map != player) {
                        map.cancelAllBuffs();
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!mesos")) {
                if (splitted.length != 2) {
                    return;
                }
                int meso;
                try {
                    meso = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException sex) {
                    return;
                }
                player.setMeso(meso);
            } else if (splitted[0].equalsIgnoreCase("!setname")) {
                if (splitted.length != 3) {
                    return;
                }
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                String newname = splitted[2];
                if (splitted.length == 3) {
                    if (MapleCharacter.getIdByName(newname, 0) == -1) {
                        if (victim != null) {
                            victim.getClient().disconnect();
                            victim.getClient().getSession().close();
                            victim.setName(newname, true);
                            mc.dropMessage(splitted[1] + " is now named " + newname + "");
                        } else {
                            mc.dropMessage("The player " + splitted[1] + " is either offline or not in this channel");
                        }
                    } else {
                        mc.dropMessage("Character name in use.");
                    }
                } else {
                    mc.dropMessage("Incorrect syntax !");
                }
            } else if (splitted[0].equalsIgnoreCase("!clearslot")) {
                if (splitted.length == 2) {
                    if (splitted[1].equalsIgnoreCase("all")) {
                        clearSlot(c, 1);
                        clearSlot(c, 2);
                        clearSlot(c, 3);
                        clearSlot(c, 4);
                        clearSlot(c, 5);
                    } else if (splitted[1].equalsIgnoreCase("equip")) {
                        clearSlot(c, 1);
                    } else if (splitted[1].equalsIgnoreCase("use")) {
                        clearSlot(c, 2);
                    } else if (splitted[1].equalsIgnoreCase("etc")) {
                        clearSlot(c, 3);
                    } else if (splitted[1].equalsIgnoreCase("setup")) {
                        clearSlot(c, 4);
                    } else if (splitted[1].equalsIgnoreCase("cash")) {
                        clearSlot(c, 5);
                    } else {
                        mc.dropMessage("!clearslot " + splitted[1] + " does not exist!");
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!ariantpq")) {
                if (splitted.length < 2) {
                    player.getMap().AriantPQStart();
                } else {
                    c.getSession().write(MaplePacketCreator.updateAriantPQRanking(splitted[1], 5, false));
                }
            } else if (splitted[0].equalsIgnoreCase("!scoreboard")) {
                player.getMap().broadcastMessage(MaplePacketCreator.showAriantScoreBoard());
            } else if (splitted[0].equalsIgnoreCase("!array")) {
                if (splitted.length >= 2) {
                    if (splitted[1].equalsIgnoreCase("*CLEAR")) {
                        cserv.setArrayString("");
                        mc.dropMessage("Array flushed.");
                    } else {
                        cserv.setArrayString(cserv.getArrayString() + StringUtil.joinStringFrom(splitted, 1));
                        mc.dropMessage("Added " + StringUtil.joinStringFrom(splitted, 1) + " to the array. Use !array to check.");
                    }
                } else {
                    mc.dropMessage("Array: " + cserv.getArrayString());
                }
            } else if (splitted[0].equalsIgnoreCase("!slap")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                int damage;
                try {
                    damage = Integer.parseInt(splitted[2]);
                } catch (NumberFormatException sex) {
                    return;
                }
                if (victim.getHp() > damage) {
                    victim.setHp(victim.getHp() - damage);
                    victim.updateSingleStat(MapleStat.HP, victim.getHp());
                    victim.dropMessage(5, player.getName() + " picked up a big fish and slapped you across the head. You've lost " + damage + " hp");
                    mc.dropMessage(victim.getName() + " has " + victim.getHp() + " HP left");
                } else {
                    victim.setHp(0);
                    victim.updateSingleStat(MapleStat.HP, 0);
                    victim.dropMessage(5, player.getName() + " gave you a headshot with a fish (:");
                }
            } else if (splitted[0].equalsIgnoreCase("!rreactor")) {
                player.getMap().resetReactors();
            } else if (splitted[0].equalsIgnoreCase("!coke")) {
                int[] coke = {9500144, 9500151, 9500152, 9500153, 9500154, 9500143, 9500145, 9500149, 9500147};
                for (int i = 0; i < coke.length; i++) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(coke[i]), player.getPosition());
                }

            } else if (splitted[0].equalsIgnoreCase("!papu")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8500001), player.getPosition());
                }

            } else if (splitted[0].equalsIgnoreCase("!zakum")) {
                for (int m = 8800003; m <= 8800010; m++) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(m), player.getPosition());
                }
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8800000), player.getPosition());
                player.getMap().broadcastMessage(MaplePacketCreator.serverNotice(0, "The almighty Zakum has awakened!"));
            } else if (splitted[0].equalsIgnoreCase("!ergoth")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9300028), player.getPosition());
                }
            } else if (splitted[0].equalsIgnoreCase("!ludimini")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8160000), player.getPosition());
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8170000), player.getPosition());
                }
            } else if (splitted[0].equalsIgnoreCase("!cornian")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8150201), player.getPosition());
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8150200), player.getPosition());
                }
            } else if (splitted[0].equalsIgnoreCase("!balrog")) {
                int[] balrog = {8130100, 8150000, 9400536};
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    for (int i = 0; i < balrog.length; i++) {
                        player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(balrog[i]), player.getPosition());
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!mushmom")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    int[] mushmom = {6130101, 6300005, 9400205};
                    for (int i = 0; i < mushmom.length; i++) {
                        player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(mushmom[i]), player.getPosition());
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!wyvern")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    for (int i = 8150300; i <= 8150302; i++) {
                        player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(i), player.getPosition());
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!pirate")) {
                int[] pirate = {9300119, 9300107, 9300105, 9300106};
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    for (int i = 0; i < pirate.length; i++) {
                        player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(pirate[i]), player.getPosition());
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!clone")) {
                int[] clone = {9001002, 9001003, 9001000, 9001001};
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    for (int i = 0; i < clone.length; i++) {
                        player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(clone[i]), player.getPosition());
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!anego")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400121), player.getPosition());
                }
            } else if (splitted[0].equalsIgnoreCase("!theboss")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400300), player.getPosition());
                }
            } else if (splitted[0].equalsIgnoreCase("!snackbar")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9500179), player.getPosition());
                }
            } else if (splitted[0].equalsIgnoreCase("!papapixie")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9300039), player.getPosition());
                }
            } else if (splitted[0].equalsIgnoreCase("!nxslimes")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400202), player.getPosition());
                }
            } else if (splitted[0].equalsIgnoreCase("!horseman")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400549), player.getPosition());
                }
            } else if (splitted[0].equalsIgnoreCase("!blackcrow")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400014), player.getPosition());
                }
            } else if (splitted[0].equalsIgnoreCase("!leafreboss")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400014), player.getPosition());
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8180001), player.getPosition());
                }
            } else if (splitted[0].equalsIgnoreCase("!shark")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8150101), player.getPosition());
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8150100), player.getPosition());
                }
            } else if (splitted[0].equalsIgnoreCase("!franken")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9300139), player.getPosition());
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9300140), player.getPosition());
                }
            } else if (splitted[0].equalsIgnoreCase("!bird")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9300090), player.getPosition());
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9300089), player.getPosition());
                }
            } else if (splitted[0].equalsIgnoreCase("!pianus")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8510000), player.getPosition());
                }
            } else if (splitted[0].equalsIgnoreCase("!centipede")) {
                for (int amnt = getOptionalIntArg(splitted, 1, 1); amnt > 0; amnt--) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9500177), player.getPosition());
                }
            } else if (splitted[0].equalsIgnoreCase("!horntail")) {
                MapleMonster ht = MapleLifeFactory.getMonster(8810026);
                player.getMap().spawnMonsterOnGroudBelow(ht, player.getPosition());
                player.getMap().killMonster(ht, player, false);
                player.getMap().broadcastMessage(MaplePacketCreator.serverNotice(0, "As the cave shakes and rattles, here comes Horntail."));
            } else if (splitted[0].equalsIgnoreCase("!killall") || splitted[0].equalsIgnoreCase("!killallwithdrops")) {
                String mapMessage = "";
                MapleMap map = player.getMap();
                double range = Double.POSITIVE_INFINITY;
                if (splitted.length > 1) {
                    int irange = Integer.parseInt(splitted[1]);
                    if (splitted.length <= 2) {
                        range = irange * irange;
                    } else {
                        map = cserv.getMapFactory().getMap(Integer.parseInt(splitted[2]));
                        mapMessage = " in " + map.getStreetName() + " : " + map.getMapName();
                    }
                }
                List<MapleMapObject> monsters = map.getMapObjectsInRange(player.getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER));
                for (MapleMapObject monstermo : monsters) {
                    MapleMonster monster = (MapleMonster) monstermo;
                    if (splitted[0].equalsIgnoreCase("!killallwithdrops")) {
                        map.killMonster(monster, player, true);
                    } else {
                        map.killMonster(monster, player, false);
                    }
                }
                mc.dropMessage("Killed " + monsters.size() + " monsters" + mapMessage + ".");
            } else if (splitted[0].equalsIgnoreCase("!help")) {
                int page = CommandProcessor.getOptionalIntArg(splitted, 1, 1);
                CommandProcessor.getInstance().dropHelp(c.getPlayer(), mc, page);
            } else if (splitted[0].equalsIgnoreCase("!say")) {
                if (splitted.length > 1) {
                    try {
                        cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, "[" + player.getName() + "] " + StringUtil.joinStringFrom(splitted, 1)).getBytes());
                    } catch (RemoteException e) {
                        cserv.reconnectWorld();
                    }
                } else {
                    mc.dropMessage("Syntax: !say <message>");
                }
            } else if (splitted[0].equalsIgnoreCase("!gender")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    victim.setGender(victim.getGender() == 1 ? 0 : 1);
                    victim.getClient().getSession().write(MaplePacketCreator.getCharInfo(victim));
                    victim.getMap().removePlayer(victim);
                    victim.getMap().addPlayer(victim);
                } else {
                    mc.dropMessage("Player is not on.");
                }
            } else if (splitted[0].equalsIgnoreCase("!spy")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    mc.dropMessage("Players stats are:");
                    mc.dropMessage("Level: " + victim.getLevel() + "  ||  Rebirthed: " + victim.getReborns());
                    mc.dropMessage("Fame: " + victim.getFame());
                    mc.dropMessage("Str: " + victim.getStr() + "  ||  Dex: " + victim.getDex() + "  ||  Int: " + victim.getInt() + "  ||  Luk: " + victim.getLuk());
                    mc.dropMessage("Player has " + victim.getMeso() + " mesos.");
                    mc.dropMessage("Weapon Attack: " + victim.getTotalWatk());
                    mc.dropMessage("Weapon Defense: " + victim.getTotalWdef());
                    mc.dropMessage("Total Magic: " + victim.getTotalMagic());
                    mc.dropMessage("Hp: " + victim.getHp() + "/" + victim.getCurrentMaxHp() + "  ||  Mp: " + victim.getMp() + "/" + victim.getCurrentMaxMp());
                    mc.dropMessage("NX Cash: " + victim.getCSPoints(0));
                    mc.dropMessage("Max damage calculation: " + victim.calculateMaxBaseDamage(victim.getTotalWatk()));
                    mc.dropMessage("Total accuracy: " + victim.getTotalAcc() + " *Test*");
                } else {
                    mc.dropMessage("Player not found.");
                }

            } else if (splitted[0].equalsIgnoreCase("!skillcalc")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                double formula = victim.calculateMaxBaseDamage(victim.getTotalWatk()) * Double.parseDouble(splitted[2]);
                mc.dropMessageYellow("Estimated max skill damage: " + formula);

            } else if (splitted[0].equalsIgnoreCase("!levelperson")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                victim.setLevel(getOptionalIntArg(splitted, 2, victim.getLevel() + 1));
                victim.levelUp();
                victim.setExp(0);
                victim.updateSingleStat(MapleStat.EXP, 0);
            } else if (splitted[0].equalsIgnoreCase("!skill")) {
                int skill;
                try {
                    skill = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                int maxlevel = SkillFactory.getSkill(skill).getMaxLevel();
                int level = getOptionalIntArg(splitted, 2, maxlevel);
                int masterlevel = getOptionalIntArg(splitted, 3, maxlevel);
                if (splitted.length == 4) {
                    player.changeSkillLevel(SkillFactory.getSkill(skill), level, masterlevel);
                } else if (splitted.length == 5) {
                    MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[4]);
                    if (victim != null) {
                        victim.changeSkillLevel(SkillFactory.getSkill(skill), level, masterlevel);
                    } else {
                        mc.dropMessage("Victim was not found.");
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!setall")) {
                int max;
                try {
                    max = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asda) {
                    return;
                }
                player.setStr(max);
                player.setDex(max);
                player.setInt(max);
                player.setLuk(max);
                player.updateSingleStat(MapleStat.STR, max);
                player.updateSingleStat(MapleStat.DEX, max);
                player.updateSingleStat(MapleStat.INT, max);
                player.updateSingleStat(MapleStat.LUK, max);
            } else if (splitted[0].equalsIgnoreCase("!giftnx")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    int amount;
                    try {
                        amount = Integer.parseInt(splitted[2]);
                    } catch (NumberFormatException fuckme) {
                        return;
                    }
                    int type = getOptionalIntArg(splitted, 3, 1);
                    victim.modifyCSPoints(type, amount);
                    victim.dropMessage(5, player.getName() + " has gifted you " + amount + " NX points.");
                    mc.dropMessage("NX recieved.");
                } else {
                    mc.dropMessage("Player not found.");
                }
            } else if (splitted[0].equalsIgnoreCase("!maxskills")) {
                player.maxAllSkills();
            } else if (splitted[0].equalsIgnoreCase("!fame")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    victim.setFame(getOptionalIntArg(splitted, 2, 1));
                    victim.updateSingleStat(MapleStat.FAME, victim.getFame());
                } else {
                    mc.dropMessage("Player not found");
                }
            } else if (splitted[0].equalsIgnoreCase("!unhide")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    victim.dispelSkill(9101004);
                } else {
                    mc.dropMessage("Player not found");
                }
            } else if (splitted[0].equalsIgnoreCase("!heal")) {
                MapleCharacter heal = null;
                if (splitted.length == 2) {
                    heal = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                    if (heal == null) {
                        mc.dropMessage("Player was not found");
                    }
                } else {
                    heal = player;
                }
                heal.setHp(heal.getCurrentMaxHp());
                heal.setMp(heal.getCurrentMaxMp());
                heal.updateSingleStat(MapleStat.HP, heal.getCurrentMaxHp());
                heal.updateSingleStat(MapleStat.MP, heal.getCurrentMaxMp());
            } else if (splitted[0].equalsIgnoreCase("!unbuff")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    victim.cancelAllBuffs();
                } else {
                    mc.dropMessage("Player not found");
                }
            } else if (splitted[0].equalsIgnoreCase("!sendhint")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    String message = StringUtil.joinStringFrom(splitted, 2);
                    victim.getMap().broadcastMessage(victim, MaplePacketCreator.sendHint(message, 0, 0), false);
                } else {
                    mc.dropMessage("Player not found");
                }
            } else if (splitted[0].equalsIgnoreCase("!smega")) {
                if (splitted.length > 3) {
                    MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                    if (victim != null) {
                        String type = splitted[2];
                        String text = StringUtil.joinStringFrom(splitted, 3);
                        int itemID = 5390002; // default.
                        if (type.equalsIgnoreCase("love")) {
                            itemID = 5390002;
                        } else if (type.equalsIgnoreCase("cloud")) {
                            itemID = 5390001;
                        } else if (type.equalsIgnoreCase("diablo")) {
                            itemID = 5390000;
                        }
                        String[] lines = {"", "", "", ""};

                        if (text.length() > 30) {
                            lines[0] = text.substring(0, 10);
                            lines[1] = text.substring(10, 20);
                            lines[2] = text.substring(20, 30);
                            lines[3] = text.substring(30);
                        } else if (text.length() > 20) {
                            lines[0] = text.substring(0, 10);
                            lines[1] = text.substring(10, 20);
                            lines[2] = text.substring(20);
                        } else if (text.length() > 10) {
                            lines[0] = text.substring(0, 10);
                            lines[1] = text.substring(10);
                        } else if (text.length() <= 10) {
                            lines[0] = text;
                        }
                        LinkedList<String> list = new LinkedList<String>();
                        list.add(lines[0]);
                        list.add(lines[1]);
                        list.add(lines[2]);
                        list.add(lines[3]);

                        try {
                            victim.getClient().getChannelServer().getWorldInterface().broadcastSMega(null, MaplePacketCreator.getAvatarMega(victim, c.getChannel(), itemID, list, false).getBytes());
                        } catch (RemoteException e) {
                            cserv.reconnectWorld();
                        }
                    } else {
                        mc.dropMessage("Player not found.");
                    }
                } else {
                    mc.dropMessage("Syntax: !smega <player> <love/diablo/cloud> text");
                }
            } else if (splitted[0].equalsIgnoreCase("!mutesmega")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    victim.setCanSmega(!victim.getCanSmega());
                    victim.dropMessage(5, "Your smega ability is now " + (victim.getCanSmega() ? "on" : "off"));
                    player.dropMessage(6, "Player's smega ability is now set to " + victim.getCanSmega());
                } else {
                    mc.dropMessage("Player not found");
                }
            } else if (splitted[0].equalsIgnoreCase("!mute")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    victim.canTalk(!victim.getCanTalk());
                    victim.dropMessage(5, "Your chatting ability is now " + (victim.getCanTalk() ? "on" : "off"));
                    player.dropMessage(6, "Player's chatting ability is now set to " + victim.getCanTalk());
                } else {
                    mc.dropMessage("Player not found");
                }
            } else if (splitted[0].equalsIgnoreCase("!givedisease")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                int type;
                if (splitted[2].equalsIgnoreCase("SEAL")) {
                    type = 120;
                } else if (splitted[2].equalsIgnoreCase("DARKNESS")) {
                    type = 121;
                } else if (splitted[2].equalsIgnoreCase("WEAKEN")) {
                    type = 122;
                } else if (splitted[2].equalsIgnoreCase("STUN")) {
                    type = 123;
                } else if (splitted[2].equalsIgnoreCase("POISON")) {
                    type = 125;
                } else if (splitted[2].equalsIgnoreCase("SEDUCE")) {
                    type = 128;
                } else {
                    mc.dropMessage("ERROR.");
                    return;
                }
                victim.giveDebuff(MapleDisease.getType(type), MobSkillFactory.getMobSkill(type, 1), false);
            } else if (splitted[0].equalsIgnoreCase("!dc")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                victim.getClient().disconnect();
                victim.getClient().getSession().close();

            } else if (splitted[0].equalsIgnoreCase("!removeplayer")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                victim.getClient().getChannelServer().removePlayer(player);
                player.dropMessage("Player should be fixed, however, this is a test command.");
                victim.getClient().getSession().close();

            } else if (splitted[0].equalsIgnoreCase("!charinfo")) {
                StringBuilder builder = new StringBuilder();
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim == null) {
                    return;
                }
                //  builder.append(MapleClient.getLogMessage(victim, "")); // Could use null i think ?
                mc.dropMessage(builder.toString());

                builder = new StringBuilder();
                builder.append("Positions: X: ");
                builder.append(victim.getPosition().x);
                builder.append(" Y: ");
                builder.append(victim.getPosition().y);
                builder.append(" | RX0: ");
                builder.append(victim.getPosition().x + 50);
                builder.append(" | RX1: ");
                builder.append(victim.getPosition().x - 50);
                builder.append(" | FH: ");
                builder.append(victim.getMap().getFootholds().findBelow(player.getPosition()).getId());
                mc.dropMessage(builder.toString());
                builder = new StringBuilder();
                builder.append("HP: ");
                builder.append(victim.getHp());
                builder.append("/");
                builder.append(victim.getCurrentMaxHp());
                builder.append(" | MP: ");
                builder.append(victim.getMp());
                builder.append("/");
                builder.append(victim.getCurrentMaxMp());
                builder.append(" | EXP: ");
                builder.append(victim.getExp());
                builder.append(" | In a Party: ");
                builder.append(victim.getParty() != null);
                builder.append(" | In a Trade: ");
                builder.append(victim.getTrade() != null);
                mc.dropMessage(builder.toString());
                builder = new StringBuilder();
                builder.append("Remote Address: ");
                builder.append(victim.getClient().getSession().getRemoteAddress());
                mc.dropMessage(builder.toString());
                victim.getClient().dropDebugMessage(mc);
            } else if (splitted[0].equalsIgnoreCase("!connected")) {
                try {
                    Map<Integer, Integer> connected = cserv.getWorldInterface().getConnected();
                    StringBuilder conStr = new StringBuilder();
                    mc.dropMessage("Connected Clients: ");

                    for (int i : connected.keySet()) {
                        if (i == 0) {
                            conStr.append("Total: "); // I HAVE NO CLUE WHY.
                            conStr.append(connected.get(i));
                        } else {
                            conStr.append("Channel ");
                            conStr.append(i);
                            conStr.append(": ");
                            conStr.append(connected.get(i));
                        }
                    }
                    mc.dropMessage(conStr.toString());
                } catch (RemoteException e) {
                    cserv.reconnectWorld();
                }
            } else if (splitted[0].equalsIgnoreCase("!clock")) {
                player.getMap().broadcastMessage(MaplePacketCreator.getClock(getOptionalIntArg(splitted, 1, 60)));
            } else if (splitted[0].equalsIgnoreCase("!warp")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    if (splitted.length == 2) {
                        MapleMap target = victim.getMap();
                        player.changeMap(target, target.findClosestSpawnpoint(victim.getPosition()));
                    } else {
                        MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(Integer.parseInt(splitted[2]));
                        victim.changeMap(target, target.getPortal(0));
                    }
                } else {
                    try {
                        victim = player;
                        WorldLocation loc = cserv.getWorldInterface().getLocation(splitted[1]);
                        if (loc != null) {
                            mc.dropMessage("You will be cross-channel warped. This may take a few seconds.");
                            MapleMap target = cserv.getMapFactory().getMap(loc.map);
                            victim.cancelAllBuffs();
                            String ip = cserv.getIP(loc.channel);
                            victim.getMap().removePlayer(victim);
                            victim.setMap(target);
                            String[] socket = ip.split(":");
                            if (victim.getTrade() != null) {
                                MapleTrade.cancelTrade(player);
                            }
                            victim.saveToDB(true, true);
                            if (victim.getCheatTracker() != null) {
                                victim.getCheatTracker().dispose();
                            }
                            ChannelServer.getInstance(c.getChannel()).removePlayer(player);
                            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
                            try {
                                c.getSession().write(MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            MapleMap target = cserv.getMapFactory().getMap(Integer.parseInt(splitted[1]));
                            player.changeMap(target, target.getPortal(0));
                        }
                    } catch (Exception e) {
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!warphere")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                MapleMap pmap = player.getMap();
                if (victim != null) {
                    victim.changeMap(pmap, player.getPosition());
                } else {
                    try {
                        String name = splitted[1];
                        WorldChannelInterface wci = cserv.getWorldInterface();
                        int channel = wci.find(name);
                        if (channel > -1) {
                            ChannelServer pserv = ChannelServer.getInstance(channel);
                            MapleCharacter world_victim = pserv.getPlayerStorage().getCharacterByName(name);
                            if (world_victim != null) {
                                ChangeChannelHandler.changeChannel(c.getChannel(), world_victim.getClient());
                                world_victim.changeMap(pmap, player.getPosition());
                            }
                        } else {
                            mc.dropMessage("Player not online.");
                        }
                    } catch (RemoteException e) {
                        cserv.reconnectWorld();
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!jail")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    victim.changeMap(200090300, 0);
                    mc.dropMessage(victim.getName() + " was jailed!");
                    victim.dropMessage("You have been jailed.");
                } else {
                    mc.dropMessage(splitted[1] + " not found!");
                }
            } else if (splitted[0].equalsIgnoreCase("!tempjail")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    //victim.changeMap(200090300, 0);
                    if (!splitted[3].equalsIgnoreCase("quiet")) {
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(5, "MZ Jail: " + victim.getName() + " has been jailed for " + splitted[2] + " minutes. Reason: " + StringUtil.joinStringFrom(splitted, 4)));
                    }
                    victim.jailTheNoob(Integer.parseInt(splitted[2]));
                    mc.dropMessage(victim.getName() + " was  temporary jailed! for " + splitted[2] + " minutes.");
                    victim.dropMessage(5, "You have been temporary jailed for " + splitted[2] + " minutes. All the minutes you spend here will be accounted for even if you relog.");
                } else {
                    mc.dropMessage(splitted[1] + " not found!");
                }
            } else if (splitted[0].equalsIgnoreCase("!unjail")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    victim.unJail();
                    player.dropMessage("Unjail complete");
                }
            } else if (splitted[0].equalsIgnoreCase("!map")) {
                int mapid;
                try {
                    mapid = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException mwa) {
                    return;
                }
                player.changeMap(mapid, getOptionalIntArg(splitted, 2, 0));
         
            } else if (splitted[0].equalsIgnoreCase("!mesosrate")) { // All these could be so much shorter but cbf.
                int set;
                try {
                    set = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                if (splitted.length > 2) {
                    for (ChannelServer channel : cservs) {
                        channel.setMesoRate(set);
                        channel.broadcastPacket(MaplePacketCreator.serverNotice(0, "Meso Rate has been changed to " + set + "x"));
                    }
                } else if (splitted.length == 2) {
                    cserv.setMesoRate(set);
                    cserv.broadcastPacket(MaplePacketCreator.serverNotice(0, "Meso Rate has been changed to " + set + "x"));
                } else {
                    mc.dropMessage("Syntax: !mesorate <number>");
                }
            } else if (splitted[0].equalsIgnoreCase("!droprate")) {
                int set;
                try {
                    set = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                if (splitted.length > 2) {
                    for (ChannelServer channel : cservs) {
                        channel.setDropRate(set);
                        channel.broadcastPacket(MaplePacketCreator.serverNotice(0, "Drop Rate has been changed to " + set + "x"));
                    }
                } else if (splitted.length == 2) {
                    cserv.setDropRate(set);
                    cserv.broadcastPacket(MaplePacketCreator.serverNotice(0, "Drop Rate has been changed to " + set + "x"));
                } else {
                    mc.dropMessage("Syntax: !droprate <number>");
                }
            } else if (splitted[0].equalsIgnoreCase("!bossdroprate")) {
                int set;
                try {
                    set = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                if (splitted.length > 2) {
                    for (ChannelServer channel : cservs) {
                        channel.setBossDropRate(set);
                        channel.broadcastPacket(MaplePacketCreator.serverNotice(0, "Boss Drop Rate has been changed to " + set + "x"));
                    }
                } else if (splitted.length == 2) {
                    cserv.setBossDropRate(set);
                    cserv.broadcastPacket(MaplePacketCreator.serverNotice(0, "Boss Drop Rate has been changed to " + set + "x"));
                } else {
                    mc.dropMessage("Syntax: !bossdroprate <number>");
                }
            } else if (splitted[0].equalsIgnoreCase("!exprate")) {
                int set;
                try {
                    set = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                if (splitted.length > 2) {
                    for (ChannelServer channel : cservs) {
                        channel.setExpRate(set);
                        channel.broadcastPacket(MaplePacketCreator.serverNotice(0, "Exp Rate has been changed to " + set + "x"));
                    }
                } else if (splitted.length == 2) {
                    cserv.setExpRate(set);
                    cserv.broadcastPacket(MaplePacketCreator.serverNotice(0, "Exp Rate has been changed to " + set + "x"));
                } else {
                    mc.dropMessage("Syntax: !exprate <number>");
                }
            } else if (splitted[0].equalsIgnoreCase("!godlyitemrate")) {
                int set;
                try {
                    set = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                if (splitted.length > 2) {
                    for (ChannelServer channel : cservs) {
                        channel.setGodlyItemRate((short) set);
                        channel.broadcastPacket(MaplePacketCreator.serverNotice(0, "Godly items will now drop at a " + set + "% rate."));
                    }
                } else if (splitted.length == 2) {
                    cserv.setGodlyItemRate((short) set);
                    cserv.broadcastPacket(MaplePacketCreator.serverNotice(0, "Godly items will now drop at a  " + set + "% rate."));
                } else {
                    mc.dropMessage("Syntax: !godlyitemrate <number>");
                }
            } else if (splitted[0].equalsIgnoreCase("!itemstat")) {
                int set;
                try {
                    set = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                if (splitted.length > 2) {
                    for (ChannelServer channel : cservs) {
                        channel.setItemMultiplier((short) set);
                        channel.broadcastPacket(MaplePacketCreator.serverNotice(0, "Item stat multiplier has been changed to " + set + "x"));
                    }
                } else if (splitted.length == 2) {
                    cserv.setItemMultiplier((short) set);
                    cserv.broadcastPacket(MaplePacketCreator.serverNotice(0, "Item stat multiplier has been changed to " + set + "x"));
                } else {
                    mc.dropMessage("Syntax: !setItemMultiplier <number>");
                }

            } else if (splitted[0].equals("!pollquestion")) {
                cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, "There is a poll question in Channel " + c.getChannel() + ": " + StringUtil.joinStringFrom(splitted, 1)).getBytes());
                cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, "Just say Yes or No to answer the poll! You MUST be in the right channel.").getBytes());
                ChannelServer.getInstance(c.getChannel()).pollstarted = true;
                ChannelServer.getInstance(c.getChannel()).pollquestion = StringUtil.joinStringFrom(splitted, 1);
                ChannelServer.getInstance(c.getChannel()).firstoption = 0;
                ChannelServer.getInstance(c.getChannel()).secondoption = 0;
                for (ChannelServer channel : cservs) {
                    for (MapleCharacter mch : channel.getPlayerStorage().getAllCharacters()) {
                        mch.warning[2] = false;
                    }
                }

                mc.dropMessage("Done.");

            } else if (splitted[0].equals("!endpoll")) {
                ChannelServer.getInstance(c.getChannel()).pollstarted = false;
                mc.dropMessage("Done.");

            } else if (splitted[0].equals("!tfquestion")) { // Alright I suppose?
                cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, "TF Question in Channel " + c.getChannel() + ": " + StringUtil.joinStringFrom(splitted, 3) + " Worth: " + splitted[1] + " mesos.").getBytes());
                cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, "Remember, you only have 25 seconds to do a True, false question. You must be in the right channel. Just say true or false").getBytes());
                for (ChannelServer channels : cservs) {
                    for (MapleCharacter mch : channels.getPlayerStorage().getAllCharacters()) {
                        mch.setanswered = false;
                        player.getClient().getChannelServer().tfeventstarted = true;
                        player.getClient().getChannelServer().tfworth = Integer.parseInt(splitted[1]);
                        player.getClient().getChannelServer().tfeventtrueorfalse = Boolean.parseBoolean(splitted[2]);
                    }
                }
                player.closeTf(25000);

            } else if (splitted[0].equalsIgnoreCase("!togglegodlyitems")) {
                if (splitted.length > 1) {
                    for (ChannelServer channel : cservs) {
                        channel.setGodlyItems(!cserv.isGodlyItems());
                        if (channel.isGodlyItems()) {
                            channel.broadcastPacket(MaplePacketCreator.serverNotice(0, "Godly items will now drop at a " + channel.getGodlyItemRate() + "% rate. Items like these will be multiplied by " + channel.getItemMultiplier() + "x each rate."));
                        } else {
                            channel.broadcastPacket(MaplePacketCreator.serverNotice(0, "Godly item drops have been turned off."));
                        }
                    }
                } else {
                    cserv.setGodlyItems(!cserv.isGodlyItems());
                    if (cserv.isGodlyItems()) {
                        cserv.broadcastPacket(MaplePacketCreator.serverNotice(0, "Godly items will now drop at a " + cserv.getGodlyItemRate() + "% rate. Items like these will be multiplied by " + cserv.getItemMultiplier() + "x each rate."));
                    } else {
                        cserv.broadcastPacket(MaplePacketCreator.serverNotice(0, "Godly item drops have been turned off."));
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!servermessage")) {
                String outputMessage = StringUtil.joinStringFrom(splitted, 1);
                if (outputMessage.equalsIgnoreCase("!array")) {
                    outputMessage = cserv.getArrayString();
                }
                cserv.setServerMessage(outputMessage);
            } else if (splitted[0].equalsIgnoreCase("!whosthere")) {
                StringBuilder builder = new StringBuilder();
                mc.dropMessage("Players on Map: ");
                for (MapleCharacter chr : player.getMap().getCharacters()) {
                    if (builder.length() > 150) { // wild guess :o
                        mc.dropMessage(builder.toString());
                        builder = new StringBuilder();
                    }
                    builder.append(MapleCharacterUtil.makeMapleReadable(chr.getName()));
                    builder.append(", ");
                }
                player.dropMessage(6, builder.toString());
            } else if (splitted[0].equalsIgnoreCase("!cheaters")) {
                try {
                    List<CheaterData> cheaters = cserv.getWorldInterface().getCheaters();
                    for (CheaterData cheater : cheaters) {
                        mc.dropMessage(cheater.getInfo());
                    }
                } catch (RemoteException e) {
                    cserv.reconnectWorld();
                }
            } else if (splitted[0].equalsIgnoreCase("!getrings")) {
                mc.dropMessage("1112800 - clover");
                mc.dropMessage("1112001 - crush");
                mc.dropMessage("1112801 - flower");
                mc.dropMessage("1112802 - Star");
                mc.dropMessage("1112803 - moonstone");
                mc.dropMessage("1112806 - Stargem");
                mc.dropMessage("1112807 - golden");
                mc.dropMessage("1112809 - silverswan");

            } else if (splitted[0].equalsIgnoreCase("!nearestPortal")) {
                final MaplePortal portal = player.getMap().findClosestSpawnpoint(player.getPosition());
                mc.dropMessage(portal.getName() + " id: " + portal.getId() + " script: " + portal.getScriptName());
            } else if (splitted[0].equalsIgnoreCase("!unban")) {
                if (MapleCharacter.unban(splitted[1])) {
                    mc.dropMessage("Sucess!");
                } else {
                    mc.dropMessage("Error while unbanning.");
                }
            } else if (splitted[0].equalsIgnoreCase("!spawn")) {
                int mid;
                int num = getOptionalIntArg(splitted, 2, 1);
                try {
                    mid = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException asd) {
                    return;
                }
                if (num > 20) {
                    mc.dropMessage("Remember that we know what you're doing ;] please dont over summon");
                }
                if (num > 100) {
                    player.dropMessage("You can't spawn more than 100 monsters at a time. Why would you even want too?");
                }
                Integer hp = getNamedIntArg(splitted, 1, "hp");
                Integer exp = getNamedIntArg(splitted, 1, "exp");
                Double php = getNamedDoubleArg(splitted, 1, "php");
                Double pexp = getNamedDoubleArg(splitted, 1, "pexp");
                MapleMonster onemob = MapleLifeFactory.getMonster(mid);
                int newhp = 0;
                int newexp = 0;
                if (hp != null) {
                    newhp = hp.intValue();
                } else if (php != null) {
                    newhp = (int) (onemob.getMaxHp() * (php.doubleValue() / 100));
                } else {
                    newhp = onemob.getMaxHp();
                }
                if (exp != null) {
                    newexp = exp.intValue();
                } else if (pexp != null) {
                    newexp = (int) (onemob.getExp() * (pexp.doubleValue() / 100));
                } else {
                    newexp = onemob.getExp();
                }
                if (newhp < 1) {
                    newhp = 1;
                }
                MapleMonsterStats overrideStats = new MapleMonsterStats();
                overrideStats.setHp(newhp);
                overrideStats.setExp(newexp);
                overrideStats.setMp(onemob.getMaxMp());
                if (num > 20) {
                    num = 20;
                }
                for (int i = 0; i < num; i++) {
                    MapleMonster mob = MapleLifeFactory.getMonster(mid);
                    mob.setHp(newhp);
                    mob.setOverrideStats(overrideStats);
                    player.getMap().spawnMonsterOnGroudBelow(mob, player.getPosition());
                }
            } else if (splitted[0].equals("!ban")) {
                String originalReason = StringUtil.joinStringFrom(splitted, 2);
                String reason = player.getName() + " banned " + splitted[1] + ": " + originalReason;
                MapleCharacter target = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (target != null) {
                    if (!target.isGM() || player.getGMLevel() > 3) {
                        String readableTargetName = MapleCharacterUtil.makeMapleReadable(target.getName());
                        String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                        reason += "  IP: " + ip;
                        target.ban(reason, false);
                        try {
                            cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(6, readableTargetName + " has been banned for " + originalReason).getBytes());
                        } catch (RemoteException e) {
                            cserv.reconnectWorld();
                        }
                    } else {
                        mc.dropMessage("Please dont ban " + cserv.getServerName() + " GMs");
                    }
                } else {
                    if (MapleCharacter.ban(splitted[1], reason, false)) {
                        String readableTargetName = MapleCharacterUtil.makeMapleReadable(target.getName());
                        String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                        reason += " (IP: " + ip + ")";
                        try {
                            cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(6, readableTargetName + " has been banned for " + originalReason).getBytes());
                        } catch (RemoteException e) {
                            cserv.reconnectWorld();
                        }
                    } else {
                        mc.dropMessage("Failed to ban " + splitted[1]);
                    }
                }

            } else if (splitted[0].equals("!setstrike")) {
                MapleCharacter target = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                target.setStrike(Integer.parseInt(splitted[2]));
                player.dropMessage("Done!");
                target.dropMessage(player.getName() + " has set your strikes to " + splitted[2]);

            } else if (splitted[0].equals("!strike")) {
                MapleCharacter target = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                int strike = 1;
                if (target.getStrike() != 2) {
                    //target.setStrikePrompt(true);
                    // target.start(c, 9010000);
                    target.setStrike(target.getStrike() + strike);
                    if (!splitted[2].equals(null)) {
                        target.dropMessage("You now have " + target.getStrike() + " strikes for " + StringUtil.joinStringFrom(splitted, 2) + ". After you have 3 strikes you will be banned.");
                    } else {
                        target.dropMessage("You now have " + target.getStrike() + " strikes. After you have 3 strikes you will be banned.");
                    }
                    target.setStrikePrompt(true);
                    NPCScriptManager.getInstance().start(
                            player.getClient(), 9010000, "9010000.js", target);
                    player.dropMessage("Done! " + target + " now has " + target.getStrike() + " strikes.");
                } else {
                    if (player.warned4 == false) {
                        player.dropMessage("This player now has 2 strikes. Are you sure you want to throw the final strike and ban him? Use the command again if you are sure!");
                        player.warned4 = true;
                    } else {
                        target.ban("Having 3 strikes", false);

                        player.dropMessage("Player banned.");
                        player.warned4 = false;
                    }
                }

            } else if (splitted[0].equals("!tempban")) {
                Calendar tempB = Calendar.getInstance();
                String originalReason = joinAfterString(splitted, ":");

                if (splitted.length < 4 || originalReason == null) {
                    mc.dropMessage("Syntax helper: !tempban <name> [i / m / w / d / h] <amount> [r [reason id] : Text Reason");
                    return;
                }

                int yChange = getNamedIntArg(splitted, 1, "y", 0);
                int mChange = getNamedIntArg(splitted, 1, "m", 0);
                int wChange = getNamedIntArg(splitted, 1, "w", 0);
                int dChange = getNamedIntArg(splitted, 1, "d", 0);
                int hChange = getNamedIntArg(splitted, 1, "h", 0);
                int iChange = getNamedIntArg(splitted, 1, "i", 0);
                int gReason = getNamedIntArg(splitted, 1, "r", 7);

                String reason = player.getName() + " tempbanned " + splitted[1] + ": " + originalReason;

                if (gReason > 14) {
                    mc.dropMessage("You have entered an incorrect ban reason ID, please try again.");
                    return;
                }

                DateFormat df = DateFormat.getInstance();
                tempB.set(tempB.get(Calendar.YEAR) + yChange, tempB.get(Calendar.MONTH) + mChange, tempB.get(Calendar.DATE) +
                        (wChange * 7) + dChange, tempB.get(Calendar.HOUR_OF_DAY) + hChange, tempB.get(Calendar.MINUTE) +
                        iChange);

                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);

                if (victim == null) {
                    int accId = MapleClient.findAccIdForCharacterName(splitted[1]);
                    if (accId >= 0 && MapleCharacter.tempban(reason, tempB, gReason, accId)) {
                        String readableTargetName = MapleCharacterUtil.makeMapleReadable(victim.getName());
                        cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, readableTargetName + " has been banned for " + originalReason));

                    } else {
                        mc.dropMessage("There was a problem offline banning character " + splitted[1] + ".");
                    }
                } else {
                    victim.tempban(reason, tempB, gReason);
                    mc.dropMessage("The character " + splitted[1] + " has been successfully tempbanned till " + df.format(tempB.getTime()));
                }
            } else if (splitted[0].equalsIgnoreCase("!search")) {
                if (splitted.length > 2) {
                    String type = splitted[1];
                    String search = StringUtil.joinStringFrom(splitted, 2);
                    MapleData data = null;
                    MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/" + "String.wz"));
                    mc.dropMessage("<<Type: " + type + " | Search: " + search + ">>");
                    if (type.equalsIgnoreCase("NPC") || type.equalsIgnoreCase("NPCS")) {
                        List<String> retNpcs = new ArrayList<String>();
                        data = dataProvider.getData("Npc.img");
                        List<Pair<Integer, String>> npcPairList = new LinkedList<Pair<Integer, String>>();
                        for (MapleData npcIdData : data.getChildren()) {
                            int npcIdFromData = Integer.parseInt(npcIdData.getName());
                            String npcNameFromData = MapleDataTool.getString(npcIdData.getChildByPath("name"), "NO-NAME");
                            npcPairList.add(new Pair<Integer, String>(npcIdFromData, npcNameFromData));
                        }
                        for (Pair<Integer, String> npcPair : npcPairList) {
                            if (npcPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                                retNpcs.add(npcPair.getLeft() + " - " + npcPair.getRight());
                            }
                        }
                        if (retNpcs != null && retNpcs.size() > 0) {
                            for (String singleRetNpc : retNpcs) {
                                mc.dropMessage(singleRetNpc);
                            }
                        } else {
                            mc.dropMessage("No NPC's Found");
                        }
                    } else if (type.equalsIgnoreCase("MAP") || type.equalsIgnoreCase("MAPS")) {
                        List<String> retMaps = new ArrayList<String>();
                        data = dataProvider.getData("Map.img");
                        List<Pair<Integer, String>> mapPairList = new LinkedList<Pair<Integer, String>>();
                        for (MapleData mapAreaData : data.getChildren()) {
                            for (MapleData mapIdData : mapAreaData.getChildren()) {
                                int mapIdFromData = Integer.parseInt(mapIdData.getName());
                                String mapNameFromData = MapleDataTool.getString(mapIdData.getChildByPath("streetName"), "NO-NAME") + " - " + MapleDataTool.getString(mapIdData.getChildByPath("mapName"), "NO-NAME");
                                mapPairList.add(new Pair<Integer, String>(mapIdFromData, mapNameFromData));
                            }
                        }
                        for (Pair<Integer, String> mapPair : mapPairList) {
                            if (mapPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                                retMaps.add(mapPair.getLeft() + " - " + mapPair.getRight());
                            }
                        }
                        if (retMaps != null && retMaps.size() > 0) {
                            for (String singleRetMap : retMaps) {
                                mc.dropMessage(singleRetMap);
                            }
                        } else {
                            mc.dropMessage("No Maps Found");
                        }
                    } else if (type.equalsIgnoreCase("MOB") || type.equalsIgnoreCase("MOBS") || type.equalsIgnoreCase("MONSTER") || type.equalsIgnoreCase("MONSTERS")) {
                        List<String> retMobs = new ArrayList<String>();
                        data = dataProvider.getData("Mob.img");
                        List<Pair<Integer, String>> mobPairList = new LinkedList<Pair<Integer, String>>();
                        for (MapleData mobIdData : data.getChildren()) {
                            int mobIdFromData = Integer.parseInt(mobIdData.getName());
                            String mobNameFromData = MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME");
                            mobPairList.add(new Pair<Integer, String>(mobIdFromData, mobNameFromData));
                        }
                        for (Pair<Integer, String> mobPair : mobPairList) {
                            if (mobPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                                retMobs.add(mobPair.getLeft() + " - " + mobPair.getRight());
                            }
                        }
                        if (retMobs != null && retMobs.size() > 0) {
                            for (String singleRetMob : retMobs) {
                                mc.dropMessage(singleRetMob);
                            }
                        } else {
                            mc.dropMessage("No Mob's Found");
                        }
                    } else if (type.equalsIgnoreCase("REACTOR") || type.equalsIgnoreCase("REACTORS")) {
                        mc.dropMessage("NOT ADDED YET");

                    } else if (type.equalsIgnoreCase("ITEM") || type.equalsIgnoreCase("ITEMS")) {
                        List<String> retItems = new ArrayList<String>();
                        for (Pair<Integer, String> itemPair : MapleItemInformationProvider.getInstance().getAllItems()) {
                            if (itemPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                                retItems.add(itemPair.getLeft() + " - " + itemPair.getRight());
                            }
                        }
                        if (retItems != null && retItems.size() > 0) {
                            for (String singleRetItem : retItems) {
                                mc.dropMessage(singleRetItem);
                            }
                        } else {
                            mc.dropMessage("No Item's Found");
                        }
                    } else if (type.equalsIgnoreCase("SKILL") || type.equalsIgnoreCase("SKILLS")) {
                        List<String> retSkills = new ArrayList<String>();
                        data = dataProvider.getData("Skill.img");
                        List<Pair<Integer, String>> skillPairList = new LinkedList<Pair<Integer, String>>();
                        for (MapleData skillIdData : data.getChildren()) {
                            int skillIdFromData = Integer.parseInt(skillIdData.getName());
                            String skillNameFromData = MapleDataTool.getString(skillIdData.getChildByPath("name"), "NO-NAME");
                            skillPairList.add(new Pair<Integer, String>(skillIdFromData, skillNameFromData));
                        }
                        for (Pair<Integer, String> skillPair : skillPairList) {
                            if (skillPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                                retSkills.add(skillPair.getLeft() + " - " + skillPair.getRight());
                            }
                        }
                        if (retSkills != null && retSkills.size() > 0) {
                            for (String singleRetSkill : retSkills) {
                                mc.dropMessage(singleRetSkill);
                            }
                        } else {
                            mc.dropMessage("No Skills Found");
                        }
                    } else {
                        mc.dropMessage("Sorry, that search call is unavailable");
                    }
                } else {
                    mc.dropMessage("Invalid search.  Proper usage: '!search <type> <search for>', where <type> is MAP, USE, ETC, CASH, EQUIP, MOB (or MONSTER), or SKILL.");
                }

            } else if (splitted[0].equals("!boattest")) {
                c.getSession().write(MaplePacketCreator.hiBalrog());

            } else if (splitted[0].equals("!gotolink")) {
                c.getSession().write(MaplePacketCreator.sendLinky(splitted[1]));


            } else if (splitted[0].equals("!addmkills")) {
                c.getPlayer().setMkills(getOptionalIntArg(splitted, 1, 1));
                //player.setMkills(player.getMkills() + 1);
                mc.dropMessage("You now have " + player.getMkills() + " monster kills!");
            } else if (splitted[0].equals("!viewmkills")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    mc.dropMessage(victim.getName() + " have " + victim.getMkills() + " monster kills");
                } else {
                    mc.dropMessage("Im sorry, but " + victim.getName() + " is offline.");
                }

            } else if (splitted[0].equals("!mkills")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                int amount = Integer.parseInt(splitted[2]);
                if (splitted.length == 2) {
                    if (victim != null) {
                        victim.setMkills(victim.getMkills() + amount);
                    } else {
                        mc.dropMessage("Im sorry, but " + victim.getName() + " is offline.");
                    }
                } else {
                    mc.dropMessage("Please use !mkills [username] [amount of mkills] -> to set victim mkills");
                }
            } else if (splitted[0].equalsIgnoreCase("!msearch")) {
                try {
                    URL url;
                    URLConnection urlConn;

                    BufferedReader dis;

                    String replaced;
                    if (splitted.length > 1) {
                        replaced = StringUtil.joinStringFrom(splitted, 1).replace(' ', '%');
                    } else {
                        mc.dropMessage("Syntax: !search item name/map name/monster name");
                        return;
                    }

                    url = new URL("http://www.mapletip.com/search_java.php?search_value=" + replaced + "&check=true");
                    urlConn = url.openConnection();
                    urlConn.setDoInput(true);
                    urlConn.setUseCaches(false);
                    dis = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    String s;

                    while ((s = dis.readLine()) != null) {
                        mc.dropMessage(s);
                    }
                    mc.dropMessage("Search for " + '"' + replaced.replace('%', ' ') + '"' + " was completed.");
                    dis.close();
                } catch (MalformedURLException mue) {
                    mc.dropMessage("Malformed URL Exception: " + mue.toString());
                } catch (IOException ioe) {
                    mc.dropMessage("IO Exception: " + ioe.toString());
                } catch (Exception e) {
                    mc.dropMessage("General Exception: " + e.toString());
                }
            } else if (splitted[0].equalsIgnoreCase("!npc")) {
                int npcId;
                try {
                    npcId = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException nfe) {
                    return;
                }
                MapleNPC npc = MapleLifeFactory.getNPC(npcId);
                if (npc != null && !npc.getName().equalsIgnoreCase("MISSINGNO")) {
                    npc.setPosition(player.getPosition());
                    npc.setCy(player.getPosition().y);
                    npc.setRx0(player.getPosition().x + 50);
                    npc.setRx1(player.getPosition().x - 50);
                    npc.setFh(player.getMap().getFootholds().findBelow(player.getPosition()).getId());
                    npc.setCustom(true);
                    player.getMap().addMapObject(npc);
                    player.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
                } else {
                    mc.dropMessage("You have entered an invalid Npc-Id");
                }

            } else if (splitted[0].equalsIgnoreCase("!removenpcs")) {
                List<MapleMapObject> npcs = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.NPC));
                for (MapleMapObject npcmo : npcs) {
                    MapleNPC npc = (MapleNPC) npcmo;
                    if (npc.isCustom()) {
                        player.getMap().removeMapObject(npc.getObjectId());
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!mynpcpos")) {
                Point pos = player.getPosition();
                mc.dropMessage("X: " + pos.x + " | Y: " + pos.y + "  | RX0: " + (pos.x + 50) + " | RX1: " + (pos.x - 50) + " | FH: " + player.getMap().getFootholds().findBelow(pos).getId());
            } else if (splitted[0].equalsIgnoreCase("!cleardrops")) {
                MapleMap map = player.getMap();
                double range = Double.POSITIVE_INFINITY;
                java.util.List<MapleMapObject> items = map.getMapObjectsInRange(player.getPosition(), range, Arrays.asList(MapleMapObjectType.ITEM));
                for (MapleMapObject itemmo : items) {
                    map.removeMapObject(itemmo);
                    map.broadcastMessage(MaplePacketCreator.removeItemFromMap(itemmo.getObjectId(), 0, player.getId()));
                }
                mc.dropMessage("You have destroyed " + items.size() + " items on the ground.");
            } else if (splitted[0].equalsIgnoreCase("!clearshops")) {
                MapleShopFactory.getInstance().clear();
            } else if (splitted[0].equalsIgnoreCase("!clearevents")) {
                for (ChannelServer instance : ChannelServer.getAllInstances()) {
                    instance.reloadEvents();
                }
            } else if (splitted[0].equalsIgnoreCase("!permban")) {
                String name = splitted[1];
                String reason = StringUtil.joinStringFrom(splitted, 2);
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(name);
                if (victim != null) {
                    if (!victim.isGM()) {
                        victim.ban(reason, true);
                        mc.dropMessage("Character permanently banned. !");
                    } else {
                        mc.dropMessage("You can't ban a GM. Sorry");
                    }
                } else {
                    if (MapleCharacter.ban(name, reason, false)) {
                        mc.dropMessage("Permanently banned sucessfully");
                    } else {
                        mc.dropMessage("Error while banning.");
                    }

                }
            } else if (splitted[0].equalsIgnoreCase("!emote")) {
                String name = splitted[1];
                int emote;
                try {
                    emote = Integer.parseInt(splitted[2]);
                } catch (NumberFormatException nfe) {
                    return;
                }
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(name);
                if (victim != null) {
                    victim.getMap().broadcastMessage(victim, MaplePacketCreator.facialExpression(victim, emote), victim.getPosition());
                } else {
                    mc.dropMessage("Player was not found");
                }

            } else if (splitted[0].equals("!addclones")) {
                if (splitted.length < 2) {
                    return;
                }
                int clones;
                try {
                    clones = getOptionalIntArg(splitted, 1, 1);
                } catch (NumberFormatException asdasd) {
                    return;
                }
                if (player.getFakeChars().size() >= 5) {
                    mc.dropMessage("You are not allowed to clone yourself over 5 times.");
                } else {
                    for (int i = 0; i < clones && i + player.getFakeChars().size() <= 6; i++) {
                        FakeCharacter fc = new FakeCharacter(player, player.getId() + player.getFakeChars().size() + clones + i);
                        player.getFakeChars().add(fc);
                        c.getChannelServer().addClone(fc);
                    }
                    mc.dropMessage("You have cloned yourself " + player.getFakeChars().size() + " times so far.");
                }
            } else if (splitted[0].equals("!removeclones")) {
                for (FakeCharacter fc : player.getFakeChars()) {
                    if (fc.getFakeChar().getMap() == player.getMap()) {
                        c.getChannelServer().getAllClones().remove(fc);
                        player.getMap().removePlayer(fc.getFakeChar());
                    }
                }
                player.getFakeChars().clear();
                mc.dropMessage("All your clones in the map removed.");
            } else if (splitted[0].equals("!removeallclones")) {
                for (FakeCharacter fc : c.getChannelServer().getAllClones()) {
                    if (fc.getOwner() != null) {
                        fc.getOwner().getFakeChars().remove(fc);
                    }
                    fc.getFakeChar().getMap().removePlayer(fc.getFakeChar());
                }
                c.getChannelServer().getAllClones().clear();
                mc.dropMessage("ALL clones have been removed.");
            } else if (splitted[0].equals("!follow")) {
                int slot = Integer.parseInt(splitted[1]);
                FakeCharacter fc = player.getFakeChars().get(slot);
                if (fc == null) {
                    mc.dropMessage("Clone does not exist.");
                } else {
                    fc.setFollow(true);
                }

            } else if (splitted[0].equals("!startsnowball")) {
                player.getClient().getChannelServer().snowballOn = true;
                cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, "The snowball event has started in Channel " + c.getChannel() + ". Use @snowball to join the event and make sure you are in the same channel as the event.").getBytes());
                player.dropMessage("Done!");

            } else if (splitted[0].equals("!closesnowball")) {
                player.getClient().getChannelServer().snowballOn = false;
                player.dropMessage("Done");

            } else if (splitted[0].equals("!startsnowball2")) {
                player.getClient().getChannelServer().snowballStarted = true;
                player.dropMessage("Done");

            } else if (splitted[0].equals("!closesnowball2")) {
                player.getClient().getChannelServer().snowballStarted = false;
                player.dropMessage("Done");

            } else if (splitted[0].equals("!clearsnowball")) {
                player.getClient().getChannelServer().snowballStarted = false;
                player.getClient().getChannelServer().snowballOn = false;
                player.getClient().getChannelServer().snowballTeam0 = 0;
                player.getClient().getChannelServer().snowballTeam1 = 0;
                player.dropMessage("Snowball variables cleared.");

            } else if (splitted[0].equalsIgnoreCase("!warp2")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    if (splitted.length == 2) {
                        MapleMap target = victim.getMap();
                        c.getPlayer().changeMap(target, target.findClosestSpawnpoint(victim.getPosition()));
                    } else {
                        int mapid = Integer.parseInt(splitted[2]);
                        int spawnpoint = getOptionalIntArg(splitted, 3, 0);
                        MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(mapid);
                        victim.changeMap(target, target.getPortal(spawnpoint));
                    }
                } else {
                    try {
                        victim = c.getPlayer();
                        WorldLocation loc = c.getChannelServer().getWorldInterface().getLocation(splitted[1]);
                        if (loc != null) {
                            mc.dropMessage("You'll be warped across channels. This may take a few seconds ..");
                            MapleMap target = c.getChannelServer().getMapFactory().getMap(loc.map);
                            String ip = c.getChannelServer().getIP(loc.channel);
                            c.getPlayer().getMap().removePlayer(c.getPlayer());
                            victim.setMap(target);
                            String[] socket = ip.split(":");
                            if (c.getPlayer().getTrade() != null) {
                                MapleTrade.cancelTrade(c.getPlayer());
                            }
                            try {
                                WorldChannelInterface wci = ChannelServer.getInstance(c.getChannel()).getWorldInterface();
                                wci.addBuffsToStorage(c.getPlayer().getId(), c.getPlayer().getAllBuffs());
                                wci.addCooldownsToStorage(c.getPlayer().getId(), c.getPlayer().getAllCooldowns());
                            } catch (RemoteException e) {
                                c.getChannelServer().reconnectWorld();
                            }
                            if (c.getPlayer().getCheatTracker() != null) {
                                c.getPlayer().getCheatTracker().dispose();
                            }
                            ChannelServer.getInstance(c.getChannel()).removePlayer(c.getPlayer());
                            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
                            try {
                                MaplePacket packet = MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]));
                                c.getSession().write(packet);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            int map = Integer.parseInt(splitted[1]);
                            int spawnpoint = getOptionalIntArg(splitted, 2, 0);
                            MapleMap target = cserv.getMapFactory().getMap(map);
                            c.getPlayer().changeMap(target, target.getPortal(spawnpoint));
                        }

                    } catch (Exception e) {
                        mc.dropMessage("Exception caught : " + e.getMessage() + ". Please report this to the admin.");
                    }
                }
            } else if (splitted[0].equals("!upwego")) {
                player.dropMessage("Up we gooo! :D");
                c.getSession().write(MaplePacketCreator.leftKnockBack());
                c.getSession().write(MaplePacketCreator.leftKnockBack());
                c.getSession().write(MaplePacketCreator.leftKnockBack());
                c.getSession().write(MaplePacketCreator.leftKnockBack());
                c.getSession().write(MaplePacketCreator.leftKnockBack());
                c.getSession().write(MaplePacketCreator.enableActions());

            } else if (splitted[0].equals("!event")) {
                if (player.getClient().getChannelServer().eventOn == false) {
                    int mapid = getOptionalIntArg(splitted, 1, c.getPlayer().getMapId());
                    player.getClient().getChannelServer().eventOn = true;
                    player.getClient().getChannelServer().eventMap = mapid;
                    try {
                        cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(6, c.getChannel(), "[Event] A GM is hosting an event in has  Channel " + c.getChannel() + " in " + c.getChannelServer().getMapFactory().getMap(player.getMapId()).getMapName() + "! Use @joinevent to join it!").getBytes());
                    } catch (RemoteException e) {
                        cserv.reconnectWorld();
                    }
                } else {
                    player.getClient().getChannelServer().eventOn = false;
                    try {
                        cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(6, c.getChannel(), "[Event] The event has ended. Thanks to all of those who participated.").getBytes());
                    } catch (RemoteException e) {
                        cserv.reconnectWorld();
                    }
                }


            } else if (splitted[0].equals("!pause")) {
                int slot = Integer.parseInt(splitted[1]);
                FakeCharacter fc = player.getFakeChars().get(slot);
                if (fc == null) {
                    mc.dropMessage("Clone does not exist.");
                } else {
                    fc.setFollow(false);
                }
            } else if (splitted[0].equals("!stance")) {
                if (splitted.length == 3) {
                    int slot = Integer.parseInt(splitted[1]);
                    int stance = Integer.parseInt(splitted[2]);
                    player.getFakeChars().get(slot).getFakeChar().setStance(stance);
                }
            } else if (splitted[0].equalsIgnoreCase("!killmonster")) {
                if (splitted.length == 2) {
                    MapleMap map = c.getPlayer().getMap();
                    int targetId = Integer.parseInt(splitted[1]);
                    List<MapleMapObject> monsters = map.getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
                    for (MapleMapObject monsterm : monsters) {
                        MapleMonster monster = (MapleMonster) monsterm;
                        if (monster.getId() == targetId) {
                            map.killMonster(monster, player, false);
                            break;
                        }
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!removeoid")) {
                if (splitted.length == 2) {
                    MapleMap map = c.getPlayer().getMap();
                    int oid = Integer.parseInt(splitted[1]);
                    MapleMapObject obj = map.getMapObject(oid);
                    if (obj == null) {
                        mc.dropMessage("This oid does not exist.");
                    } else {
                        map.removeMapObject(obj);
                    }
                }

            } else if (splitted[0].equalsIgnoreCase("!1337bomb")) {
                int mob;
                mob = Integer.parseInt(splitted[2]);
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                victim.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(mob), victim.getPosition());

            } else if (splitted[0].equalsIgnoreCase("!gmtext")) {
                int text;
                //RegularChat
                if (splitted[1].equalsIgnoreCase("normal")) {
                    text = 0;
                    //MultiChat
                } else if (splitted[1].equalsIgnoreCase("orange")) {
                    text = 1;
                } else if (splitted[1].equalsIgnoreCase("pink")) {
                    text = 2;
                } else if (splitted[1].equalsIgnoreCase("purple")) {
                    text = 3;
                } else if (splitted[1].equalsIgnoreCase("green")) {
                    text = 4;
                    //ServerNotice
                } else if (splitted[1].equalsIgnoreCase("red")) {
                    text = 5;
                } else if (splitted[1].equalsIgnoreCase("blue")) {
                    text = 6;
                    //RegularChat
                } else if (splitted[1].equalsIgnoreCase("whitebg")) {
                    text = 7;
                    //Whisper
                } else if (splitted[1].equalsIgnoreCase("lightinggreen")) {
                    text = 8;
                    //MapleTip
                } else if (splitted[1].equalsIgnoreCase("yellow")) {
                    text = 9;
                } else {
                    mc.dropMessage("Wrong syntax: use !gmtext normal/orange/pink/purple/green/blue/red/whitebg/lightinggreen/yellow");
                    return;
                }

                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE characters SET gmtext = ? WHERE name = ?");
                ps.setString(2, player.getName());
                ps.setInt(1, text);
                ps.executeUpdate();
                ps.close();
                player.setGMText(text);
            } else if (splitted[0].equalsIgnoreCase("!currentdate")) {
                Calendar cal = Calendar.getInstance();
                int day = cal.get(Calendar.DATE);
                int month = cal.get(Calendar.MONTH) + 1; // its an array of months.
                int year = cal.get(Calendar.YEAR);
                mc.dropMessage(day + "/" + month + "/" + year);
            } else if (splitted[0].equalsIgnoreCase("!maxmesos")) {
                player.gainMeso(Integer.MAX_VALUE - player.getMeso());
            } else if (splitted[0].equalsIgnoreCase("!fullcharge")) {
                player.setEnergyBar(10000);
                c.getSession().write(MaplePacketCreator.giveEnergyCharge(10000));
            } else if (splitted[0].equals("!youlose")) {
                for (MapleCharacter victim : player.getMap().getCharacters()) {
                    if (victim != null) {
                        if (victim.getHp() <= 0) {
                            victim.dropMessage("You have lost the event.");
                            victim.changeMap(100000000);
                        } else {
                            victim.setHp(victim.getCurrentMaxHp());
                            victim.updateSingleStat(MapleStat.HP, victim.getHp());
                            victim.setMp(victim.getCurrentMaxMp());
                            victim.updateSingleStat(MapleStat.MP, victim.getMp());
                        }
                    }
                }

            } else if (splitted[0].equals("!votemap")) {
                String text = ("All vote for MapleZtory for vote points!!"); // ~ ms.mmonaions.com ~");
                for (MapleCharacter mch : player.getMap().getCharacters()) {
                    mch.getMap().broadcastMessage(MaplePacketCreator.getChatText(mch.getId(), text, false, 0));
                }

            } else if (splitted[0].equals("!votechannel")) {
                String text = ("All vote for MapleZtory for vote points!!"); //ms.mmonations.com ~");
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    mch.getMap().broadcastMessage(MaplePacketCreator.getChatText(mch.getId(), text, false, 0));
                }

            } else if (splitted[0].equals("!voteworld")) {
                String text = ("All vote for MapleZtory for vote points!!"); //ms.mmonations.com ~");
                for (ChannelServer channels : cservs) {
                    for (MapleCharacter mch : channels.getPlayerStorage().getAllCharacters()) {
                        mch.getMap().broadcastMessage(MaplePacketCreator.getChatText(mch.getId(), text, false, 0));
                    }
                }

            } else if (splitted[0].equals("!donateworld")) {
                String text = ("All donate for MapleZtory!!"); //www.MaplePassion.no-ip.biz and click on the paypal button to donate!!!");
                for (ChannelServer channels : cservs) {
                    for (MapleCharacter mch : channels.getPlayerStorage().getAllCharacters()) {
                        mch.getMap().broadcastMessage(MaplePacketCreator.getChatText(mch.getId(), text, false, 0));
                    }
                }

            } else if (splitted[0].equals("!maplock")) {
                if (!splitted[1].equals(null)) {
                    int map = Integer.parseInt(splitted[1]);
                    MapleMap Map = c.getChannelServer().getMapFactory().getMap(map);
                    boolean locked = c.getChannelServer().getMapFactory().getMap(map).isClosed();
                    Map.setClosed(!locked);
                    if (Map.isClosed()) {
                        mc.dropMessage("The map " + c.getChannelServer().getMapFactory().getMap(map).getMapName() + " is now locked");
                    } else {
                        mc.dropMessage("The map " + c.getChannelServer().getMapFactory().getMap(map).getMapName() + " is now unlocked");
                    }
                } else {
                    if (player.getMap().isClosed()) {
                        mc.dropMessage("The map is now unlocked");
                    } else {
                        mc.dropMessage("The map is now locked.");
                    }
                    player.getMap().setClosed(!player.getMap().isClosed());
                }

            } else if (splitted[0].equalsIgnoreCase("!setxtreme")) {
                player.getClient().getChannelServer().xtremeEvent = true;
                player.dropMessage("Done!");
                int half = Integer.parseInt(splitted[1]) / 1000;
                cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, "The Xtreme Event has started for " + half + " seconds in Channel " + player.getClient().getChannel() + ". Kill a monster to find out what it is!").getBytes());
                player.timeXtreme(Integer.parseInt(splitted[1]));


            } else if (splitted[0].equals("!changejob")) {
                //change this command to public if u want.
                int id = c.getPlayer().getId();
                int job = Integer.parseInt(splitted[1]);
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps;
                try {
                    ps = con.prepareStatement("select * from jobs where characterid = ? and jobid = ?");
                    ps.setInt(1, id);
                    ps.setInt(2, job);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        mc.dropMessage("You don't have the following job before you rebirthed.");
                        ps.close();
                    }
                    job = rs.getInt("jobid");
                    c.getPlayer().changeJob(MapleJob.getById(job));

                    ps.close();
                } catch (SQLException e) {
                    System.out.println("SQL Exception: " + e);
                }
            }
        }
    }

    @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[]{
                    new CommandDefinition("lowhp", 3),
                    new CommandDefinition("sp", 3),
                    new CommandDefinition("ap", 3),
                    new CommandDefinition("job", 3),
                    new CommandDefinition("whereami", 3),
                    new CommandDefinition("shop", 3),
                    new CommandDefinition("opennpc", 3),
                    new CommandDefinition("levelup", 3),
                    new CommandDefinition("setmaxmp", 3),
                    new CommandDefinition("setmaxhp", 3),
                    new CommandDefinition("healmap", 3),
                    new CommandDefinition("item", 3),
                    new CommandDefinition("noname", 3),
                    new CommandDefinition("dropmesos", 3),
                    new CommandDefinition("level", 3),
                    new CommandDefinition("online", 3),
                    new CommandDefinition("banreason", 3),
                    new CommandDefinition("whitechat", 3),
                    new CommandDefinition("joinguild", 3),
                    new CommandDefinition("unbuffmap", 3),
                    new CommandDefinition("mesos", 3),
                    new CommandDefinition("setname", 3),
                    new CommandDefinition("clearslot", 3),
                    new CommandDefinition("ariantpq", 3),
                    new CommandDefinition("scoreboard", 3),
                    new CommandDefinition("array", 3),
                    new CommandDefinition("slap", 3),
                    new CommandDefinition("rreactor", 3),
                    new CommandDefinition("coke", 3),
                    new CommandDefinition("papu", 3),
                    new CommandDefinition("zakum", 3),
                    new CommandDefinition("ergoth", 3),
                    new CommandDefinition("ludimini", 3),
                    new CommandDefinition("cornian", 3),
                    new CommandDefinition("balrog", 3),
                    new CommandDefinition("mushmom", 3),
                    new CommandDefinition("wyvern", 3),
                    new CommandDefinition("pirate", 3),
                    new CommandDefinition("clone", 3),
                    new CommandDefinition("anego", 3),
                    new CommandDefinition("theboss", 3),
                    new CommandDefinition("snackbar", 3),
                    new CommandDefinition("papapixie", 3),
                    new CommandDefinition("nxslimes", 3),
                    new CommandDefinition("horseman", 3),
                    new CommandDefinition("blackcrow", 3),
                    new CommandDefinition("leafreboss", 3),
                    new CommandDefinition("shark", 3),
                    new CommandDefinition("franken", 3),
                    new CommandDefinition("bird", 3),
                    new CommandDefinition("pianus", 3),
                    new CommandDefinition("centipede", 3),
                    new CommandDefinition("horntail", 3),
                    new CommandDefinition("killall", 3),
                    new CommandDefinition("help", 3),
                    new CommandDefinition("say", 3),
                    new CommandDefinition("gender", 3),
                    new CommandDefinition("spy", 3),
                    new CommandDefinition("levelperson", 3),
                    new CommandDefinition("skill", 3),
                    new CommandDefinition("setall", 3),
                    new CommandDefinition("giftnx", 3),
                    new CommandDefinition("maxskills", 3),
                    new CommandDefinition("fame", 3),
                    new CommandDefinition("unhide", 3),
                    new CommandDefinition("heal", 3),
                    new CommandDefinition("unbuff", 3),
                    new CommandDefinition("sendhint", 3),
                    new CommandDefinition("smega", 3),
                    new CommandDefinition("mutesmega", 3),
                    new CommandDefinition("mute", 3),
                    new CommandDefinition("givedisease", 3),
                    new CommandDefinition("dc", 3),
                    new CommandDefinition("charinfo", 3),
                    new CommandDefinition("connected", 3),
                    new CommandDefinition("clock", 3),
                    new CommandDefinition("warp", 3),
                    new CommandDefinition("warphere", 3),
                    new CommandDefinition("jail", 3),
                    new CommandDefinition("map", 3),
                    new CommandDefinition("mesosrate", 3),
                    new CommandDefinition("droprate", 3),
                    new CommandDefinition("bossdroprate", 3),
                    new CommandDefinition("exprate", 3),
                    new CommandDefinition("godlyitemrate", 3),
                    new CommandDefinition("itemstat", 3),
                    new CommandDefinition("togglegodlyitems", 3),
                    new CommandDefinition("servermessage", 3),
                    new CommandDefinition("whosthere", 3),
                    new CommandDefinition("cheaters", 3),
                    new CommandDefinition("fakerelog", 3),
                    new CommandDefinition("getrings", 3),
                    new CommandDefinition("ring", 3),
                    new CommandDefinition("removering", 3),
                    new CommandDefinition("nearestportal", 3),
                    new CommandDefinition("unban", 3),
                    new CommandDefinition("spawn", 3),
                    new CommandDefinition("ban", 3),
                    new CommandDefinition("tempban", 3),
                    new CommandDefinition("search", 3),
                    new CommandDefinition("msearch", 3),
                    new CommandDefinition("npc", 3),
                    new CommandDefinition("addmkills", 3),
                    new CommandDefinition("mkills", 3),
                    new CommandDefinition("viewmkills", 3),
                    new CommandDefinition("removenpcs", 3),
                    new CommandDefinition("mynpcpos", 3),
                    new CommandDefinition("cleardrops", 3),
                    new CommandDefinition("clearshops", 3),
                    new CommandDefinition("clearevents", 3),
                    new CommandDefinition("permban", 3),
                    new CommandDefinition("emote", 3),
                    new CommandDefinition("proitem", 3),
                    new CommandDefinition("addclones", 3),
                    new CommandDefinition("removeclones", 3),
                    new CommandDefinition("removeallclones", 3),
                    new CommandDefinition("follow", 3),
                    new CommandDefinition("pause", 3),
                    new CommandDefinition("stance", 3),
                    new CommandDefinition("killmonster", 3),
                    new CommandDefinition("removeoid", 3),
                    new CommandDefinition("gmtext", 3),
                    new CommandDefinition("currentdate", 3),
                    new CommandDefinition("maxmesos", 3),
                    new CommandDefinition("fullcharge", 3),
                    new CommandDefinition("youlose", 3),
                    new CommandDefinition("warp2", 4),
                    new CommandDefinition("startsnowball", 4),
                    new CommandDefinition("startsnowball2", 4),
                    new CommandDefinition("clearsnowball", 4),
                    new CommandDefinition("closesnowball", 4),
                    new CommandDefinition("closesnowball2", 4),
                    new CommandDefinition("worldmessage", 4),
                    new CommandDefinition("strike", 4),
                    new CommandDefinition("setstrike", 4),
                    new CommandDefinition("mapitem", 4),
                    new CommandDefinition("tfquestion", 4),
                    new CommandDefinition("maplock", 4), // credits to Xerixe
                    new CommandDefinition("votemap", 4),
                    new CommandDefinition("votechannel", 4),
                    new CommandDefinition("voteworld", 4),
                    new CommandDefinition("donateworld", 4),
                    new CommandDefinition("unequipanddrop", 4),
                    new CommandDefinition("startsnowball", 4),
                    new CommandDefinition("startsnowball2", 4),
                    new CommandDefinition("closesnowball", 4),
                    new CommandDefinition("closesnowball2", 4),
                    new CommandDefinition("clearsnowball", 4),
                    new CommandDefinition("setstrike", 4),
                    new CommandDefinition("strike", 4),
                    new CommandDefinition("setxtreme", 4),
                    new CommandDefinition("1337bomb", 4),
                    new CommandDefinition("event", 3),
                    new CommandDefinition("upwego", 3),
                    new CommandDefinition("pollquestion", 3),
                    new CommandDefinition("endpoll", 3),
                    new CommandDefinition("boattest", 3),
                    new CommandDefinition("tempjail", 3),
                    new CommandDefinition("unjail", 3),
                    new CommandDefinition("gotolink", 3),
                    new CommandDefinition("removeplayer", 3),
                    new CommandDefinition("killallwithdrops", 3), // referenced in !killall
                    new CommandDefinition("skillcalc", 3),
                    new CommandDefinition("changejob", 3)
                };
    }
}
