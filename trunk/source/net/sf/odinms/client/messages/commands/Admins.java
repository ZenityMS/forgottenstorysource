package net.sf.odinms.client.messages.commands;

import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.messages.CommandDefinition;
import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleDisease;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.client.anticheat.CheatingOffense;
import net.sf.odinms.client.messages.CommandProcessor;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.net.ExternalCodeTableGetter;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.net.PacketProcessor;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.net.RecvPacketOpcode;
import net.sf.odinms.net.SendPacketOpcode;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.channel.handler.ChangeChannelHandler;
import net.sf.odinms.scripting.portal.PortalScriptManager;
import net.sf.odinms.scripting.reactor.ReactorScriptManager;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MapleShopFactory;
import net.sf.odinms.server.PlayerInteraction.HiredMerchant;
import net.sf.odinms.server.ShutdownServer;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.server.life.MobSkill;
import net.sf.odinms.server.life.MobSkillFactory;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.server.maps.MapleMapObjectType;
import net.sf.odinms.server.maps.MapleReactor;
import net.sf.odinms.server.maps.MapleMapItem;
import net.sf.odinms.server.maps.MapleReactorFactory;
import net.sf.odinms.server.maps.MapleReactorStats;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.StringUtil;
import net.sf.odinms.tools.performance.CPUSampler;
import static net.sf.odinms.client.messages.CommandProcessor.getOptionalIntArg;

