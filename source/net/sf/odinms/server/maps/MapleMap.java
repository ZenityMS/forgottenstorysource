/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.
You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.odinms.server.maps;

import java.awt.Point;
import java.awt.Rectangle;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Calendar;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import net.sf.odinms.client.Equip;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleBuffStat;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.client.status.MonsterStatus;
import net.sf.odinms.client.status.MonsterStatusEffect;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.net.world.guild.Territory;
import net.sf.odinms.net.world.guild.TerritoryMonster;
import net.sf.odinms.net.world.guild.TerritoryStorage;
import net.sf.odinms.net.world.remote.WorldRegistry;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.server.MapleSnowball;
import net.sf.odinms.server.MapleStatEffect;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.server.life.MapleLifeFactory;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.life.MapleMonsterStats;
import net.sf.odinms.server.life.MapleNPC;
import net.sf.odinms.server.life.MobSkill;
import net.sf.odinms.server.life.MobSkillFactory;
import net.sf.odinms.server.life.SpawnPoint;
import net.sf.odinms.server.maps.pvp.PvPLibrary;
import net.sf.odinms.tools.MaplePacketCreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapleMap {

    private static final int MAX_OID = 20000;
    private static final List<MapleMapObjectType> rangedMapobjectTypes = Arrays.asList(MapleMapObjectType.ITEM,
            MapleMapObjectType.MONSTER, MapleMapObjectType.DOOR, MapleMapObjectType.SUMMON, MapleMapObjectType.REACTOR);
    /**
     * Holds a mapping of all oid -> MapleMapObject on this map. mapobjects is NOT a synchronized collection since it
     * has to be synchronized together with runningOid that's why all access to mapobjects have to be done trough an
     * explicit synchronized block
     */
    private Map<Integer, MapleMapObject> mapobjects = new LinkedHashMap<Integer, MapleMapObject>();
    private Collection<SpawnPoint> monsterSpawn = new LinkedList<SpawnPoint>();
    private AtomicInteger spawnedMonstersOnMap = new AtomicInteger(0);
    private Collection<MapleCharacter> characters = new LinkedHashSet<MapleCharacter>();
    private Map<Integer, MaplePortal> portals = new HashMap<Integer, MaplePortal>();
    private List<Rectangle> areas = new ArrayList<Rectangle>();
    private MapleFootholdTree footholds = null;
    private int mapid;
    private boolean warned10 = false;
    private int runningOid = 100;
    private int returnMapId;
    private int channel;
    private float monsterRate;
    private boolean dropsDisabled = false;
    private boolean clock;
    private boolean boat;
    private boolean timer = false;
    private boolean docked;
    private boolean closed;
    private String mapName;
    private String streetName;
    private MapleSnowball snowball0 = null;
    private MapleSnowball snowball1 = null;
    private MapleMapEffect mapEffect = null;
    private boolean everlast = false;
    private int forcedReturnMap = 999999999;
    private List<MonsterStatus> redTeamBuffs = new LinkedList<MonsterStatus>();
    private List<MonsterStatus> blueTeamBuffs = new LinkedList<MonsterStatus>();
    private int timeLimit;
    private static Logger log = LoggerFactory.getLogger(MapleMap.class);
    private MapleMapTimer mapTimer = null;
    private int dropLife = 180000; //Time in milliseconds drops last before disappearing
    private int decHP = 0;
    private int protectItem = 0;
    private List<Point> takenSpawns = new LinkedList<Point>();
    private boolean town;
    private boolean showGate = false;
    private List<GuardianSpawnPoint> guardianSpawns = new LinkedList<GuardianSpawnPoint>();
    private TerritoryStorage territoryStorage;

    public MapleMap(int mapid, int channel, int returnMapId, float monsterRate) {
        this.mapid = mapid;
        this.channel = channel;
        this.returnMapId = returnMapId;
        if (monsterRate > 0) {
            this.monsterRate = monsterRate;
            boolean greater1 = monsterRate > 1.0;
            this.monsterRate = (float) Math.abs(1.0 - this.monsterRate);
            this.monsterRate = this.monsterRate / 2.0f;
            if (greater1) {
                this.monsterRate = 1.0f + this.monsterRate;
            } else {
                this.monsterRate = 1.0f - this.monsterRate;
            }
            TimerManager.getInstance().register(new RespawnWorker(), 10000);
        }
        try {
            //get the territory storage and store it in here
            Registry registry = LocateRegistry.getRegistry("localhost", Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
            WorldRegistry worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
            territoryStorage = worldRegistry.getTerritoryStorage();
        } catch (Exception e) {
            log.error("Error connecting to world server to get territory storage.", e);
        }

    }

    public void toggleDrops() {
        dropsDisabled = !dropsDisabled;
    }

    public int getId() {
        return mapid;
    }

    public MapleMap getReturnMap() {
        return ChannelServer.getInstance(channel).getMapFactory().getMap(returnMapId);
    }

    public int getReturnMapId() {
        return returnMapId;
    }

    public int getForcedReturnId() {
        return forcedReturnMap;
    }

    public MapleMap getForcedReturnMap() {
        return ChannelServer.getInstance(channel).getMapFactory().getMap(forcedReturnMap);
    }

    public void setForcedReturnMap(int map) {
        this.forcedReturnMap = map;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getCurrentPartyId() {
        for (MapleCharacter chr : this.getCharacters()) {
            if (chr.getPartyId() != -1) {
                return chr.getPartyId();
            }
        }
        return -1;
    }

    public void addMapObject(MapleMapObject mapobject) {
        synchronized (this.mapobjects) {
            mapobject.setObjectId(runningOid);
            this.mapobjects.put(Integer.valueOf(runningOid), mapobject);
            incrementRunningOid();
        }
    }

    private void spawnAndAddRangedMapObject(MapleMapObject mapobject, DelayedPacketCreation packetbakery) {
        spawnAndAddRangedMapObject(mapobject, packetbakery, null);
    }

    private void spawnAndAddRangedMapObject(MapleMapObject mapobject, DelayedPacketCreation packetbakery, SpawnCondition condition) {
        synchronized (this.mapobjects) {
            mapobject.setObjectId(runningOid);

            synchronized (characters) {
                for (MapleCharacter chr : characters) {
                    if (condition == null || condition.canSpawn(chr)) {
                        if (chr.getPosition().distanceSq(mapobject.getPosition()) <= MapleCharacter.MAX_VIEW_RANGE_SQ && !chr.isFake()) {
                            packetbakery.sendPackets(chr.getClient());
                            chr.addVisibleMapObject(mapobject);
                        }
                    }
                }
            }

            this.mapobjects.put(Integer.valueOf(runningOid), mapobject);
            incrementRunningOid();
        }
    }

    private void incrementRunningOid() {
        runningOid++;
        for (int numIncrements = 1; numIncrements < MAX_OID; numIncrements++) {
            if (runningOid > MAX_OID) {
                runningOid = 100;
            }
            if (this.mapobjects.containsKey(Integer.valueOf(runningOid))) {
                runningOid++;
            } else {
                return;
            }
        }
        throw new RuntimeException("Out of OIDs on map " + mapid + " (channel: " + channel + ")");
    }

    public void removeMapObject(int num) {
        synchronized (this.mapobjects) {
            if (mapobjects.containsKey(num)) {
                this.mapobjects.remove(Integer.valueOf(num));
            }
        }
    }

    public void removeMapObject(MapleMapObject obj) {
        removeMapObject(obj.getObjectId());
    }

    private Point calcPointBelow(Point initial) {
        MapleFoothold fh = footholds.findBelow(initial);
        if (fh == null) {
            return null;
        }
        int dropY = fh.getY1();
        if (!fh.isWall() && fh.getY1() != fh.getY2()) {
            double s1 = Math.abs(fh.getY2() - fh.getY1());
            double s2 = Math.abs(fh.getX2() - fh.getX1());
            double s4 = Math.abs(initial.x - fh.getX1());
            double alpha = Math.atan(s2 / s1);
            double beta = Math.atan(s1 / s2);
            double s5 = Math.cos(alpha) * (s4 / Math.cos(beta));
            if (fh.getY2() < fh.getY1()) {
                dropY = fh.getY1() - (int) s5;
            } else {
                dropY = fh.getY1() + (int) s5;
            }
        }
        return new Point(initial.x, dropY);
    }

    private Point calcDropPos(Point initial, Point fallback) {
        Point ret = calcPointBelow(new Point(initial.x, initial.y - 99));
        if (ret == null) {
            return fallback;
        }
        return ret;
    }

    private void dropFromMonster(MapleCharacter dropOwner, MapleMonster monster) {
        if (dropsDisabled || monster.dropsDisabled()) {
            return;
        }
        /*
         * drop logic: decide based on monster what the max drop count is get drops (not allowed: multiple mesos,
         * multiple items of same type exception: event drops) calculate positions
         */
        int maxDrops;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final boolean isBoss = monster.isBoss();
        ChannelServer cserv = dropOwner.getClient().getChannelServer();
        String text = "shit";
        int maxMesos;
        int droppedMesos = 0;

        if (text.startsWith("BossHunterPQ")) {
            bossHunterDrops(monster, dropOwner);
            return;
        }

        if (cserv.isXtremeEvent()) { //Xtreme Event
            maxDrops = 4;
            MapleCharacter chr = dropOwner;
            XtremeDrops(chr, monster);
            monster.disableDrops();
        }

        if (monster.getId() == 9410013) { // vending machine
            maxDrops = 4;
            MapleCharacter chr = dropOwner;
            superPianusDrops(chr, monster);
            monster.disableDrops();
        }

        if (monster.getId() == 8800002) { // special zakum drops
            maxDrops = 4;
            MapleCharacter chr = dropOwner;
            //superPianusDrops(chr, monster);
            ZakumDrops(chr, monster);
            monster.disableDrops();
        }

        if (monster.getId() == 9400551) { // bob drops
            maxDrops = 4;
            MapleCharacter chr = dropOwner;
            int dropArray[] = {4001126, 4001126, 4001126, 4001040, 4001040, 4001040, 4001040, 4001040, 4001126, 4001126, 4001126};
            customDrops(chr, monster, dropArray, 2);
            monster.disableDrops();
        }

        if (monster.getId() == 8510000) { //Super Pianus
            MapleCharacter chr = dropOwner;
            if (chr.getEventInstance() != null) {
                if (chr.getEventInstance().getName().startsWith("AquaPQ")) {
                    MaplePacket packet = MaplePacketCreator.serverNotice(0, "To the crew that have finally slain Super Pianus after numerous attempts, I salute thee! You are the true heroes of Aquarium!!");
                    chr.getMap().broadcastMessage(MaplePacketCreator.serverNotice(1, "You have killed Super Pianus. You will be warped out in 2 minutes. You have also gained 60 Aqua Points"));
                    chr.setPassionPoints(chr.getPassionPoints() + 50);
                    chr.gainItem(4000261, (short) 1, false, false);
                    chr.dropMessage(5, "You have gaind Pin Hov's charm. This can be added to your necklace at Professor Foxwit");
                    MapleMap toGoto = chr.getClient().getChannelServer().getMapFactory().getMap(230000000);
                    MapleMap frm = chr.getMap();
                    chr.getEventInstance().dispose2(); //End the instance o.O
                    for (MapleCharacter aaa : characters) {
                        aaa.getClient().getSession().write(MaplePacketCreator.getClock(120));
                        if (aaa.getEventInstance() != null) {
                            aaa.getEventInstance().unregisterPlayer(aaa);
                        }
                    }
                    TimerManager tMan = TimerManager.getInstance();
                    tMan.schedule(new warpAll(toGoto, frm), 120000);
                    try {
                        chr.getClient().getChannelServer().getWorldInterface().broadcastMessage(chr.getName(), packet.getBytes());
                    } catch (RemoteException e) {
                        chr.getClient().getChannelServer().reconnectWorld();
                    }
                    superPianusDrops(chr, monster);
                    monster.disableDrops();
                }
            }
        }

        if (monster.getId() == 9400507) { //Lil Puff
            MapleCharacter chr = dropOwner;
            if (chr.getEventInstance() != null) {
                if (chr.getEventInstance().getName().startsWith("ProBosses")) {
                    // MaplePacket packet = MaplePacketCreator.serverNotice(0, "Super Horntail: To the party that defated me, you are strong, but I will be back to reclaim my title as the strongest monster in MapleZtory!!");
                    // chr.getMap().broadcastMessage(MaplePacketCreator.serverNotice(1, "You have killed Super Horntail. You will be warped out in 2 minutes. You have also gained 100 Passion Points"));
                    chr.setPassionPoints(chr.getPassionPoints() + 50);
                    chr.dropMessage("You have killed Lil Puff! Please wait for the next monster to spawn");
                    MapleMapFactory mf = chr.getEventInstance().getMapFactory();
                    MapleMap bossmap = mf.getMap(240030103);
                    bossmap.removePortals();
                    bossmap.killAllMonsters(false);
                    MapleMonster mob = MapleLifeFactory.getMonster(9400512);
                    MapleMonsterStats overrideStats = new MapleMonsterStats();
                    overrideStats.setHp(1000000000);
                    overrideStats.setExp(1147000000);
                    overrideStats.setMp(mob.getMaxMp());
                    mob.setOverrideStats(overrideStats);
                    mob.setHp(overrideStats.getHp());
                    bossmap.spawnMonsterOnGroudBelow(mob, new java.awt.Point(-182, -178));
                }
            }
        }

        if (monster.getId() == 9500328) { // Super Balrog
            MapleCharacter chr = dropOwner;
            if (chr.getEventInstance() != null) {
                if (chr.getEventInstance().getName().startsWith("BoatPQ")) {
                    int dropArray[] = {4001040, 4001040, 4001040, 4001040, 1002418, 1002418, 1002418, 1002418};
                    customDrops(chr, monster, dropArray, 7);
                    monster.disableDrops();
                    if (countMobOnMap() <= 0) {
                        chr.gainItem(2388017, (short) 1, false, false);
                        chr.finishAchievement(38);
                        chr.getMap().broadcastMessage(MaplePacketCreator.serverNotice(1, "You have killed the Balrogs! You will be warped out in 2 minutes. You have also been given a Crimson Balrog Card! This card can be traded in for free EXP at Sam."));
                        MapleMap toGoto = chr.getClient().getChannelServer().getMapFactory().getMap(230000000);
                        MapleMap frm = chr.getMap();
                        chr.getEventInstance().dispose2(); //End the instance o.O
                        for (MapleCharacter aaa : characters) {
                            aaa.getClient().getSession().write(MaplePacketCreator.getClock(120));
                            if (aaa.getEventInstance() != null) {
                                aaa.getEventInstance().unregisterPlayer(aaa);
                            }
                        }
                        TimerManager tMan = TimerManager.getInstance();
                        tMan.schedule(new warpAll(toGoto, frm), 120000);
                    }
                }
            }
        }

        if (monster.getId() == 8500002) { // Papu drops
            MapleCharacter chr = dropOwner;
            int dropArray[] = {1332026, 1072211, 1040112, 1060101, 1072198, 1041120, 1061119, 1312015, 1072221, 1072178, 1051097, 1050094, 1051101, 1072183, 1462018, 1051085, 1002405, 1082127, 1050090, 1050106, 1072173, 1060106, 1040117, 1041118, 1332027, 1050098, 2041013, 2041022, 2041016, 2041019, 2000005};
            customDrops(chr, monster, dropArray, 2);
            monster.disableDrops();
        }

        if (monster.getId() == 9400512) { //Puff
            MapleCharacter chr = dropOwner;
            if (chr.getEventInstance() != null) {
                if (chr.getEventInstance().getName().startsWith("ProBosses")) {
                    chr.setPassionPoints(chr.getPassionPoints() + 50);
                    chr.dropMessage("You have killed Puff! As the map shakes and rattles here comes Big Puff Daddy!");
                    MapleMapFactory mf = chr.getEventInstance().getMapFactory();
                    MapleMap bossmap = mf.getMap(240030103);
                    bossmap.removePortals();
                    bossmap.killAllMonsters(false);
                    MapleMonster mob = MapleLifeFactory.getMonster(9400569);
                    MapleMonsterStats overrideStats = new MapleMonsterStats();
                    overrideStats.setHp(2147000000);
                    overrideStats.setExp(2147000000);
                    overrideStats.setMp(mob.getMaxMp());
                    mob.setOverrideStats(overrideStats);
                    mob.setHp(overrideStats.getHp());
                    bossmap.spawnMonsterOnGroudBelow(mob, new java.awt.Point(-182, -178));
                }
            }
        }

        if (monster.getId() == 9400569) { // Big Puff Daddy
            MapleCharacter chr = dropOwner;
            if (chr.getEventInstance() != null) {
                int dmapid = 912000000; // Donator Map
                if (chr.getEventInstance().getName().startsWith("ProBosses") || dropOwner.getMapId() == dmapid) {
                    MaplePacket packet = MaplePacketCreator.serverNotice(0, "[BPD]: I have been defeated, but I will return to reclaim my title as the strongest monster in MapleZtory!!");
                    chr.getMap().broadcastMessage(MaplePacketCreator.serverNotice(1, "You have killed BPD. You will be warped out in 2 minutes. You have also gained 60 Aqua Points"));
                    chr.setPassionPoints(chr.getPassionPoints() + 100);
                    MapleMap toGoto = chr.getClient().getChannelServer().getMapFactory().getMap(240030103);
                    MapleMap frm = chr.getMap();
                    chr.getEventInstance().dispose2(); //End the instance o.O
                    for (MapleCharacter aaa : characters) {
                        aaa.getClient().getSession().write(MaplePacketCreator.getClock(120));
                        if (aaa.getEventInstance() != null) {
                            aaa.getEventInstance().unregisterPlayer(aaa);
                        }
                    }
                    TimerManager tMan = TimerManager.getInstance();
                    tMan.schedule(new warpAll(toGoto, frm), 120000);
                    try {
                        chr.getClient().getChannelServer().getWorldInterface().broadcastMessage(chr.getName(), packet.getBytes());
                    } catch (RemoteException e) {
                        chr.getClient().getChannelServer().reconnectWorld();
                    }
                    superPianusDropsWithCustomStats(chr, monster, 110);
                    monster.disableDrops();
                }
            }
        }

        if (isBoss) {
            maxDrops = 10 * cserv.getBossDropRate();
        } else {
            maxDrops = 4 * cserv.getDropRate();
        }

        List<Integer> toDrop = new ArrayList<Integer>();
        for (int i = 0; i < maxDrops; i++) {
            toDrop.add(monster.getDrop());
        }

        Set<Integer> alreadyDropped = new HashSet<Integer>();
        int htpendants = 0;
        int htstones = 0;
        int mesos = 0;

        for (int i = 0; i < toDrop.size(); i++) {
            if (toDrop.get(i) == -1) {
                if (!this.isPQMap()) {
                    if (alreadyDropped.contains(-1)) {
                        if (!isBoss) {
                            toDrop.remove(i);
                            i--;
                        } else {
                            if (mesos < 7) { // 8 bags
                                mesos++;
                            } else {
                                toDrop.remove(i);
                                i--;
                            }
                        }
                    } else {
                        alreadyDropped.add(-1);
                    }
                }
            } else {
                if (alreadyDropped.contains(toDrop.get(i)) && !isBoss) {
                    toDrop.remove(i);
                    i--;
                } else {
                    if (toDrop.get(i) == 2041200) { // stone
                        if (htstones > 2) {
                            toDrop.remove(i);
                            i--;
                            continue;
                        } else {
                            htstones++;
                        }
                    } else if (toDrop.get(i) == 1122000) { // pendant
                        if (htstones > 2) {
                            toDrop.remove(i);
                            i--;
                            continue;
                        } else {
                            htpendants++;
                        }
                    }
                    alreadyDropped.add(toDrop.get(i));
                }
            }
        }
        if (toDrop.size() > maxDrops) {
            toDrop = toDrop.subList(0, maxDrops);
        }
        if (mesos < 7 && isBoss) {
            for (int i = mesos; i < 7; i++) {
                toDrop.add(-1);
            }
        }
//        if (Math.rint(Math.random() * 100) <= 2 || isBoss) {
//            toDrop.add(2022065);
//        }

        Point[] toPoint = new Point[toDrop.size()];
        int shiftDirection = 0;
        int shiftCount = 0;

        int curX = Math.min(Math.max(monster.getPosition().x - 25 * (toDrop.size() / 2), footholds.getMinDropX() + 25), footholds.getMaxDropX() - toDrop.size() * 25);
        int curY = Math.max(monster.getPosition().y, footholds.getY1());
        //int monsterShift = curX -
        while (shiftDirection < 3 && shiftCount < 1000) {
            // TODO for real center drop the monster width is needed o.o"
            if (shiftDirection == 1) {
                curX += 25;
            } else if (shiftDirection == 2) {
                curX -= 25;
            }
            // now do it
            for (int i = 0; i < toDrop.size(); i++) {
                MapleFoothold wall = footholds.findWall(new Point(curX, curY), new Point(curX + toDrop.size() * 25, curY));
                if (wall != null) {
                    //System.out.println("found a wall. wallX " + wall.getX1() + " curX " + curX);
                    if (wall.getX1() < curX) {
                        shiftDirection = 1;
                        shiftCount++;
                        break;
                    } else if (wall.getX1() == curX) {
                        if (shiftDirection == 0) {
                            shiftDirection = 1;
                        }
                        shiftCount++;
                        break;
                    } else {
                        shiftDirection = 2;
                        shiftCount++;
                        break;
                    }
                } else if (i == toDrop.size() - 1) {
                    //System.out.println("ok " + curX);
                    shiftDirection = 3;
                }
                final Point dropPos = calcDropPos(new Point(curX + i * 25, curY), new Point(monster.getPosition()));
                toPoint[i] = new Point(curX + i * 25, curY);
                final int drop = toDrop.get(i);

                if (drop == -1) { // meso
                    final int mesoRate = ChannelServer.getInstance(dropOwner.getClient().getChannel()).getMesoRate();
                    Random r = new Random();
                    double mesoDecrease = Math.pow(0.93, monster.getExp() / 300.0);
                    if (mesoDecrease > 1.0) {
                        mesoDecrease = 1.0;
                    }
                    int tempmeso = Math.min(30000, (int) (mesoDecrease * (monster.getExp()) * (1.0 + r.nextInt(20)) / 10.0));
                    if (dropOwner.getBuffedValue(MapleBuffStat.MESOUP) != null) {
                        tempmeso = (int) (tempmeso * dropOwner.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
                    }
                    if (tempmeso < 0 || tempmeso > Integer.MAX_VALUE) { // to prevent mesos not dropping
                        tempmeso = 100000000;
                    }

                    /*             if (monster.getId() == 9400238) { // So drumming bunnies become a Meso Spot
                    tempmeso *= (int) 35;
                    } */

                    final int meso = tempmeso;

                    if (meso > 0) {
                        final MapleMonster dropMonster = monster;
                        final MapleCharacter dropChar = dropOwner;
                        final boolean publicLoott = this.isPQMap();
                        TimerManager.getInstance().schedule(new Runnable() {

                            public void run() {
                                spawnMesoDrop(meso * mesoRate * dropChar.mesomultiplier, meso, dropPos, dropMonster, dropChar, isBoss || publicLoott);
                            }
                        }, monster.getAnimationTime("die1"));
                    }
                } else {
                    IItem idrop;
                    MapleInventoryType type = ii.getInventoryType(drop);
                    if (type.equals(MapleInventoryType.EQUIP)) {
                        Equip nEquip = ii.randomizeStats(dropOwner.getClient(), (Equip) ii.getEquipById(drop));
                        idrop = nEquip;
                    } else {
                        idrop = new Item(drop, (byte) 0, (short) 1);
                        // Randomize quantity for certain items
                        if (ii.isArrowForBow(drop) || ii.isArrowForCrossBow(drop)) {
                            if (dropOwner.getJob().getId() / 100 == 3) // is there a better way to do this ):
                            {
                                idrop.setQuantity((short) (1 + 100 * Math.random()));
                            }
                        } else if (ii.isThrowingStar(drop) || ii.isBullet(drop)) {
                            idrop.setQuantity((short) (1));
                        }
                    }

                    final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, monster, dropOwner);
                    final MapleMapObject dropMonster = monster;
                    final MapleCharacter dropChar = dropOwner;
                    final TimerManager tMan = TimerManager.getInstance();

                    tMan.schedule(new Runnable() {

                        public void run() {
                            spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

                                public void sendPackets(MapleClient c) {
                                    c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, mdrop.getObjectId(), dropMonster.getObjectId(), isBoss ? 0 : dropChar.getId(), dropMonster.getPosition(), dropPos, (byte) 1));
                                }
                            }, null);

                            tMan.schedule(new ExpireMapItemJob(mdrop), dropLife);
                        }
                    }, monster.getAnimationTime("die1"));

                }
            }
        }
    }

    public boolean damageMonster(MapleCharacter chr, MapleMonster monster, int damage) {
        if (damage > 10000) {
            chr.finishAchievement(20);
        }
        if (damage >= 99999) {
            chr.finishAchievement(21);
        }

        if (monster.getId() == 9400569 && damage > 9) { // realistic bossing method to have a midrate server that is challenging too.
            damage = damage / 10;
        }

        if (monster.getId() == 8800000) {
            Collection<MapleMapObject> objects = chr.getMap().getMapObjects();
            for (MapleMapObject object : objects) {
                MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
                if (mons != null) {
                    if (mons.getId() >= 8800003 && mons.getId() <= 8800010) {
                        return true;
                    }
                }
            }
        }

        // double checking to potentially avoid synchronisation overhead
        if (monster.isAlive()) {

            synchronized (monster) {
                if (!monster.isAlive()) {
                    return false;
                }
                if (damage > 0) {
                    int monsterhp = monster.getHp();
                    monster.damage(chr, damage, true);
                    if (!monster.isAlive()) { // monster just died
                        killMonster(monster, chr, true);
                        if (monster.getId() >= 8810002 && monster.getId() <= 8810009) {
                            Collection<MapleMapObject> objects = chr.getMap().getMapObjects();
                            for (MapleMapObject object : objects) {
                                MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
                                if (mons != null) {
                                    if (mons.getId() == 8810018) {
                                        damageMonster(chr, mons, monsterhp);
                                    }
                                }
                            }
                        }
                    } else {
                        if (monster.getId() >= 8810002 && monster.getId() <= 8810009) {
                            Collection<MapleMapObject> objects = chr.getMap().getMapObjects();
                            for (MapleMapObject object : objects) {
                                MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
                                if (mons != null) {
                                    if (mons.getId() == 8810018) {
                                        damageMonster(chr, mons, damage);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // the monster is dead, as damageMonster returns immediately for dead monsters this makes
            // this block implicitly synchronized for ONE monster
            return true;
        }
        return false;
    }

    public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops) {
        killMonster(monster, chr, withDrops, false, 1);
    }

    public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops, final boolean secondTime) {
        killMonster(monster, chr, withDrops, secondTime, 1);

    }

    @SuppressWarnings("static-access")
    public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops, final boolean secondTime, int animation) {
        if (monster.getId() == 8810018 && !secondTime) {
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    killMonster(monster, chr, withDrops, true, 1);
                    killAllMonsters(false);
                }
            }, 3000);
            return;
        }
        if (monster.getBuffToGive() > -1) {
            broadcastMessage(MaplePacketCreator.showOwnBuffEffect(monster.getBuffToGive(), 11));
            MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
            MapleStatEffect statEffect = mii.getItemEffect(monster.getBuffToGive());
            synchronized (this.characters) {
                for (MapleCharacter character : this.characters) {
                    if (character.isAlive()) {
                        statEffect.applyTo(character);
                        broadcastMessage(MaplePacketCreator.showBuffeffect(character.getId(), monster.getBuffToGive(), 11, (byte) 1));
                    }
                }
            }
        }
        if (monster.getCP() > 0) {
            chr.gainCP(monster.getCP());
        }

        if (chr.getMkills() == 1000) {
            chr.finishAchievement(34);
        }

        if (chr.getMkills() == 10000) {
            chr.finishAchievement(35);
        }

        if (chr.getMkills() == 100000) {
            chr.finishAchievement(36);
        }

        if (monster.isBoss()) {
            chr.setBossCounter(chr.getBossCount() + 1);
        } // you can get free bos dude  WTF
        // w8

        if (monster.getId() == 9410013) {
            try {
                chr.getClient().getChannelServer().getWorldInterface().broadcastMessage(chr.getName(), MaplePacketCreator.serverNotice(6, "To the crew that defeated the vending machine, congratulations!").getBytes());
            } catch (RemoteException e) {
                chr.getClient().getChannelServer().reconnectWorld();
            }
        }

        if (monster.getId() == 8810018) {
            // wrong  HIODW I?S iHOW IS Ihow is it wrong  this id = dead horntail id which its suposed to be o0-o weird because server kills the dead orntail and shi tdrops L:o
            try {
                chr.getClient().getChannelServer().getWorldInterface().broadcastMessage(chr.getName(), MaplePacketCreator.serverNotice(6, "To the crew that have finally conquered Horned Tail after numerous attempts, I salute thee! You are the true heroes of Leafre!!").getBytes());
            } catch (RemoteException e) {
                chr.getClient().getChannelServer().reconnectWorld();
            }
        }
        spawnedMonstersOnMap.decrementAndGet();
        monster.setHp(0);
        broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), animation), monster.getPosition());
        removeMapObject(monster);
        if (monster.getId() >= 8800003 && monster.getId() <= 8800010) {
            boolean makeZakReal = true;
            Collection<MapleMapObject> objects = getMapObjects();
            for (MapleMapObject object : objects) {
                MapleMonster mons = getMonsterByOid(object.getObjectId());
                if (mons != null) {
                    if (mons.getId() >= 8800003 && mons.getId() <= 8800010) {
                        makeZakReal = false;
                    }
                }
            }
            if (makeZakReal) {
                for (MapleMapObject object : objects) {
                    MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
                    if (mons != null) {
                        if (mons.getId() == 8800000) {
                            makeMonsterReal(mons);
                            updateMonsterController(mons);
                        }
                    }
                }
            }
        }
        MapleCharacter dropOwner = monster.killBy(chr);
        if (withDrops && !monster.dropsDisabled()) {
            if (dropOwner == null) {
                dropOwner = chr;
            }
            dropFromMonster(dropOwner, monster);
        }
        if (dropOwner != null) {
            if (dropOwner.getGuild() != null) {
                Territory[] territories = territoryStorage.getTerritories();

                for (int i = 0; i < territories.length; i++) {
                    if (getReturnMapId() == territories[i].getTownMapId()) {
                        if (dropOwner.getGuildId() == territories[i].getOwnerId()) {
                            //if we finally get here, it means the dropOwner is in a guild that owns the territory of this map
                            int bonusExp = (int) ((monster.getExp() * ChannelServer.getInstance(dropOwner.getClient().getChannel()).getExpRate()) * (territories[i].getOwnerLevel() / 5.0));
                            dropOwner.gainExp(bonusExp, true, false, false);
                        }
                    }
                }

                try {
                    TerritoryMonster tm = monster.getTerritoryMonster();

                    if (tm != null) {
                        //give the territory points to dropOwner, they probably deserve them
                        if (dropOwner.isTerritoryMap()) {
                            dropOwner.getClient().getChannelServer().getWorldInterface().addPointsToTerritory(dropOwner.getGuildId(), tm.getTerritoryId(), tm.getPointValue());
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }
        monster.removeListeners();
    }

    public void killAllMonsters(boolean drop) {
        List<MapleMapObject> players = null;
        if (drop) {
            players = getAllPlayer();
        }
        List<MapleMapObject> monsters = getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
        for (MapleMapObject monstermo : monsters) {
            MapleMonster monster = (MapleMonster) monstermo;
            spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0);
            broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), true), monster.getPosition());
            removeMapObject(monster);
            if (drop) {
                int random = (int) Math.random() * (players.size());
                dropFromMonster((MapleCharacter) players.get(random), monster);
            }
        }
    }

    public void killMonster(int monsId) {
        for (MapleMapObject mmo : getMapObjects()) {
            if (mmo instanceof MapleMonster) {
                if (((MapleMonster) mmo).getId() == monsId) {
                    this.killMonster((MapleMonster) mmo, (MapleCharacter) getAllPlayer().get(0), false);
                }
            }
        }
    }

    public List<MapleMapObject> getAllPlayer() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER));
    }

    public void destroyReactor(int oid) {
        synchronized (this.mapobjects) {
            final MapleReactor reactor = getReactorByOid(oid);
            TimerManager tMan = TimerManager.getInstance();
            broadcastMessage(MaplePacketCreator.destroyReactor(reactor));
            reactor.setAlive(false);
            removeMapObject(reactor);
            reactor.setTimerActive(false);
            if (reactor.getDelay() > 0) {
                tMan.schedule(new Runnable() {

                    @Override
                    public void run() {
                        respawnReactor(reactor);
                    }
                }, reactor.getDelay());
            }
        }
    }

    /*
     * command to reset all item-reactors in a map to state 0 for GM/NPC use - not tested (broken reactors get removed
     * from mapobjects when destroyed) Should create instances for multiple copies of non-respawning reactors...
     */
    public void resetReactors() {
        synchronized (this.mapobjects) {
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    ((MapleReactor) o).setState((byte) 0);
                    ((MapleReactor) o).setTimerActive(false);
                    broadcastMessage(MaplePacketCreator.triggerReactor((MapleReactor) o, 0));
                }
            }
        }
    }

    /*
     * command to shuffle the positions of all reactors in a map for PQ purposes (such as ZPQ/LMPQ)
     */
    public void shuffleReactors() {
        List<Point> points = new ArrayList<Point>();
        synchronized (this.mapobjects) {
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    points.add(((MapleReactor) o).getPosition());
                }
            }

            Collections.shuffle(points);

            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    ((MapleReactor) o).setPosition(points.remove(points.size() - 1));
                }
            }
        }
    }

    /**
     * Automagically finds a new controller for the given monster from the chars on the map...
     *
     * @param monster
     */
    public void updateMonsterController(MapleMonster monster) {
        synchronized (monster) {
            if (!monster.isAlive()) {
                return;
            }
            if (monster.getController() != null) {
                // monster has a controller already, check if he's still on this map
                if (monster.getController().getMap() != this) {
                    log.warn("Monstercontroller wasn't on same map");
                    monster.getController().stopControllingMonster(monster);
                } else {
                    // controller is on the map, monster has an controller, everything is fine
                    return;
                }
            }
            int mincontrolled = -1;
            MapleCharacter newController = null;
            synchronized (characters) {
                for (MapleCharacter chr : characters) {
                    if (!chr.isHidden() && (chr.getControlledMonsters().size() < mincontrolled || mincontrolled == -1)) {
                        if (!chr.getName().equals("FaekChar")) { // TODO remove me for production release
                            mincontrolled = chr.getControlledMonsters().size();
                            newController = chr;
                        }
                    }
                }
            }
            if (newController != null) { // was a new controller found? (if not no one is on the map)
                if (monster.isFirstAttack()) {
                    newController.controlMonster(monster, true);
                    monster.setControllerHasAggro(true);
                    monster.setControllerKnowsAboutAggro(true);
                } else {
                    newController.controlMonster(monster, false);
                }
            }
        }
    }

    public Collection<MapleMapObject> getMapObjects() {
        return Collections.unmodifiableCollection(mapobjects.values());
    }

    public boolean containsNPC(int npcid) {
        synchronized (mapobjects) {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.NPC) {
                    if (((MapleNPC) obj).getId() == npcid) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public MapleMapObject getMapObject(int oid) {
        return mapobjects.get(oid);
    }

    /**
     * returns a monster with the given oid, if no such monster exists returns null
     *
     * @param oid
     * @return
     */
    public MapleMonster getMonsterByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid);
        if (mmo == null) {
            return null;
        }
        if (mmo.getType() == MapleMapObjectType.MONSTER) {
            return (MapleMonster) mmo;
        }
        return null;
    }

    public MapleReactor getReactorByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid);
        if (mmo == null) {
            return null;
        }
        if (mmo.getType() == MapleMapObjectType.REACTOR) {
            return (MapleReactor) mmo;
        }
        return null;
    }

    public MapleReactor getReactorByName(String name) {
        synchronized (mapobjects) {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.REACTOR) {
                    if (((MapleReactor) obj).getName().equals(name)) {
                        return (MapleReactor) obj;
                    }
                }
            }
        }
        return null;
    }

    //backwards compatible
    public void spawnMonsterOnGroudBelow(MapleMonster mob, Point pos) {
        spawnMonsterOnGroundBelow(mob, pos);
    }

    public void spawnMonsterOnGroundBelow(MapleMonster mob, Point pos) {
        Point spos = getGroundBelow(pos);
        mob.setPosition(spos);
        spawnMonster(mob);
    }

    public void spawnFakeMonsterOnGroundBelow(MapleMonster mob, Point pos) {
        Point spos = getGroundBelow(pos);
        mob.setPosition(spos);
        spawnFakeMonster(mob);
    }

    public Point getGroundBelow(Point pos) {
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        spos.y -= 1;
        return spos;
    }

    public void spawnRevives(final MapleMonster monster) {
        monster.setMap(this);
        synchronized (this.mapobjects) {
            spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

                public void sendPackets(MapleClient c) {
                    c.getSession().write(MaplePacketCreator.spawnMonster(monster, false));
                }
            }, null);
            updateMonsterController(monster);
        }
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void spawnMonster(final MapleMonster monster) {
        monster.setMap(this);
        monster.setTerritoryMonster(territoryStorage.getMonsterByMonsterId(monster.getId()));

        synchronized (this.mapobjects) {
            spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

                public void sendPackets(MapleClient c) {
                    c.getSession().write(MaplePacketCreator.spawnMonster(monster, true));
                    if (monster.getId() == 9300166) {
                        TimerManager.getInstance().schedule(new Runnable() {

                            @Override
                            public void run() {
                                killMonster(monster, (MapleCharacter) getAllPlayer().get(0), false, false, 3);
                            }
                        }, new Random().nextInt(4500 + 500));
                    }
                }
            }, null);
            updateMonsterController(monster);
        }
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void spawnMonsterWithEffect(final MapleMonster monster, final int effect, Point pos) {
        try {
            monster.setMap(this);
            Point spos = new Point(pos.x, pos.y - 1);
            spos = calcPointBelow(spos);
            spos.y -= 1;
            monster.setPosition(spos);
            monster.disableDrops();
            synchronized (this.mapobjects) {
                spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

                    public void sendPackets(MapleClient c) {
                        c.getSession().write(MaplePacketCreator.spawnMonster(monster, true, effect));
                    }
                }, null);
                /*if (monster.hasBossHPBar()) {
                broadcastMessage(monster.makeBossHPBarPacket(), monster.getPosition());
                }*/
                updateMonsterController(monster);
            }
            spawnedMonstersOnMap.incrementAndGet();
        } catch (Exception e) {
        }
    }

    public void spawnFakeMonster(final MapleMonster monster) {
        monster.setMap(this);
        monster.setFake(true);
        synchronized (this.mapobjects) {
            spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

                public void sendPackets(MapleClient c) {
                    c.getSession().write(MaplePacketCreator.spawnFakeMonster(monster, 0));
                }
            }, null);
        }
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void makeMonsterReal(final MapleMonster monster) {
        monster.setFake(false);
        broadcastMessage(MaplePacketCreator.makeMonsterReal(monster));
        /*if (monster.hasBossHPBar()) {
        broadcastMessage(monster.makeBossHPBarPacket(), monster.getPosition());
        }*/
        updateMonsterController(monster);
    }

    public void spawnReactor(final MapleReactor reactor) {
        reactor.setMap(this);
        synchronized (this.mapobjects) {
            spawnAndAddRangedMapObject(reactor, new DelayedPacketCreation() {

                public void sendPackets(MapleClient c) {
                    c.getSession().write(reactor.makeSpawnData());
                }
            }, null);
            //broadcastMessage(reactor.makeSpawnData());
        }
    }

    private void respawnReactor(final MapleReactor reactor) {
        reactor.setState((byte) 0);
        reactor.setAlive(true);
        spawnReactor(reactor);
    }

    public void spawnDoor(final MapleDoor door) {
        synchronized (this.mapobjects) {
            spawnAndAddRangedMapObject(door, new DelayedPacketCreation() {

                public void sendPackets(MapleClient c) {
                    c.getSession().write(MaplePacketCreator.spawnDoor(door.getOwner().getId(), door.getTargetPosition(), false));
                    if (door.getOwner().getParty() != null && (door.getOwner() == c.getPlayer() || door.getOwner().getParty().containsMembers(new MaplePartyCharacter(c.getPlayer())))) {
                        c.getSession().write(MaplePacketCreator.partyPortal(door.getTown().getId(), door.getTarget().getId(), door.getTargetPosition()));
                    }
                    c.getSession().write(MaplePacketCreator.spawnPortal(door.getTown().getId(), door.getTarget().getId(), door.getTargetPosition()));
                    c.getSession().write(MaplePacketCreator.enableActions());
                }
            }, new SpawnCondition() {

                public boolean canSpawn(MapleCharacter chr) {
                    return chr.getMapId() == door.getTarget().getId() ||
                            chr == door.getOwner() && chr.getParty() == null;
                }
            });
        }
    }

    public void spawnSummon(final MapleSummon summon) {
        spawnAndAddRangedMapObject(summon, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                int skillLevel = summon.getOwner().getSkillLevel(SkillFactory.getSkill(summon.getSkill()));
                c.getSession().write(MaplePacketCreator.spawnSpecialMapObject(summon, skillLevel, true));
            }
        }, null);
    }

    public void spawnMist(final MapleMist mist, final int duration, boolean poison, boolean fake) {
        addMapObject(mist);
        broadcastMessage(fake ? mist.makeFakeSpawnData(30) : mist.makeSpawnData());
        TimerManager tMan = TimerManager.getInstance();
        final ScheduledFuture<?> poisonSchedule;
        if (poison) {
            Runnable poisonTask = new Runnable() {

                @Override
                public void run() {
                    List<MapleMapObject> affectedMonsters = getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.MONSTER));
                    for (MapleMapObject mo : affectedMonsters) {
                        if (mist.makeChanceResult()) {
                            MonsterStatusEffect poisonEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), mist.getSourceSkill(), false);
                            ((MapleMonster) mo).applyStatus(mist.getOwner(), poisonEffect, true, duration);
                        }
                    }
                }
            };
            poisonSchedule = tMan.register(poisonTask, 2000, 2500);
        } else {
            poisonSchedule = null;
        }
        tMan.schedule(new Runnable() {

            @Override
            public void run() {
                removeMapObject(mist);
                if (poisonSchedule != null) {
                    poisonSchedule.cancel(false);
                }
                broadcastMessage(mist.makeDestroyData());
            }
        }, duration);
    }

    public void disappearingItemDrop(final MapleMapObject dropper,
            final MapleCharacter owner, final IItem item, Point pos) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner);
        broadcastMessage(MaplePacketCreator.dropItemFromMapObject(item.getItemId(), drop.getObjectId(), 0, 0, dropper.getPosition(), droppos, (byte) 3), drop.getPosition());
    }

    public void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final IItem item, Point pos, final boolean ffaDrop, final boolean expire) {
        TimerManager tMan = TimerManager.getInstance();
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner);
        spawnAndAddRangedMapObject(drop, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                c.getSession().write(MaplePacketCreator.dropItemFromMapObject(item.getItemId(), drop.getObjectId(), 0, ffaDrop ? 0 : owner.getId(),
                        dropper.getPosition(), droppos, (byte) 1));
            }
        }, null);
        broadcastMessage(MaplePacketCreator.dropItemFromMapObject(item.getItemId(), drop.getObjectId(), 0, ffaDrop ? 0
                : owner.getId(), dropper.getPosition(), droppos, (byte) 0), drop.getPosition());

        if (expire) {
            tMan.schedule(new ExpireMapItemJob(drop), dropLife);
        }

        activateItemReactors(drop);
    }

    private class TimerDestroyWorker implements Runnable {

        @Override
        public void run() {
            if (mapTimer != null) {
                int warpMap = mapTimer.warpToMap();
                int minWarp = mapTimer.minLevelToWarp();
                int maxWarp = mapTimer.maxLevelToWarp();
                mapTimer = null;
                if (warpMap != -1) {
                    MapleMap map2wa2 = ChannelServer.getInstance(channel).getMapFactory().getMap(warpMap);
                    String warpmsg = "You will now be warped to " + map2wa2.getStreetName() + " : " + map2wa2.getMapName();
                    broadcastMessage(MaplePacketCreator.serverNotice(6, warpmsg));
                    for (MapleCharacter chr : getCharacters()) {
                        try {
                            if (chr.getLevel() >= minWarp && chr.getLevel() <= maxWarp) {
                                chr.changeMap(map2wa2, map2wa2.getPortal(0));
                            } else {
                                chr.getClient().getSession().write(MaplePacketCreator.serverNotice(5, "You are not at least level " + minWarp + " or you are higher than level " + maxWarp + "."));
                            }
                        } catch (Exception ex) {
                            String errormsg = "There was a problem warping you. Please contact a GM";
                            chr.getClient().getSession().write(MaplePacketCreator.serverNotice(5, errormsg));
                        }
                    }
                }
            }
        }
    }

    public void addMapTimer(int duration) {
        ScheduledFuture<?> sf0f = TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
        mapTimer = new MapleMapTimer(sf0f, duration, -1, -1, -1);
        // TimerManager.getInstance().

        broadcastMessage(mapTimer.makeSpawnData());
    }

    public void addMapTimer(int duration, int mapToWarpTo) {
        ScheduledFuture<?> sf0f = TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
        mapTimer = new MapleMapTimer(sf0f, duration, mapToWarpTo, 0, 256);
        // TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
        broadcastMessage(mapTimer.makeSpawnData());
    }

    public void addMapTimer(int duration, int mapToWarpTo, int minLevelToWarp) {
        ScheduledFuture<?> sf0f = TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
        mapTimer = new MapleMapTimer(sf0f, duration, mapToWarpTo, minLevelToWarp, 256);
        // TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
        broadcastMessage(mapTimer.makeSpawnData());
    }

    public void addMapTimer(int duration, int mapToWarpTo, int minLevelToWarp, int maxLevelToWarp) {
        ScheduledFuture<?> sf0f = TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
        mapTimer = new MapleMapTimer(sf0f, duration, mapToWarpTo, minLevelToWarp, maxLevelToWarp);
        // TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
        broadcastMessage(mapTimer.makeSpawnData());
    }

    public void clearMapTimer() {
        if (mapTimer != null) {
            mapTimer.getSF0F().cancel(true);
        }
        mapTimer = null;
    }

    private void activateItemReactors(MapleMapItem drop) {
        IItem item = drop.getItem();
        final TimerManager tMan = TimerManager.getInstance();
        //check for reactors on map that might use this item
        for (MapleMapObject o : mapobjects.values()) {
            if (o.getType() == MapleMapObjectType.REACTOR) {
                if (((MapleReactor) o).getReactorType() == 100) {
                    if (((MapleReactor) o).getReactItem().getLeft() == item.getItemId() && ((MapleReactor) o).getReactItem().getRight() <= item.getQuantity()) {
                        Rectangle area = ((MapleReactor) o).getArea();

                        if (area.contains(drop.getPosition())) {
                            MapleClient ownerClient = null;
                            if (drop.getOwner() != null) {
                                ownerClient = drop.getOwner().getClient();
                            }
                            MapleReactor reactor = (MapleReactor) o;
                            if (!reactor.isTimerActive()) {
                                tMan.schedule(new ActivateItemReactor(drop, reactor, ownerClient), 5000);
                                reactor.setTimerActive(true);
                            }
                        }
                    }
                }
            }
        }
    }

    public void AriantPQStart() {
        int i = 1;
        for (MapleCharacter chars2 : this.getCharacters()) {
            broadcastMessage(MaplePacketCreator.updateAriantPQRanking(chars2.getName(), 0, false));
            broadcastMessage(MaplePacketCreator.serverNotice(0, MaplePacketCreator.updateAriantPQRanking(chars2.getName(), 0, false).toString()));
            if (this.getCharacters().size() > i) {
                broadcastMessage(MaplePacketCreator.updateAriantPQRanking(null, 0, true));
                broadcastMessage(MaplePacketCreator.serverNotice(0, MaplePacketCreator.updateAriantPQRanking(chars2.getName(), 0, true).toString()));
            }
            i++;
        }
    }

    public void spawnMesoDrop(final int meso, final int displayMeso, Point position, final MapleMapObject dropper, final MapleCharacter owner, final boolean ffaLoot) {
        TimerManager tMan = TimerManager.getInstance();
        final Point droppos = calcDropPos(position, position);
        final MapleMapItem mdrop = new MapleMapItem(meso, displayMeso, droppos, dropper, owner);
        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                c.getSession().write(MaplePacketCreator.dropMesoFromMapObject(displayMeso, mdrop.getObjectId(), dropper.getObjectId(),
                        ffaLoot ? 0 : owner.getId(), dropper.getPosition(), droppos, (byte) 1));
            }
        }, null);
        tMan.schedule(new ExpireMapItemJob(mdrop), dropLife);
    }

    public void startMapEffect(String msg, int itemId) {
        if (mapEffect != null) {
            return;
        }
        mapEffect = new MapleMapEffect(msg, itemId);
        broadcastMessage(mapEffect.makeStartData());
        TimerManager tMan = TimerManager.getInstance();
        /*tMan.schedule(new Runnable() {
        @Override
        public void run() {
        mapEffect.setActive(false);
        broadcastMessage(mapEffect.makeStartData());
        }
        }, 20000);*/
        tMan.schedule(new Runnable() {

            @Override
            public void run() {
                broadcastMessage(mapEffect.makeDestroyData());
                mapEffect = null;
            }
        }, 30000);
    }

    /**
     * Adds a player to this map and sends nescessary data
     *
     * @param chr
     */
    public void addPlayer(MapleCharacter chr) {
        //log.warn("[dc] [level2] Player {} enters map {}", new Object[] { chr.getName(), mapid });
        synchronized (characters) {
            this.characters.add(chr);
        }
        synchronized (this.mapobjects) {
            if (!chr.isHidden()) {
                broadcastMessage(chr, (MaplePacketCreator.spawnPlayerMapobject(chr)), false);
                MaplePet[] pets = chr.getPets();
                for (int i = 0; i < 3; i++) {
                    if (pets[i] != null) {
                        pets[i].setPos(getGroundBelow(chr.getPosition()));
                        broadcastMessage(chr, MaplePacketCreator.showPet(chr, pets[i], false, false), false);
                    } else {
                        break;
                    }
                }
                if (chr.getChalkboard() != null) {
                    broadcastMessage(chr, (MaplePacketCreator.useChalkboard(chr, false)), false);
                }
            } else {
                broadcastGMMessage(chr, (MaplePacketCreator.spawnPlayerMapobject(chr)), false);
                MaplePet[] pets = chr.getPets();
                for (int i = 0; i < 3; i++) {
                    if (pets[i] != null) {
                        pets[i].setPos(getGroundBelow(chr.getPosition()));
                        broadcastGMMessage(chr, MaplePacketCreator.showPet(chr, pets[i], false, false), false);
                    } else {
                        break;
                    }
                }
                if (chr.getChalkboard() != null) {
                    broadcastGMMessage(chr, (MaplePacketCreator.useChalkboard(chr, false)), false);
                }
            }
            sendObjectPlacement(chr.getClient());
            //chr.getClient().getSession().write(MaplePacketCreator.spawnPlayerMapobject(chr));
            switch (getId()) {
                case 1:
                case 2:
                case 809000101:
                case 809000201:
                    chr.getClient().getSession().write(MaplePacketCreator.showEquipEffect());
            }
            MaplePet[] pets = chr.getPets();
            for (int i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    pets[i].setPos(getGroundBelow(chr.getPosition()));
                    chr.getClient().getSession().write(MaplePacketCreator.showPet(chr, pets[i], false, false));
                }
            }
            if (chr.getChalkboard() != null) {
                chr.getClient().getSession().write((MaplePacketCreator.useChalkboard(chr, false)));
            }
            this.mapobjects.put(Integer.valueOf(chr.getObjectId()), chr);
        }

        MapleStatEffect summonStat = chr.getStatForBuff(MapleBuffStat.SUMMON);
        if (summonStat != null) {
            MapleSummon summon = chr.getSummons().get(summonStat.getSourceId());
            summon.setPosition(getGroundBelow(chr.getPosition()));
            chr.getMap().spawnSummon(summon);
            updateMapObjectVisibility(chr, summon);
        }

        if (mapEffect != null) {
            mapEffect.sendStartData(chr.getClient());
        }

        if (MapleTVEffect.active) {
            if (hasMapleTV() && MapleTVEffect.packet != null) {
                chr.getClient().getSession().write(MapleTVEffect.packet);
            }
        }

        if (getTimeLimit() > 0 && getForcedReturnMap() != null) {
            chr.getClient().getSession().write(MaplePacketCreator.getClock(getTimeLimit()));
            chr.startMapTimeLimitTask(this, this.getForcedReturnMap());
        }

        if (chr.getEventInstance() != null && chr.getEventInstance().isTimerStarted()) {
            chr.getClient().getSession().write(MaplePacketCreator.getClock((int) (chr.getEventInstance().getTimeLeft() / 1000)));
        }

        if (hasClock()) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            int second = cal.get(Calendar.SECOND);
            chr.getClient().getSession().write((MaplePacketCreator.getClockTime(hour, min, second)));
        }

        if (hasBoat() == 2) {
            chr.getClient().getSession().write((MaplePacketCreator.boatPacket(true)));
        } else if (hasBoat() == 1 && (chr.getMapId() != 200090000 || chr.getMapId() != 200090010)) {
            chr.getClient().getSession().write(MaplePacketCreator.boatPacket(false));
        }

        chr.receivePartyMemberHP();
    }

    public void removePlayer(MapleCharacter chr) {
        //log.warn("[dc] [level2] Player {} leaves map {}", new Object[] { chr.getName(), mapid });
        synchronized (characters) {
            characters.remove(chr);
        }
        removeMapObject(Integer.valueOf(chr.getObjectId()));
        broadcastMessage(MaplePacketCreator.removePlayerFromMap(chr.getId()));
        for (MapleMonster monster : chr.getControlledMonsters()) {
            monster.setController(null);
            monster.setControllerHasAggro(false);
            monster.setControllerKnowsAboutAggro(false);
            updateMonsterController(monster);
        }

        if (chr.hasFakeChar()) {
            for (FakeCharacter ch : chr.getFakeChars()) {
                if (ch.follow()) {
                    chr.howmanyfakechars = chr.getFakeChars().size();
                    ch.getFakeChar().getMap().removePlayer(ch.getFakeChar());
                }
            }
        }

        chr.leaveMap();
        chr.cancelMapTimeLimitTask();

        for (MapleSummon summon : chr.getSummons().values()) {
            if (summon.isPuppet()) {
                chr.cancelBuffStats(MapleBuffStat.PUPPET);
            } else {
                removeMapObject(summon);
            }
        }
    }

    /**
     * Broadcasts the given packet to everyone on the map but the source. source = null Broadcasts to everyone
     *
     * @param source
     * @param packet
     */
    // public void broadcastMessage(MapleCharacter source, MaplePacket packet) {
    // synchronized (characters) {
    // for (MapleCharacter chr : characters) {
    // if (chr != source) {
    // chr.getClient().getSession().write(packet);
    // }
    // }
    // }
    // }
    /**
     * Broadcast a message to everyone in the map
     *
     * @param packet
     */
    public void broadcastMessage(MaplePacket packet) {
        broadcastMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }

    /**
     * Nonranged. Repeat to source according to parameter.
     *
     * @param source
     * @param packet
     * @param repeatToSource
     */
    public void broadcastMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource) {
        broadcastMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }

    /**
     * Ranged and repeat according to parameters.
     *
     * @param source
     * @param packet
     * @param repeatToSource
     * @param ranged
     */
    public void broadcastMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource, boolean ranged) {
        broadcastMessage(repeatToSource ? null : source, packet, ranged ? MapleCharacter.MAX_VIEW_RANGE_SQ : Double.POSITIVE_INFINITY, source.getPosition());
    }

    /**
     * Always ranged from Point.
     *
     * @param packet
     * @param rangedFrom
     */
    public void broadcastMessage(MaplePacket packet, Point rangedFrom) {
        broadcastMessage(null, packet, MapleCharacter.MAX_VIEW_RANGE_SQ, rangedFrom);
    }

    /**
     * Always ranged from point. Does not repeat to source.
     *
     * @param source
     * @param packet
     * @param rangedFrom
     */
    public void broadcastMessage(MapleCharacter source, MaplePacket packet, Point rangedFrom) {
        broadcastMessage(source, packet, MapleCharacter.MAX_VIEW_RANGE_SQ, rangedFrom);
    }

    private void broadcastMessage(MapleCharacter source, MaplePacket packet, double rangeSq, Point rangedFrom) {
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                if (chr != source && !chr.isFake()) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().getSession().write(packet);
                        }
                    } else {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        }
    }

    public void broadcastGMMessage(MaplePacket packet) {
        broadcastGMMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }

    public void broadcastGMMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource) {
        broadcastGMMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }

    private void broadcastGMMessage(MapleCharacter source, MaplePacket packet, double rangeSq, Point rangedFrom) {
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                if (chr != source && !chr.isFake() && chr.isGM()) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().getSession().write(packet);
                        }
                    } else {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        }
    }

    public void broadcastBlueFamilyMessage(MaplePacket packet) {
        broadcastBlueFamilyMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }

    public void broadcastBlueFamilyMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource) {
        broadcastBlueFamilyMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }

    private void broadcastBlueFamilyMessage(MapleCharacter source, MaplePacket packet, double rangeSq, Point rangedFrom) {
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                if (chr != source && !chr.isFake() && chr.isBlueFamily()) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().getSession().write(packet);
                        }
                    } else {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        }
    }

    public void broadcastOwnerMessage(MaplePacket packet) {
        broadcastOwnerMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }

    public void broadcastOwnerMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource) {
        broadcastOwnerMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }

    private void broadcastOwnerMessage(MapleCharacter source, MaplePacket packet, double rangeSq, Point rangedFrom) {
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                if (chr != source && !chr.isFake() && chr.isOwner()) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().getSession().write(packet);
                        }
                    } else {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        }
    }

    public void broadcastRedFamilyMessage(MaplePacket packet) {
        broadcastRedFamilyMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }

    public void broadcastRedFamilyMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource) {
        broadcastRedFamilyMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }

    private void broadcastRedFamilyMessage(MapleCharacter source, MaplePacket packet, double rangeSq, Point rangedFrom) {
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                if (chr != source && !chr.isFake() && chr.isRedFamily()) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().getSession().write(packet);
                        }
                    } else {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        }
    }

    public void broadcastNONGMMessage(MaplePacket packet) {
        broadcastNONGMMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }

    public void broadcastNONGMMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource) {
        broadcastNONGMMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }

    private void broadcastNONGMMessage(MapleCharacter source, MaplePacket packet, double rangeSq, Point rangedFrom) {
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                if (chr != source && !chr.isFake() && !chr.isGM()) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().getSession().write(packet);
                        }
                    } else {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        }
    }

    private boolean isNonRangedType(MapleMapObjectType type) {
        switch (type) {
            case NPC:
            case PLAYER:
            case HIRED_MERCHANT:
            case MIST:
            case PLAYER_NPC:
                //case REACTOR:
                return true;
        }
        return false;
    }

    private void sendObjectPlacement(MapleClient mapleClient) {
        for (MapleMapObject o : mapobjects.values()) {
            if (isNonRangedType(o.getType())) {
                // make sure not to spawn a dead reactor
                // if (o.getType() == MapleMapObjectType.REACTOR) {
                // if (reactors.get((MapleReactor) o)) {
                // o.sendSpawnData(mapleClient);
                // }
                // } else
                o.sendSpawnData(mapleClient);
            } else if (o.getType() == MapleMapObjectType.MONSTER) {
                updateMonsterController((MapleMonster) o);
            }
        }
        MapleCharacter chr = mapleClient.getPlayer();

        if (chr != null) {
            for (MapleMapObject o : getMapObjectsInRange(chr.getPosition(), MapleCharacter.MAX_VIEW_RANGE_SQ, rangedMapobjectTypes)) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    if (((MapleReactor) o).isAlive()) {
                        o.sendSpawnData(chr.getClient());
                        chr.addVisibleMapObject(o);
                    }
                } else {
                    o.sendSpawnData(chr.getClient());
                    chr.addVisibleMapObject(o);
                }
            }
        } else {
            log.info("sendObjectPlacement invoked with null char");
        }
    }

    public List<MapleMapObject> getMapObjectsInRange(Point from, double rangeSq, List<MapleMapObjectType> types) {
        List<MapleMapObject> ret = new LinkedList<MapleMapObject>();
        synchronized (mapobjects) {
            for (MapleMapObject l : mapobjects.values()) {
                if (types.contains(l.getType())) {
                    if (from.distanceSq(l.getPosition()) <= rangeSq) {
                        ret.add(l);
                    }
                }
            }
        }
        return ret;
    }

    public List<MapleMapObject> getItemsInRange(Point from, double rangeSq) {
        List<MapleMapObject> ret = new LinkedList<MapleMapObject>();
        synchronized (mapobjects) {
            for (MapleMapObject l : mapobjects.values()) {
                if (l.getType() == MapleMapObjectType.ITEM) {
                    if (from.distanceSq(l.getPosition()) <= rangeSq) {
                        ret.add(l);
                    }
                }
            }
        }
        return ret;
    }

    public List<MapleMapObject> getMapObjectsInRect(Rectangle box, List<MapleMapObjectType> types) {
        List<MapleMapObject> ret = new LinkedList<MapleMapObject>();
        synchronized (mapobjects) {
            for (MapleMapObject l : mapobjects.values()) {
                if (types.contains(l.getType())) {
                    if (box.contains(l.getPosition())) {
                        ret.add(l);
                    }
                }
            }
        }
        return ret;
    }

    public List<MapleCharacter> getPlayersInRect(Rectangle box, List<MapleCharacter> chr) {
        List<MapleCharacter> character = new LinkedList<MapleCharacter>();
        synchronized (characters) {
            for (MapleCharacter a : characters) {
                if (chr.contains(a.getClient().getPlayer())) {
                    if (box.contains(a.getPosition())) {
                        character.add(a);
                    }
                }
            }
        }
        return character;
    }

    public void addPortal(MaplePortal myPortal) {
        portals.put(myPortal.getId(), myPortal);
    }

    public MaplePortal getPortal(String portalname) {
        for (MaplePortal port : portals.values()) {
            if (port.getName().equals(portalname)) {
                return port;
            }
        }
        return null;
    }

    public MaplePortal getPortal(int portalid) {
        return portals.get(portalid);
    }

    public void addMapleArea(Rectangle rec) {
        areas.add(rec);
    }

    public List<Rectangle> getAreas() {
        return new ArrayList<Rectangle>(areas);
    }

    public Rectangle getArea(int index) {
        return areas.get(index);
    }

    public void setFootholds(MapleFootholdTree footholds) {
        this.footholds = footholds;
    }

    public MapleFootholdTree getFootholds() {
        return footholds;
    }

    /**
     * not threadsafe, please synchronize yourself
     *
     * @param monster
     */
    public void addMonsterSpawn(MapleMonster monster, int mobTime) {
        Point newpos = calcPointBelow(monster.getPosition());
        newpos.y -= 1;
        SpawnPoint sp = new SpawnPoint(monster, newpos, mobTime);

        monsterSpawn.add(sp);
        if (sp.shouldSpawn() || mobTime == -1) { // -1 does not respawn and should not either but force ONE spawn
            sp.spawnMonster(this);
        }
    }

    public float getMonsterRate() {
        return monsterRate;
    }

    public Collection<MapleCharacter> getCharacters() {
        synchronized (characters) {
            return Collections.unmodifiableCollection(this.characters);
        }
    }

    public void removePortals() {
        for (MaplePortal pt : getPortals()) {
            pt.setScriptName("blank");
        }
    }

    public void spawnNpc(int npcId, Point pos) {
        MapleNPC npc = MapleLifeFactory.getNPC(npcId);
        if (npc != null && !npc.getName().equals("MISSINGNO")) {
            npc.setPosition(pos);
            npc.setCy(pos.y);
            npc.setRx0(pos.x + 50);
            npc.setRx1(pos.x - 50);
            npc.setFh(getFootholds().findBelow(pos).getId());
            npc.setCustom(true);
            addMapObject(npc);
            broadcastMessage(MaplePacketCreator.spawnNPC(npc, false));
        }
    }

    private void XtremeDrops(final MapleCharacter dropOwner, final MapleMonster monster) {
        //double rand;
        ChannelServer cserv = dropOwner.getClient().getChannelServer();
        int randd;
        int toAdd;
        int times;
        int dropArray[] = {1082223, 1072344, 1102041, 1102042, 1082230, 1122000, 1002357, 1012070, 1012071, 1012072, 1050018, 1051017}; //These are the drops, -1 means meso :D
        short dropAmount[] = {100, 200, 100, 1, 1, 5, 50, 100, 1, 50, 100, 150, 1, 1, 1, 1, 1}; //Drop amount - Amount of what item to drop
        int dropAmountz[] = {100, 200, 100, 1, 1, 5, 50, 100, 1, 50, 100, 150, 1, 1, 1, 1, 1}; //Drop amount - Amount of what item to drop
        times = (int) (5 + Math.floor(Math.random() * 10)); // 40
        List<Integer> toDrop = new ArrayList<Integer>();
        List<Integer> amountDrop = new ArrayList<Integer>();
        for (int i = 0; i < times; i++) {
            randd = (int) (Math.floor(Math.random() * (dropArray.length)));
            toAdd = dropArray[randd];
            toDrop.add(toAdd);
            amountDrop.add(dropAmountz[randd]);
        }
        final int mesoRate = cserv.getMesoRate();
        //Set<Integer> alreadyDropped = new HashSet<Integer>();

        if (toDrop.size() > times) {
            toDrop = toDrop.subList(0, times);
        }
        Point[] toPoint = new Point[toDrop.size()];
        int shiftDirection = 0;
        int shiftCount = 0;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        int curX = Math.min(Math.max(monster.getPosition().x - 25 * (toDrop.size() / 2), footholds.getMinDropX() + 25),
                footholds.getMaxDropX() - toDrop.size() * 25);
        int curY = Math.max(monster.getPosition().y, footholds.getY1());
        //int monsterShift = curX -
        while (shiftDirection < 3 && shiftCount < 1000) {
            // TODO for real center drop the monster width is needed o.o"
            if (shiftDirection == 1) {
                curX += 25;
            } else if (shiftDirection == 2) {
                curX -= 25;
            }
            // now do it
            for (int i = 0; i < times; i++) {
                MapleFoothold wall = footholds.findWall(new Point(curX, curY), new Point(curX + toDrop.size() * 25, curY));
                if (wall != null) {
                    //System.out.println("found a wall. wallX " + wall.getX1() + " curX " + curX);
                    if (wall.getX1() < curX) {
                        shiftDirection = 1;
                        shiftCount++;
                        break;
                    } else if (wall.getX1() == curX) {
                        if (shiftDirection == 0) {
                            shiftDirection = 1;
                        }
                        shiftCount++;
                        break;
                    } else {
                        shiftDirection = 2;
                        shiftCount++;
                        break;
                    }
                } else if (i == toDrop.size() - 1) {
                    //System.out.println("ok " + curX);
                    shiftDirection = 3;
                }
                final Point dropPos = calcDropPos(new Point(curX + i * 25, curY), new Point(monster.getPosition()));
                toPoint[i] = new Point(curX + i * 25, curY);
                final int drop = toDrop.get(i);
                //final int dropAmounti = amountDrop.get(i);
                //final short dropAmountReal = (short) (dropAmounti);
                int tempmeso;
                if (drop == -1) { // meso
                    //final int mesoRate = ChannelServer.getInstance(dropOwner.getClient().getChannel()).getMesoRate();
                    Random r = new Random();
                    double mesoDecrease = Math.pow(0.93, monster.getExp() / 300.0);
                    if (mesoDecrease > 1.0) {
                        mesoDecrease = 1.0;
                    }
                    tempmeso = Math.min(30000, (int) (mesoDecrease * (monster.getExp()) *
                            (1.0 + r.nextInt(20)) / 10.0));
                } else {
                    tempmeso = 0;
                }
                if (dropOwner.getBuffedValue(MapleBuffStat.MESOUP) != null) {
                    tempmeso = (int) (tempmeso * dropOwner.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
                }
                final int meso = tempmeso;

                if (meso > 0) {
                    final MapleMonster dropMonster = monster;
                    final MapleCharacter dropChar = dropOwner;
                    TimerManager.getInstance().schedule(new Runnable() {

                        public void run() {
                            spawnMesoDrop(meso * mesoRate, meso, dropPos, dropMonster, dropChar, true);
                        }
                    }, monster.getAnimationTime("die1"));
                } else {
                    IItem idrop;
                    MapleInventoryType type = ii.getInventoryType(drop);
                    if (type.equals(MapleInventoryType.EQUIP)) {
                        Equip nEquip = ii.randomizeStats((Equip) ii.getEquipById(drop), 30);
                        idrop = nEquip;
                    } else {
                        idrop = new Item(drop, (byte) 0, (short) 1);
                        // Randomize quantity for certain items
                        if (ii.isArrowForBow(drop) || ii.isArrowForCrossBow(drop) || ii.isThrowingStar(drop)) {
                            idrop.setQuantity((short) (1 + ii.getSlotMax(drop) * Math.random()));
                        }
                        //idrop.setQuantity(dropAmountReal); //Set the quantity! w00t!
                    }

                    StringBuilder logMsg = new StringBuilder("Created as a drop from monster ");
                    logMsg.append(monster.getObjectId());
                    logMsg.append(" (");
                    logMsg.append(monster.getId());
                    logMsg.append(") at ");
                    logMsg.append(dropPos.toString());
                    logMsg.append(" on map ");
                    logMsg.append(mapid);
                    idrop.log(logMsg.toString(), false);

                    final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, monster, dropOwner);
                    final MapleMapObject dropMonster = monster;
                    final MapleCharacter dropChar = dropOwner;
                    final TimerManager tMan = TimerManager.getInstance();
                    final MapleClient c;

                    tMan.schedule(new Runnable() {

                        public void run() {
                            spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

                                public void sendPackets(MapleClient c) {
                                    c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, mdrop.getObjectId(), dropMonster.getObjectId(), monster.isBoss() ? 0 : dropChar.getId(), dropMonster.getPosition(), dropPos, (byte) 1));
                                }
                            });

                            tMan.schedule(new ExpireMapItemJob(mdrop), 60000);
                        }
                    }, monster.getAnimationTime("die1"));
                    //activateItemReactors(mdrop); -- No we dont need to activate reactors... =.="

                }
            }
        }
    }

    private void bossHunterDrops(final MapleMonster monster, final MapleCharacter dropOwner) {
        //double rand;
        ChannelServer cserv = dropOwner.getClient().getChannelServer();
        int randd;
        int toAdd;
        int times = 1;
        int dropArray[] = {1002739, 1002740, 1002749, 1002750, 1052148, 1052149, 1072342, 1072343, 1072345, 1072346};
        List<Integer> toDrop = new ArrayList<Integer>();
        if (Math.random() * 3000 > monster.getLevel()) {
            return;
        }
        randd = (int) (Math.floor(Math.random() * (dropArray.length)));
        toAdd = dropArray[randd];
        toDrop.add(toAdd);
        final int mesoRate = cserv.getMesoRate();
        //Set<Integer> alreadyDropped = new HashSet<Integer>();

        Point[] toPoint = new Point[toDrop.size()];
        int shiftDirection = 0;
        int shiftCount = 0;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        int curX = Math.min(Math.max(monster.getPosition().x - 25 * (toDrop.size() / 2), footholds.getMinDropX() + 25),
                footholds.getMaxDropX() - toDrop.size() * 25);
        int curY = Math.max(monster.getPosition().y, footholds.getY1());
        //int monsterShift = curX -
        try {
            while (shiftDirection < 3 && shiftCount < 1000) {
                //real center drop the monster width is needed o.o"
                if (shiftDirection == 1) {
                    curX += 25;
                } else if (shiftDirection == 2) {
                    curX -= 25;
                }
                // now do it
                for (int i = 0; i < times; i++) {
                    MapleFoothold wall = footholds.findWall(new Point(curX, curY), new Point(curX + toDrop.size() * 25, curY));
                    if (wall != null) {
                        //System.out.println("found a wall. wallX " + wall.getX1() + " curX " + curX);
                        if (wall.getX1() < curX) {
                            shiftDirection = 1;
                            shiftCount++;
                            break;
                        } else if (wall.getX1() == curX) {
                            if (shiftDirection == 0) {
                                shiftDirection = 1;
                            }
                            shiftCount++;
                            break;
                        } else {
                            shiftDirection = 2;
                            shiftCount++;
                            break;
                        }
                    } else if (i == toDrop.size() - 1) {
                        //System.out.println("ok " + curX);
                        shiftDirection = 3;
                    }
                    final Point dropPos = calcDropPos(new Point(curX + i * 25, curY), new Point(monster.getPosition()));
                    toPoint[i] = new Point(curX + i * 25, curY);
                    final int drop = toDrop.get(i);
                    int tempmeso;
                    if (drop == -1) { // meso
                        //final int mesoRate = ChannelServer.getInstance(dropOwner.getClient().getChannel()).getMesoRate();
                        Random r = new Random();
                        double mesoDecrease = Math.pow(0.93, monster.getExp() / 300.0);
                        if (mesoDecrease > 1.0) {
                            mesoDecrease = 1.0;
                        }
                        tempmeso = Math.min(30000, (int) (mesoDecrease * (monster.getExp()) *
                                (1.0 + r.nextInt(20)) / 10.0));
                    } else {
                        tempmeso = 0;
                    }
                    if (dropOwner.getBuffedValue(MapleBuffStat.MESOUP) != null) {
                        tempmeso = (int) (tempmeso * dropOwner.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
                    }
                    final int meso = tempmeso;
                    int addnx;
                    addnx = 1;
                    if (addnx > 0) {
                        dropOwner.modifyCSPoints(0, addnx);
                        dropOwner.getClient().getSession().write(MaplePacketCreator.serverNotice(5, "You have gained NX Cash (+" + addnx + ")."));
                    }


                    if (meso > 0) {
                        final MapleMonster dropMonster = monster;
                        final MapleCharacter dropChar = dropOwner;
                        TimerManager.getInstance().schedule(new Runnable() {

                            public void run() {
                                spawnMesoDrop(meso * mesoRate, meso, dropPos, dropMonster, dropChar, true);
                            }
                        }, monster.getAnimationTime("die1"));
                    } else {
                        IItem idrop;
                        MapleInventoryType type = ii.getInventoryType(drop);
                        if (type.equals(MapleInventoryType.EQUIP)) {
                            Equip nEquip = ii.randomizeStats((Equip) ii.getEquipById(drop), monster.getLevel() / 10);
                            idrop = nEquip;
                        } else {
                            idrop = new Item(drop, (byte) 0, (short) 1);
                            // Randomize quantity for certain items
                            if (ii.isArrowForBow(drop) || ii.isArrowForCrossBow(drop) || ii.isThrowingStar(drop)) {
                                idrop.setQuantity((short) (1 + ii.getSlotMax(drop) * Math.random()));
                            }
                        }

                        StringBuilder logMsg = new StringBuilder("Created as a drop from monster ");
                        logMsg.append(monster.getObjectId());
                        logMsg.append(" (");
                        logMsg.append(monster.getId());
                        logMsg.append(") at ");
                        logMsg.append(dropPos.toString());
                        logMsg.append(" on map ");
                        logMsg.append(mapid);
                        idrop.log(logMsg.toString(), false);

                        final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, monster, dropOwner);
                        final MapleMapObject dropMonster = monster;
                        final MapleCharacter dropChar = dropOwner;
                        final TimerManager tMan = TimerManager.getInstance();
                        final MapleClient c;

                        tMan.schedule(new Runnable() {

                            public void run() {
                                spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

                                    public void sendPackets(MapleClient c) {
                                        c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, mdrop.getObjectId(), dropMonster.getObjectId(), monster.isBoss() ? 0 : dropChar.getId(), dropMonster.getPosition(), dropPos, (byte) 1));
                                    }
                                });

                                tMan.schedule(new ExpireMapItemJob(mdrop), 60000);
                            }
                        }, monster.getAnimationTime("die1"));
                        //activateItemReactors(mdrop); -- No we dont need to activate reactors... =.="

                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            return;
        }
    }

    private void ZakumDrops(final MapleCharacter dropOwner, final MapleMonster monster) {
        //double rand;
        ChannelServer cserv = dropOwner.getClient().getChannelServer();
        int randd;
        int toAdd;
        int times;
        int dropArray[] = {1412010, 1402016, 1432011, 1302023, 1442020, 1422013, 1322029, 1312015, 1442002, 1382008, 1372009, 1462018, 1452017, 1472031, 1472033, 1332027, 2020014, 2020015, 2001001, 2000006, 2000005, 4006000, 4006001}; //These are the drops, -1 means meso :D
        //short dropAmount[] = {100, 200, 100, 1, 1, 5, 50, 100, 1, 50, 100, 150, 1, 1, 1, 1, 1}; //Drop amount - Amount of what item to drop
        int dropAmountz[] = {100, 200, 100, 1, 1, 5, 50, 100, 1, 50, 100, 150, 1, 1, 1, 1, 1, 100, 50, 20, 100, 100, 3}; //Drop amount - Amount of what item to drop
        times = (int) (5 + Math.floor(Math.random() * 60));
        List<Integer> toDrop = new ArrayList<Integer>();
        List<Integer> amountDrop = new ArrayList<Integer>();
        for (int i = 0; i < times; i++) {
            randd = (int) (Math.floor(Math.random() * (dropArray.length)));
            toAdd = dropArray[randd];
            toDrop.add(toAdd);
            amountDrop.add(dropAmountz[randd]);
        }
        final int mesoRate = cserv.getMesoRate();
        //Set<Integer> alreadyDropped = new HashSet<Integer>();

        if (toDrop.size() > times) {
            toDrop = toDrop.subList(0, times);
        }
        Point[] toPoint = new Point[toDrop.size()];
        int shiftDirection = 0;
        int shiftCount = 0;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        int curX = Math.min(Math.max(monster.getPosition().x - 25 * (toDrop.size() / 2), footholds.getMinDropX() + 25),
                footholds.getMaxDropX() - toDrop.size() * 25);
        int curY = Math.max(monster.getPosition().y, footholds.getY1());
        //int monsterShift = curX -
        while (shiftDirection < 3 && shiftCount < 1000) {
            // TODO for real center drop the monster width is needed o.o"
            if (shiftDirection == 1) {
                curX += 25;
            } else if (shiftDirection == 2) {
                curX -= 25;
            }
            // now do it
            for (int i = 0; i < times; i++) {
                MapleFoothold wall = footholds.findWall(new Point(curX, curY), new Point(curX + toDrop.size() * 25, curY));
                if (wall != null) {
                    //System.out.println("found a wall. wallX " + wall.getX1() + " curX " + curX);
                    if (wall.getX1() < curX) {
                        shiftDirection = 1;
                        shiftCount++;
                        break;
                    } else if (wall.getX1() == curX) {
                        if (shiftDirection == 0) {
                            shiftDirection = 1;
                        }
                        shiftCount++;
                        break;
                    } else {
                        shiftDirection = 2;
                        shiftCount++;
                        break;
                    }
                } else if (i == toDrop.size() - 1) {
                    //System.out.println("ok " + curX);
                    shiftDirection = 3;
                }
                final Point dropPos = calcDropPos(new Point(curX + i * 25, curY), new Point(monster.getPosition()));
                toPoint[i] = new Point(curX + i * 25, curY);
                final int drop = toDrop.get(i);
                //final int dropAmounti = amountDrop.get(i);
                //final short dropAmountReal = (short) (dropAmounti);
                int tempmeso;
                if (drop == -1) { // meso
                    //final int mesoRate = ChannelServer.getInstance(dropOwner.getClient().getChannel()).getMesoRate();
                    Random r = new Random();
                    double mesoDecrease = Math.pow(0.93, monster.getExp() / 300.0);
                    if (mesoDecrease > 1.0) {
                        mesoDecrease = 1.0;
                    }
                    tempmeso = Math.min(30000, (int) (mesoDecrease * (monster.getExp()) *
                            (1.0 + r.nextInt(20)) / 10.0));
                } else {
                    tempmeso = 0;
                }
                if (dropOwner.getBuffedValue(MapleBuffStat.MESOUP) != null) {
                    tempmeso = (int) (tempmeso * dropOwner.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
                }
                final int meso = tempmeso;

                if (meso > 0) {
                    final MapleMonster dropMonster = monster;
                    final MapleCharacter dropChar = dropOwner;
                    TimerManager.getInstance().schedule(new Runnable() {

                        public void run() {
                            spawnMesoDrop(meso * mesoRate, meso, dropPos, dropMonster, dropChar, true);
                        }
                    }, monster.getAnimationTime("die1"));
                } else {
                    IItem idrop;
                    MapleInventoryType type = ii.getInventoryType(drop);
                    if (type.equals(MapleInventoryType.EQUIP)) {
                        Equip nEquip = ii.randomizeStats((Equip) ii.getEquipById(drop), 10);
                        /*                      Equip lol = (Equip) ii.getEquipById(3);
                        ii.randomizeStats(lol, 3); */
                        idrop = nEquip;
                    } else {
                        idrop = new Item(drop, (byte) 0, (short) 1);
                        // Randomize quantity for certain items
                        if (ii.isArrowForBow(drop) || ii.isArrowForCrossBow(drop) || ii.isThrowingStar(drop)) {
                            idrop.setQuantity((short) (1 + ii.getSlotMax(drop) * Math.random()));
                        }
                        //idrop.setQuantity(dropAmountReal); //Set the quantity! w00t!
                    }

                    StringBuilder logMsg = new StringBuilder("Created as a drop from monster ");
                    logMsg.append(monster.getObjectId());
                    logMsg.append(" (");
                    logMsg.append(monster.getId());
                    logMsg.append(") at ");
                    logMsg.append(dropPos.toString());
                    logMsg.append(" on map ");
                    logMsg.append(mapid);
                    idrop.log(logMsg.toString(), false);

                    final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, monster, dropOwner);
                    final MapleMapObject dropMonster = monster;
                    final MapleCharacter dropChar = dropOwner;
                    final TimerManager tMan = TimerManager.getInstance();
                    final MapleClient c;

                    tMan.schedule(new Runnable() {

                        public void run() {
                            spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

                                public void sendPackets(MapleClient c) {
                                    c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, mdrop.getObjectId(), dropMonster.getObjectId(), monster.isBoss() ? 0 : dropChar.getId(), dropMonster.getPosition(), dropPos, (byte) 1));
                                }
                            });

                            tMan.schedule(new ExpireMapItemJob(mdrop), 60000);
                        }
                    }, monster.getAnimationTime("die1"));
                    //activateItemReactors(mdrop); -- No we dont need to activate reactors... =.="

                }
            }
        }
    }

    private void superPianusDrops(final MapleCharacter dropOwner, final MapleMonster monster) {
        //double rand;
        ChannelServer cserv = dropOwner.getClient().getChannelServer();
        int randd;
        int toAdd;
        int times;
        int dropArray[] = {1082223, 1072344, 1102041, 1102042, 1082230, 1122000, 1002357, 1012070, 1012071, 1012072, 1050018, 1051017}; //These are the drops, -1 means meso :D
        short dropAmount[] = {100, 200, 100, 1, 1, 5, 50, 100, 1, 50, 100, 150, 1, 1, 1, 1, 1}; //Drop amount - Amount of what item to drop
        int dropAmountz[] = {100, 200, 100, 1, 1, 5, 50, 100, 1, 50, 100, 150, 1, 1, 1, 1, 1}; //Drop amount - Amount of what item to drop
        times = (int) (5 + Math.floor(Math.random() * 40));
        List<Integer> toDrop = new ArrayList<Integer>();
        List<Integer> amountDrop = new ArrayList<Integer>();
        for (int i = 0; i < times; i++) {
            randd = (int) (Math.floor(Math.random() * (dropArray.length)));
            toAdd = dropArray[randd];
            toDrop.add(toAdd);
            amountDrop.add(dropAmountz[randd]);
        }
        final int mesoRate = cserv.getMesoRate();
        //Set<Integer> alreadyDropped = new HashSet<Integer>();

        if (toDrop.size() > times) {
            toDrop = toDrop.subList(0, times);
        }
        Point[] toPoint = new Point[toDrop.size()];
        int shiftDirection = 0;
        int shiftCount = 0;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        int curX = Math.min(Math.max(monster.getPosition().x - 25 * (toDrop.size() / 2), footholds.getMinDropX() + 25),
                footholds.getMaxDropX() - toDrop.size() * 25);
        int curY = Math.max(monster.getPosition().y, footholds.getY1());
        //int monsterShift = curX -
        while (shiftDirection < 3 && shiftCount < 1000) {
            // TODO for real center drop the monster width is needed o.o"
            if (shiftDirection == 1) {
                curX += 25;
            } else if (shiftDirection == 2) {
                curX -= 25;
            }
            // now do it
            for (int i = 0; i < times; i++) {
                MapleFoothold wall = footholds.findWall(new Point(curX, curY), new Point(curX + toDrop.size() * 25, curY));
                if (wall != null) {
                    //System.out.println("found a wall. wallX " + wall.getX1() + " curX " + curX);
                    if (wall.getX1() < curX) {
                        shiftDirection = 1;
                        shiftCount++;
                        break;
                    } else if (wall.getX1() == curX) {
                        if (shiftDirection == 0) {
                            shiftDirection = 1;
                        }
                        shiftCount++;
                        break;
                    } else {
                        shiftDirection = 2;
                        shiftCount++;
                        break;
                    }
                } else if (i == toDrop.size() - 1) {
                    //System.out.println("ok " + curX);
                    shiftDirection = 3;
                }
                final Point dropPos = calcDropPos(new Point(curX + i * 25, curY), new Point(monster.getPosition()));
                toPoint[i] = new Point(curX + i * 25, curY);
                final int drop = toDrop.get(i);
                //final int dropAmounti = amountDrop.get(i);
                //final short dropAmountReal = (short) (dropAmounti);
                int tempmeso;
                if (drop == -1) { // meso
                    //final int mesoRate = ChannelServer.getInstance(dropOwner.getClient().getChannel()).getMesoRate();
                    Random r = new Random();
                    double mesoDecrease = Math.pow(0.93, monster.getExp() / 300.0);
                    if (mesoDecrease > 1.0) {
                        mesoDecrease = 1.0;
                    }
                    tempmeso = Math.min(30000, (int) (mesoDecrease * (monster.getExp()) *
                            (1.0 + r.nextInt(20)) / 10.0));
                } else {
                    tempmeso = 0;
                }
                if (dropOwner.getBuffedValue(MapleBuffStat.MESOUP) != null) {
                    tempmeso = (int) (tempmeso * dropOwner.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
                }
                final int meso = tempmeso;

                if (meso > 0) {
                    final MapleMonster dropMonster = monster;
                    final MapleCharacter dropChar = dropOwner;
                    TimerManager.getInstance().schedule(new Runnable() {

                        public void run() {
                            spawnMesoDrop(meso * mesoRate, meso, dropPos, dropMonster, dropChar, true);
                        }
                    }, monster.getAnimationTime("die1"));
                } else {
                    IItem idrop;
                    MapleInventoryType type = ii.getInventoryType(drop);
                    if (type.equals(MapleInventoryType.EQUIP)) {
                        Equip nEquip = ii.randomizeStats((Equip) ii.getEquipById(drop), 30);
                        idrop = nEquip;
                    } else {
                        idrop = new Item(drop, (byte) 0, (short) 1);
                        // Randomize quantity for certain items
                        if (ii.isArrowForBow(drop) || ii.isArrowForCrossBow(drop) || ii.isThrowingStar(drop)) {
                            idrop.setQuantity((short) (1 + ii.getSlotMax(drop) * Math.random()));
                        }
                        //idrop.setQuantity(dropAmountReal); //Set the quantity! w00t!
                    }

                    StringBuilder logMsg = new StringBuilder("Created as a drop from monster ");
                    logMsg.append(monster.getObjectId());
                    logMsg.append(" (");
                    logMsg.append(monster.getId());
                    logMsg.append(") at ");
                    logMsg.append(dropPos.toString());
                    logMsg.append(" on map ");
                    logMsg.append(mapid);
                    idrop.log(logMsg.toString(), false);

                    final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, monster, dropOwner);
                    final MapleMapObject dropMonster = monster;
                    final MapleCharacter dropChar = dropOwner;
                    final TimerManager tMan = TimerManager.getInstance();
                    final MapleClient c;

                    tMan.schedule(new Runnable() {

                        public void run() {
                            spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

                                public void sendPackets(MapleClient c) {
                                    c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, mdrop.getObjectId(), dropMonster.getObjectId(), monster.isBoss() ? 0 : dropChar.getId(), dropMonster.getPosition(), dropPos, (byte) 1));
                                }
                            });

                            tMan.schedule(new ExpireMapItemJob(mdrop), 60000);
                        }
                    }, monster.getAnimationTime("die1"));
                    //activateItemReactors(mdrop); -- No we dont need to activate reactors... =.="

                }
            }
        }
    }

    public int countMonster(final MapleCharacter chr) {
        MapleMap map = chr.getClient().getPlayer().getMap();
        double range = Double.POSITIVE_INFINITY;
        List<MapleMapObject> monsters = map.getMapObjectsInRange(chr.getClient().getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER));
        return monsters.size();
    }

    private void superPianusDropsWithCustomStats(final MapleCharacter dropOwner, final MapleMonster monster, int stats) {
        //double rand;
        ChannelServer cserv = dropOwner.getClient().getChannelServer();
        int randd;
        int toAdd;
        int times;
        int dropArray[] = {1082223, 1072344, 1102041, 1102042, 1082230, 1122000, 1002357, 1012070, 1012071, 1012072, 1050018, 1051017, 4001040, 4001040, 4001040, 4001040, 4001040, 4001040, 4001040, 4001040, 4001040, 4001040}; //These are the drops, -1 means meso :D
        short dropAmount[] = {100, 200, 100, 1, 1, 5, 50, 100, 1, 50, 100, 150, 1, 1, 1, 1, 1}; //Drop amount - Amount of what item to drop
        int dropAmountz[] = {100, 200, 100, 1, 1, 5, 100, 50, 1, 50, 100, 150, 1, 1, 1, 1, 1, 40, 80, 30, 20, 5, 70, 40, 60, 90, 100}; //Drop amount - Amount of what item to drop
        times = (int) (5 + Math.floor(Math.random() * 40));
        List<Integer> toDrop = new ArrayList<Integer>();
        List<Integer> amountDrop = new ArrayList<Integer>();
        for (int i = 0; i < times; i++) {
            randd = (int) (Math.floor(Math.random() * (dropArray.length)));
            toAdd = dropArray[randd];
            toDrop.add(toAdd);
            amountDrop.add(dropAmountz[randd]);
        }
        final int mesoRate = cserv.getMesoRate();
        //Set<Integer> alreadyDropped = new HashSet<Integer>();

        if (toDrop.size() > times) {
            toDrop = toDrop.subList(0, times);
        }
        Point[] toPoint = new Point[toDrop.size()];
        int shiftDirection = 0;
        int shiftCount = 0;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        int curX = Math.min(Math.max(monster.getPosition().x - 25 * (toDrop.size() / 2), footholds.getMinDropX() + 25),
                footholds.getMaxDropX() - toDrop.size() * 25);
        int curY = Math.max(monster.getPosition().y, footholds.getY1());
        //int monsterShift = curX -
        while (shiftDirection < 3 && shiftCount < 1000) {
            // TODO for real center drop the monster width is needed o.o"
            if (shiftDirection == 1) {
                curX += 25;
            } else if (shiftDirection == 2) {
                curX -= 25;
            }
            // now do it
            for (int i = 0; i < times; i++) {
                MapleFoothold wall = footholds.findWall(new Point(curX, curY), new Point(curX + toDrop.size() * 25, curY));
                if (wall != null) {
                    //System.out.println("found a wall. wallX " + wall.getX1() + " curX " + curX);
                    if (wall.getX1() < curX) {
                        shiftDirection = 1;
                        shiftCount++;
                        break;
                    } else if (wall.getX1() == curX) {
                        if (shiftDirection == 0) {
                            shiftDirection = 1;
                        }
                        shiftCount++;
                        break;
                    } else {
                        shiftDirection = 2;
                        shiftCount++;
                        break;
                    }
                } else if (i == toDrop.size() - 1) {
                    //System.out.println("ok " + curX);
                    shiftDirection = 3;
                }
                final Point dropPos = calcDropPos(new Point(curX + i * 25, curY), new Point(monster.getPosition()));
                toPoint[i] = new Point(curX + i * 25, curY);
                final int drop = toDrop.get(i);
                //final int dropAmounti = amountDrop.get(i);
                //final short dropAmountReal = (short) (dropAmounti);
                int tempmeso;
                if (drop == -1) { // meso
                    //final int mesoRate = ChannelServer.getInstance(dropOwner.getClient().getChannel()).getMesoRate();
                    Random r = new Random();
                    double mesoDecrease = Math.pow(0.93, monster.getExp() / 300.0);
                    if (mesoDecrease > 1.0) {
                        mesoDecrease = 1.0;
                    }
                    tempmeso = Math.min(30000, (int) (mesoDecrease * (monster.getExp()) *
                            (1.0 + r.nextInt(20)) / 10.0));
                } else {
                    tempmeso = 0;
                }
                if (dropOwner.getBuffedValue(MapleBuffStat.MESOUP) != null) {
                    tempmeso = (int) (tempmeso * dropOwner.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
                }
                final int meso = tempmeso;

                if (meso > 0) {
                    final MapleMonster dropMonster = monster;
                    final MapleCharacter dropChar = dropOwner;
                    TimerManager.getInstance().schedule(new Runnable() {

                        public void run() {
                            spawnMesoDrop(meso * mesoRate, meso, dropPos, dropMonster, dropChar, true);
                        }
                    }, monster.getAnimationTime("die1"));
                } else {
                    IItem idrop;
                    MapleInventoryType type = ii.getInventoryType(drop);
                    if (type.equals(MapleInventoryType.EQUIP)) {
                        Equip nEquip = ii.randomizeStats((Equip) ii.getEquipById(drop), stats);
                        idrop = nEquip;
                    } else {
                        idrop = new Item(drop, (byte) 0, (short) 1);
                        // Randomize quantity for certain items
                        if (ii.isArrowForBow(drop) || ii.isArrowForCrossBow(drop) || ii.isThrowingStar(drop)) {
                            idrop.setQuantity((short) (1 + ii.getSlotMax(drop) * Math.random()));
                        }
                        //idrop.setQuantity(dropAmountReal); //Set the quantity! w00t!
                    }

                    StringBuilder logMsg = new StringBuilder("Created as a drop from monster ");
                    logMsg.append(monster.getObjectId());
                    logMsg.append(" (");
                    logMsg.append(monster.getId());
                    logMsg.append(") at ");
                    logMsg.append(dropPos.toString());
                    logMsg.append(" on map ");
                    logMsg.append(mapid);
                    idrop.log(logMsg.toString(), false);

                    final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, monster, dropOwner);
                    final MapleMapObject dropMonster = monster;
                    final MapleCharacter dropChar = dropOwner;
                    final TimerManager tMan = TimerManager.getInstance();
                    final MapleClient c;

                    tMan.schedule(new Runnable() {

                        public void run() {
                            spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

                                public void sendPackets(MapleClient c) {
                                    c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, mdrop.getObjectId(), dropMonster.getObjectId(), monster.isBoss() ? 0 : dropChar.getId(), dropMonster.getPosition(), dropPos, (byte) 1));
                                }
                            });

                            tMan.schedule(new ExpireMapItemJob(mdrop), 60000);
                        }
                    }, monster.getAnimationTime("die1"));
                    //activateItemReactors(mdrop); -- No we dont need to activate reactors... =.="

                }
            }
        }
    }

    public MapleCharacter getCharacterById(int id) {
        for (MapleCharacter c : this.characters) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    private void updateMapObjectVisibility(MapleCharacter chr, MapleMapObject mo) {
        if (chr.isFake()) {
            return;
        }
        if (!chr.isMapObjectVisible(mo)) { // monster entered view range
            if (mo.getType() == MapleMapObjectType.SUMMON || mo.getPosition().distanceSq(chr.getPosition()) <= MapleCharacter.MAX_VIEW_RANGE_SQ) {
                chr.addVisibleMapObject(mo);
                mo.sendSpawnData(chr.getClient());
            }
        } else { // monster left view range
            if (mo.getType() != MapleMapObjectType.SUMMON && mo.getPosition().distanceSq(chr.getPosition()) > MapleCharacter.MAX_VIEW_RANGE_SQ) {
                chr.removeVisibleMapObject(mo);
                mo.sendDestroyData(chr.getClient());
            }
        }
    }

    public void moveMonster(MapleMonster monster, Point reportedPos) {
        monster.setPosition(reportedPos);
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                updateMapObjectVisibility(chr, monster);
            }
        }
    }

    public void movePlayer(MapleCharacter player, Point newPosition) {
        if (player.isFake()) {
            return;
        }
        player.setPosition(newPosition);
        Collection<MapleMapObject> visibleObjects = player.getVisibleMapObjects();
        MapleMapObject[] visibleObjectsNow = visibleObjects.toArray(new MapleMapObject[visibleObjects.size()]);
        for (MapleMapObject mo : visibleObjectsNow) {
            if (mapobjects.get(mo.getObjectId()) == mo) {
                updateMapObjectVisibility(player, mo);
            } else {
                player.removeVisibleMapObject(mo);
            }
        }
        for (MapleMapObject mo : getMapObjectsInRange(player.getPosition(), MapleCharacter.MAX_VIEW_RANGE_SQ,
                rangedMapobjectTypes)) {
            if (!player.isMapObjectVisible(mo)) {
                mo.sendSpawnData(player.getClient());
                player.addVisibleMapObject(mo);
            }
        }
    }

    public MaplePortal findClosestSpawnpoint(Point from) {
        MaplePortal closest = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : portals.values()) {
            double distance = portal.getPosition().distanceSq(from);
            if (portal.getType() >= 0 && portal.getType() <= 2 && distance < shortestDistance && portal.getTargetMapId() == 999999999) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }

    public void spawnDebug(MessageCallback mc) {
        mc.dropMessage("Spawndebug...");
        synchronized (mapobjects) {
            mc.dropMessage("Mapobjects in map: " + mapobjects.size() + " \"spawnedMonstersOnMap\": " +
                    spawnedMonstersOnMap + " spawnpoints: " + monsterSpawn.size() +
                    " maxRegularSpawn: " + getMaxRegularSpawn());
            int numMonsters = 0;
            for (MapleMapObject mo : mapobjects.values()) {
                if (mo instanceof MapleMonster) {
                    numMonsters++;
                }
            }
            mc.dropMessage("actual monsters: " + numMonsters);
        }
    }

    private int getMaxRegularSpawn() {
        return (int) (monsterSpawn.size() / monsterRate);
    }

    public Collection<MaplePortal> getPortals() {
        return Collections.unmodifiableCollection(portals.values());
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setClock(boolean hasClock) {
        this.clock = hasClock;
    }

    public boolean hasClock() {
        return clock;
    }

    public void setTown(boolean isTown) {
        this.town = isTown;
    }

    public boolean isTown() {
        return town;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public void setEverlast(boolean everlast) {
        this.everlast = everlast;
    }

    public boolean getEverlast() {
        return everlast;
    }

    public int getSpawnedMonstersOnMap() {
        return spawnedMonstersOnMap.get();
    }

    public Collection<MapleCharacter> getNearestPvpChar(Point attacker, double maxRange, double maxHeight, Collection<MapleCharacter> chr) {
        Collection<MapleCharacter> character = new LinkedList<MapleCharacter>();
        for (MapleCharacter a : characters) {
            if (chr.contains(a.getClient().getPlayer())) {
                Point attackedPlayer = a.getPosition();
                MaplePortal Port = a.getMap().findClosestSpawnpoint(a.getPosition());
                Point nearestPort = Port.getPosition();
                double safeDis = attackedPlayer.distance(nearestPort);
                double distanceX = attacker.distance(attackedPlayer.getX(), attackedPlayer.getY());
                if (PvPLibrary.isLeft) {
                    if (attacker.x > attackedPlayer.x && distanceX < maxRange && distanceX > 2 &&
                            attackedPlayer.y >= attacker.y - maxHeight && attackedPlayer.y <= attacker.y + maxHeight && safeDis > 2) {
                        character.add(a);
                    }
                }
                if (PvPLibrary.isRight) {
                    if (attacker.x < attackedPlayer.x && distanceX < maxRange && distanceX > 2 &&
                            attackedPlayer.y >= attacker.y - maxHeight && attackedPlayer.y <= attacker.y + maxHeight && safeDis > 2) {
                        character.add(a);
                    }
                }
            }
        }
        return character;
    }

    private class ExpireMapItemJob implements Runnable {

        private MapleMapItem mapitem;

        public ExpireMapItemJob(MapleMapItem mapitem) {
            this.mapitem = mapitem;
        }

        @Override
        public void run() {
            if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
                synchronized (mapitem) {
                    if (mapitem.isPickedUp()) {
                        return;
                    }
                    MapleMap.this.broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0),
                            mapitem.getPosition());
                    MapleMap.this.removeMapObject(mapitem);
                    mapitem.setPickedUp(true);
                }
            }
        }
    }

    private class ActivateItemReactor implements Runnable {

        private MapleMapItem mapitem;
        private MapleReactor reactor;
        private MapleClient c;

        public ActivateItemReactor(MapleMapItem mapitem, MapleReactor reactor, MapleClient c) {
            this.mapitem = mapitem;
            this.reactor = reactor;
            this.c = c;
        }

        @Override
        public void run() {
            if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
                synchronized (mapitem) {
                    TimerManager tMan = TimerManager.getInstance();
                    if (mapitem.isPickedUp()) {
                        return;
                    }
                    MapleMap.this.broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0), mapitem.getPosition());
                    MapleMap.this.removeMapObject(mapitem);
                    reactor.hitReactor(c);
                    reactor.setTimerActive(false);
                    //reactor.increaseState();
                    if (reactor.getDelay() > 0) { //This shit is negative.. Fix?
                        tMan.schedule(new Runnable() {

                            @Override
                            public void run() {
                                reactor.setState((byte) 0);
                                broadcastMessage(MaplePacketCreator.triggerReactor(reactor, 0));
                            }
                        }, reactor.getDelay());
                    }
                }
            }
        }
    }

    private class RespawnWorker implements Runnable {

        @Override
        public void run() {
            int playersOnMap = characters.size();

            if (playersOnMap == 0) {
                return;
            }

            int ispawnedMonstersOnMap = spawnedMonstersOnMap.get();
            int numShouldSpawn = (int) Math.round(Math.random() * ((2 + playersOnMap / 1.5 + (getMaxRegularSpawn() - ispawnedMonstersOnMap) / 4.0)));
            if (numShouldSpawn + ispawnedMonstersOnMap > getMaxRegularSpawn()) {
                numShouldSpawn = getMaxRegularSpawn() - ispawnedMonstersOnMap;
            }

            if (numShouldSpawn <= 0) {
                return;
            }

            // k find that many monsters that need respawning and respawn them O.o
            List<SpawnPoint> randomSpawn = new ArrayList<SpawnPoint>(monsterSpawn);
            Collections.shuffle(randomSpawn);
            int spawned = 0;
            for (SpawnPoint spawnPoint : randomSpawn) {
                if (spawnPoint.shouldSpawn()) {
                    spawnPoint.spawnMonster(MapleMap.this);
                    spawned++;
                }
                if (spawned >= numShouldSpawn) {
                    break;
                }
            }
        }
    }

    private static interface DelayedPacketCreation {

        void sendPackets(MapleClient c);
    }

    private static interface SpawnCondition {

        boolean canSpawn(MapleCharacter chr);
    }

    public int getHPDec() {
        return decHP;
    }

    public void setHPDec(int delta) {
        decHP = delta;
    }

    public int getHPDecProtect() {
        return this.protectItem;
    }

    public void setHPDecProtect(int delta) {
        this.protectItem = delta;
    }

    public int hasBoat() {
        if (boat && docked) {
            return 2;
        } else if (boat) {
            return 1;
        } else {
            return 0;
        }
    }

    public void setBoat(boolean hasBoat) {
        this.boat = hasBoat;
    }

    public void setDocked(boolean isDocked) {
        this.docked = isDocked;
    }

    public void addBotPlayer(MapleCharacter chr) {
        synchronized (characters) {
            this.characters.add(chr);
        }
        synchronized (this.mapobjects) {
            if (!chr.isHidden()) {
                broadcastMessage(chr, (MaplePacketCreator.spawnPlayerMapobject(chr)), false);
            } else {
                broadcastGMMessage(chr, (MaplePacketCreator.spawnPlayerMapobject(chr)), false);
            }
            this.mapobjects.put(Integer.valueOf(chr.getObjectId()), chr);
        }
    }

    public int playerCount() {
        List<MapleMapObject> players = getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER));
        return players.size();
    }

    public int mobCount() {
        List<MapleMapObject> mobsCount = getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
        return mobsCount.size();
    }

    public int countReactorsOnMap() {
        int count = 0;
        Collection<MapleMapObject> mmos = this.getMapObjects();
        for (MapleMapObject mmo : mmos) {
            if (mmo instanceof MapleReactor) {
                count++;
            }
        }
        return count;
    }

    public int countMobOnMap() {
        int count = 0;
        Collection<MapleMapObject> mmos = this.getMapObjects();
        for (MapleMapObject mmo : mmos) {
            if (mmo instanceof MapleMonster) {
                count++;
            }
        }
        return count;
    }

    public int countMobOnMap(int monsterid) {
        int count = 0;
        Collection<MapleMapObject> mmos = this.getMapObjects();
        for (MapleMapObject mmo : mmos) {
            if (mmo instanceof MapleMonster) {
                MapleMonster monster = (MapleMonster) mmo;
                if (monster.getId() == monsterid) {
                    count++;
                }
            }
        }
        return count;
    }

    public void setReactorState() {
        synchronized (this.mapobjects) {
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    ((MapleReactor) o).setState((byte) 1);
                    broadcastMessage(MaplePacketCreator.triggerReactor((MapleReactor) o, 1));
                }
            }
        }
    }

    public void setShowGate(boolean gate) {
        this.showGate = gate;
    }

    public boolean hasShowGate() {
        return showGate;
    }

    public boolean hasMapleTV() {
        int tvIds[] = {9250042, 9250043, 9250025, 9250045, 9250044, 9270001, 9270002, 9250023, 9250024, 9270003, 9270004, 9250026, 9270006, 9270007, 9250046, 9270000, 9201066, 9270005, 9270008, 9270009, 9270010, 9270011, 9270012, 9270013, 9270014, 9270015, 9270016, 9270040};
        for (int id : tvIds) {
            if (containsNPC(id)) {
                return true;
            }
        }
        return false;
    }

    public void removeMonster(MapleMonster mons) {
        spawnedMonstersOnMap.decrementAndGet();
        broadcastMessage(MaplePacketCreator.killMonster(mons.getObjectId(), true), mons.getPosition());
        removeMapObject(mons);
    }

    public boolean isPQMap() {
        switch (getId()) {
            case 103000800:
            case 103000804:
            case 922010100:
            case 922010200:
            case 922010201:
            case 922010300:
            case 922010400:
            case 922010401:
            case 922010402:
            case 922010403:
            case 922010404:
            case 922010405:
            case 922010500:
            case 922010600:
            case 922010700:
            case 922010800:
                return true;
            default:
                return false;
        }
    }

    public void setSnowBall(MapleSnowball ball) {
        switch (ball.getTeam()) {
            case 0:
                this.snowball0 = ball;
                break;
            case 1:
                this.snowball1 = ball;
                break;
            default:
                break;
        }
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public MapleSnowball getSnowBall(int team) {
        switch (team) {
            case 0:
                return snowball0;
            case 1:
                return snowball1;
            default:
                return null;
        }
    }

    public boolean isMiniDungeonMap() {
        switch (mapid) {
            case 100020000:
            case 105040304:
            case 105050100:
            case 221023400:
                return true;
            default:
                return false;
        }
    }

    public boolean hasTimer() {
        return timer;
    }

    public void setTimer(boolean hasClock) {
        this.timer = hasClock;
    }

    private final class warpAll implements Runnable {

        private MapleMap toGo;
        private MapleMap from;

        public warpAll(MapleMap toGoto, MapleMap from) {
            this.toGo = toGoto;
            this.from = from;
        }

        @Override
        public void run() {
            synchronized (toGo) {
                for (MapleCharacter ppp : characters) {
                    if (ppp.getMap().equals(from)) {
                        ppp.changeMap(toGo, toGo.getPortal(0));
                        if (ppp.getEventInstance() != null) {
                            ppp.getEventInstance().unregisterPlayer(ppp);
                        }
                    }
                }
            }
        }
    }

    public void addClock(int time) {
        broadcastMessage(MaplePacketCreator.getClock(time));
    }

    public boolean isPurpleCPQMap() {
        switch (this.getId()) {
            case 980000301:
            case 980000401:
                return true;
        }
        return false;
    }

    public void buffMonsters(int team, MonsterStatus status) {
        if (team == 0) {
            redTeamBuffs.add(status);
        } else if (team == 1) {
            blueTeamBuffs.add(status);
        }
        for (MapleMapObject mmo : this.mapobjects.values()) {
            if (mmo.getType() == MapleMapObjectType.MONSTER) {
                MapleMonster mob = (MapleMonster) mmo;
                if (mob.getTeam() == team) {
                    int skillID = getSkillId(status);
                    if (skillID != -1) {
                        MobSkill skill = getMobSkill(skillID, this.getSkillLevel(status));
                        mob.applyMonsterBuff(status, skill.getX(), skill.getSkillId(),
                                1000 * 60 * 10, skill);
                    }
                }
            }
        }
    }

    public void debuffMonsters(int team, MonsterStatus status) {
        if (team == 0) {
            removeStatus(status, team);
        } else if (team == 1) {
            removeStatus(status, team);
        }
        for (MapleMapObject mmo : this.mapobjects.values()) {
            if (mmo.getType() == MapleMapObjectType.MONSTER) {
                MapleMonster mob = (MapleMonster) mmo;
                if (mob.getTeam() == team) {
                    int skillID = getSkillId(status);
                    if (skillID != -1) {
                        if (mob.getMonsterBuffs().contains(status)) {
                            mob.cancelMonsterBuff(status);
                        }
                    }
                }
            }
        }
    }

    public GuardianSpawnPoint getRandomGuardianSpawn(int team) {
        boolean alltaken = false;
        for (GuardianSpawnPoint a : this.guardianSpawns) {
            if (!a.isTaken()) {
                alltaken = false;
                break;
            }
        }
        if (alltaken) {
            return null;
        }
        if (this.guardianSpawns.size() > 0) {
            while (true) {
                for (GuardianSpawnPoint gsp : this.guardianSpawns) {
                    if (!gsp.isTaken() && Math.random() < 0.3 && (gsp.getTeam() == -1 || gsp.getTeam() == team)) {
                        return gsp;
                    }
                }
            }
        }
        return null;
    }

    public void addGuardianSpawnPoint(GuardianSpawnPoint a) {
        this.guardianSpawns.add(a);
    }

    public int spawnGuardian(MonsterStatus status, int team) {
        List<MapleMapObject> reactors = getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
        for (GuardianSpawnPoint gs : this.guardianSpawns) {
            for (MapleMapObject o : reactors) {
                MapleReactor reactor = (MapleReactor) o;
                if (reactor.getCancelStatus().equals(status) && (reactor.getId() - 9980000) == team) {
                    return 0;
                }
            }
        }
        GuardianSpawnPoint pt = this.getRandomGuardianSpawn(team);
        if (pt == null) {
            return -1;
        }
        int reactorID = 9980000 + team;
        MapleReactor reactor = new MapleReactor(MapleReactorFactory.getReactor(reactorID), reactorID);
        pt.setTaken(true);
        reactor.setPosition(pt.getPosition());
        this.spawnReactor(reactor);
        reactor.setCancelStatus(status);
        reactor.setGuardian(pt);
        this.buffMonsters(team, status);
        getReactorByOid(reactor.getObjectId()).hitReactor(((MapleCharacter) this.getAllPlayer().get(0)).getClient());
        return 1;
    }

    public boolean isCPQMap() {
        switch (this.getId()) {
            case 980000101:
            case 980000201:
            case 980000301:
            case 980000401:
            case 980000501:
            case 980000601:
                return true;
        }
        return false;
    }

    public void mapMessage(int type, String message) {
        broadcastMessage(MaplePacketCreator.serverNotice(type, message));
    }

    public void removeStatus(MonsterStatus status, int team) {
        List<MonsterStatus> a = null;
        if (team == 0) {
            a = redTeamBuffs;
        } else if (team == 1) {
            a = blueTeamBuffs;
        }
        List<MonsterStatus> r = new LinkedList<MonsterStatus>();
        for (MonsterStatus ms : a) {
            if (ms.equals(status)) {
                r.add(ms);
            }
        }
        for (MonsterStatus al : r) {
            if (a.contains(al)) {
                a.remove(al);
            }
        }
    }

    public int getSkillId(MonsterStatus status) {
        if (status == MonsterStatus.WEAPON_ATTACK_UP) {
            return 100;
        } else if (status.equals(MonsterStatus.MAGIC_ATTACK_UP)) {
            return 101;
        } else if (status.equals(MonsterStatus.WEAPON_DEFENSE_UP)) {
            return 112;
        } else if (status.equals(MonsterStatus.MAGIC_DEFENSE_UP)) {
            return 113;
        } else if (status.equals(MonsterStatus.WEAPON_IMMUNITY)) {
            return 140;
        } else if (status.equals(MonsterStatus.MAGIC_IMMUNITY)) {
            return 141;
        }
        return -1;
    }

    public MobSkill getMobSkill(int skillId, int level) {
        return MobSkillFactory.getMobSkill(skillId, level);
    }

    public MobSkill getMobSkill(int skillID) {
        return MobSkillFactory.getMobSkill(skillID, 1);
    }

    public int getSkillLevel(MonsterStatus status) {
        if (status == MonsterStatus.WEAPON_ATTACK_UP) {
            return 1;
        } else if (status.equals(MonsterStatus.MAGIC_ATTACK_UP)) {
            return 1;
        } else if (status.equals(MonsterStatus.WEAPON_DEFENSE_UP)) {
            return 1;
        } else if (status.equals(MonsterStatus.MAGIC_DEFENSE_UP)) {
            return 1;
        } else if (status.equals(MonsterStatus.WEAPON_IMMUNITY)) {
            return 10;
        } else if (status.equals(MonsterStatus.MAGIC_IMMUNITY)) {
            return 9;
        }
        return -1;
    }

    /*   public void addMonsterSpawn(MapleMonster monster, int mobTime, int team) {
    Point newpos = calcPointBelow(monster.getPosition());
    newpos.y -= 1;
    SpawnPoint sp = new SpawnPoint(monster, newpos, mobTime, team);

    monsterSpawn.add(sp);
    if (sp.shouldSpawn() || mobTime == -1 || (team == 0 || team == 1)) { // -1 does not respawn and should not either but force ONE spawn
    sp.spawnMonster(this);
    }
    } */
    public void spawnCPQMonster(final MapleMonster monster, final int team) {
        monster.setMap(this);
        monster.setTeam(team);
        synchronized (this.mapobjects) {
            spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

                public void sendPackets(MapleClient c) {
                    if (c.getPlayer().getParty() != null) {
                        if (monster.getTeam() == c.getPlayer().getTeam()) {
                            c.getSession().write(MaplePacketCreator.spawnFakeMonster(monster, 0));
                        } else {
                            c.getSession().write(MaplePacketCreator.spawnMonster(monster, true));
                        }
                    } else {
                        c.getSession().write(MaplePacketCreator.spawnMonster(monster, true));
                    }
                    /*                                      Random rand = new Random();
                    if (monster.getId() == 9300166) {
                    removeAfter = rand.nextInt((4500) + 500);
                    }*/
                    if (monster.getRemoveAfter() > 0) { // 9300166
                        TimerManager.getInstance().schedule(new Runnable() {

                            @Override
                            public void run() {
                                killMonster(monster, (MapleCharacter) getAllPlayer().get(0), false, false, 3);
                            }
                        }, monster.getRemoveAfter());
                    }
                }
            }, null);
            updateMonsterController(monster);
            List<MonsterStatus> teamS = null;
            if (team == 0) {
                teamS = redTeamBuffs;
            } else if (team == 1) {
                teamS = blueTeamBuffs;
            }
            if (teamS != null) {
                for (MonsterStatus status : teamS) {
                    int skillID = getSkillId(status);
                    MobSkill skill = getMobSkill(skillID, this.getSkillLevel(status));
                    monster.applyMonsterBuff(status, skill.getX(), skill.getSkillId(),
                            60 * 1000 * 10, skill);
                }
            }
        }
        spawnedMonstersOnMap.incrementAndGet();
    }

    /*   public Point getRandomSP(int team) {
    if (takenSpawns.size() > 0) {
    for (SpawnPoint sp : monsterSpawn) {
    for (Point pt : takenSpawns) {
    if ((sp.getPosition().x == pt.x && sp.getPosition().y == pt.y) || (sp.getTeam() != team && !this.isBlueCPQMap())) {
    continue;
    } else {
    takenSpawns.add(pt);
    return sp.getPosition();
    }
    }
    }
    } else {
    for (SpawnPoint sp : monsterSpawn) {
    if (sp.getTeam() == team || this.isBlueCPQMap()) {
    takenSpawns.add(sp.getPosition());
    return sp.getPosition();
    }
    }
    }
    return null;
    } */
    public boolean isBlueCPQMap() {
        switch (this.getId()) {
            case 980000501:
            case 980000601:
                return true;
        }
        return false;
    }

    private void customDrops(final MapleCharacter dropOwner, final MapleMonster monster, int dropArray[], int time2) {
        //double rand;
        ChannelServer cserv = dropOwner.getClient().getChannelServer();
        int randd;
        int toAdd;
        int times;
        //int dropArray[] = {1082223, 1072344, 1102041, 1102042, 1082230, 1122000, 1002357, 1012070, 1012071, 1012072, 1050018, 1051017}; //These are the drops, -1 means meso :D
        //short dropAmount[] = {100, 200, 100, 1, 1, 5, 50, 100, 1, 50, 100, 150, 1, 1, 1, 1, 1}; //Drop amount - Amount of what item to drop
        int dropAmountz[] = {100, 200, 100, 1, 1, 5, 50, 100, 1, 50, 100, 150, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}; //Drop amount - Amount of what item to drop
        times = (int) (5 + Math.floor(Math.random() * time2)); // 40
        List<Integer> toDrop = new ArrayList<Integer>();
        List<Integer> amountDrop = new ArrayList<Integer>();
        for (int i = 0; i < times; i++) {
            randd = (int) (Math.floor(Math.random() * (dropArray.length)));
            toAdd = dropArray[randd];
            toDrop.add(toAdd);
            amountDrop.add(dropAmountz[randd]);
        }
        final int mesoRate = cserv.getMesoRate();
        //Set<Integer> alreadyDropped = new HashSet<Integer>();

        if (toDrop.size() > times) {
            toDrop = toDrop.subList(0, times);
        }
        Point[] toPoint = new Point[toDrop.size()];
        int shiftDirection = 0;
        int shiftCount = 0;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        int curX = Math.min(Math.max(monster.getPosition().x - 25 * (toDrop.size() / 2), footholds.getMinDropX() + 25),
                footholds.getMaxDropX() - toDrop.size() * 25);
        int curY = Math.max(monster.getPosition().y, footholds.getY1());
        //int monsterShift = curX -
        while (shiftDirection < 3 && shiftCount < 1000) {
            // TODO for real center drop the monster width is needed o.o"
            if (shiftDirection == 1) {
                curX += 25;
            } else if (shiftDirection == 2) {
                curX -= 25;
            }
            // now do it
            for (int i = 0; i < times; i++) {
                MapleFoothold wall = footholds.findWall(new Point(curX, curY), new Point(curX + toDrop.size() * 25, curY));
                if (wall != null) {
                    //System.out.println("found a wall. wallX " + wall.getX1() + " curX " + curX);
                    if (wall.getX1() < curX) {
                        shiftDirection = 1;
                        shiftCount++;
                        break;
                    } else if (wall.getX1() == curX) {
                        if (shiftDirection == 0) {
                            shiftDirection = 1;
                        }
                        shiftCount++;
                        break;
                    } else {
                        shiftDirection = 2;
                        shiftCount++;
                        break;
                    }
                } else if (i == toDrop.size() - 1) {
                    //System.out.println("ok " + curX);
                    shiftDirection = 3;
                }
                final Point dropPos = calcDropPos(new Point(curX + i * 25, curY), new Point(monster.getPosition()));
                toPoint[i] = new Point(curX + i * 25, curY);
                final int drop = toDrop.get(i);
                //final int dropAmounti = amountDrop.get(i);
                //final short dropAmountReal = (short) (dropAmounti);
                int tempmeso;
                if (drop == -1) { // meso
                    //final int mesoRate = ChannelServer.getInstance(dropOwner.getClient().getChannel()).getMesoRate();
                    Random r = new Random();
                    double mesoDecrease = Math.pow(0.93, monster.getExp() / 300.0);
                    if (mesoDecrease > 1.0) {
                        mesoDecrease = 1.0;
                    }
                    tempmeso = Math.min(30000, (int) (mesoDecrease * (monster.getExp()) *
                            (1.0 + r.nextInt(20)) / 10.0));
                } else {
                    tempmeso = 0;
                }
                if (dropOwner.getBuffedValue(MapleBuffStat.MESOUP) != null) {
                    tempmeso = (int) (tempmeso * dropOwner.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
                }
                final int meso = tempmeso;

                if (meso > 0) {
                    final MapleMonster dropMonster = monster;
                    final MapleCharacter dropChar = dropOwner;
                    TimerManager.getInstance().schedule(new Runnable() {

                        public void run() {
                            spawnMesoDrop(meso * mesoRate, meso, dropPos, dropMonster, dropChar, true);
                        }
                    }, monster.getAnimationTime("die1"));
                } else {
                    IItem idrop;
                    MapleInventoryType type = ii.getInventoryType(drop);
                    if (type.equals(MapleInventoryType.EQUIP)) {
                        Equip nEquip = ii.randomizeStats((Equip) ii.getEquipById(drop), 30);
                        idrop = nEquip;
                    } else {
                        idrop = new Item(drop, (byte) 0, (short) 1);
                        // Randomize quantity for certain items
                        if (ii.isArrowForBow(drop) || ii.isArrowForCrossBow(drop) || ii.isThrowingStar(drop)) {
                            idrop.setQuantity((short) (1 + ii.getSlotMax(drop) * Math.random()));
                        }
                        // idrop.setQuantity(dropAmountReal); //Set the quantity! w00t!
                    }

                    StringBuilder logMsg = new StringBuilder("Created as a drop from monster ");
                    logMsg.append(monster.getObjectId());
                    logMsg.append(" (");
                    logMsg.append(monster.getId());
                    logMsg.append(") at ");
                    logMsg.append(dropPos.toString());
                    logMsg.append(" on map ");
                    logMsg.append(mapid);
                    idrop.log(logMsg.toString(), false);

                    final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, monster, dropOwner);
                    final MapleMapObject dropMonster = monster;
                    final MapleCharacter dropChar = dropOwner;
                    final TimerManager tMan = TimerManager.getInstance();
                    final MapleClient c;

                    tMan.schedule(new Runnable() {

                        public void run() {
                            spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

                                public void sendPackets(MapleClient c) {
                                    c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, mdrop.getObjectId(), dropMonster.getObjectId(), monster.isBoss() ? 0 : dropChar.getId(), dropMonster.getPosition(), dropPos, (byte) 1));
                                }
                            });

                            tMan.schedule(new ExpireMapItemJob(mdrop), 60000);
                        }
                    }, monster.getAnimationTime("die1"));
                    //activateItemReactors(mdrop); -- No we dont need to activate reactors... =.="

                }
            }
        }
    }
}
