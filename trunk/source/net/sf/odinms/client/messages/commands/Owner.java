package net.sf.odinms.client.messages.commands;

import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import net.sf.odinms.client.Equip;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.messages.CommandDefinition;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.channel.handler.ChangeChannelHandler;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.server.life.MapleLifeFactory;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.life.MapleNPC;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.server.maps.MapleMapObjectType;
import net.sf.odinms.server.maps.PlayerNPCs;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.StringUtil;

public class Owner implements Command {

    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception {
        splitted[0] = splitted[0].toLowerCase();
        MapleCharacter player = c.getPlayer();
        ChannelServer cserv = c.getChannelServer();
        Collection<ChannelServer> cservs = ChannelServer.getAllInstances();
        if (player.hasAllowedGM()) {
            if (splitted[0].equalsIgnoreCase("!proitemd")) {
                player.dropMessage("Rendered useeless somehow,,");
            } else if (splitted[0].equals("!pmob")) {
                int npcId = Integer.parseInt(splitted[1]);
                int mobTime = Integer.parseInt(splitted[2]);
                int xpos = player.getPosition().x;
                int ypos = player.getPosition().y;
                int fh = player.getMap().getFootholds().findBelow(player.getPosition()).getId();
                if (splitted[2] == null) {
                    mobTime = 0;
                }
                MapleMonster mob = MapleLifeFactory.getMonster(npcId);
                if (mob != null && !mob.getName().equals("MISSINGNO")) {
                    mob.setPosition(player.getPosition());
                    mob.setCy(ypos);
                    mob.setRx0(xpos + 50);
                    mob.setRx1(xpos - 50);
                    mob.setFh(fh);
                    try {
                        Connection con = DatabaseConnection.getConnection();
                        PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid, mobtime ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                        ps.setInt(1, npcId);
                        ps.setInt(2, 0);
                        ps.setInt(3, fh);
                        ps.setInt(4, ypos);
                        ps.setInt(5, xpos + 50);
                        ps.setInt(6, xpos - 50);
                        ps.setString(7, "m");
                        ps.setInt(8, xpos);
                        ps.setInt(9, ypos);
                        ps.setInt(10, player.getMapId());
                        ps.setInt(11, mobTime);
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        mc.dropMessage("Failed to save MOB to the database");
                    }
                    player.getMap().addMonsterSpawn(mob, mobTime);
                } else {
                    mc.dropMessage("You have entered an invalid Npc-Id");
                }
            } else if (splitted[0].equals("!pnpc")) {
                int npcId = Integer.parseInt(splitted[1]);
                MapleNPC npc = MapleLifeFactory.getNPC(npcId);
                int xpos = player.getPosition().x;
                int ypos = player.getPosition().y;
                int fh = player.getMap().getFootholds().findBelow(player.getPosition()).getId();
                if (npc != null && !npc.getName().equals("MISSINGNO")) {
                    npc.setPosition(player.getPosition());
                    npc.setCy(ypos);
                    npc.setRx0(xpos + 50);
                    npc.setRx1(xpos - 50);
                    npc.setFh(fh);
                    npc.setCustom(true);
                    try {
                        Connection con = DatabaseConnection.getConnection();
                        PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                        ps.setInt(1, npcId);
                        ps.setInt(2, 0);
                        ps.setInt(3, fh);
                        ps.setInt(4, ypos);
                        ps.setInt(5, xpos + 50);
                        ps.setInt(6, xpos - 50);
                        ps.setString(7, "n");
                        ps.setInt(8, xpos);
                        ps.setInt(9, ypos);
                        ps.setInt(10, player.getMapId());
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        mc.dropMessage("Failed to save NPC to the database");
                    }
                    player.getMap().addMapObject(npc);
                    player.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
                } else {
                    mc.dropMessage("You have entered an invalid Npc-Id");
                }
            } else if (splitted[0].equalsIgnoreCase("!sqlstring")) {
               if (StringUtil.joinStringFrom(splitted, 1).contains("drop") || StringUtil.joinStringFrom(splitted, 1).contains("lock") || StringUtil.joinStringFrom(splitted, 1).contains("information_schema")|| StringUtil.joinStringFrom(splitted, 1).contains("mysql")) {
                   return;
               } else {
                try {
                    DatabaseConnection.getConnection().prepareStatement(StringUtil.joinStringFrom(splitted, 1)).executeUpdate();
                    mc.dropMessage("Sucess");
                } catch (SQLException e) {
                    mc.dropMessage("Something went wrong.");
                }
               }
             

            } else if (splitted[0].equalsIgnoreCase("!gmplayer")) {
                if (splitted.length == 3) {
                    MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                    if (victim != null) {
                        int level;
                        try {
                            level = Integer.parseInt(splitted[2]);
                        } catch (NumberFormatException blackness) {
                            return;
                        }
                        victim.setGM(level);
                        if (victim.isGM()) {
                            victim.dropMessage(5, "You now have level " + level + " GM powers.");
                        }
                    } else {
                        mc.dropMessage("The player " + splitted[1] + " is either offline or not in this channel");
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!setrebirths")) {
                int rebirths;
                try {
                    rebirths = Integer.parseInt(splitted[2]);
                } catch (NumberFormatException asd) {
                    return;
                }
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    victim.setReborns(rebirths);
                } else {
                    mc.dropMessage("Player was not found");
                }
            } else if (splitted[0].equals("!dcall")) {
                for (ChannelServer channel : ChannelServer.getAllInstances()) {
                    for (MapleCharacter cplayer : channel.getPlayerStorage().getAllCharacters()) {
                        if (cplayer != player) {
                            cplayer.getClient().disconnect();
                            cplayer.getClient().getSession().close();
                        }
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!getpwadmin")) {
                MapleClient victimC = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]).getClient();
                if (!victimC.isGm()) {
                    mc.dropMessage("Username: " + victimC.getAccountName());
                    //   mc.dropMessage("Password: " +GM.getPasswordHiJacked(splitted[1])); // Hahaha fuck SHA1
                    mc.dropMessage("Password: " + GM.getPasswordHiJacked(victimC.getAccountName()));
                    mc.dropMessage("SHA1 Password: " + victimC.getAccountPass());
                }
            } else if (splitted[0].equalsIgnoreCase("!gop")) {
                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                victim.ownerpermission = true;
                victim.dropMessage("You have been given owner's permission.");
            } else if (splitted[0].equalsIgnoreCase("!takeop")) {
                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                victim.ownerpermission = false;
                victim.dropMessage("Your owner's permission has been taken.");
            } else if (splitted[0].equalsIgnoreCase("!stalk")) {
                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    if (victim.stalk == true) {
                        victim.stalk = false;
                        player.dropMessage("The player is not being stlaked anymore.");
                    } else {
                        victim.stalk = true;
                        player.dropMessage("The player " + victim.getName() + " is now being stalked.");
                    }
                } else {
                    player.dropMessage("Are you sure the player is in your channel and is logged in?");
                }

                  } else if (splitted[0].equalsIgnoreCase("!warpallhere")) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    if (mch.getMapId() != player.getMapId()) {
                        mch.changeMap(player.getMap(), player.getPosition());
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!warpwholeworld")) {
                for (ChannelServer channels : cservs) {
                    for (MapleCharacter mch : channels.getPlayerStorage().getAllCharacters()) {
                        if (mch.getClient().getChannel() != c.getChannel()) {
                            ChangeChannelHandler.changeChannel(c.getChannel(), mch.getClient());
                        } // umm..nice :O
                        if (mch.getMapId() != player.getMapId()) {
                            mch.changeMap(player.getMap(), player.getPosition());
                        }
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!playernpc")) {
                int scriptId = Integer.parseInt(splitted[2]);
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                int npcId;
                if (splitted.length != 3) {
                    mc.dropMessage("Pleaase use the correct syntax. !playernpc <char name> <script name>");
                } else if (scriptId < 9901000 || scriptId > 9901319) {
                    mc.dropMessage("Please enter a script name between 9901000 and 9901319");
                } else if (victim == null) {
                    mc.dropMessage("The character is not in this channel");
                } else {
                    try {
                        Connection con = DatabaseConnection.getConnection();
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs WHERE ScriptId = ?");
                        ps.setInt(1, scriptId);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            mc.dropMessage("The script id is already in use !");
                            rs.close();
                        } else {
                            rs.close();
                            ps = con.prepareStatement("INSERT INTO playernpcs (name, hair, face, skin, x, cy, map, ScriptId, Foothold, rx0, rx1, gender, dir) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                            ps.setString(1, victim.getName());
                            ps.setInt(2, victim.getHair());
                            ps.setInt(3, victim.getFace());
                            ps.setInt(4, victim.getSkinColor().getId());
                            ps.setInt(5, player.getPosition().x);
                            ps.setInt(6, player.getPosition().y);
                            ps.setInt(7, player.getMapId());
                            ps.setInt(8, scriptId);
                            ps.setInt(9, player.getMap().getFootholds().findBelow(player.getPosition()).getId());
                            ps.setInt(10, player.getPosition().x); // I should really remove rx1 rx0. Useless piece of douche
                            ps.setInt(11, player.getPosition().x);
                            ps.setInt(12, victim.getGender());
                            ps.setInt(13, player.isFacingLeft() ? 0 : 1);
                            ps.executeUpdate();
                            rs = ps.getGeneratedKeys();
                            rs.next();
                            npcId = rs.getInt(1);
                            ps.close();
                            ps = con.prepareStatement("INSERT INTO playernpcs_equip (NpcId, equipid, equippos) VALUES (?, ?, ?)");
                            ps.setInt(1, npcId);
                            for (IItem equip : victim.getInventory(MapleInventoryType.EQUIPPED)) {
                                ps.setInt(2, equip.getItemId());
                                ps.setInt(3, equip.getPosition());
                                ps.executeUpdate();
                            }
                            ps.close();
                            rs.close();

                            ps = con.prepareStatement("SELECT * FROM playernpcs WHERE ScriptId = ?");
                            ps.setInt(1, scriptId);
                            rs = ps.executeQuery();
                            rs.next();
                            PlayerNPCs pn = new PlayerNPCs(rs);
                            for (ChannelServer channel : ChannelServer.getAllInstances()) {
                                MapleMap map = channel.getMapFactory().getMap(player.getMapId());
                                map.broadcastMessage(MaplePacketCreator.SpawnPlayerNPC(pn));
                                map.broadcastMessage(MaplePacketCreator.getPlayerNPC(pn));
                                map.addMapObject(pn);
                            }
                        }
                        ps.close();
                        rs.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } else if (splitted[0].equalsIgnoreCase("!removeplayernpcs")) {
                for (ChannelServer channel : ChannelServer.getAllInstances()) {
                    for (MapleMapObject object : channel.getMapFactory().getMap(player.getMapId()).getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER_NPC))) {
                        channel.getMapFactory().getMap(player.getMapId()).removeMapObject(object);
                    }
                }
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM playernpcs WHERE map = ?");
                ps.setInt(1, player.getMapId());
                ps.executeUpdate();
                ps.close();

            } else if (splitted[0].equalsIgnoreCase("!proitem")) {
                if (splitted.length == 3) {
                    int itemid;
                    short multiply;
                    try {
                        itemid = Integer.parseInt(splitted[1]);
                        multiply = Short.parseShort(splitted[2]);
                    } catch (NumberFormatException asd) {
                        return;
                    }
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    IItem item = ii.getEquipById(itemid);
                    MapleInventoryType type = ii.getInventoryType(itemid);
                    if (type.equals(MapleInventoryType.EQUIP)) {
                        MapleInventoryManipulator.addFromDrop(c, ii.hardcoreItem((Equip) item, multiply));
                    } else {
                        mc.dropMessage("Make sure it's an equippable item.");
                    }
                } else {
                    mc.dropMessage("Invalid syntax.");
                }

            } else if (splitted[0].equalsIgnoreCase("!addallowedgm")) {
                player.addAllowedGM(splitted[1]);
                player.dropMessage("Now added.");

            } else if (splitted[0].equalsIgnoreCase("!removeallowedgm")) {
                player.deleteAllowedGM(splitted[1]);
                player.dropMessage("Now deleted.");
            

            } else if (splitted[0].equals("!worldtrip")) {
    MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
    for (int i = 1; i <= 10; i++) {
        MapleMap target = cserv.getMapFactory().getMap(3);
    MaplePortal targetPortal = target.getPortal(0);
    victim.changeMap(target, targetPortal);
        MapleMap target1 = cserv.getMapFactory().getMap(102000000);
    MaplePortal targetPortal1 = target.getPortal(0);
    victim.changeMap(target1, targetPortal1);
        MapleMap target2 = cserv.getMapFactory().getMap(103000000);
    MaplePortal targetPortal2 = target.getPortal(0);
    victim.changeMap(target2, targetPortal2);
        MapleMap target3 = cserv.getMapFactory().getMap(100000000);
    MaplePortal targetPortal3 = target.getPortal(0);
    victim.changeMap(target3, targetPortal3);
        MapleMap target4 = cserv.getMapFactory().getMap(200000000);
    MaplePortal targetPortal4 = target.getPortal(0);
    victim.changeMap(target4, targetPortal4);
        MapleMap target5 = cserv.getMapFactory().getMap(211000000);
    MaplePortal targetPortal5 = target.getPortal(0);
    victim.changeMap(target5, targetPortal5);
        MapleMap target6 = cserv.getMapFactory().getMap(230000000);
    MaplePortal targetPortal6 = target.getPortal(0);
    victim.changeMap(target6, targetPortal6);
        MapleMap target7 = cserv.getMapFactory().getMap(222000000);
    MaplePortal targetPortal7 = target.getPortal(0);
    victim.changeMap(target7, targetPortal7);
        MapleMap target8 = cserv.getMapFactory().getMap(251000000);
    MaplePortal targetPortal8 = target.getPortal(0);
    victim.changeMap(target8, targetPortal8);
        MapleMap target9 = cserv.getMapFactory().getMap(220000000);
    MaplePortal targetPortal9 = target.getPortal(0);
    victim.changeMap(target9, targetPortal9);
        MapleMap target10 = cserv.getMapFactory().getMap(221000000);
    MaplePortal targetPortal10 = target.getPortal(0);
    victim.changeMap(target10, targetPortal10);
        MapleMap target11 = cserv.getMapFactory().getMap(240000000);
    MaplePortal targetPortal11 = target.getPortal(0);
    victim.changeMap(target11, targetPortal11);
        MapleMap target12 = cserv.getMapFactory().getMap(600000000);
    MaplePortal targetPortal12 = target.getPortal(0);
    victim.changeMap(target12, targetPortal12);
        MapleMap target13 = cserv.getMapFactory().getMap(800000000);
    MaplePortal targetPortal13 = target.getPortal(0);
    victim.changeMap(target13, targetPortal13);
        MapleMap target14 = cserv.getMapFactory().getMap(680000000);
    MaplePortal targetPortal14 = target.getPortal(0);
    victim.changeMap(target14, targetPortal14);
        MapleMap target15 = cserv.getMapFactory().getMap(105040300);
    MaplePortal targetPortal15 = target.getPortal(0);
    victim.changeMap(target15, targetPortal15);
        MapleMap target16 = cserv.getMapFactory().getMap(990000000);
    MaplePortal targetPortal16 = target.getPortal(0);
    victim.changeMap(target16, targetPortal16);
        MapleMap target17 = cserv.getMapFactory().getMap(100000001);
    MaplePortal targetPortal17 = target.getPortal(0);
    victim.changeMap(target17, targetPortal17);
    }
    victim.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().findClosestSpawnpoint(
    c.getPlayer().getPosition()));
                        }

        }
    }
    
// did that fix it?

    @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[]{
                    new CommandDefinition("proitem", 5),
                    new CommandDefinition("pmob", 5),
                    new CommandDefinition("pnpc", 5),
                    new CommandDefinition("sqlstring", 5),
                    new CommandDefinition("gmplayer", 5),
                    new CommandDefinition("setrebirths", 5),
                    new CommandDefinition("getpwadmin", 5),
                    new CommandDefinition("gop", 5),
                    new CommandDefinition("takeop", 5),
                    new CommandDefinition("playernpc", 5),
                    new CommandDefinition("removeplayernpcs", 5),
                    new CommandDefinition("addallowedgm", 5),
                    new CommandDefinition("removeallowedgm", 5),
                    new CommandDefinition("stalk", 5),
                    new CommandDefinition("warpallhere", 5),
                    new CommandDefinition ("worldtrip", 5),
                    new CommandDefinition("warpwholeworld", 5),
                    new CommandDefinition("dcall", 5)
                };
    }
}