public class Admins implements Command {

    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception {
        splitted[0] = splitted[0].toLowerCase();
        MapleCharacter player = c.getPlayer();
        if (player.hasAllowedGM()) {
            ChannelServer cserv = c.getChannelServer();
            Collection<ChannelServer> cservs = ChannelServer.getAllInstances();
            if (splitted[0].equals("!speakall")) {
                String text = StringUtil.joinStringFrom(splitted, 1);
                for (MapleCharacter mch : player.getMap().getCharacters()) {
                    mch.getMap().broadcastMessage(MaplePacketCreator.getChatText(mch.getId(), text, false, 0));
                }
            } else if (splitted[0].equals("!killnear")) {
                MapleMap map = player.getMap();
                List<MapleMapObject> players = map.getMapObjectsInRange(player.getPosition(), (double) 50000, Arrays.asList(MapleMapObjectType.PLAYER));
                for (MapleMapObject closeplayers : players) {
                    MapleCharacter playernear = (MapleCharacter) closeplayers;
                    if (playernear.isAlive() && playernear != player) {
                    }
                    playernear.setHp(0);
                    playernear.updateSingleStat(MapleStat.HP, 0);
                    player.setHp(30000);
                    player.updateSingleStat(MapleStat.HP, 30000);
                    playernear.dropMessage(5, "You were too close to a GM.");
                }
            } else if (splitted[0].equals("!packet")) {
                if (splitted.length > 1) {
                    c.getSession().write(MaplePacketCreator.sendPacket(StringUtil.joinStringFrom(splitted, 1)));
                } else {
                    mc.dropMessage("Please enter packet data");
                }
            } else if (splitted[0].equals("!drop")) {
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                int itemId = Integer.parseInt(splitted[1]);
                if (itemId == 4001126 && player.hasOwnerPermission() && !player.isOwner()) {
                    player.dropMessage("You need the owner's permission to drop a maple leaf.");
                    return;
                }
                short quantity = (short) getOptionalIntArg(splitted, 2, 1);
                IItem toDrop;
                if (ii.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                    toDrop = ii.getEquipById(itemId);
                } else {
                    toDrop = new Item(itemId, (byte) 0, quantity);
                }
                player.getMap().spawnItemDrop(player, player, toDrop, player.getPosition(), true, true);
            } else if (splitted[0].equalsIgnoreCase("!startProfiling")) {
                CPUSampler sampler = CPUSampler.getInstance();
                sampler.addIncluded("net.sf.odinms");
                sampler.start();
            } else if (splitted[0].equalsIgnoreCase("!stopProfiling")) {
                CPUSampler sampler = CPUSampler.getInstance();
                try {
                    String filename = "odinprofile.txt";
                    if (splitted.length > 1) {
                        filename = splitted[1];
                    }
                    File file = new File(filename);
                    if (file.exists()) {
                        file.delete();
                    }
                    sampler.stop();
                    FileWriter fw = new FileWriter(file);
                    sampler.save(fw, 1, 10);
                    fw.close();
                } catch (IOException e) {
                }
                sampler.reset();
            } else if (splitted[0].equalsIgnoreCase("!reloadops")) {
                try {
                    ExternalCodeTableGetter.populateValues(SendPacketOpcode.getDefaultProperties(), SendPacketOpcode.values());
                    ExternalCodeTableGetter.populateValues(RecvPacketOpcode.getDefaultProperties(), RecvPacketOpcode.values());
                } catch (Exception e) {
                }
                PacketProcessor.getProcessor(PacketProcessor.Mode.CHANNELSERVER).reset(PacketProcessor.Mode.CHANNELSERVER);
                PacketProcessor.getProcessor(PacketProcessor.Mode.CHANNELSERVER).reset(PacketProcessor.Mode.CHANNELSERVER);
            } else if (splitted[0].equals("!closemerchants")) {
                mc.dropMessage("Closing and saving merchants, please wait...");
                for (ChannelServer channel : ChannelServer.getAllInstances()) {
                    for (MapleCharacter players : channel.getPlayerStorage().getAllCharacters()) {
                        players.getInteraction().closeShop(true);
                    }
                }
                mc.dropMessage("All merchants have been closed and saved.");
            } else if (splitted[0].equals("!shutdown")) {
                int time = 60000;
                if (splitted.length > 1) {
                    time = Integer.parseInt(splitted[1]) * 60000;
                }
                CommandProcessor.forcePersisting();
                c.getChannelServer().shutdown(time);
            } else if (splitted[0].equals("!shutdownworld")) {
                int time = 60000;
                if (splitted.length > 1) {
                    time = Integer.parseInt(splitted[1]) * 60000;
                }
                CommandProcessor.forcePersisting();
                c.getChannelServer().shutdownWorld(time);
            } else if (splitted[0].equals("!shutdownnow")) {
                CommandProcessor.forcePersisting();
                new ShutdownServer(c.getChannel()).run();
            } else if (splitted[0].equalsIgnoreCase("!mesoperson")) {
                int mesos;
                try {
                    mesos = Integer.parseInt(splitted[2]);
                } catch (NumberFormatException blackness) {
                    return;
                }
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (!player.hasOwnerPermission() && !player.isOwner()) {
                    player.dropMessage("You need the owner's permission to give mesos to someone");
                    return;
                }
                if (victim != null) {
                    victim.gainMeso(mesos, true, true, true);
                } else {
                    mc.dropMessage("Player was not found");
                }

            } else if (splitted[0].equalsIgnoreCase("!kill")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    victim.setHp(0);
                    victim.setMp(0);
                    victim.updateSingleStat(MapleStat.HP, 0);
                    victim.updateSingleStat(MapleStat.MP, 0);
                } else {
                    mc.dropMessage("Player not found");
                }
            } else if (splitted[0].equalsIgnoreCase("!jobperson")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                int job;
                try {
                    job = Integer.parseInt(splitted[2]);
                } catch (NumberFormatException blackness) {
                    return;
                }
                if (!player.hasOwnerPermission() && !player.isOwner()) {
                    player.dropMessage("You need the owner's permission to change someone's job.");
                }
                if (victim != null) {
                    victim.setJob(job);
                } else {
                    mc.dropMessage("Player not found");
                }
            } else if (splitted[0].equalsIgnoreCase("!spawndebug")) {
                player.getMap().spawnDebug(mc);
            } else if (splitted[0].equalsIgnoreCase("!timerdebug")) {
                TimerManager.getInstance().dropDebugInfo(mc);
            } else if (splitted[0].equalsIgnoreCase("!threads")) {
                Thread[] threads = new Thread[Thread.activeCount()];
                Thread.enumerate(threads);
                String filter = "";
                if (splitted.length > 1) {
                    filter = splitted[1];
                }
                for (int i = 0; i < threads.length; i++) {
                    String tstring = threads[i].toString();
                    if (tstring.toLowerCase().indexOf(filter.toLowerCase()) > -1) {
                        mc.dropMessage(i + ": " + tstring);
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!showtrace")) {
                Thread[] threads = new Thread[Thread.activeCount()];
                Thread.enumerate(threads);
                Thread t = threads[Integer.parseInt(splitted[1])];
                mc.dropMessage(t.toString() + ":");
                for (StackTraceElement elem : t.getStackTrace()) {
                    mc.dropMessage(elem.toString());
                }

            } else if (splitted[0].equalsIgnoreCase("!shopitem")) {
                if (splitted.length < 5) {
                    mc.dropMessage("!shopitem <shopid> <itemid> <price> <position>");
                } else {
                    try {
                        Connection con = DatabaseConnection.getConnection();
                        PreparedStatement ps = con.prepareStatement("INSERT INTO shopitems (shopid, itemid, price, position) VALUES (" + Integer.parseInt(splitted[1]) + ", " + Integer.parseInt(splitted[2]) + ", " + Integer.parseInt(splitted[3]) + ", " + Integer.parseInt(splitted[4]) + ");");
                        ps.executeUpdate();
                        ps.close();
                        MapleShopFactory.getInstance().clear();
                        mc.dropMessage("Done adding shop item.");
                    } catch (SQLException e) {
                        mc.dropMessage("Something wrong happened.");
                    }
                }


            } else if (splitted[0].equalsIgnoreCase("!toggleoffense")) {
                try {
                    CheatingOffense co = CheatingOffense.valueOf(splitted[1]);
                    co.setEnabled(!co.isEnabled());
                } catch (IllegalArgumentException iae) {
                    mc.dropMessage("Offense " + splitted[1] + " not found");
                }
            } else if (splitted[0].equalsIgnoreCase("!tdrops")) {
                player.getMap().toggleDrops();
            } else if (splitted[0].equalsIgnoreCase("!givemonsbuff")) {
                int mask = 0;
                mask |= Integer.decode(splitted[1]);
                MobSkill skill = MobSkillFactory.getMobSkill(128, 1);
                c.getSession().write(MaplePacketCreator.applyMonsterStatusTest(Integer.valueOf(splitted[2]), mask, 0, skill, Integer.valueOf(splitted[3])));
            } else if (splitted[0].equalsIgnoreCase("!givemonstatus")) {
                int mask = 0;
                mask |= Integer.decode(splitted[1]);
                c.getSession().write(MaplePacketCreator.applyMonsterStatusTest2(Integer.valueOf(splitted[2]), mask, 1000, Integer.valueOf(splitted[3])));
            } else if (splitted[0].equalsIgnoreCase("!sreactor")) {
                MapleReactorStats reactorSt = MapleReactorFactory.getReactor(Integer.parseInt(splitted[1]));
                MapleReactor reactor = new MapleReactor(reactorSt, Integer.parseInt(splitted[1]));
                reactor.setDelay(-1);
                reactor.setPosition(player.getPosition());
                player.getMap().spawnReactor(reactor);
            } else if (splitted[0].equalsIgnoreCase("!hreactor")) {
                player.getMap().getReactorByOid(Integer.parseInt(splitted[1])).hitReactor(c);
            } else if (splitted[0].equalsIgnoreCase("!lreactor")) {
                MapleMap map = player.getMap();
                List<MapleMapObject> reactors = map.getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
                for (MapleMapObject reactorL : reactors) {
                    MapleReactor reactor2l = (MapleReactor) reactorL;
                    mc.dropMessage("Reactor: oID: " + reactor2l.getObjectId() + " reactorID: " + reactor2l.getId() + " Position: " + reactor2l.getPosition().toString() + " State: " + reactor2l.getState());
                }
            } else if (splitted[0].equalsIgnoreCase("!dreactor")) {
                MapleMap map = player.getMap();
                List<MapleMapObject> reactors = map.getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
                if (splitted[1].equalsIgnoreCase("all")) {
                    for (MapleMapObject reactorL : reactors) {
                        MapleReactor reactor2l = (MapleReactor) reactorL;
                        player.getMap().destroyReactor(reactor2l.getObjectId());
                    }
                } else {
                    player.getMap().destroyReactor(Integer.parseInt(splitted[1]));
                }
            } else if (splitted[0].equalsIgnoreCase("!writecommands")) {
                CommandProcessor.getInstance().writeCommandList();
            } else if (splitted[0].equalsIgnoreCase("!saveall")) {
                for (ChannelServer chan : ChannelServer.getAllInstances()) {
                    for (MapleCharacter chr : chan.getPlayerStorage().getAllCharacters()) {
                        chr.saveToDB(true, false);
                    }
                }
                mc.dropMessage("save complete");
            } else if (splitted[0].equalsIgnoreCase("!saveevery")) {
                for (ChannelServer chan : ChannelServer.getAllInstances()) {
                    for (MapleCharacter chr : chan.getPlayerStorage().getAllCharacters()) {
                        chr.saveToDB(true, false);
                    }
                }
                mc.dropMessage("save complete");
            } else if (splitted[0].equalsIgnoreCase("!reloadnews")) {
                for (ChannelServer channels : cservs) {
                    for (MapleCharacter mch : channels.getPlayerStorage().getAllCharacters()) {
                        mch.reloadNews();
                    }
                }
                player.dropMessage("News reloaded.");

            } else if (splitted[0].equalsIgnoreCase("!addnews")) {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps;
                ps = con.prepareStatement("INSERT INTO news(lmao, news) VALUES(0, ?)");
                ps.setString(1, StringUtil.joinStringFrom(splitted, 1));
                ps.executeUpdate();
                ps.close();
                for (ChannelServer channels : cservs) {
                    for (MapleCharacter mch : channels.getPlayerStorage().getAllCharacters()) {
                        mch.reloadNews();
                    }
                }

            } else if (splitted[0].equalsIgnoreCase("!notice")) {
                int joinmod = 1;
                int range = -1;
                if (splitted[1].equalsIgnoreCase("m")) {
                    range = 0;
                } else if (splitted[1].equalsIgnoreCase("c")) {
                    range = 1;
                } else if (splitted[1].equalsIgnoreCase("w")) {
                    range = 2;
                }
                int tfrom = 2;
                int type;
                if (range == -1) {
                    range = 2;
                    tfrom = 1;
                }
                if (splitted[tfrom].equalsIgnoreCase("n")) {
                    type = 0;
                } else if (splitted[tfrom].equalsIgnoreCase("p")) {
                    type = 1;
                } else if (splitted[tfrom].equalsIgnoreCase("l")) {
                    type = 2;
                } else if (splitted[tfrom].equalsIgnoreCase("nv")) {
                    type = 5;
                } else if (splitted[tfrom].equalsIgnoreCase("v")) {
                    type = 5;
                } else if (splitted[tfrom].equalsIgnoreCase("b")) {
                    type = 6;
                } else {
                    type = 0;
                    joinmod = 0;
                }
                String prefix = "";
                if (splitted[tfrom].equalsIgnoreCase("nv")) {
                    prefix = "[Notice] ";
                }
                joinmod += tfrom;
                String outputMessage = StringUtil.joinStringFrom(splitted, joinmod);
                if (outputMessage.equalsIgnoreCase("!array")) {
                    outputMessage = c.getChannelServer().getArrayString();
                }
                MaplePacket packet = MaplePacketCreator.serverNotice(type, prefix + outputMessage);
                if (range == 0) {
                    player.getMap().broadcastMessage(packet);
                } else if (range == 1) {
                    ChannelServer.getInstance(c.getChannel()).broadcastPacket(packet);
                } else if (range == 2) {
                    try {
                        ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(player.getName(), packet.getBytes());
                    } catch (RemoteException e) {
                        c.getChannelServer().reconnectWorld();
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!noteall")) {
                for (ChannelServer channels : cservs) {
                    for (MapleCharacter mch : channels.getPlayerStorage().getAllCharacters()) {
                        c.getPlayer().sendNote2(mch.getId(), StringUtil.joinStringFrom(splitted, 1));
                        mch.showNote();
                    }
                    player.dropMessage("Done");
                }

            } else if (splitted[0].equalsIgnoreCase("!setdonatorlvl")) {
                MapleCharacter donator = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (donator != null) {
                    donator.setDonatorLevel(Integer.parseInt(splitted[2]));
                    donator.dropMessage(5, "You are now a level " + splitted[2] + " Donator");
                    player.dropMessage("The player " + donator.getName() + " is now a donator of level " + splitted[2] + ".");
                } else {
                    player.dropMessage("That player is offline");
                }
            } else if (splitted[0].equalsIgnoreCase("!getdonatorlvl")) {
                MapleCharacter donator = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (donator != null) {
                    player.dropMessage(5, donator.getName() + " is a level " + donator.getDonatorLevel() + " donator.");
                } else {
                    player.dropMessage("That player is offline");
                }

            } else if (splitted[0].equalsIgnoreCase("!strip")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    victim.unequipEverything();
                    victim.dropMessage("You've been stripped by " + player.getName() + " biatch :D");
                } else {
                    player.dropMessage(6, "Player is not on.");
                }
            } else if (splitted[0].equalsIgnoreCase("!speak")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    String text = StringUtil.joinStringFrom(splitted, 2);
                    victim.getMap().broadcastMessage(MaplePacketCreator.getChatText(victim.getId(), text, false, 0));
                } else {
                    mc.dropMessage("Player not found");
                }
            } else if (splitted[0].equalsIgnoreCase("!changechannel")) {
                int channel;

                if (splitted.length == 3) {
                    try {
                        channel = Integer.parseInt(splitted[2]);
                    } catch (NumberFormatException blackness) {
                        return;
                    }
                    if (channel <= ChannelServer.getAllInstances().size() || channel < 0) {
                        String name = splitted[1];
                        try {
                            int vchannel = c.getChannelServer().getWorldInterface().find(name);
                            if (vchannel > -1) {
                                ChannelServer pserv = ChannelServer.getInstance(vchannel);
                                MapleCharacter victim = pserv.getPlayerStorage().getCharacterByName(name);
                                ChangeChannelHandler.changeChannel(channel, victim.getClient());
                            } else {
                                mc.dropMessage("Player not found");
                            }
                        } catch (RemoteException rawr) {
                            c.getChannelServer().reconnectWorld();
                        }
                    } else {
                        mc.dropMessage("Channel not found.");
                    }
                } else {
                    try {
                        channel = Integer.parseInt(splitted[1]);
                    } catch (NumberFormatException blackness) {
                        return;
                    }
                    if (channel <= ChannelServer.getAllInstances().size() || channel < 0) {
                        ChangeChannelHandler.changeChannel(channel, c);
                    }
                }

            } else if (splitted[0].equalsIgnoreCase("!clearguilds")) {
                try {
                    mc.dropMessage("Attempting to reload all guilds... this may take a while...");
                    cserv.getWorldInterface().clearGuilds();
                    mc.dropMessage("Completed.");
                } catch (RemoteException re) {
                    mc.dropMessage("RemoteException occurred while attempting to reload guilds.");
                }
            } else if (splitted[0].equalsIgnoreCase("!clearPortalScripts")) {
                PortalScriptManager.getInstance().clearScripts();
            } else if (splitted[0].equalsIgnoreCase("!clearReactorDrops")) {
                ReactorScriptManager.getInstance().clearDrops();
            } else if (splitted[0].equalsIgnoreCase("!monsterdebug")) {
                MapleMap map = player.getMap();
                double range = Double.POSITIVE_INFINITY;
                if (splitted.length > 1) {
                    int irange = Integer.parseInt(splitted[1]);
                    if (splitted.length <= 2) {
                        range = irange * irange;
                    } else {
                        map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[2]));
                    }
                }
                List<MapleMapObject> monsters = map.getMapObjectsInRange(player.getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER));
                for (MapleMapObject monstermo : monsters) {
                    MapleMonster monster = (MapleMonster) monstermo;
                    mc.dropMessage("Monster " + monster.toString());
                }
            } else if (splitted[0].equalsIgnoreCase("!itemperson")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                int item;
                try {
                    item = Integer.parseInt(splitted[2]);
                    if (item == 4001126 && !player.hasOwnerPermission() && !player.isOwner()) {
                        player.dropMessage("You need the owner's permission to give a maple leaf.");
                        return;
                    }
                } catch (NumberFormatException blackness) {
                    return;
                }
                short quantity = (short) getOptionalIntArg(splitted, 3, 1);
                if (victim != null) {
                    MapleInventoryManipulator.addById(victim.getClient(), item, quantity);
                } else {
                    mc.dropMessage("Player not found");
                }

            } else if (splitted[0].equals("!pausemap") || splitted[0].equals("!unpause")) {
                for (MapleCharacter chr : c.getPlayer().getMap().getCharacters()) {
                    if (splitted[0].equals("!pausemap")) {
                        if (chr.isGM()) {
                        } else {
                            MobSkill ms = new MobSkill(123, 1);
                            ms.setDuration((long) Double.POSITIVE_INFINITY);
                            chr.giveDebuff(MapleDisease.STUN, ms, false);
                        }
                    } else {
                        if (chr.isGM()) {
                        } else {
                            chr.cancelAllDebuffs();
                        }
                    }
                }
                mc.dropMessage("Done.");

            } else if (splitted[0].equalsIgnoreCase("!setaccgm")) {
                int accountid;
                Connection con = DatabaseConnection.getConnection();
                try {
                    PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
                    ps.setString(1, splitted[1]);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        accountid = rs.getInt("accountid");
                        ps.close();
                        ps = con.prepareStatement("UPDATE accounts SET gm = ? WHERE id = ?");
                        ps.setInt(1, 1);
                        ps.setInt(2, accountid);
                        ps.executeUpdate();
                    } else {
                        mc.dropMessage("Player was not found in the database.");
                    }
                    ps.close();
                    rs.close();
                } catch (SQLException se) {
                }
            } else if (splitted[0].equals("!servercheck")) {
                try {
                    cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(1, "Server check will commence soon. Please @save, and log off safely.").getBytes());
                } catch (RemoteException asd) {
                    cserv.reconnectWorld();
                }
            } else if (splitted[0].equalsIgnoreCase("!itemvac")) {
                List<MapleMapObject> items = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
                for (MapleMapObject item : items) {
                    MapleMapItem mapItem = (MapleMapItem) item;
                    if (mapItem.getMeso() > 0) {
                        player.gainMeso(mapItem.getMeso(), true);
                    } else if (mapItem.getItem().getItemId() >= 5000000 && mapItem.getItem().getItemId() <= 5000100) {
                        int petId = MaplePet.createPet(mapItem.getItem().getItemId());
                        if (petId == -1) {
                            return;
                        }
                        MapleInventoryManipulator.addById(c, mapItem.getItem().getItemId(), mapItem.getItem().getQuantity(), null, petId);
                    } else {
                        MapleInventoryManipulator.addFromDrop(c, mapItem.getItem(), true);
                    }
                    mapItem.setPickedUp(true);
                    player.getMap().removeMapObject(item); // just incase ?
                    player.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapItem.getObjectId(), 2, player.getId()), mapItem.getPosition());
                }

            } else if (splitted[0].equalsIgnoreCase("!merchantsave")) {
                for (ChannelServer channel : ChannelServer.getAllInstances()) {
                    for (ChannelServer channels : cservs) {
                        for (MapleCharacter mch : channels.getPlayerStorage().getAllCharacters()) {
                            for (int i = 910000001; i <= 910000022; i++) {
                                for (MapleMapObject obj : channel.getMapFactory().getMap(i).getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.HIRED_MERCHANT))) {
                                    HiredMerchant hm = (HiredMerchant) obj;
                                    hm.closeShop(true);
                                    player.setHasMerchant(false);
                                }
                            }
                        }
                    }
                }
                player.dropMessage("Done! Remember this has closed all shops....");
            }
        }
    }

    @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[]{
                    new CommandDefinition("speakall", 4),
                    new CommandDefinition("killnear", 4),
                    new CommandDefinition("packet", 4),
                    new CommandDefinition("drop", 4),
                    new CommandDefinition("startprofiling", 4),
                    new CommandDefinition("stopprofiling", 4),
                    new CommandDefinition("reloadops", 4),
                    new CommandDefinition("closemerchants", 4),
                    new CommandDefinition("shutdown", 4),
                    new CommandDefinition("shutdownworld", 4),
                    new CommandDefinition("shutdownnow", 4),
                    new CommandDefinition("setrebirths", 4),
                    new CommandDefinition("mesoperson", 4),
                    new CommandDefinition("kill", 4),
                    new CommandDefinition("jobperson", 4),
                    new CommandDefinition("spawndebug", 4),
                    new CommandDefinition("timerdebug", 4),
                    new CommandDefinition("threads", 4),
                    new CommandDefinition("showtrace", 4),
                    new CommandDefinition("toggleoffense", 4),
                    new CommandDefinition("tdrops", 4),
                    new CommandDefinition("givemonsbuff", 4),
                    new CommandDefinition("givemonstatus", 4),
                    new CommandDefinition("sreactor", 4),
                    new CommandDefinition("hreactor", 4),
                    new CommandDefinition("dreactor", 4),
                    new CommandDefinition("writecommands", 4),
                    new CommandDefinition("saveall", 4),
                    new CommandDefinition("notice", 4),
                    new CommandDefinition("speak", 4),
                    new CommandDefinition("changechannel", 4),
                    new CommandDefinition("clearguilds", 4),
                    new CommandDefinition("clearportalscripts", 4),
                    new CommandDefinition("shopitem", 4),
                    new CommandDefinition("clearreactordrops", 4),
                    new CommandDefinition("monsterdebug", 4),
                    new CommandDefinition("itemperson", 4),
                    new CommandDefinition("setaccgm", 4),
                    new CommandDefinition("strip", 4),
                    new CommandDefinition("servercheck", 4),
                    new CommandDefinition("itemvac", 4),
                    new CommandDefinition("merchantsave", 4),
                    new CommandDefinition("addnews", 4),
                    new CommandDefinition("reloadnews", 4),
                    new CommandDefinition("pausemap", 4),
                    new CommandDefinition("saveevery", 4),
                    new CommandDefinition("setdonatorlvl", 4),
                    new CommandDefinition("getdonatorlvl", 4),
                    new CommandDefinition("noteall", 4),
                    new CommandDefinition("unpause", 4),};
    }
}
