package net.sf.odinms.client;

import java.awt.Point;
import java.io.File;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.HashMap;
import net.sf.odinms.client.anticheat.CheatTracker;
import net.sf.odinms.database.DatabaseException;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.world.MapleMessengerCharacter;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.net.world.PlayerBuffValueHolder;
import net.sf.odinms.scripting.event.EventInstanceManager;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.server.MapleShop;
import net.sf.odinms.server.MapleStatEffect;
import net.sf.odinms.server.MapleStorage;
import net.sf.odinms.server.MapleTrade;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.server.maps.AbstractAnimatedMapleMapObject;
import net.sf.odinms.server.maps.MapleDoor;
import net.sf.odinms.server.maps.MapleMapObjectType;
import net.sf.odinms.server.maps.SavedLocationType;
import net.sf.odinms.server.maps.SummonMovementType;
import net.sf.odinms.server.quest.MapleCustomQuest;
import net.sf.odinms.server.quest.MapleQuest;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.Pair;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.net.PacketProcessor;
import net.sf.odinms.net.world.MapleMessenger;
import net.sf.odinms.net.world.MapleParty;
import net.sf.odinms.net.world.PartyOperation;
import net.sf.odinms.net.world.PlayerCoolDownValueHolder;
import net.sf.odinms.net.world.guild.MapleGuild;
import net.sf.odinms.net.world.guild.MapleGuildCharacter;
import net.sf.odinms.net.world.remote.WorldChannelInterface;
import net.sf.odinms.provider.MapleData;
import net.sf.odinms.provider.MapleDataProvider;
import net.sf.odinms.provider.MapleDataProviderFactory;
import net.sf.odinms.scripting.npc.NPCScriptManager;
import net.sf.odinms.server.MapleAchievements;
import net.sf.odinms.server.MapleSnowball;
import net.sf.odinms.server.MapleSquadType;
import net.sf.odinms.server.MonsterCarnival;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.life.MobSkill;
import net.sf.odinms.server.maps.FakeCharacter;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.server.maps.MapleMapFactory;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.server.PlayerInteraction.IPlayerInteractionManager;
import net.sf.odinms.server.PlayerInteraction.MapleMiniGame;
import net.sf.odinms.server.PlayerInteraction.MaplePlayerShop;
import net.sf.odinms.server.maps.MapleSummon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapleCharacter extends AbstractAnimatedMapleMapObject implements InventoryContainer {

    private static Logger log = LoggerFactory.getLogger(PacketProcessor.class);
    public static final double MAX_VIEW_RANGE_SQ = 850 * 850;
    private int world;
    private int accountid;
    private int rank;
    private int rankMove;
    private int jobRank;
    private int jobRankMove;
    private String name;
    private int level;
    private int reborns;
    private int realreborns;
    private int mkills;
    private int family;
    private int str, dex, luk, int_;
    private AtomicInteger exp = new AtomicInteger();
    private int hp, maxhp;
    private int mp, maxmp;
    private int mpApUsed, hpApUsed;
    private int hair, face;
    private AtomicInteger meso = new AtomicInteger();
    private int remainingAp, remainingSp;
    private int savedLocations[];
    private int fame;
    private long lastfametime;
    private List<Integer> lastmonthfameids;
    private transient int localmaxhp, localmaxmp;
    private transient int localstr, localdex, localluk, localint;
    private transient int magic, watk;
    private transient int acc;
    private transient double speedMod, jumpMod;
    private transient int localmaxbasedamage;
    private int id;
    private MapleClient client;
    private MapleMap map;
    private int initialSpawnPoint;
    private int mapid;
    private MapleShop shop = null;
    private IPlayerInteractionManager interaction = null;
    private MapleStorage storage = null;
    private MaplePet[] pets = new MaplePet[3];
    private ScheduledFuture<?> fullnessSchedule;
    private ScheduledFuture<?> fullnessSchedule_1;
    private ScheduledFuture<?> fullnessSchedule_2;
    private SkillMacro[] skillMacros = new SkillMacro[5];
    private MapleTrade trade = null;
    private MapleSkinColor skinColor = MapleSkinColor.NORMAL;
    private MapleJob job = MapleJob.BEGINNER;
    private int gender;
    private int gmLevel;
    private boolean hidden;
    private boolean canDoor = true;
    private int chair;
    private int itemEffect;
    private int APQScore;
    private List<MapleRing> crushRings = new ArrayList<MapleRing>();
    private List<MapleRing> friendshipRings = new ArrayList<MapleRing>();
    private List<MapleRing> marriageRings = new ArrayList<MapleRing>();
    private MapleParty party;
    private EventInstanceManager eventInstance = null;
    private MapleInventory[] inventory;
    private Map<MapleQuest, MapleQuestStatus> quests;
    private Set<MapleMonster> controlled = new LinkedHashSet<MapleMonster>();
    private Set<MapleMapObject> visibleMapObjects = new LinkedHashSet<MapleMapObject>();
    private Map<ISkill, SkillEntry> skills = new LinkedHashMap<ISkill, SkillEntry>();
    private Map<MapleBuffStat, MapleBuffStatValueHolder> effects = new LinkedHashMap<MapleBuffStat, MapleBuffStatValueHolder>();
    private HashMap<Integer, MapleKeyBinding> keymap = new LinkedHashMap<Integer, MapleKeyBinding>();
    private List<MapleDoor> doors = new ArrayList<MapleDoor>();
    public int jumpquests = gmLevel;
    private Map<Integer, MapleSummon> summons = new LinkedHashMap<Integer, MapleSummon>();
    private BuddyList buddylist;
    private Map<Integer, MapleCoolDownValueHolder> coolDowns = new LinkedHashMap<Integer, MapleCoolDownValueHolder>();
    private CheatTracker anticheat;
    private ScheduledFuture<?> dragonBloodSchedule;
    private ScheduledFuture<?> mapTimeLimitTask = null;
    private int guildid;
    private int guildrank, allianceRank;
    private MapleGuildCharacter mgc = null;
    private int paypalnx = 0, maplepoints = 0, cardnx = 0;
    private boolean incs, inmts;
    private int currentPage = 0, currentType = 0, currentTab = 1;
    private MapleMessenger messenger = null;
    int messengerposition = 4;
    private ScheduledFuture<?> hpDecreaseTask;
    private List<MapleDisease> diseases = new ArrayList<MapleDisease>();
    private ScheduledFuture<?> beholderHealingSchedule;
    private ScheduledFuture<?> beholderBuffSchedule;
    private ScheduledFuture<?> BerserkSchedule;
    private boolean Berserk = false;
    public SummonMovementType getMovementType;
    private String chalktext;
    // ------- CPQ ---------
    private int CP;
    private int totalCP;
    private int team;
    private int cpqRanking = 0;
    private long lastCatch = 0;
    private int cp = 0;
    private int totCP = 0;
    private int cpqteam;
    private MonsterCarnival monsterCarnival;
    // isbetachar
    private int betachar;
    // ------ Marriage -------
    private boolean married = false;
    private int partnerid;
    private int marriageQuestLevel;
    // ------ Spam Block -------
    private boolean canSmega = true;
    private boolean smegaEnabled = true;
    private boolean canTalk = true;
    //jump point system
    private int jumpPoints;
    //passion point system
    private int passionPoints;
    //rebirth point system
    private int rebirthPoints;
    // ETC
    private int relationship;
    private int strike;
    private int jailtime;
    private boolean strikeprompt;
    public String lastnpc;
    // PVP Toggle
    public boolean pvp;
    // Warning Variables
    public boolean ownerpermission = false;
    public boolean warned = false;
    public boolean stalk = false;
    public boolean warned2 = false;
    public boolean warned3 = false;
    public boolean warned4 = false;
    public boolean warned6 = false;
    public boolean warned9 = false;
    public boolean[] warning = new boolean[25];
    public boolean donator1 = false;
    public boolean donator2 = false;
    public boolean donator3 = false;
    public boolean donator4 = false;
    public boolean donator5 = false;
    public int donatorlevel;
    public boolean warned10 = false;
    public boolean soulisgay = false;
    public boolean letsfucksoul = false;
    // boss counter ... will be complete later :D
    public int bosscounter;
    public int mesomultiplier = 1; // if it was zero it would multiply it times 0
    // Autojob
    public boolean autojobon = false;
    public boolean autorebon = false;
    // Capture the Flag + True False Event
    public int blueteamwins = 0;// stupid
    public int redteamwins = 0;// stupid
    public boolean autorebirth = false;
    public int timeshit = 0;
    private MaplePlayerShop playerShop = null;
    private MapleMiniGame miniGame = null;
    public boolean hasflag = false;
    public boolean redteam = false; // stupid
    public boolean blueteam = false; // stupid
    public boolean isExiting = false; // useless
    public int howmanyfakechars = 0;
    public boolean setanswered = false;
    public boolean canpickup = true;
    public boolean hasSpawned = false;
    public boolean invincibility = false;
    public String flagPromptName = "None";
    public String flagPromptName2 = "None";
    // Vote Points
    private int votepoints;
    // ------- Misc. ------------
    private int zakumLvl; // Zero means they havent started yet.
    private int bossPoints;
    private List<FakeCharacter> fakes = new ArrayList<FakeCharacter>();
    private boolean isfake = false;
    private int clan;
    private boolean mapUpdated = true;
    private int bombpoints;
    private int pvpkills;
    private int pvpdeaths;
    private int donatePoints = 0;
    private MapleMount maplemount;
    private int gmtext = 0;
    private boolean challenged = false;
    private boolean shield = false;
    // -------- Calc. Dmg ---------
    private double sword;
    private double blunt;
    private double axe;
    private double spear;
    private double polearm;
    private double claw;
    private double dagger;
    private double staffwand = 0.1;
    private double crossbow;
    private double bow;
    private int skill = 0;
    private ISkill skil;
    private int maxDis;
    public int mpoints = 0;
    private transient int wdef, mdef;
    //private List<Integer> jobs = new ArrayList<Integer>();
    private int energybar = 0;
    private long afkTime;
    private long lastLogin = 0;
    private int ringRequest;
    private boolean hasMerchant;
    /* Achievements */
    private List<Integer> finishedAchievements = new ArrayList<Integer>();
    /* News */
    private List<String> news = new ArrayList<String>();
    // Prevent CC at ban
    public boolean banned = false;

    public MapleCharacter() {
        setStance(0);
        inventory = new MapleInventory[MapleInventoryType.values().length];
        for (MapleInventoryType type : MapleInventoryType.values()) {
            inventory[type.ordinal()] = new MapleInventory(type, (byte) 100);
        }

        savedLocations = new int[SavedLocationType.values().length];
        for (int i = 0; i < SavedLocationType.values().length; i++) {
            savedLocations[i] = -1;
        }

        quests = new LinkedHashMap<MapleQuest, MapleQuestStatus>();
        anticheat = new CheatTracker(this);
        afkTime = System.currentTimeMillis();
        setPosition(new Point(0, 0));
    }

    public MapleCharacter getThis() {
        return this;
    }

    public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) throws SQLException {
        MapleCharacter ret = new MapleCharacter();
        ret.client = client;
        ret.id = charid;

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
        ps.setInt(1, charid);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
            throw new RuntimeException("Loading the Char Failed (char not found)");
        }
        ret.name = rs.getString("name");
        ret.level = rs.getInt("level");
        ret.pvpdeaths = rs.getInt("pvpdeaths");
        ret.pvpkills = rs.getInt("pvpkills");
        ret.reborns = rs.getInt("reborns");
        ret.realreborns = rs.getInt("realreborns");
        ret.mkills = rs.getInt("mkills");
        ret.betachar = rs.getInt("betachar");
        ret.bosscounter = rs.getInt("bosscounter");
        ret.donatorlevel = rs.getInt("donatorlevel");
        ret.family = rs.getInt("family");
        ret.fame = rs.getInt("fame");
        ret.str = rs.getInt("str");
        ret.dex = rs.getInt("dex");
        ret.int_ = rs.getInt("int");
        ret.luk = rs.getInt("luk");
        ret.exp.set(rs.getInt("exp"));
        if (ret.exp.get() < 0) {
            ret.exp.set(0);
        }
        ret.hp = rs.getInt("hp");
        if (ret.hp < 50) {
            ret.hp = 50;
        }
        ret.maxhp = rs.getInt("maxhp");
        ret.mp = rs.getInt("mp");
        if (ret.mp < 50) {
            ret.mp = 50;
        }
        ret.maxmp = rs.getInt("maxmp");

        ret.hpApUsed = rs.getInt("hpApUsed");
        ret.mpApUsed = rs.getInt("mpApUsed");
        ret.hasMerchant = rs.getInt("HasMerchant") == 1;
        ret.remainingSp = rs.getInt("sp");
        ret.remainingAp = rs.getInt("ap");

        ret.meso.set(rs.getInt("meso"));
        ret.gmLevel = rs.getInt("gm");
        ret.clan = rs.getInt("clan");

        int mountexp = rs.getInt("mountexp");
        int mountlevel = rs.getInt("mountlevel");
        int mounttiredness = rs.getInt("mounttiredness");

        ret.married = rs.getInt("married") == 0 ? false : true;
        ret.partnerid = rs.getInt("partnerid");
        ret.marriageQuestLevel = rs.getInt("marriagequest");

        ret.zakumLvl = rs.getInt("zakumLvl");
        ret.jumpPoints = rs.getInt("jumpPoints");

        ret.rebirthPoints = rs.getInt("rebirthPoints");
        ret.passionPoints = rs.getInt("passionPoints");
        ret.strike = rs.getInt("strike");
        ret.jailtime = rs.getInt("jailtime");
        ret.relationship = rs.getInt("relationship");
        ret.skinColor = MapleSkinColor.getById(rs.getInt("skincolor"));
        ret.gender = rs.getInt("gender");
        ret.job = MapleJob.getById(rs.getInt("job"));

        ret.hair = rs.getInt("hair");
        ret.face = rs.getInt("face");

        ret.accountid = rs.getInt("accountid");

        ret.mapid = rs.getInt("map");
        ret.initialSpawnPoint = rs.getInt("spawnpoint");
        ret.world = rs.getInt("world");

        ret.rank = rs.getInt("rank");
        ret.rankMove = rs.getInt("rankMove");
        ret.jobRank = rs.getInt("jobRank");
        ret.jobRankMove = rs.getInt("jobRankMove");

        ret.guildid = rs.getInt("guildid");
        ret.guildrank = rs.getInt("guildrank");
        ret.allianceRank = rs.getInt("allianceRank");
        if (ret.guildid > 0) {
            ret.mgc = new MapleGuildCharacter(ret);
        }
        int buddyCapacity = rs.getInt("buddyCapacity");
        ret.bossPoints = rs.getInt("bosspoints");
        ret.buddylist = new BuddyList(buddyCapacity);
        ret.gmtext = rs.getInt("gmtext");
        if (channelserver) {

            MapleMapFactory mapFactory = ChannelServer.getInstance(client.getChannel()).getMapFactory();
            ret.map = mapFactory.getMap(ret.mapid);
            if (ret.map == null) { //char is on a map that doesn't exist warp it to henesys

                ret.map = mapFactory.getMap(100000000);
            }
            MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
            if (portal == null) {
                portal = ret.map.getPortal(0); // char is on a spawnpoint that doesn't exist - select the first spawnpoint instead
                ret.initialSpawnPoint = 0;
            }
            ret.setPosition(portal.getPosition());

            int partyid = rs.getInt("party");
            if (partyid >= 0) {
                try {
                    MapleParty party = client.getChannelServer().getWorldInterface().getParty(partyid);
                    if (party != null && party.getMemberById(ret.id) != null) {
                        ret.party = party;
                    }
                } catch (RemoteException e) {
                    client.getChannelServer().reconnectWorld();
                }
            }

            int messengerid = rs.getInt("messengerid");
            int position = rs.getInt("messengerposition");
            if (messengerid > 0 && position < 4 && position > -1) {
                try {
                    WorldChannelInterface wci = ChannelServer.getInstance(client.getChannel()).getWorldInterface();
                    MapleMessenger messenger = wci.getMessenger(messengerid);
                    if (messenger != null) {
                        ret.messenger = messenger;
                        ret.messengerposition = position;
                    }
                } catch (RemoteException e) {
                    client.getChannelServer().reconnectWorld();
                }
            }
        }

        rs.close();
        ps.close();
        ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
        ps.setInt(1, ret.accountid);
        rs = ps.executeQuery();
        if (rs.next()) {
            ret.getClient().setAccountName(rs.getString("name"));
            ret.getClient().setAccountPass(rs.getString("password"));
            ret.getClient().setGuest(rs.getInt("guest") > 0);
            ret.donatePoints = rs.getInt("donorPoints");
            ret.lastLogin = rs.getLong("LastLoginInMilliseconds");
            ret.votepoints = rs.getInt("votepoints");
            if (!ret.isGM()) {
                if (ret.donatePoints == 1) {
                    ret.gmLevel = 1;
                } else if (ret.donatePoints > 1) {
                    ret.gmLevel = 2;
                }
            }
            ret.paypalnx = rs.getInt("paypalNX");
            ret.maplepoints = rs.getInt("mPoints");
            ret.cardnx = rs.getInt("cardNX");
        }
        rs.close();
        ps.close();


        String sql = "SELECT * FROM inventoryitems " + "LEFT JOIN inventoryequipment USING (inventoryitemid) " + "WHERE characterid = ?";
        if (!channelserver) {
            sql += " AND inventorytype = " + MapleInventoryType.EQUIPPED.getType();
        }
        ps = con.prepareStatement(sql);
        ps.setInt(1, charid);
        rs = ps.executeQuery();
        while (rs.next()) {
            MapleInventoryType type = MapleInventoryType.getByType((byte) rs.getInt("inventorytype"));
            if (type.equals(MapleInventoryType.EQUIP) || type.equals(MapleInventoryType.EQUIPPED)) {
                int itemid = rs.getInt("itemid");
                int ringId = rs.getInt("ringid");
                if (ringId > 0) {
                    MapleRing ring = MapleRing.loadFromDb(ringId);
                    if (itemid >= 1112001 && itemid <= 1112006) {
                        if (ring != null) {
                            ret.crushRings.add(ring);
                        }
                    } else if (itemid >= 1112800 && itemid <= 1112802) {
                        if (ring != null) {
                            ret.friendshipRings.add(ring);
                        }
                    } else if (itemid >= 1112803 && itemid <= 1112807 || itemid == 1112809) {
                        if (ring != null) {
                            ret.marriageRings.add(ring);
                        }
                    }
                }
                Equip equip = new Equip(itemid, (byte) rs.getInt("position"), rs.getInt("ringid"));
                equip.setOwner(rs.getString("owner"));
                equip.setQuantity((short) rs.getInt("quantity"));
                equip.setAcc((short) rs.getInt("acc"));
                equip.setAvoid((short) rs.getInt("avoid"));
                equip.setDex((short) rs.getInt("dex"));
                equip.setHands((short) rs.getInt("hands"));
                equip.setHp((short) rs.getInt("hp"));
                equip.setInt((short) rs.getInt("int"));
                equip.setJump((short) rs.getInt("jump"));
                equip.setLuk((short) rs.getInt("luk"));
                equip.setMatk((short) rs.getInt("matk"));
                equip.setMdef((short) rs.getInt("mdef"));
                equip.setMp((short) rs.getInt("mp"));
                equip.setSpeed((short) rs.getInt("speed"));
                equip.setStr((short) rs.getInt("str"));
                equip.setWatk((short) rs.getInt("watk"));
                equip.setWdef((short) rs.getInt("wdef"));
                equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                equip.setLocked((byte) rs.getInt("locked"));
                equip.setLevel((byte) rs.getInt("level"));
                ret.getInventory(type).addFromDB(equip);
            } else {
                Item item = new Item(rs.getInt("itemid"), (byte) rs.getInt("position"), (short) rs.getInt("quantity"), rs.getInt("petid"));
                item.setOwner(rs.getString("owner"));
                ret.getInventory(type).addFromDB(item);
            }
        }
        rs.close();
        ps.close();

        if (channelserver) {
            ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            PreparedStatement pse = con.prepareStatement("SELECT * FROM queststatusmobs WHERE queststatusid = ?");
            while (rs.next()) {
                MapleQuest q = MapleQuest.getInstance(rs.getInt("quest"));
                MapleQuestStatus status = new MapleQuestStatus(q, MapleQuestStatus.Status.getById(rs.getInt("status")));
                long cTime = rs.getLong("time");
                if (cTime > -1) {
                    status.setCompletionTime(cTime * 1000);
                }
                status.setForfeited(rs.getInt("forfeited"));
                ret.quests.put(q, status);
                pse.setInt(1, rs.getInt("queststatusid"));
                ResultSet rsMobs = pse.executeQuery();
                while (rsMobs.next()) {
                    status.setMobKills(rsMobs.getInt("mob"), rsMobs.getInt("count"));
                }
                rsMobs.close();
            }
            rs.close();
            ps.close();
            pse.close();

            ps = con.prepareStatement("SELECT skillid,skilllevel,masterlevel FROM skills WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            while (rs.next()) {
                ret.skills.put(SkillFactory.getSkill(rs.getInt("skillid")), new SkillEntry(rs.getInt("skilllevel"), rs.getInt("masterlevel")));
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            while (rs.next()) {
                int skill1 = rs.getInt("skill1");
                int skill2 = rs.getInt("skill2");
                int skill3 = rs.getInt("skill3");
                String name = rs.getString("name");
                int shout = rs.getInt("shout");
                int position = rs.getInt("position");
                SkillMacro macro = new SkillMacro(skill1, skill2, skill3, name, shout, position);
                ret.skillMacros[position] = macro;
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT `key`,`type`,`action` FROM keymap WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            while (rs.next()) {
                int key = rs.getInt("key");
                int type = rs.getInt("type");
                int action = rs.getInt("action");
                ret.keymap.put(Integer.valueOf(key), new MapleKeyBinding(type, action));
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT `locationtype`,`map` FROM savedlocations WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            while (rs.next()) {
                String locationType = rs.getString("locationtype");
                int mapid = rs.getInt("map");
                ret.savedLocations[SavedLocationType.valueOf(locationType).ordinal()] = mapid;
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 30");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            ret.lastfametime = 0;
            ret.lastmonthfameids = new ArrayList<Integer>(31);
            while (rs.next()) {
                ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
                ret.lastmonthfameids.add(Integer.valueOf(rs.getInt("characterid_to")));
            }
            rs.close();
            ps.close();
            ret.buddylist.loadFromDb(charid);
            ret.storage = MapleStorage.loadOrCreateFromDB(ret.accountid);
        }

        String achsql = "SELECT * FROM achievements WHERE accountid = ?";
        ps = con.prepareStatement(achsql);
        ps.setInt(1, ret.accountid);
        rs = ps.executeQuery();
        while (rs.next()) {
            ret.finishedAchievements.add(rs.getInt("achievementid"));
        }

        String newssql = "SELECT * FROM news where lmao = 0";
        ps = con.prepareStatement(newssql);
        rs = ps.executeQuery();
        while (rs.next()) {
            ret.news.add(rs.getString("news"));
        }

        if (ret.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null) {
            ret.maplemount = new MapleMount(ret, ret.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18).getItemId(), 1004);
            ret.maplemount.setExp(mountexp);
            ret.maplemount.setLevel(mountlevel);
            ret.maplemount.setTiredness(mounttiredness);
            ret.maplemount.setActive(false);
        } else {
            ret.maplemount = new MapleMount(ret, 0, 1004);
            ret.maplemount.setExp(mountexp);
            ret.maplemount.setLevel(mountlevel);
            ret.maplemount.setTiredness(mounttiredness);
            ret.maplemount.setActive(false);
        }
        /*
        ps = con.prepareStatement("SELECT jobid FROM JobChanges WHERE cid = ?");
        ps.setInt(1, charid);
        rs = ps.executeQuery();
        while (rs.next()) {
        ret.jobs.add(rs.getInt("jobid"));
        }
        java.util.Collections.sort(ret.jobs);
        rs.close();
        ps.close();
         */

        ret.recalcLocalStats();
        ret.silentEnforceMaxHpMp();
        return ret;

    }

    public static MapleCharacter getDefault(MapleClient client, int chrid) {
        MapleCharacter ret = getDefault(client);
        ret.id = chrid;
        return ret;
    }

    public static MapleCharacter getDefault(MapleClient client) {
        MapleCharacter ret = new MapleCharacter();
        ret.client = client;
        ret.hp = 50;
        ret.maxhp = 50;
        ret.mp = 5;
        ret.maxmp = 5;
        ret.map = null;
        ret.exp.set(0);
        ret.gmLevel = 0;
        ret.clan = -1;
        ret.job = MapleJob.BEGINNER;
        ret.meso.set(0);
        ret.level = 1;
        ret.reborns = 0;
        ret.realreborns = 0;
        ret.mkills = 0;
        ret.donatorlevel = 0;
        ret.bosscounter = 0;
        ret.family = 0;
        ret.pvpdeaths = 0;
        ret.pvpkills = 0;
        ret.bombpoints = 0;
        ret.accountid = client.getAccID();
        ret.buddylist = new BuddyList(20);
        ret.CP = 0;
        ret.totalCP = 0;
        ret.betachar = 1;
        ret.team = -1;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, ret.accountid);
            ResultSet rs = ps.executeQuery();
            rs = ps.executeQuery();
            if (rs.next()) {
                ret.getClient().setAccountName(rs.getString("name"));
                ret.getClient().setAccountPass(rs.getString("password"));
                ret.getClient().setGuest(rs.getInt("guest") > 0);
                ret.donatePoints = rs.getInt("donorPoints");
                ret.paypalnx = rs.getInt("paypalNX");
                ret.maplepoints = rs.getInt("mPoints");
                ret.cardnx = rs.getInt("cardNX");
                ret.lastLogin = rs.getLong("LastLoginInMilliseconds");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            log.error("ERROR", e);
        }

        ret.incs = false;
        ret.inmts = false;
        ret.APQScore = 0;
        ret.maplemount = null;
        ret.setDefaultKeyMap();
        ret.recalcLocalStats();
        return ret;
    }

    public void saveToDB(boolean update, boolean full) {
        Connection con = DatabaseConnection.getConnection();
        try {
            // clients should not be able to log back before their old state is saved (see MapleClient#getLoginState) so we are save to switch to a very low isolation level here
            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            // connections are thread local now, no need to synchronize anymore =)
            con.setAutoCommit(false);
            PreparedStatement ps;
            if (update) {
                ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, map = ?, meso = ?, hpApUsed = ?, mpApUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, messengerid = ?, messengerposition = ?, reborns = ?, realreborns, mkills = ?, family = ?, pvpkills = ?, pvpdeaths = ?, clan = ?, mountlevel = ?, mountexp = ?, mounttiredness = ?, married = ?, partnerid = ?, zakumlvl = ?, jumpPoints = ?, passionPoints = ?, rebirthPoints = ?, strike = ?, jailtime = ?, bosscounter = ?, relationship = ?, betachar = ?, donatorlevel = ?, marriagequest = ?, bosspoints = ? WHERE id = ?");
            } else {
                ps = con.prepareStatement("INSERT INTO characters (level, fame, str, dex, luk, `int`, exp, hp, mp, maxhp, maxmp, sp, ap, gm, skincolor, gender, job, hair, face, map, meso, hpApUsed, mpApUsed, spawnpoint, party, buddyCapacity, messengerid, messengerposition, reborns, realreborns, mkills, family, pvpkills, pvpdeaths, clan, mountlevel, mountexp, mounttiredness, married, partnerid, zakumlvl, jumpPoints, passionPoints, rebirthPoints, strike, jailtime, bosscounter, relationship, betachar, donatorlevel, marriagequest, bosspoints, accountid, name, world) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }
            ps.setInt(1, level);
            ps.setInt(2, fame);
            ps.setInt(3, str);
            ps.setInt(4, dex);
            ps.setInt(5, luk);
            ps.setInt(6, int_);
            ps.setInt(7, exp.get());
            ps.setInt(8, hp);
            ps.setInt(9, mp);
            ps.setInt(10, maxhp);
            ps.setInt(11, maxmp);
            ps.setInt(12, remainingSp);
            ps.setInt(13, remainingAp);
            ps.setInt(14, gmLevel);
            ps.setInt(15, skinColor.getId());
            ps.setInt(16, gender);
            ps.setInt(17, job.getId());
            ps.setInt(18, hair);
            ps.setInt(19, face);
            if (map == null) {
                ps.setInt(20, 220000003);
            } else {
                if (map.getForcedReturnId() != 999999999) {
                    ps.setInt(20, map.getForcedReturnId());
                } else {
                    ps.setInt(20, map.getId());
                }
            }
            ps.setInt(21, meso.get());
            ps.setInt(22, hpApUsed);
            ps.setInt(23, mpApUsed);
            if (map == null || map.getId() == 610020000 || map.getId() == 610020001) {
                ps.setInt(24, 220000003);
            } else {
                MaplePortal closest = map.findClosestSpawnpoint(getPosition());
                if (closest != null) {
                    ps.setInt(24, closest.getId());
                } else {
                    ps.setInt(24, 0);
                }
            }
            if (party != null) {
                ps.setInt(25, party.getId());
            } else {
                ps.setInt(25, -1);
            }
            ps.setInt(26, buddylist.getCapacity());
            if (messenger != null) {
                ps.setInt(27, messenger.getId());
                ps.setInt(28, messengerposition);
            } else {
                ps.setInt(27, 0);
                ps.setInt(28, 4);
            }
            ps.setInt(29, reborns);
            ps.setInt(30, realreborns);
            ps.setInt(31, mkills);
            ps.setInt(32, family);
            ps.setInt(33, pvpkills);
            ps.setInt(34, pvpdeaths);
            ps.setInt(35, clan);
            if (maplemount != null) {
                ps.setInt(37, maplemount.getLevel());
                ps.setInt(38, maplemount.getExp());
                ps.setInt(39, maplemount.getTiredness());
            } else {
                ps.setInt(36, 1);
                ps.setInt(37, 0);
                ps.setInt(38, 0);
            }

            ps.setInt(39, married ? 1 : 0);
            ps.setInt(40, partnerid);
            ps.setInt(41, zakumLvl > 2 ? 2 : zakumLvl);
            ps.setInt(42, jumpPoints);
            ps.setInt(43, passionPoints);
            ps.setInt(44, rebirthPoints);
            ps.setInt(45, strike);
            ps.setInt(46, jailtime);
            ps.setInt(47, bosscounter);
            ps.setInt(48, relationship);
            ps.setInt(49, betachar);
            ps.setInt(50, donatorlevel);
            ps.setInt(51, marriageQuestLevel);
            ps.setInt(52, bossPoints);
            //ps.setInt(45, bossRepeats);
            if (update) {
                ps.setInt(53, id);
            } else {
                ps.setInt(53, accountid);
                ps.setString(54, name);
                ps.setInt(55, world);
            }
            if (!full) {
                ps.executeUpdate();
                ps.close();
            } else {
                int updateRows = ps.executeUpdate();
                if (!update) {
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        this.id = rs.getInt(1);
                    } else {
                        throw new DatabaseException("Inserting char failed.");
                    }
                    rs.close();
                } else if (updateRows < 1) {
                    throw new DatabaseException("Character not in database (" + id + ")");
                }
                ps.close();

                for (int i = 0; i < 3; i++) {
                    if (pets[i] != null) {
                        pets[i].saveToDb();
                    } else {
                        break;
                    }
                }

                deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?");

                for (int i = 0; i < 5; i++) {
                    SkillMacro macro = skillMacros[i];
                    if (macro != null) {
                        ps = con.prepareStatement("INSERT INTO skillmacros (characterid, skill1, skill2, skill3, name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?)");

                        ps.setInt(1, id);
                        ps.setInt(2, macro.getSkill1());
                        ps.setInt(3, macro.getSkill2());
                        ps.setInt(4, macro.getSkill3());
                        ps.setString(5, macro.getName());
                        ps.setInt(6, macro.getShout());
                        ps.setInt(7, i);

                        ps.executeUpdate();
                        ps.close();
                    }
                }

                deleteWhereCharacterId(con, "DELETE FROM inventoryitems WHERE characterid = ?");

                ps = con.prepareStatement("INSERT INTO inventoryitems (characterid, itemid, inventorytype, position, quantity, owner, petid) VALUES (?, ?, ?, ?, ?, ?, ?)");
                PreparedStatement pse = con.prepareStatement("INSERT INTO inventoryequipment VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                for (MapleInventory iv : inventory) {
                    ps.setInt(3, iv.getType().getType());
                    for (IItem item : iv.list()) {
                        ps.setInt(1, id);
                        ps.setInt(2, item.getItemId());
                        ps.setInt(4, item.getPosition());
                        ps.setInt(5, item.getQuantity());
                        ps.setString(6, item.getOwner());
                        ps.setInt(7, item.getPetId());
                        ps.executeUpdate();
                        ResultSet rs = ps.getGeneratedKeys();
                        int itemid;
                        if (rs.next()) {
                            itemid = rs.getInt(1);
                            rs.close();
                        } else {
                            rs.close();
                            throw new DatabaseException("Inserting char failed.");
                        }

                        if (iv.getType().equals(MapleInventoryType.EQUIP) || iv.getType().equals(MapleInventoryType.EQUIPPED)) {
                            pse.setInt(1, itemid);
                            IEquip equip = (IEquip) item;
                            pse.setInt(2, equip.getUpgradeSlots());
                            pse.setInt(3, equip.getLevel());
                            pse.setInt(4, equip.getStr());
                            pse.setInt(5, equip.getDex());
                            pse.setInt(6, equip.getInt());
                            pse.setInt(7, equip.getLuk());
                            pse.setInt(8, equip.getHp());
                            pse.setInt(9, equip.getMp());
                            pse.setInt(10, equip.getWatk());
                            pse.setInt(11, equip.getMatk());
                            pse.setInt(12, equip.getWdef());
                            pse.setInt(13, equip.getMdef());
                            pse.setInt(14, equip.getAcc());
                            pse.setInt(15, equip.getAvoid());
                            pse.setInt(16, equip.getHands());
                            pse.setInt(17, equip.getSpeed());
                            pse.setInt(18, equip.getJump());
                            pse.setInt(19, equip.getRingId());
                            pse.setInt(20, equip.getLocked());
                            pse.executeUpdate();
                        }
                    }
                }
                ps.close();
                pse.close();

                deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`) VALUES (DEFAULT, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                pse = con.prepareStatement("INSERT INTO queststatusmobs VALUES (DEFAULT, ?, ?, ?)");
                ps.setInt(1, id);
                for (MapleQuestStatus q : quests.values()) {
                    ps.setInt(2, q.getQuest().getId());
                    ps.setInt(3, q.getStatus().getId());
                    ps.setInt(4, (int) (q.getCompletionTime() / 1000));
                    ps.setInt(5, q.getForfeited());
                    ps.executeUpdate();
                    ResultSet rs = ps.getGeneratedKeys();
                    rs.next();
                    for (int mob : q.getMobKills().keySet()) {
                        pse.setInt(1, rs.getInt(1));
                        pse.setInt(2, mob);
                        pse.setInt(3, q.getMobKills(mob));
                        pse.executeUpdate();
                    }
                    rs.close();
                }
                ps.close();
                pse.close();


                deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel) VALUES (?, ?, ?, ?)");
                ps.setInt(1, id);
                for (Entry<ISkill, SkillEntry> skill_ : skills.entrySet()) {
                    ps.setInt(2, skill_.getKey().getId());
                    ps.setInt(3, skill_.getValue().skillevel);
                    ps.setInt(4, skill_.getValue().masterlevel);
                    ps.executeUpdate();
                }
                ps.close();

                deleteWhereCharacterId(con, "DELETE FROM keymap WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
                ps.setInt(1, id);
                for (Entry<Integer, MapleKeyBinding> keybinding : keymap.entrySet()) {
                    ps.setInt(2, keybinding.getKey().intValue());
                    ps.setInt(3, keybinding.getValue().getType());
                    ps.setInt(4, keybinding.getValue().getAction());
                    ps.executeUpdate();
                }
                ps.close();

                deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO savedlocations (characterid, `locationtype`, `map`) VALUES (?, ?, ?)");
                ps.setInt(1, id);
                for (SavedLocationType savedLocationType : SavedLocationType.values()) {
                    if (savedLocations[savedLocationType.ordinal()] != -1) {
                        ps.setString(2, savedLocationType.name());
                        ps.setInt(3, savedLocations[savedLocationType.ordinal()]);
                        ps.executeUpdate();
                    }
                }
                ps.close();

                deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ? AND pending = 0");
                ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`) VALUES (?, ?, 0)");
                ps.setInt(1, id);
                for (BuddylistEntry entry : buddylist.getBuddies()) {
                    if (entry.isVisible()) {
                        ps.setInt(2, entry.getCharacterId());
                        ps.executeUpdate();
                    }
                }
                ps.close();

                ps = con.prepareStatement("UPDATE accounts SET `paypalNX` = ?, `mPoints` = ?, `cardNX` = ?, `donorPoints` = ?, `votepoints` = ? WHERE id = ?");
                ps.setInt(1, paypalnx);
                ps.setInt(2, maplepoints);
                ps.setInt(3, cardnx);
                ps.setInt(4, donatePoints);
                ps.setInt(5, votepoints);
                ps.setInt(6, client.getAccID());
                ps.executeUpdate();
                ps.close();

                if (storage != null) {
                    storage.saveToDB();
                }
                if (update) {
                    ps = con.prepareStatement("DELETE FROM achievements WHERE accountid = ?");
                    ps.setInt(1, accountid);
                    ps.executeUpdate();
                    ps.close();

                    for (Integer achid : finishedAchievements) {
                        ps = con.prepareStatement("INSERT INTO achievements(charid, achievementid, accountid) VALUES(?, ?, ?)");
                        ps.setInt(1, id);
                        ps.setInt(2, achid);
                        ps.setInt(3, accountid);
                        ps.executeUpdate();
                        ps.close();
                    }
                }
            }
            con.commit();
        } catch (Exception e) {
            log.error(MapleClient.getLogMessage(this, "[charsave] Error saving character data"), e);
            try {
                con.rollback();
            } catch (SQLException e1) {
                log.error(MapleClient.getLogMessage(this, "[charsave] Error Rolling Back"), e);
            }
        } finally {
            try {
                con.setAutoCommit(true);
                con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            } catch (SQLException e) {
                log.error(MapleClient.getLogMessage(this, "[charsave] Error going back to autocommit mode"), e);
            }
        }
    }

    private void deleteWhereCharacterId(Connection con, String sql) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public MapleQuestStatus getQuest(MapleQuest quest) {
        if (!quests.containsKey(quest)) {
            return new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
        }
        return quests.get(quest);
    }

    public void updateQuest(MapleQuestStatus quest) {
        quests.put(quest.getQuest(), quest);
        if (!(quest.getQuest() instanceof MapleCustomQuest)) {
            if (quest.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
                client.getSession().write(MaplePacketCreator.startQuest(this, (short) quest.getQuest().getId()));
                client.getSession().write(MaplePacketCreator.updateQuestInfo(this, (short) quest.getQuest().getId(), quest.getNpc(), (byte) 8));
            } else if (quest.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
                client.getSession().write(MaplePacketCreator.completeQuest(this, (short) quest.getQuest().getId()));
            } else if (quest.getStatus().equals(MapleQuestStatus.Status.NOT_STARTED)) {
                client.getSession().write(MaplePacketCreator.forfeitQuest(this, (short) quest.getQuest().getId()));
            }
        }
    }

    public static int getIdByName(String name, int world) {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        try {
            ps = con.prepareStatement("SELECT id FROM characters WHERE name = ? AND world = ?");
            ps.setString(1, name);
            ps.setInt(2, world);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            int id = rs.getInt("id");
            rs.close();
            ps.close();
            return id;
        } catch (SQLException e) {
            log.error("ERROR", e);
        }
        return -1;
    }

    public static String getNameById(int id, int world) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT name FROM characters WHERE id = ? AND world = ?");
            ps.setInt(1, id);
            ps.setInt(2, world);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }
            String name = rs.getString("name");
            rs.close();
            ps.close();
            return name;
        } catch (SQLException e) {
            log.error("ERROR", e);
        }
        return null;
    }

    public Integer getBuffedValue(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return Integer.valueOf(mbsvh.value);
    }

    public boolean isBuffFrom(MapleBuffStat stat, ISkill skill) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null) {
            return false;
        }
        return mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skill.getId();
    }

    public int getBuffSource(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null) {
            return -1;
        }
        return mbsvh.effect.getSourceId();
    }

    public int getItemQuantity(int itemid, boolean checkEquipped) {
        MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(itemid);
        MapleInventory iv = inventory[type.ordinal()];
        int possesed = iv.countById(itemid);
        if (checkEquipped) {
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        return possesed;
    }

    public void setBuffedValue(MapleBuffStat effect, int value) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return;
        }
        mbsvh.value = value;
    }

    public Long getBuffedStarttime(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return Long.valueOf(mbsvh.startTime);
    }

    public MapleStatEffect getStatForBuff(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.effect;
    }

    private void prepareDragonBlood(final MapleStatEffect bloodEffect) {
        if (dragonBloodSchedule != null) {
            dragonBloodSchedule.cancel(false);
        }
        dragonBloodSchedule = TimerManager.getInstance().register(new Runnable() {

            @Override
            public void run() {
                addHP(-bloodEffect.getX());
                getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(bloodEffect.getSourceId(), 5));
                getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), bloodEffect.getSourceId(), 5, (byte) 3), false);
            }
        }, 4000, 4000);
    }

    public void startFullnessSchedule(final int decrease, final MaplePet pet, int petSlot) {
        ScheduledFuture<?> schedule = TimerManager.getInstance().register(new Runnable() {

            @Override
            public void run() {
                if (pet != null) {
                    int newFullness = pet.getFullness() - decrease;
                    if (newFullness <= 5) {
                        pet.setFullness(15);
                        unequipPet(pet, true, true);
                    } else {
                        pet.setFullness(newFullness);
                        getClient().getSession().write(MaplePacketCreator.updatePet(pet, true));
                    }
                }
            }
        }, 60000, 60000);
        switch (petSlot) {
            case 0:
                fullnessSchedule = schedule;
                break;
            case 1:
                fullnessSchedule_1 = schedule;
                break;
            case 2:
                fullnessSchedule_2 = schedule;
                break;
            default:
                break;
        }
    }

    public void cancelFullnessSchedule(int petSlot) {
        switch (petSlot) {
            case 0:
                if (fullnessSchedule != null) {
                    fullnessSchedule.cancel(false);
                }
            case 1:
                if (fullnessSchedule_1 != null) {
                    fullnessSchedule_1.cancel(false);
                }
            case 2:
                if (fullnessSchedule_2 != null) {
                    fullnessSchedule_2.cancel(false);
                }
            default:
                break;
        }
    }

    public void startMapTimeLimitTask(final MapleMap from, final MapleMap to) {
        if (to.getTimeLimit() > 0 && from != null) {
            final MapleCharacter chr = this;
            mapTimeLimitTask = TimerManager.getInstance().register(new Runnable() {

                @Override
                public void run() {
                    MaplePortal pfrom = null;
                    if (from.isMiniDungeonMap()) {
                        pfrom = from.getPortal("MD00");
                    } else {
                        pfrom = from.getPortal(0);
                    }
                    if (pfrom != null) {
                        chr.changeMap(from, pfrom);
                    }
                }
            }, from.getTimeLimit() * 1000, from.getTimeLimit() * 1000);
        }
    }

    public void cancelMapTimeLimitTask() {
        if (mapTimeLimitTask != null) {
            mapTimeLimitTask.cancel(false);
        }
    }

    public void registerEffect(MapleStatEffect effect, long starttime, ScheduledFuture<?> schedule) {
        if (effect.isHide() && isGM()) {
            this.hidden = true;
            getMap().broadcastNONGMMessage(this, MaplePacketCreator.removePlayerFromMap(getId()), false);
            //this.setOffOnline(false);
        } else if (effect.isDragonBlood()) {
            prepareDragonBlood(effect);
        } else if (effect.isBerserk()) {
            checkBerserk();
        } else if (effect.isBeholder()) {
            prepareBeholderEffect();
        }
        for (Pair<MapleBuffStat, Integer> statup : effect.getStatups()) {
            effects.put(statup.getLeft(), new MapleBuffStatValueHolder(effect, starttime, schedule, statup.getRight().intValue()));
        }
        recalcLocalStats();
    }

    private List<MapleBuffStat> getBuffStats(MapleStatEffect effect, long startTime) {
        List<MapleBuffStat> stats = new ArrayList<MapleBuffStat>();
        for (Entry<MapleBuffStat, MapleBuffStatValueHolder> stateffect : effects.entrySet()) {
            MapleBuffStatValueHolder mbsvh = stateffect.getValue();
            if (mbsvh.effect.sameSource(effect) && (startTime == -1 || startTime == mbsvh.startTime)) {
                try {
                    stats.add(stateffect.getKey());
                } catch (Exception e) {
                    // do nothing?
                }
            }
        }
        return stats;
    }

    private void deregisterBuffStats(List<MapleBuffStat> stats) {
        List<MapleBuffStatValueHolder> effectsToCancel = new ArrayList<MapleBuffStatValueHolder>(stats.size());
        for (MapleBuffStat stat : stats) {
            MapleBuffStatValueHolder mbsvh = effects.get(stat);
            if (mbsvh != null) {
                effects.remove(stat);
                boolean addMbsvh = true;
                for (MapleBuffStatValueHolder contained : effectsToCancel) {
                    if (mbsvh.startTime == contained.startTime && contained.effect == mbsvh.effect) {
                        addMbsvh = false;
                    }
                }
                if (addMbsvh) {
                    effectsToCancel.add(mbsvh);
                }
                if (stat == MapleBuffStat.SUMMON || stat == MapleBuffStat.PUPPET) {
                    int summonId = mbsvh.effect.getSourceId();
                    MapleSummon summon = summons.get(summonId);
                    if (summon != null) {
                        getMap().broadcastMessage(MaplePacketCreator.removeSpecialMapObject(summon, true));
                        getMap().removeMapObject(summon);
                        removeVisibleMapObject(summon);
                        summons.remove(summonId);
                    }
                    if (summon.getSkill() == 1321007) {
                        if (beholderHealingSchedule != null) {
                            beholderHealingSchedule.cancel(false);
                            beholderHealingSchedule = null;
                        }
                        if (beholderBuffSchedule != null) {
                            beholderBuffSchedule.cancel(false);
                            beholderBuffSchedule = null;
                        }
                    }
                } else if (stat == MapleBuffStat.DRAGONBLOOD) {
                    dragonBloodSchedule.cancel(false);
                    dragonBloodSchedule = null;
                }
            }
        }
        for (MapleBuffStatValueHolder cancelEffectCancelTasks : effectsToCancel) {
            if (getBuffStats(cancelEffectCancelTasks.effect, cancelEffectCancelTasks.startTime).size() == 0) {
                cancelEffectCancelTasks.schedule.cancel(false);
            }
        }
    }

    /**
     * @param effect
     * @param overwrite when overwrite is set no data is sent and all the Buffstats in the StatEffect are deregistered
     * @param startTime
     */
    public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime) {
        List<MapleBuffStat> buffstats;
        if (!overwrite) {
            buffstats = getBuffStats(effect, startTime);
        } else {
            List<Pair<MapleBuffStat, Integer>> statups = effect.getStatups();
            buffstats = new ArrayList<MapleBuffStat>(statups.size());
            for (Pair<MapleBuffStat, Integer> statup : statups) {
                buffstats.add(statup.getLeft());
            }
        }
        deregisterBuffStats(buffstats);
        if (effect.isMagicDoor()) {
            // remove for all on maps
            if (!getDoors().isEmpty()) {
                MapleDoor door = getDoors().iterator().next();
                for (MapleCharacter chr : door.getTarget().getCharacters()) {
                    door.sendDestroyData(chr.getClient());
                }
                for (MapleCharacter chr : door.getTown().getCharacters()) {
                    door.sendDestroyData(chr.getClient());
                }
                for (MapleDoor destroyDoor : getDoors()) {
                    door.getTarget().removeMapObject(destroyDoor);
                    door.getTown().removeMapObject(destroyDoor);
                }
                clearDoors();
                silentPartyUpdate();
            }
        }

        if (effect.isMonsterRiding()) {
            if (effect.getSourceId() != 5221006) {
                this.getMount().cancelSchedule();
                this.getMount().setActive(false);
            }
        }

        // check if we are still logged in o.o
        if (!overwrite) {
            cancelPlayerBuffs(buffstats);
            if (effect.isHide() && (MapleCharacter) getMap().getMapObject(getObjectId()) != null) {
                this.hidden = false;
                getMap().broadcastNONGMMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
                setOffOnline(true);
                for (int i = 0; i < 3; i++) {
                    if (pets[i] != null) {
                        getMap().broadcastNONGMMessage(this, MaplePacketCreator.showPet(this, pets[i], false, false), false);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    public void cancelBuffStats(MapleBuffStat stat) {
        List<MapleBuffStat> buffStatList = Arrays.asList(stat);
        deregisterBuffStats(buffStatList);
        cancelPlayerBuffs(buffStatList);
    }

    public void cancelEffectFromBuffStat(MapleBuffStat stat) {
        cancelEffect(effects.get(stat).effect, false, -1);
    }

    private void cancelPlayerBuffs(List<MapleBuffStat> buffstats) {
        if (getClient().getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
            recalcLocalStats();
            enforceMaxHpMp();
            getClient().getSession().write(MaplePacketCreator.cancelBuff(buffstats));
            getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignBuff(getId(), buffstats), false);
        }
    }

    public void dispel() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isSkill()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void cancelAllBuffs() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            cancelEffect(mbsvh.effect, false, mbsvh.startTime);
        }
    }

    public void cancelMorphs() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isMorph() && mbsvh.effect.getSourceId() != 5111005 && mbsvh.effect.getSourceId() != 5121003) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs) {
        for (PlayerBuffValueHolder mbsvh : buffs) {
            mbsvh.effect.silentApplyBuff(this, mbsvh.startTime);
        }
    }

    public List<PlayerBuffValueHolder> getAllBuffs() {
        List<PlayerBuffValueHolder> ret = new ArrayList<PlayerBuffValueHolder>();
        for (MapleBuffStatValueHolder mbsvh : effects.values()) {
            ret.add(new PlayerBuffValueHolder(mbsvh.startTime, mbsvh.effect));
        }
        return ret;
    }

    public void cancelMagicDoor() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isMagicDoor()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void handleOrbgain() {
        MapleStatEffect ceffect = null;
        int advComboSkillLevel = getSkillLevel(SkillFactory.getSkill(1120003));
        if (advComboSkillLevel > 0) {
            ceffect = SkillFactory.getSkill(1120003).getEffect(advComboSkillLevel);
        } else {
            ceffect = SkillFactory.getSkill(1111002).getEffect(getSkillLevel(SkillFactory.getSkill(1111002)));
        }
        if (getBuffedValue(MapleBuffStat.COMBO) < ceffect.getX() + 1) {
            int neworbcount = getBuffedValue(MapleBuffStat.COMBO) + 1;
            if (advComboSkillLevel > 0 && ceffect.makeChanceResult()) {
                if (neworbcount < ceffect.getX() + 1) {
                    neworbcount++;
                }
            }
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, neworbcount));
            setBuffedValue(MapleBuffStat.COMBO, neworbcount);
            int duration = ceffect.getDuration();
            duration += (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis()));
            getClient().getSession().write(MaplePacketCreator.giveBuff(1111002, duration, stat));
            getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat, ceffect), false);
        }
    }

    public void handleOrbconsume() {
        ISkill combo = SkillFactory.getSkill(1111002);
        MapleStatEffect ceffect = combo.getEffect(getSkillLevel(combo));
        List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, 1));
        setBuffedValue(MapleBuffStat.COMBO, 1);
        int duration = ceffect.getDuration();
        duration += (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis()));
        getClient().getSession().write(MaplePacketCreator.giveBuff(1111002, duration, stat));
        getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat, ceffect), false);
    }

    private void silentEnforceMaxHpMp() {
        setMp(getMp());
        setHp(getHp(), true);
    }

    private void enforceMaxHpMp() {
        List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>(2);
        if (getMp() > getCurrentMaxMp()) {
            setMp(getMp());
            stats.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(getMp())));
        }
        if (getHp() > getCurrentMaxHp()) {
            setHp(getHp());
            stats.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(getHp())));
        }
        if (stats.size() > 0) {
            getClient().getSession().write(MaplePacketCreator.updatePlayerStats(stats));
        }
    }

    public MapleMap getMap() {
        return map;
    }

    public void setMap(MapleMap newmap) {
        this.map = newmap;
    }

    public int getMapId() {
        if (map != null) {
            return map.getId();
        }
        return mapid;
    }

    public int getInitialSpawnpoint() {
        return initialSpawnPoint;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getRank() {
        return rank;
    }

    public int getRankMove() {
        return rankMove;
    }

    public int getJobRank() {
        return jobRank;
    }

    public int getJobRankMove() {
        return jobRankMove;
    }

    public int getAPQScore() {
        return APQScore;
    }

    public int getFame() {
        return fame;
    }

    public int getCP() {
        return this.CP;
    }

    public int getTeam() {
        return this.team;
    }

    public int getTotalCP() {
        return this.totalCP;
    }

    public void setCP(int cp) {
        this.CP = cp;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public void setTotalCP(int totalcp) {
        this.totalCP = totalcp;
    }

    public int getStr() {
        return str;
    }

    public int getDex() {
        return dex;
    }

    public int getLuk() {
        return luk;
    }

    public int getInt() {
        return int_;
    }

    public MapleClient getClient() {
        return client;
    }

    public int getExp() {
        return exp.get();
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxhp;
    }

    public int getMp() {
        return mp;
    }

    public int getMaxMp() {
        return maxmp;
    }

    public int getRemainingAp() {
        return remainingAp;
    }

    public int getRemainingSp() {
        return remainingSp;
    }

    public int getMpApUsed() {
        return mpApUsed;
    }

    public void setMpApUsed(int mpApUsed) {
        this.mpApUsed = mpApUsed;
    }

    public int getHpApUsed() {
        return hpApUsed;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHpApUsed(int hpApUsed) {
        this.hpApUsed = hpApUsed;
    }

    public MapleSkinColor getSkinColor() {
        return skinColor;
    }

    public MapleJob getJob() {
        return job;
    }

    public int getGender() {
        return gender;
    }

    public int getHair() {
        return hair;
    }

    public int getFace() {
        return face;
    }

    public void setName(String name, boolean changeName) {
        if (!changeName) {
            this.name = name;
        } else {
            Connection con = DatabaseConnection.getConnection();
            try {
                con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                con.setAutoCommit(false);
                PreparedStatement sn = con.prepareStatement("UPDATE characters SET name = ? WHERE id = ?");
                sn.setString(1, name);
                sn.setInt(2, id);
                sn.execute();
                con.commit();
                sn.close();
                this.name = name;
            } catch (SQLException se) {
                log.error("SQL error: " + se.getLocalizedMessage(), se);
            }
        }
    }

    public void setStr(int str) {
        this.str = str;
        recalcLocalStats();
    }

    public void setDex(int dex) {
        this.dex = dex;
        recalcLocalStats();
    }

    public void setLuk(int luk) {
        this.luk = luk;
        recalcLocalStats();
    }

    public void setInt(int int_) {
        this.int_ = int_;
        recalcLocalStats();
    }

    public void setMaxHp(int hp) {
        this.maxhp = hp;
        recalcLocalStats();
    }

    public void setMaxMp(int mp) {
        this.maxmp = mp;
        recalcLocalStats();
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    public void setAPQScore(int score) {
        this.APQScore = score;
    }

    public void setRemainingAp(int remainingAp) {
        this.remainingAp = remainingAp;
    }

    public void setRemainingSp(int remainingSp) {
        this.remainingSp = remainingSp;
    }

    public void setSkinColor(MapleSkinColor skinColor) {
        this.skinColor = skinColor;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setGM(int gmlevel) {
        this.gmLevel = gmlevel;
    }

    public CheatTracker getCheatTracker() {
        return anticheat;
    }

    public BuddyList getBuddylist() {
        return buddylist;
    }

    public void addFame(int famechange) {
        this.fame += famechange;
    }

    public void changeMap(int map) {
        changeMap(map, 0);

    }

    public void changeMap(int map, int portal) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);
        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, String portal) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);
        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, MaplePortal portal) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);
        changeMap(warpMap, portal);
    }

    public void changeMap(final MapleMap to, final Point pos) {
        MaplePacket warpPacket = MaplePacketCreator.getWarpToMap(to, 0x80, this);
        changeMapInternal(to, pos, warpPacket);
    }

    public void changeMap(final MapleMap to, final MaplePortal pto) {
        if (to.getId() == 100000200 || to.getId() == 211000100 || to.getId() == 220000300) {
            MaplePacket warpPacket = MaplePacketCreator.getWarpToMap(to, pto.getId() - 2, this);
            changeMapInternal(to, pto.getPosition(), warpPacket);
        } else {
            MaplePacket warpPacket = MaplePacketCreator.getWarpToMap(to, pto.getId(), this);
            changeMapInternal(to, pto.getPosition(), warpPacket);
        }
    }

    private void changeMapInternal(final MapleMap to, final Point pos, MaplePacket warpPacket) {
        if (getCheatTracker().Spam(2000, 5)) {
            client.getSession().write(MaplePacketCreator.enableActions());
        } else {
            if (to.isClosed()) {
                dropMessage(5, "This portal is closed right now.");
                return;
            }

            if (to.getId() == 912000000 && !isDonator()) {
                dropMessage("Non-Donators cannot go into this map. You will not be able to find a way around this, trust me.");
                return;
            }

            warpPacket.setOnSend(new Runnable() {

                @Override
                public void run() {
                    IPlayerInteractionManager interaction = MapleCharacter.this.getInteraction();
                    if (interaction != null) {
                        if (interaction.isOwner(MapleCharacter.this)) {
                            if (interaction.getShopType() == 2) {
                                interaction.removeAllVisitors(3, 1);
                                interaction.closeShop(true);
                                dropMessage(1, "Please check Fredrick to recieve your items.");
                            } else if (interaction.getShopType() == 1) {
                                getClient().getSession().write(MaplePacketCreator.shopVisitorLeave(0));
                                if (interaction.getItems().size() == 0) {
                                    interaction.removeAllVisitors(3, 1);
                                    interaction.closeShop(true); // that saves the items - > fredrick, not returning
                                    dropMessage(1, "lols check fredirkc nao");
                                }
                            } else if (interaction.getShopType() == 3 || interaction.getShopType() == 4) {
                                interaction.removeAllVisitors(3, 1);
                            }
                        } else {
                            interaction.removeVisitor(MapleCharacter.this);
                        }
                    }
                    MapleCharacter.this.setInteraction(null);
                    map.removePlayer(MapleCharacter.this);
                    if (getClient().getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
                        map = to;
                        setPosition(pos);
                        to.addPlayer(MapleCharacter.this);
                        if (to.isCPQMap()) {
                            if (getParty() != null) {
                                if (getTeam() != 0 && getTeam() != 1) {
                                    setTeam(0);
                                    getClient().getSession().write(
                                            MaplePacketCreator.serverNotice(5, "You have been assigned to Maple Red team."));
                                }
                            } else {
                                getClient().getSession().write(
                                        MaplePacketCreator.serverNotice(5, "You are not in a party."));
                            }
                            getClient().getSession().write(MaplePacketCreator.startMonsterCarnival(getTeam()));
                        }
                        if (party != null) {
                            silentPartyUpdate();
                            getClient().getSession().write(MaplePacketCreator.updateParty(getClient().getChannel(), party, PartyOperation.SILENT_UPDATE, null));
                            updatePartyMemberHP();
                        }
                        if (getMap().getHPDec() > 0 && !inCS() && isAlive()) {
                            hpDecreaseTask = TimerManager.getInstance().schedule(new Runnable() {

                                @Override
                                public void run() {
                                    doHurtHp();
                                }
                            }, 10000);
                        }

                        if (to.getId() == 980000301) { //todo: all cpq map id's
                            setTeam(MapleCharacter.rand(0, 1));
                            getClient().getSession().write(MaplePacketCreator.startMonsterCarnival(getTeam()));
                        }

                        if (to.getId() == 109060000) {
                            int teamz = (pos.y > -80 ? 0 : 1);
                            if (to.getSnowBall(teamz) == null) {
                                MapleSnowball ball = new MapleSnowball(teamz);
                                to.setSnowBall(ball);
                            }
                        }

                    }
                }
            });
            if (hasFakeChar()) {
                for (FakeCharacter ch : getFakeChars()) {
                    if (ch.follow()) {
                        howmanyfakechars = getFakeChars().size();
                        ch.getFakeChar().getMap().removePlayer(ch.getFakeChar());
                    }
                }
            }
            getClient().getSession().write(warpPacket);
            if (hasFakeChar()) {
                reloadFakeChars();
            }
        }
    }

    public void leaveMap() {
        controlled.clear();
        visibleMapObjects.clear();
        if (chair != 0) {
            chair = 0;
        }
        if (hpDecreaseTask != null) {
            hpDecreaseTask.cancel(false);
        }
    }

    public void doHurtHp() {
        if (this.getInventory(MapleInventoryType.EQUIPPED).findById(getMap().getHPDecProtect()) != null) {
            return;
        }
        addHP(-getMap().getHPDec());
        hpDecreaseTask = TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                doHurtHp();
            }
        }, 10000);
    }

    public void changeJob(MapleJob newJob) {
        /* if (!isGM() && !jobs.contains(newJob.getId()) && newJob.getId() % 100 == 2) {
        jobs.add(newJob.getId());
        Connection con = DatabaseConnection.getConnection();
        try {
        PreparedStatement ps = con.prepareStatement("INSERT INTO JobChanges (cid, jobid) VALUES (?, ?)");
        ps.setInt(1, getId());
        ps.setInt(2, newJob.getId());
        ps.executeUpdate();
        ps.close();
        } catch (SQLException se) {
        se.printStackTrace();
        }
        }
         */
        this.job = newJob;
        this.remainingSp++;
        if (newJob.getId() % 10 == 2) {
            this.remainingSp += 2;
        }
        updateSingleStat(MapleStat.AVAILABLESP, this.remainingSp);
        updateSingleStat(MapleStat.JOB, newJob.getId());
        if (job.getId() == 100) {
            maxhp += rand(200, 250);
        } else if (job.getId() == 200) {
            maxmp += rand(100, 150);
        } else if (job.getId() % 100 == 0) {
            maxhp += rand(100, 150);
            maxhp += rand(25, 50);
        } else if (job.getId() > 0 && job.getId() < 200) {
            maxhp += rand(300, 350);
        } else if (job.getId() < 300) {
            maxmp += rand(450, 500);
        } else if (job.getId() > 0) {
            maxhp += rand(300, 350);
            maxmp += rand(150, 200);
        }
        if (maxhp >= 30000) {
            maxhp = 30000;
        }
        if (maxmp >= 30000) {
            maxmp = 30000;
        }
        try {
            setHp(maxhp);
            setMp(maxmp);
            List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(2);
            statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, Integer.valueOf(maxhp)));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, Integer.valueOf(maxmp)));
            recalcLocalStats();
            getClient().getSession().write(MaplePacketCreator.updatePlayerStats(statup));
            silentPartyUpdate();
            guildUpdate();
            getMap().broadcastMessage(this, MaplePacketCreator.showJobChange(getId()), false);
        } catch (Exception e) {
            // Just incase the family effect for changing jobs calls a NPE.
        }
    }

    public void gainAp(int ap) {
        this.remainingAp += ap;
        updateSingleStat(MapleStat.AVAILABLEAP, this.remainingAp);
    }

    public void changeSkillLevel(ISkill skill, int newLevel, int newMasterlevel) {
        skills.put(skill, new SkillEntry(newLevel, newMasterlevel));
        this.getClient().getSession().write(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel));
    }

    public void setHp(int newhp) {
        setHp(newhp, false);
    }

    public void setHp(int newhp, boolean silent) {
        if (invincibility) {
            return;
        }
        int oldHp = hp;
        if (newhp < 0) {
            newhp = 0;

        } else if (newhp > localmaxhp) {
            newhp = localmaxhp;
        }
        this.hp = newhp;

        if (!silent) {
            updatePartyMemberHP();
        }
        if (oldHp > hp && !isAlive()) {
            if (invincibility) {
                return;
            }
            playerDead();
        }
        this.checkBerserk();
    }

    private void playerDead() {
        if (invincibility) {
            return;
        }
        if (getEventInstance() != null) {
            getEventInstance().playerKilled(this);
        }

        cancelAllBuffs();
        cancelAllDebuffs();

        int[] charmID = {5130000, 4031283};
        MapleCharacter player = getClient().getPlayer();
        int possesed = 0;
        int i;
        for (i = 0; i < charmID.length; i++) {
            int quantity = getItemQuantity(charmID[i], false);
            if (possesed == 0 && quantity > 0) {
                possesed = quantity;
                break;
            }
        }
        if (possesed > 0) {
            possesed -= 1;
            getClient().getSession().write(MaplePacketCreator.serverNotice(5, "You have used the safety charm once, so your EXP points have not been decreased. (" + possesed + "time(s) left)"));
            MapleInventoryManipulator.removeById(getClient(), MapleItemInformationProvider.getInstance().getInventoryType(charmID[i]), charmID[i], 1, true, false);
        } else {
            if (player.getJob() != MapleJob.BEGINNER) {
                //Lose XP
                int XPdummy = ExpTable.getExpNeededForLevel(player.getLevel() + 1);
                if (player.getMap().isTown()) {
                    XPdummy *= 0.01;
                }

                if (XPdummy == ExpTable.getExpNeededForLevel(player.getLevel() + 1)) {
                    if (player.getLuk() <= 100 && player.getLuk() > 8) {
                        XPdummy *= 0.10 - (player.getLuk() * 0.0005);
                    } else if (player.getLuk() < 8) {
                        XPdummy *= 0.10;
                    } else {
                        XPdummy *= 0.10 - (100 * 0.0005);
                    }
                }
                if ((player.getExp() - XPdummy) > 0) {
                    player.gainExp(-XPdummy, false, false);
                } else {
                    player.gainExp(-player.getExp(), false, false);
                }
            }
        }

        getClient().getSession().write(MaplePacketCreator.enableActions());
    }

    public void updatePartyMemberHP() {
        if (party != null) {
            int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar.getMapid() == getMapId() && partychar.getChannel() == channel) {
                    MapleCharacter other = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        other.getClient().getSession().write(MaplePacketCreator.updatePartyMemberHP(getId(), this.hp, localmaxhp));
                    }
                }
            }
        }
    }

    public void receivePartyMemberHP() {
        if (party != null) {
            int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar.getMapid() == getMapId() && partychar.getChannel() == channel) {
                    MapleCharacter other = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        getClient().getSession().write(
                                MaplePacketCreator.updatePartyMemberHP(other.getId(), other.getHp(), other.getCurrentMaxHp()));
                    }
                }
            }
        }
    }

    public void setMp(int newmp) {
        if (invincibility) {
            return;
        }
        if (newmp < 0) {
            newmp = 0;
        } else if (newmp > localmaxmp) {
            newmp = localmaxmp;
        }
        this.mp = newmp;
    }

    /**
     * Convenience function which adds the supplied parameter to the current hp then directly does a updateSingleStat.
     *
     * @see MapleCharacter#setHp(int)
     * @param delta
     */
    public void addHP(int delta) {
        setHp(hp + delta);
        updateSingleStat(MapleStat.HP, hp);
    }

    /**
     * Convenience function which adds the supplied parameter to the current mp then directly does a updateSingleStat.
     *
     * @see MapleCharacter#setMp(int)
     * @param delta
     */
    public void addMP(int delta) {
        setMp(mp + delta);
        updateSingleStat(MapleStat.MP, mp);
    }

    public void addMPHP(int hpDiff, int mpDiff) {
        setHp(hp + hpDiff);
        setMp(mp + mpDiff);
        List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
        stats.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(hp)));
        stats.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(mp)));
        MaplePacket updatePacket = MaplePacketCreator.updatePlayerStats(stats);
        client.getSession().write(updatePacket);
    }

    /**
     * Updates a single stat of this MapleCharacter for the client. This method only creates and sends an update packet,
     * it does not update the stat stored in this MapleCharacter instance.
     *
     * @param stat
     * @param newval
     * @param itemReaction
     */
    public void updateSingleStat(MapleStat stat, int newval, boolean itemReaction) {
        Pair<MapleStat, Integer> statpair = new Pair<MapleStat, Integer>(stat, Integer.valueOf(newval));
        MaplePacket updatePacket = MaplePacketCreator.updatePlayerStats(Collections.singletonList(statpair), itemReaction);
        client.getSession().write(updatePacket);
    }

    public void updateSingleStat(MapleStat stat, int newval) {
        updateSingleStat(stat, newval, false);
    }

    public void gainExp(int gain, boolean show, boolean inChat) {
        gainExp(gain, show, inChat, true, true);
    }

    public void gainExp(int gain, boolean show, boolean inChat, boolean white) {
        gainExp(gain, show, inChat, white, true);
    }

    public void gainExp(int gain, boolean show, boolean inChat, boolean white, boolean etcLose) {
        if (!etcLose && gain < 0) {
            gain += Integer.MAX_VALUE;
            int levelCap = getClient().getChannelServer().getLevelCap();
            if (getLevel() < levelCap) {
                levelUp();
            }
            while (gain > 0) {
                gain -= (ExpTable.getExpNeededForLevel(level) - this.exp.get());
                if (getLevel() < levelCap) {
                    levelUp();
                }
            }
            setExp(0);
            updateSingleStat(MapleStat.EXP, exp.get());
            return;
        }
        if (getDonatorLevel() == 3) {
            if (donator3 == false);
            gain *= 2;
            donator3 = true;
        }

        if (getDonatorLevel() == 4) {
            if (donator4 == false); // if (donator3 == true && donator4 == false); <-- will that work if the person gets straight to level 4 donator?
            gain *= 3;
            donator4 = true;
        }

        if (getDonatorLevel() == 5) { // if (donator3 == true && donator4 == true && donator5 == false) <-- will that work? O_O same as above...?
            if (donator5 == false);
            gain *= 4;
            donator5 = true;
        }

        if (getLevel() == 199) {
            levelUp();
        }

        if (ChannelServer.getInstance(getClient().getChannel()).president.equalsIgnoreCase(getName())) { // There might be a better way to do this?
            gain *= 3;
            if (warned3 == false) {
                dropMessage("You are gaining 3X more EXP because you are the President of Channel " + getClient().getChannel() + "!");
                warned3 = true;
            }
        }
        if (getLevel() < 200) {
            /*  if ((long) this.exp.get() + (long) gain > (long) Integer.MAX_VALUE) {
            int gainFirst = ExpTable.getExpNeededForLevel(level) - this.exp.get();
            gain -= gainFirst; // + 1
            this.gainExp(gainFirst, false, inChat, white);
            }
            updateSingleStat(MapleStat.EXP, this.exp.addAndGet(gain)); */
            if (this.exp.get() + gain > Integer.MAX_VALUE) {
                int gainFirst = ExpTable.getExpNeededForLevel(level + 1) - this.exp.get();
                gain -= gainFirst + 1;
                this.gainExp(gainFirst + 1, false, inChat, white);
            }
            int newexp = this.exp.addAndGet(gain);
            updateSingleStat(MapleStat.EXP, newexp);
        } else {
            return;
        }
        if (show && gain != 0) {
            client.getSession().write(MaplePacketCreator.getShowExpGain(gain, inChat, white));
        }
        if (exp.get() >= ExpTable.getExpNeededForLevel(level) && level < getClient().getChannelServer().getLevelCap()) {
            if (getClient().getChannelServer().getMultiLevel()) {
                while (level < getClient().getChannelServer().getLevelCap() && exp.get() >= ExpTable.getExpNeededForLevel(level)) {
                    levelUp();
                }
            } else {
                levelUp();
                int need = ExpTable.getExpNeededForLevel(level);
                if (exp.get() >= need) {
                    setExp(need - 1);
                    updateSingleStat(MapleStat.EXP, exp.get());
                }
            }
        }
    }

    public void silentPartyUpdate() {
        if (party != null) {
            try {
                getClient().getChannelServer().getWorldInterface().updateParty(party.getId(), PartyOperation.SILENT_UPDATE, new MaplePartyCharacter(MapleCharacter.this));
            } catch (RemoteException e) {
                log.error("REMOTE THROW", e);
                getClient().getChannelServer().reconnectWorld();
            }
        }
    }

    public boolean isPvPMap() {
        return getMapId() == 110000000;
    }

    public boolean isGM() {
        return gmLevel >= 3;
    }

    public boolean isOwner() {
        return gmLevel >= 5;
    }

    public int getGMLevel() {
        return gmLevel;
    }

    public boolean hasGmLevel(int level) {
        return gmLevel >= level;
    }

    public void setPvpSetting(boolean lol) {
        pvp = lol;
    }

    public void setPvpPlayerSettingOn() {
        pvp = true;
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                setPvpSetting(false);
                dropMessage("Your time has run out, PVP Setting is set to off.");
            }
        }, 600000);
    }

    public MapleInventory getInventory(MapleInventoryType type) {
        return inventory[type.ordinal()];
    }

    public MapleShop getShop() {
        return shop;
    }

    public void setShop(MapleShop shop) {
        this.shop = shop;
    }

    public int getMeso() {
        return meso.get();
    }

    public int getSavedLocation(SavedLocationType type) {
        return savedLocations[type.ordinal()];
    }

    public void saveLocation(SavedLocationType type) {
        savedLocations[type.ordinal()] = getMapId();
    }

    public void clearSavedLocation(SavedLocationType type) {
        savedLocations[type.ordinal()] = -1;
    }

    public void setMeso(int set) {
        meso.set(set);
        updateSingleStat(MapleStat.MESO, set, false);
    }

    public void gainMeso(int gain) {
        gainMeso(gain, true, false, false);
    }

    public void gainMeso(int gain, boolean show) {
        gainMeso(gain, show, false, false);
    }

    public void gainMeso(int gain, boolean show, boolean enableActions) {
        gainMeso(gain, show, enableActions, false);
    }

    public void gainMeso(int gain, boolean show, boolean enableActions, boolean inChat) {
        int newVal;
        long total = ((long) meso.get() + (long) gain);
        if (total >= Integer.MAX_VALUE) {
            meso.set(Integer.MAX_VALUE);
            newVal = Integer.MAX_VALUE;
        } else if (total < 0) {
            meso.set(0);
            newVal = 0;
        } else {
            newVal = meso.addAndGet(gain);
        }
        updateSingleStat(MapleStat.MESO, newVal, enableActions);
        if (newVal >= 2147483646) {
            this.finishAchievement(8);
        }
        if (show) {
            client.getSession().write(MaplePacketCreator.getShowMesoGain(gain, inChat));
        }
    }

    /**
     * Adds this monster to the controlled list. The monster must exist on the Map.
     *
     * @param monster
     */
    public void controlMonster(MapleMonster monster, boolean aggro) {
        monster.setController(this);
        controlled.add(monster);
        client.getSession().write(MaplePacketCreator.controlMonster(monster, false, aggro));
    }

    public void stopControllingMonster(MapleMonster monster) {
        controlled.remove(monster);
    }

    public void checkMonsterAggro(MapleMonster monster) {
        if (!monster.isControllerHasAggro()) {
            if (monster.getController() == this) {
                monster.setControllerHasAggro(true);
            } else {
                monster.switchController(this, true);
            }
        }
    }

    public Collection<MapleMonster> getControlledMonsters() {
        return Collections.unmodifiableCollection(controlled);
    }

    public int getNumControlledMonsters() {
        return controlled.size();
    }

    @Override
    public String toString() {
        return "Character: " + this.name;
    }

    public int getAccountID() {
        return accountid;
    }

    public void mobKilled(int id) {
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus() == MapleQuestStatus.Status.COMPLETED || q.getQuest().canComplete(this, null)) {
                continue;
            }
            if (q.mobKilled(id) && !(q.getQuest() instanceof MapleCustomQuest)) {
                client.getSession().write(MaplePacketCreator.updateQuestMobKills(q));
                if (q.getQuest().canComplete(this, null)) {
                    client.getSession().write(MaplePacketCreator.getShowQuestCompletion(q.getQuest().getId()));
                }
            }
        }
    }

    public final List<MapleQuestStatus> getStartedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.STARTED) && !(q.getQuest() instanceof MapleCustomQuest)) {
                ret.add(q);
            }
        }
        return Collections.unmodifiableList(ret);
    }

    public final List<MapleQuestStatus> getCompletedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.COMPLETED) && !(q.getQuest() instanceof MapleCustomQuest)) {
                ret.add(q);
            }
        }
        return Collections.unmodifiableList(ret);
    }

    public IPlayerInteractionManager getInteraction() {
        return interaction;
    }

    public void setInteraction(IPlayerInteractionManager box) {
        interaction = box;
    }

    public Map<ISkill, SkillEntry> getSkills() {
        return Collections.unmodifiableMap(skills);
    }

    public void dispelSkill(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (skillid == 0) {
                if (mbsvh.effect.isSkill()) {
                    switch (mbsvh.effect.getSourceId()) {
                        case 1004:
                        case 1321007:
                        case 2121005:
                        case 2221005:
                        case 2311006:
                        case 2321003:
                        case 3111002:
                        case 3111005:
                        case 3211002:
                        case 3211005:
                        case 4111002:
                            cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                    }
                }
            } else {
                if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            }
        }
    }

    public boolean isActiveBuffedValue(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                return true;
            }
        }
        return false;
    }

    public int getSkillLevel(ISkill skill) {
        SkillEntry ret = skills.get(skill);
        if (ret == null) {
            return 0;
        }
        return ret.skillevel;
    }

    public int getMasterLevel(ISkill skill) {
        SkillEntry ret = skills.get(skill);
        if (ret == null) {
            return 0;
        }
        return ret.masterlevel;
    }

    public int getTotalDex() {
        return localdex;
    }

    public int getTotalInt() {
        return localint;
    }

    public int getTotalStr() {
        return localstr;
    }

    public int getTotalLuk() {
        return localluk;
    }

    public int getTotalMagic() {
        return magic;
    }

    public void closeArena1(int amount) {
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                if (getMapId() == 101040002 || getMapId() == 101040003 || getMapId() == 102020000) {
                    changeMap(100000000);
                    dropMessage("You have run out of time to gather members for your drop game");
                }

                getClient().getChannelServer().removeMapleSquad(getClient().getChannelServer().getMapleSquad(MapleSquadType.ARIANT1), MapleSquadType.ARIANT1);
            }
        }, amount);
    }

    public void closeArena2(int amount) {
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                if (getMapId() == 101040002 || getMapId() == 101040003 || getMapId() == 102020000) {
                    changeMap(100000000);
                    dropMessage("You have run out of time to gather members for your drop game");
                }

                getClient().getChannelServer().removeMapleSquad(getClient().getChannelServer().getMapleSquad(MapleSquadType.ARIANT2), MapleSquadType.ARIANT2);
            }
        }, amount);
    }

    public void closeArena3(int amount) {
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                if (getMapId() == 101040002 || getMapId() == 101040003 || getMapId() == 102020000) {
                    changeMap(100000000);
                    dropMessage("You have run out of time to gather members for your drop game");
                }

                getClient().getChannelServer().removeMapleSquad(getClient().getChannelServer().getMapleSquad(MapleSquadType.ARIANT3), MapleSquadType.ARIANT3);
            }
        }, amount);
    }

    public double getSpeedMod() {
        return speedMod;
    }

    public double getJumpMod() {
        return jumpMod;
    }

    public int getTotalWatk() {
        return watk;
    }

    public int getTotalAcc() {
        return acc;
    }

    public static int rand(int lbound, int ubound) {
        return (int) ((Math.random() * (ubound - lbound + 1)) + lbound);
    }

    public int getMaxDis(MapleCharacter player) {
        IItem weapon_item = player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
        if (weapon_item != null) {
            MapleWeaponType weapon = MapleItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId());
            if (weapon == MapleWeaponType.SPEAR || weapon == MapleWeaponType.POLE_ARM) {
                maxDis = 106;
            }
            if (weapon == MapleWeaponType.DAGGER || weapon == MapleWeaponType.SWORD1H || weapon == MapleWeaponType.AXE1H || weapon == MapleWeaponType.BLUNT1H) {
                maxDis = 63;
            }
            if (weapon == MapleWeaponType.SWORD2H || weapon == MapleWeaponType.AXE1H || weapon == MapleWeaponType.BLUNT1H) {
                maxDis = 73;
            }
            if (weapon == MapleWeaponType.STAFF || weapon == MapleWeaponType.WAND) {
                maxDis = 51;
            }
            if (weapon == MapleWeaponType.CLAW) {
                skil = SkillFactory.getSkill(4000001);
                skill = player.getSkillLevel(skil);
                if (skill > 0) {
                    maxDis = (skil.getEffect(player.getSkillLevel(skil)).getRange()) + 205;
                } else {
                    maxDis = 205;
                }
            }
            if (weapon == MapleWeaponType.BOW || weapon == MapleWeaponType.CROSSBOW) {
                skil = SkillFactory.getSkill(3000002);
                skill = player.getSkillLevel(skil);
                if (skill > 0) {
                    maxDis = (skil.getEffect(player.getSkillLevel(skil)).getRange()) + 270;
                } else {
                    maxDis = 270;
                }
            }
        }
        return maxDis;
    }

    public int calculateMaxBaseDamage(int watk) {
        int maxbasedamage;
        if (watk == 0) {
            maxbasedamage = 1;
        } else {
            IItem weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
            if (weapon_item != null) {
                MapleWeaponType weapon = MapleItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId());
                int mainstat;
                int secondarystat;
                if (weapon == MapleWeaponType.BOW || weapon == MapleWeaponType.CROSSBOW) {
                    mainstat = localdex;
                    secondarystat = localstr;
                } else if (getJob().isA(MapleJob.THIEF) && (weapon == MapleWeaponType.CLAW || weapon == MapleWeaponType.DAGGER)) {
                    mainstat = localluk;
                    secondarystat = localdex + localstr;
                } else {
                    mainstat = localstr;
                    secondarystat = localdex;
                }
                maxbasedamage = (int) (((weapon.getMaxDamageMultiplier() * mainstat + secondarystat) / 100.0) * watk);
                maxbasedamage += 10;
            } else {
                maxbasedamage = 0;
            }
        }
        return maxbasedamage;
    }

    public List<MapleRing> getCrushRings() {
        Collections.sort(crushRings);
        return crushRings;
    }

    public List<MapleRing> getFriendshipRings() {
        Collections.sort(friendshipRings);
        return friendshipRings;
    }

    public List<MapleRing> getMarriageRings() {
        Collections.sort(marriageRings);
        return marriageRings;
    }

    public int calculateMinBaseDamage(MapleCharacter player) {
        int minbasedamage = 0;
        int atk = player.getTotalWatk();
        if (atk == 0) {
            minbasedamage = 1;
        } else {
            IItem weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((byte) - 11);
            if (weapon_item != null) {
                MapleWeaponType weapon = MapleItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId());
                //mastery start
                if (player.getJob().isA(MapleJob.FIGHTER)) {
                    skil = SkillFactory.getSkill(1100000);
                    skill = player.getSkillLevel(skil);
                    if (skill > 0) {
                        sword = ((skil.getEffect(player.getSkillLevel(skil)).getMastery() * 5 + 10) / 100);
                    } else {
                        sword = 0.1;
                    }
                } else {
                    skil = SkillFactory.getSkill(1200000);
                    skill = player.getSkillLevel(skil);
                    if (skill > 0) {
                        sword = ((skil.getEffect(player.getSkillLevel(skil)).getMastery() * 5 + 10) / 100);
                    } else {
                        sword = 0.1;
                    }
                }
                skil = SkillFactory.getSkill(1100001);
                skill = player.getSkillLevel(skil);
                if (skill > 0) {
                    axe = ((skil.getEffect(player.getSkillLevel(skil)).getMastery() * 5 + 10) / 100);
                } else {
                    axe = 0.1;
                }
                skil = SkillFactory.getSkill(1200001);
                skill = player.getSkillLevel(skil);
                if (skill > 0) {
                    blunt = ((skil.getEffect(player.getSkillLevel(skil)).getMastery() * 5 + 10) / 100);
                } else {
                    blunt = 0.1;
                }
                skil = SkillFactory.getSkill(1300000);
                skill = player.getSkillLevel(skil);
                if (skill > 0) {
                    spear = ((skil.getEffect(player.getSkillLevel(skil)).getMastery() * 5 + 10) / 100);
                } else {
                    spear = 0.1;
                }
                skil = SkillFactory.getSkill(1300001);
                skill = player.getSkillLevel(skil);
                if (skill > 0) {
                    polearm = ((skil.getEffect(player.getSkillLevel(skil)).getMastery() * 5 + 10) / 100);
                } else {
                    polearm = 0.1;
                }
                skil = SkillFactory.getSkill(3200000);
                skill = player.getSkillLevel(skil);
                if (skill > 0) {
                    crossbow = ((skil.getEffect(player.getSkillLevel(skil)).getMastery() * 5 + 10) / 100);
                } else {
                    crossbow = 0.1;
                }
                skil = SkillFactory.getSkill(3100000);
                skill = player.getSkillLevel(skil);
                if (skill > 0) {
                    bow = ((skil.getEffect(player.getSkillLevel(skil)).getMastery() * 5 + 10) / 100);
                } else {
                    bow = 0.1;
                }
                if (weapon == MapleWeaponType.CROSSBOW) {
                    minbasedamage = (int) (localdex * 0.9 * 3.6 * crossbow + localstr) / 100 * (atk + 15);
                }
                if (weapon == MapleWeaponType.BOW) {
                    minbasedamage = (int) (localdex * 0.9 * 3.4 * bow + localstr) / 100 * (atk + 15);
                }
                if (getJob().isA(MapleJob.THIEF) && (weapon == MapleWeaponType.DAGGER)) {
                    minbasedamage = (int) (localluk * 0.9 * 3.6 * dagger + localstr + localdex) / 100 * atk;
                }
                if (!getJob().isA(MapleJob.THIEF) && (weapon == MapleWeaponType.DAGGER)) {
                    minbasedamage = (int) (localstr * 0.9 * 4.0 * dagger + localdex) / 100 * atk;
                }
                if (getJob().isA(MapleJob.THIEF) && (weapon == MapleWeaponType.CLAW)) {
                    minbasedamage = (int) (localluk * 0.9 * 3.6 * claw + localstr + localdex) / 100 * (atk + 15);
                }
                if (weapon == MapleWeaponType.SPEAR) {
                    minbasedamage = (int) (localstr * 0.9 * 3.0 * spear + localdex) / 100 * atk;
                }
                if (weapon == MapleWeaponType.POLE_ARM) {
                    minbasedamage = (int) (localstr * 0.9 * 3.0 * polearm + localdex) / 100 * atk;
                }
                if (weapon == MapleWeaponType.SWORD1H) {
                    minbasedamage = (int) (localstr * 0.9 * 4.0 * sword + localdex) / 100 * atk;
                }
                if (weapon == MapleWeaponType.SWORD2H) {
                    minbasedamage = (int) (localstr * 0.9 * 4.6 * sword + localdex) / 100 * atk;
                }
                if (weapon == MapleWeaponType.AXE1H) {
                    minbasedamage = (int) (localstr * 0.9 * 3.2 * axe + localdex) / 100 * atk;
                }
                if (weapon == MapleWeaponType.BLUNT1H) {
                    minbasedamage = (int) (localstr * 0.9 * 3.2 * blunt + localdex) / 100 * atk;
                }
                if (weapon == MapleWeaponType.AXE2H) {
                    minbasedamage = (int) (localstr * 0.9 * 3.4 * axe + localdex) / 100 * atk;
                }
                if (weapon == MapleWeaponType.BLUNT2H) {
                    minbasedamage = (int) (localstr * 0.9 * 3.4 * blunt + localdex) / 100 * atk;
                }
                if (weapon == MapleWeaponType.STAFF || weapon == MapleWeaponType.WAND) {
                    minbasedamage = (int) (localstr * 0.9 * 3.0 * staffwand + localdex) / 100 * atk;
                }
            }
        }
        return minbasedamage;
    }

    public int getRandomage(MapleCharacter player) {
        int maxdamage = player.getCurrentMaxBaseDamage();
        int mindamage = player.calculateMinBaseDamage(player);
        return MapleCharacter.rand(mindamage, maxdamage);
    }

    public void levelUp() {
        ISkill improvingMaxHP = null;
        int improvingMaxHPLevel = 0;
        ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
        int improvingMaxMPLevel = getSkillLevel(improvingMaxMP);
        remainingAp += 5;
        if (job == MapleJob.BEGINNER) {
            maxhp += rand(12, 16);
            maxmp += rand(10, 12);
        } else if (job.isA(MapleJob.WARRIOR)) {
            improvingMaxHP = SkillFactory.getSkill(1000001);
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += rand(24, 28);
            maxmp += rand(4, 6);
        } else if (job.isA(MapleJob.MAGICIAN)) {
            maxhp += rand(10, 14);
            maxmp += rand(22, 24);
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.THIEF) || job.isA(MapleJob.GM)) {
            maxhp += rand(20, 24);
            maxmp += rand(14, 16);
        } else if (job.isA(MapleJob.PIRATE)) {
            improvingMaxHP = SkillFactory.getSkill(5100000);
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += rand(22, 28);
            maxmp += rand(18, 23);
        }
        if (level >= 70) {
            finishAchievement(4);
        }
        if (level >= 120) {
            finishAchievement(5);
        }
        if (level == 200) {
            finishAchievement(22);
        }
        if (improvingMaxHPLevel > 0) {
            maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getX();
        }
        if (improvingMaxMPLevel > 0) {
            maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getX();
        }
        maxmp += getTotalInt() / 10;
        exp.addAndGet(-ExpTable.getExpNeededForLevel(level));
        level += 1;
        maxhp = Math.min(30000, maxhp);
        maxmp = Math.min(30000, maxmp);
        List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(8);
        statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, Integer.valueOf(remainingAp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, Integer.valueOf(maxhp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, Integer.valueOf(maxmp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(maxhp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(maxmp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.EXP, Integer.valueOf(exp.get())));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.LEVEL, Integer.valueOf(level)));
        if (job != MapleJob.BEGINNER) {
            remainingSp += 3;
            statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLESP, Integer.valueOf(remainingSp)));
        }
        /* AutoJob Start */
        if (autojobon) {
            if (level >= 70 && level <= 90) {
                int[] autojob = {110, 120, 130, 210, 220, 230, 310, 320, 410, 420, 510, 520};
                for (int t : autojob) {
                    if (getJob().getId() == t) {
                        changeJob(MapleJob.getById(getJob().getId() + 1));
                    }
                }
            } else if (level >= 120 && level <= 140) {
                int[] autojob = {111, 121, 131, 211, 221, 231, 311, 321, 411, 421, 511, 521};
                for (int t : autojob) {
                    if (getJob().getId() == t) {
                        changeJob(MapleJob.getById(getJob().getId() + 1));
                    }
                }
            } else if (level == 10 || level == 30 || (getJob().getId() == 0 && level >= 10)) {
                NPCScriptManager.getInstance().start(getClient(), 2131000, null, null);
            }
        }
        /* AutoJob End */
        setHp(maxhp);
        setMp(maxmp);
        getClient().getSession().write(MaplePacketCreator.updatePlayerStats(statup));
        getMap().broadcastMessage(this, MaplePacketCreator.showLevelup(getId()), false);
        recalcLocalStats();
        silentPartyUpdate();
        guildUpdate();
    }

    public void changeKeybinding(int key, MapleKeyBinding keybinding) {
        if (keybinding.getType() != 0) {
            keymap.put(Integer.valueOf(key), keybinding);
        } else {
            keymap.remove(Integer.valueOf(key));
        }
    }

    public void sendKeymap() {
        getClient().getSession().write(MaplePacketCreator.getKeymap(keymap));
    }

    public void sendMacros() {
        boolean macros = false;
        for (int i = 0; i < 5; i++) {
            if (skillMacros[i] != null) {
                macros = true;
            }
        }
        if (macros) {
            getClient().getSession().write(MaplePacketCreator.getMacros(skillMacros));
        }
    }

    public void updateMacros(int position, SkillMacro updateMacro) {
        skillMacros[position] = updateMacro;
    }

    public void tempban(String reason, Calendar duration, int greason) {
        banned = true;
        tempban(reason, duration, greason, client.getAccID());
        client.getSession().write(MaplePacketCreator.sendGMPolice(greason, reason, (int) (duration.getTimeInMillis() / 1000))); //put duration as seconds
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                client.getSession().close();
            }
        }, 10000);
    }

    public static boolean tempban(String reason, Calendar duration, int greason, int accountid) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET tempban = ?, banreason = ?, greason = ? WHERE id = ?");
            Timestamp TS = new Timestamp(duration.getTimeInMillis());
            ps.setTimestamp(1, TS);
            ps.setString(2, reason);
            ps.setInt(3, greason);
            ps.setInt(4, accountid);
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException ex) {
            log.error("Error while tempbanning", ex);
        }
        return false;
    }

    public void ban(String reason, boolean permBan) {
        banned = true;
        if (!client.isGuest()) {
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps;
                if (permBan) {
                    getClient().banMacs();
                    ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                    String[] ipSplit = client.getSession().getRemoteAddress().toString().split(":");
                    ps.setString(1, ipSplit[0]);
                    ps.executeUpdate();
                    ps.close();
                }
                ps = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ?, greason = ? WHERE id = ?");
                ps.setInt(1, 1);
                ps.setString(2, reason);
                ps.setInt(3, 12);
                ps.setInt(4, accountid);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                log.error("Error while banning", ex);
            }
        }
        client.getSession().write(MaplePacketCreator.sendGMPolice(0, reason, 1000000));
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                client.getSession().close();
            }
        }, 10000);

    }

    public static boolean ban(String id, String reason, boolean accountId) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps;
            if (id.matches("/[0-9]{1,3}\\..*")) {
                ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, id);
                ps.executeUpdate();
                ps.close();
            }
            if (accountId) {
                ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
            } else {
                ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            }
            boolean ret = false;
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ps = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?");
                ps.setString(1, reason);
                ps.setInt(2, rs.getInt(1));
                ps.executeUpdate();
                ret = true;
            }
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException ex) {
            log.error("Error while banning", ex);
        }
        return false;
    }

    public static int getAccIdFromCNAME(String name) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            int id_ = rs.getInt("accountid");
            rs.close();
            ps.close();
            return id_;
        } catch (SQLException e) {
            log.error("ERROR", e);
        }
        return -1;
    }

    /**
     * Oid of players is always = the cid
     */
    @Override
    public int getObjectId() {
        return getId();
    }

    /**
     * Throws unsupported operation exception, oid of players is read only
     */
    @Override
    public void setObjectId(int id) {
        throw new UnsupportedOperationException();
    }

    public MapleStorage getStorage() {
        return storage;
    }

    public int getCurrentMaxHp() {
        return localmaxhp;
    }

    public int getCurrentMaxMp() {
        return localmaxmp;
    }

    public int getCurrentMaxBaseDamage() {
        return localmaxbasedamage;
    }

    public int getTotalMdef() {
        return mdef;
    }

    public int getTotalWdef() {
        return wdef;
    }

    public void addVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.add(mo);
    }

    public void removeVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.remove(mo);
    }

    public boolean isMapObjectVisible(MapleMapObject mo) {
        return visibleMapObjects.contains(mo);
    }

    public Collection<MapleMapObject> getVisibleMapObjects() {
        return Collections.unmodifiableCollection(visibleMapObjects);
    }

    public boolean isAlive() {
        return this.hp > 0;
    }

    public boolean isDead() {
        return this.hp <= 0;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.removePlayerFromMap(this.getObjectId()));
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if ((this.isHidden() && client.getPlayer().isGM()) || !this.isHidden()) {
            client.getSession().write(MaplePacketCreator.spawnPlayerMapobject(this));
            for (int i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    client.getSession().write(MaplePacketCreator.showPet(this, pets[i], false, false));
                } else {
                    break;
                }
            }
        }
    }

    private void recalcLocalStats() {
        int oldmaxhp = localmaxhp;
        localmaxhp = getMaxHp();
        localmaxmp = getMaxMp();
        localdex = getDex();
        localint = getInt();
        localstr = getStr();
        localluk = getLuk();
        int speed = 100;
        int jump = 100;
        magic = localint;
        watk = 0;
        wdef = 0;
        mdef = 0;
        for (IItem item : getInventory(MapleInventoryType.EQUIPPED)) {
            IEquip equip = (IEquip) item;
            localmaxhp += equip.getHp();
            localmaxmp += equip.getMp();
            localdex += equip.getDex();
            localint += equip.getInt();
            localstr += equip.getStr();
            localluk += equip.getLuk();
            magic += equip.getMatk() + equip.getInt();
            watk += equip.getWatk();
            speed += equip.getSpeed();
            jump += equip.getJump();
            wdef += equip.getWdef();
            mdef += equip.getMdef();
            acc += equip.getAcc();
        }
        magic = Math.min(magic, 2000);
        Integer hbhp = getBuffedValue(MapleBuffStat.HYPERBODYHP);
        if (hbhp != null) {
            localmaxhp += (hbhp.doubleValue() / 100) * localmaxhp;
        }
        Integer hbmp = getBuffedValue(MapleBuffStat.HYPERBODYMP);
        if (hbmp != null) {
            localmaxmp += (hbmp.doubleValue() / 100) * localmaxmp;
        }
        localmaxhp = Math.min(30000, localmaxhp);
        localmaxmp = Math.min(30000, localmaxmp);
        Integer watkbuff = getBuffedValue(MapleBuffStat.WATK);
        if (watkbuff != null) {
            watk += watkbuff.intValue();
        }
        if (job.isA(MapleJob.BOWMAN)) {
            ISkill expert = null;
            if (job.isA(MapleJob.CROSSBOWMASTER)) {
                expert = SkillFactory.getSkill(3220004);
            } else if (job.isA(MapleJob.BOWMASTER)) {
                expert = SkillFactory.getSkill(3120005);
            }
            if (expert != null) {
                int boostLevel = getSkillLevel(expert);
                if (boostLevel > 0) {
                    watk += expert.getEffect(boostLevel).getX();
                }
            }
        }
        Integer matkbuff = getBuffedValue(MapleBuffStat.MATK);
        if (matkbuff != null) {
            magic += matkbuff.intValue();
        }
        Integer speedbuff = getBuffedValue(MapleBuffStat.SPEED);
        if (speedbuff != null) {
            speed += speedbuff.intValue();
        }
        Integer jumpbuff = getBuffedValue(MapleBuffStat.JUMP);
        if (jumpbuff != null) {
            jump += jumpbuff.intValue();
        }
        if (speed > 140) {
            speed = 140;
        }
        if (jump > 123) {
            jump = 123;
        }
        speedMod = speed / 100.0;
        jumpMod = jump / 100.0;
        Integer mount = getBuffedValue(MapleBuffStat.MONSTER_RIDING);
        if (mount != null) {
            jumpMod = 1.23;
            switch (mount.intValue()) {
                case 1:
                    speedMod = 1.5;
                    break;
                case 2:
                    speedMod = 1.7;
                    break;
                case 3:
                    speedMod = 1.8;
                    break;
                case 5:
                    speedMod = 1.0;
                    jumpMod = 1.0;
                    break;
                default:
                    speedMod = 2.0;
            }
        }
        localmaxbasedamage = calculateMaxBaseDamage(watk);
        if (oldmaxhp != 0 && oldmaxhp != localmaxhp) {
            updatePartyMemberHP();
        }
    }

    public void Mount(int id, int skillid) {
        maplemount = new MapleMount(this, id, skillid);
    }

    public MapleMount getMount() {
        return maplemount;
    }

    public void equipChanged() {
        getMap().broadcastMessage(this, MaplePacketCreator.updateCharLook(this), false);
        recalcLocalStats();
        enforceMaxHpMp();
        //MaplePacketCreator.getCharInfo(this);
        if (getClient().getPlayer().getMessenger() != null) {
            WorldChannelInterface wci = ChannelServer.getInstance(getClient().getChannel()).getWorldInterface();
            try {
                wci.updateMessenger(getClient().getPlayer().getMessenger().getId(), getClient().getPlayer().getName(), getClient().getChannel());
            } catch (RemoteException e) {
                getClient().getChannelServer().reconnectWorld();
            }
        }
    }

    public MaplePet getPet(int index) {
        return pets[index];
    }

    public void addPet(MaplePet pet) {
        for (int i = 0; i < 3; i++) {
            if (pets[i] == null) {
                pets[i] = pet;
                return;
            }
        }
    }

    public void removePet(MaplePet pet, boolean shift_left) {
        int slot = -1;
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == pet.getUniqueId()) {
                    pets[i] = null;
                    slot = i;
                    break;
                }
            }
        }
        if (shift_left) {
            if (slot > -1) {
                for (int i = slot; i < 3; i++) {
                    if (i != 2) {
                        pets[i] = pets[i + 1];
                    } else {
                        pets[i] = null;
                    }
                }
            }
        }
    }

    public int getNoPets() {
        int ret = 0;
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                ret++;
            } else {
                break;
            }
        }
        return ret;
    }

    public int getPetIndex(MaplePet pet) {
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == pet.getUniqueId()) {
                    return i;
                }
            } else {
                break;
            }
        }
        return -1;
    }

    public int getPetIndex(int petId) {
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == petId) {
                    return i;
                }
            } else {
                break;
            }
        }
        return -1;
    }

    public int getNextEmptyPetIndex() {
        for (int i = 0; i < 3; i++) {
            if (pets[i] == null) {
                return i;
            }
        }
        return 3;
    }

    public MaplePet[] getPets() {
        return pets;
    }

    public void unequipAllPets() {
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                unequipPet(pets[i], true);
                cancelFullnessSchedule(i);
            } else {
                break;
            }
        }
    }

    public void unequipPet(MaplePet pet, boolean shift_left) {
        unequipPet(pet, shift_left, false);
    }

    public void unequipPet(MaplePet pet, boolean shift_left, boolean hunger) {
        cancelFullnessSchedule(getPetIndex(pet));
        pet.saveToDb();
        getMap().broadcastMessage(this, MaplePacketCreator.showPet(this, pet, true, hunger), true);
        List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
        stats.add(new Pair<MapleStat, Integer>(MapleStat.PET, Integer.valueOf(0)));
        getClient().getSession().write(MaplePacketCreator.petStatUpdate(this));
        getClient().getSession().write(MaplePacketCreator.enableActions());
        removePet(pet, shift_left);
    }

    public void shiftPetsRight() {
        if (pets[2] == null) {
            pets[2] = pets[1];
            pets[1] = pets[0];
            pets[0] = null;
        }
    }

    public FameStatus canGiveFame(MapleCharacter from) {
        if (lastfametime >= System.currentTimeMillis() - 60 * 60 * 24 * 1000) {
            return FameStatus.NOT_TODAY;
        } else if (lastmonthfameids.contains(Integer.valueOf(from.getId()))) {
            return FameStatus.NOT_THIS_MONTH;
        } else {
            return FameStatus.OK;
        }
    }

    public void hasGivenFame(MapleCharacter to) {
        lastfametime = System.currentTimeMillis();
        lastmonthfameids.add(Integer.valueOf(to.getId()));
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)");
            ps.setInt(1, getId());
            ps.setInt(2, to.getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            log.error("ERROR writing famelog for char " + getName() + " to " + to.getName(), e);
        }
    }

    public MapleParty getParty() {
        return party;
    }

    public int getPartyId() {
        return (party != null ? party.getId() : -1);
    }

    public int getWorld() {
        return world;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public void setParty(MapleParty party) {
        this.party = party;
    }

    public MapleTrade getTrade() {
        return trade;
    }

    public void setTrade(MapleTrade trade) {
        this.trade = trade;
    }

    public EventInstanceManager getEventInstance() {
        return eventInstance;
    }

    public void setEventInstance(EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }

    public void addDoor(MapleDoor door) {
        doors.add(door);
    }

    public void clearDoors() {
        doors.clear();
    }

    public List<MapleDoor> getDoors() {
        return new ArrayList<MapleDoor>(doors);
    }

    public boolean canDoor() {
        return canDoor;
    }

    public void disableDoor() {
        canDoor = false;
        TimerManager tMan = TimerManager.getInstance();
        tMan.schedule(new Runnable() {

            @Override
            public void run() {
                canDoor = true;
            }
        }, 5000);
    }

    public Map<Integer, MapleSummon> getSummons() {
        return summons;
    }

    public int getChair() {
        return chair;
    }

    public int getItemEffect() {
        return itemEffect;
    }

    public void setChair(int chair) {
        this.chair = chair;
    }

    public void setItemEffect(int itemEffect) {
        this.itemEffect = itemEffect;
    }

    @Override
    public Collection<MapleInventory> allInventories() {
        return Arrays.asList(inventory);
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER;
    }

    public int getGuildId() {
        return guildid;
    }

    public int getGuildRank() {
        return guildrank;
    }

    public void setBossPoints(int points) {
        bossPoints = points;
    }

    public int getBossPoints() {
        return bossPoints;
    }

    public void setGuildId(int _id) {
        guildid = _id;
        if (guildid > 0) {
            if (mgc == null) {
                mgc = new MapleGuildCharacter(this);
            } else {
                mgc.setGuildId(guildid);
            }
        } else {
            mgc = null;
        }
    }

    public void setGuildRank(int _rank) {
        guildrank = _rank;
        if (mgc != null) {
            mgc.setGuildRank(_rank);
        }
    }

    public void setAllianceRank(int rank) {
        allianceRank = rank;
        if (mgc != null) {
            mgc.setAllianceRank(rank);
        }
    }

    public int getAllianceRank() {
        return this.allianceRank;
    }

    public MapleGuildCharacter getMGC() {
        return mgc;
    }

    public void guildUpdate() {
        if (this.guildid <= 0) {
            return;
        }

        mgc.setLevel(this.level);
        mgc.setJobId(this.job.getId());

        try {
            this.client.getChannelServer().getWorldInterface().memberLevelJobUpdate(this.mgc);
        } catch (RemoteException re) {
            log.error("RemoteExcept while trying to update level/job in guild.", re);
        }
    }
    private NumberFormat nf = new DecimalFormat("#,###,###,###");

    public String guildCost() {
        return nf.format(MapleGuild.CREATE_GUILD_COST);
    }

    public String emblemCost() {
        return nf.format(MapleGuild.CHANGE_EMBLEM_COST);
    }

    public String capacityCost() {
        return nf.format(MapleGuild.INCREASE_CAPACITY_COST);
    }

    public void genericGuildMessage(int code) {
        this.client.getSession().write(MaplePacketCreator.genericGuildMessage((byte) code));
    }

    public void disbandGuild() {
        if (guildid <= 0 || guildrank != 1) {
            log.warn(this.name + " tried to disband and s/he is either not in a guild or not leader.");
            return;
        }
        try {
            client.getChannelServer().getWorldInterface().disbandGuild(this.guildid);
        } catch (RemoteException e) {
            client.getChannelServer().reconnectWorld();
            log.error("Error while disbanding guild.", e);
        }
    }

    public void increaseGuildCapacity() {
        if (this.getMeso() < MapleGuild.INCREASE_CAPACITY_COST) {
            client.getSession().write(MaplePacketCreator.serverNotice(1, "You do not have enough mesos."));
            return;
        }

        if (this.guildid <= 0) {
            log.info(this.name + " is trying to increase guild capacity without being in the guild.");
            return;
        }

        try {
            client.getChannelServer().getWorldInterface().increaseGuildCapacity(this.guildid);
        } catch (RemoteException e) {
            client.getChannelServer().reconnectWorld();
            log.error("Error while increasing capacity.", e);
            return;
        }

        this.gainMeso(-MapleGuild.INCREASE_CAPACITY_COST, true, false, true);
    }

    public void saveGuildStatus() {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = ?, guildrank = ?, allianceRank = ? WHERE id = ?");
            ps.setInt(1, this.guildid);
            ps.setInt(2, this.guildrank);
            ps.setInt(3, this.allianceRank);
            ps.setInt(4, this.id);
            ps.execute();
            ps.close();
        } catch (SQLException se) {
            log.error("SQL error: " + se.getLocalizedMessage(), se);
        }
    }

    /**
     * Allows you to change someone's NXCash, Maple Points, and Gift Tokens!
     *
     * Created by Acrylic/Penguins
     *
     * @param type: 0 = NX, 1 = MP, 2 = GT
     * @param quantity: how much to modify it by. Negatives subtract points, Positives add points.
     */
    public void modifyCSPoints(int type, int quantity) {
        switch (type) {
            case 1:
                this.paypalnx += quantity;
                break;
            case 2:
                this.maplepoints += quantity;
                break;
            case 4:
                this.cardnx += quantity;
                break;
        }
    }

    public int getCSPoints(int type) {
        switch (type) {
            case 1:
                return this.paypalnx;
            case 2:
                return this.maplepoints;
            case 4:
                return this.cardnx;
            default:
                return 0;
        }
    }

    public boolean haveItem(int itemid, int quantity, boolean checkEquipped, boolean greaterOrEquals) {
        int possesed = getItemQuantity(itemid, checkEquipped);
        if (greaterOrEquals) {
            return possesed >= quantity;
        } else {
            return possesed == quantity;
        }
    }

    private static class MapleBuffStatValueHolder {

        public MapleStatEffect effect;
        public long startTime;
        public int value;
        public ScheduledFuture<?> schedule;

        public MapleBuffStatValueHolder(MapleStatEffect effect, long startTime, ScheduledFuture<?> schedule, int value) {
            super();
            this.effect = effect;
            this.startTime = startTime;
            this.schedule = schedule;
            this.value = value;
        }
    }

    public static class MapleCoolDownValueHolder {

        public int skillId;
        public long startTime;
        public long length;
        public ScheduledFuture<?> timer;

        public MapleCoolDownValueHolder(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
            super();
            this.skillId = skillId;
            this.startTime = startTime;
            this.length = length;
            this.timer = timer;
        }
    }

    public static class SkillEntry {

        public int skillevel;
        public int masterlevel;

        public SkillEntry(int skillevel, int masterlevel) {
            this.skillevel = skillevel;
            this.masterlevel = masterlevel;
        }

        @Override
        public String toString() {
            return skillevel + ":" + masterlevel;
        }
    }

    public enum FameStatus {

        OK, NOT_TODAY, NOT_THIS_MONTH
    }

    public int getBuddyCapacity() {
        return buddylist.getCapacity();
    }

    public void setBuddyCapacity(int capacity) {
        buddylist.setCapacity(capacity);
        client.getSession().write(MaplePacketCreator.updateBuddyCapacity(capacity));
    }

    public MapleMessenger getMessenger() {
        return messenger;
    }

    public void setMessenger(MapleMessenger messenger) {
        this.messenger = messenger;
    }

    public void checkMessenger() {
        if (messenger != null && messengerposition < 4 && messengerposition > -1) {
            try {
                WorldChannelInterface wci = ChannelServer.getInstance(client.getChannel()).getWorldInterface();
                MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(client.getPlayer(), messengerposition);
                wci.silentJoinMessenger(messenger.getId(), messengerplayer, messengerposition);
                wci.updateMessenger(getClient().getPlayer().getMessenger().getId(), getClient().getPlayer().getName(), getClient().getChannel());
            } catch (RemoteException e) {
                client.getChannelServer().reconnectWorld();
            }
        }
    }

    public int getMessengerPosition() {
        return messengerposition;
    }

    public void setMessengerPosition(int position) {
        this.messengerposition = position;
    }

    public int hasEXPCard() {
        return 1;
    }

    public void setInCS(boolean yesno) {
        this.incs = yesno;
    }

    public boolean inCS() {
        return this.incs;
    }

    public void setInMTS(boolean yesno) {
        this.inmts = yesno;
    }

    public boolean inMTS() {
        return this.inmts;
    }

    public void addCooldown(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
        if (this.coolDowns.containsKey(Integer.valueOf(skillId))) {
            this.coolDowns.remove(skillId);
        }
        this.coolDowns.put(Integer.valueOf(skillId), new MapleCoolDownValueHolder(skillId, startTime, length, timer));
    }

    public void removeCooldown(int skillId) {
        if (this.coolDowns.containsKey(Integer.valueOf(skillId))) {
            this.coolDowns.remove(Integer.valueOf(skillId));
            client.getSession().write(MaplePacketCreator.skillCooldown(skillId, 0));
        }
    }

    public boolean skillisCooling(int skillId) {
        return this.coolDowns.containsKey(Integer.valueOf(skillId));
    }

    public void giveCoolDowns(final List<PlayerCoolDownValueHolder> cooldowns) {
        for (PlayerCoolDownValueHolder cooldown : cooldowns) {
            int time = (int) ((cooldown.length + cooldown.startTime) - System.currentTimeMillis());
            ScheduledFuture<?> timer = TimerManager.getInstance().schedule(new CancelCooldownAction(this, cooldown.skillId), time);
            addCooldown(cooldown.skillId, System.currentTimeMillis(), time, timer);
        }
    }

    public void giveCoolDowns(final int skillid, long starttime, long length) {
        int time = (int) ((length + starttime) - System.currentTimeMillis());
        ScheduledFuture<?> timer = TimerManager.getInstance().schedule(new CancelCooldownAction(this, skillid), time);
        addCooldown(skillid, System.currentTimeMillis(), time, timer);
    }

    public List<PlayerCoolDownValueHolder> getAllCooldowns() {
        List<PlayerCoolDownValueHolder> ret = new ArrayList<PlayerCoolDownValueHolder>();
        for (MapleCoolDownValueHolder mcdvh : coolDowns.values()) {
            ret.add(new PlayerCoolDownValueHolder(mcdvh.skillId, mcdvh.startTime, mcdvh.length));
        }
        return ret;
    }

    public static class CancelCooldownAction implements Runnable {

        private int skillId;
        private WeakReference<MapleCharacter> target;

        public CancelCooldownAction(MapleCharacter target, int skillId) {
            this.target = new WeakReference<MapleCharacter>(target);
            this.skillId = skillId;
        }

        @Override
        public void run() {
            MapleCharacter realTarget = target.get();
            if (realTarget != null) {
                realTarget.removeCooldown(skillId);
            }
        }
    }

    public void giveDebuff(MapleDisease disease, MobSkill skill, boolean cpq) {
        synchronized (diseases) {
            if (isAlive() && !isActiveBuffedValue(2321005) && !diseases.contains(disease) && (diseases.size() < 2) || cpq) {
                diseases.add(disease);
                List<Pair<MapleDisease, Integer>> debuff = Collections.singletonList(new Pair<MapleDisease, Integer>(disease, Integer.valueOf(skill.getX())));
                long mask = 0;
                for (Pair<MapleDisease, Integer> statup : debuff) {
                    mask |= statup.getLeft().getValue();
                }
                getClient().getSession().write(MaplePacketCreator.giveDebuff(mask, debuff, skill));
                getMap().broadcastMessage(this, MaplePacketCreator.giveForeignDebuff(id, mask, skill), false);

                if (isAlive() && diseases.contains(disease)) {
                    final MapleCharacter character = this;
                    final MapleDisease disease_ = disease;
                    TimerManager.getInstance().schedule(new Runnable() {

                        @Override
                        public void run() {
                            if (character.diseases.contains(disease_)) {
                                dispelDebuff(disease_);
                            }
                        }
                    }, skill.getDuration());
                }
            }
        }
    }

    public void dispelDebuff(MapleDisease debuff) {
        if (diseases.contains(debuff)) {
            diseases.remove(debuff);
            long mask = debuff.getValue();
            getClient().getSession().write(MaplePacketCreator.cancelDebuff(mask));
            getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(id, mask), false);
        }
    }

    public MapleCharacter getPartner() {
        return client.getChannelServer().getPlayerStorage().getCharacterById(partnerid);
    }

    public void dispelDebuffs() {
        MapleDisease[] disease = {MapleDisease.POISON, MapleDisease.SLOW, MapleDisease.SEAL, MapleDisease.DARKNESS, MapleDisease.WEAKEN, MapleDisease.CURSE};
        for (int i = 0; i < diseases.size(); i++) {
            if (diseases.contains(disease[i])) {
                diseases.remove(disease[i]); // test
                long mask = 0;
                for (MapleDisease statup : diseases) {
                    mask |= statup.getValue();
                }
                getClient().getSession().write(MaplePacketCreator.cancelDebuff(mask));
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(id, mask), false);
            }
        }
    }

    public void setLevel(int level) {
        if (level <= 0) {
            level = 1;
        }
        this.level = level;
    }

    public void setMap(int PmapId) {
        this.mapid = PmapId;
    }

    public List<Integer> getQuestItemsToShow() {
        Set<Integer> delta = new HashSet<Integer>();
        for (Map.Entry<MapleQuest, MapleQuestStatus> questEntry : this.quests.entrySet()) {
            if (questEntry.getValue().getStatus() != MapleQuestStatus.Status.STARTED) {
                delta.addAll(questEntry.getKey().getQuestItemsToShowOnlyIfQuestIsActivated());
            }
        }
        List<Integer> returnThis = new ArrayList<Integer>();
        returnThis.addAll(delta);
        return Collections.unmodifiableList(returnThis);
    }

    public void sendNote2(int to, String msg) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`) VALUES (?, ?, ?, ?)");
        ps.setInt(1, to);
        ps.setString(2, this.getName());
        ps.setString(3, msg);
        ps.setLong(4, System.currentTimeMillis());
        ps.executeUpdate();
        ps.close();
    }

    public void sendNote(int to, String msg) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`) VALUES (?, ?, ?, ?)");
        ps.setInt(1, to);
        ps.setString(2, this.getName());
        ps.setString(3, msg);
        ps.setLong(4, System.currentTimeMillis());
        ps.executeUpdate();
        ps.close();
    }

    public void showNote() throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM notes WHERE `to`=?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ps.setInt(1, getId());
        ResultSet rs = ps.executeQuery();
        rs.last();
        int count = rs.getRow();
        rs.first();
        client.getSession().write(MaplePacketCreator.showNotes(rs, count));
        ps.close();
    }

    public boolean isMarried() {
        return married;
    }

    public void setMarried(boolean status) {
        this.married = status;
    }

    public int getMarriageQuestLevel() {
        return marriageQuestLevel;
    }

    public void setMarriageQuestLevel(int nf) {
        marriageQuestLevel = nf;
    }

    public void addMarriageQuestLevel() {
        marriageQuestLevel++;
    }

    public void subtractMarriageQuestLevel() {
        marriageQuestLevel -= 1;
    }

    public void setJumpPoints(int level) {
        this.jumpPoints = level;
    }

    public void gainJumpPoints(int level) {
        this.jumpPoints = this.jumpPoints + level;
    }

    public int getJumpPoints() {
        return this.jumpPoints;
    }

    public void setPassionPoints(int level) {
        this.passionPoints = level;
    }

    public int getPassionPoints() {
        return this.passionPoints;
    }

    public void setRebirthPoints(int level) {
        this.rebirthPoints = level;
    }

    public int getRebirthPoints() {
        return this.rebirthPoints;
    }

    public void gainRebirthPoints(int level) {
        this.rebirthPoints = this.rebirthPoints + level;
    }

    public void setStrike(int level) {
        this.strike = level;
    }

    public void setJailTime(int wtf) {
        this.jailtime = wtf;
    }

    public int getJailTime() {
        return this.jailtime;
    }

    public int getStrike() {
        return this.strike;
    }

    public void setRelationship(int level) {
        this.relationship = level;
    }

    public int getRelationship() {
        return this.relationship;
    }

    public void setZakumLevel(int level) {
        this.zakumLvl = level;
    }

    public int getZakumLevel() {
        return this.zakumLvl;
    }

    public void addZakumLevel() {
        this.zakumLvl += 1;
    }

    public void subtractZakumLevel() {
        this.zakumLvl -= 1;
    }

    public void setPartnerId(int pem) {
        this.partnerid = pem;
    }

    public int getPartnerId() {
        return partnerid;
    }

    public void checkBerserk() {
        if (BerserkSchedule != null) {
            BerserkSchedule.cancel(false);
        }
        final MapleCharacter chr = this;
        ISkill BerserkX = SkillFactory.getSkill(1320006);
        final int skilllevel = getSkillLevel(BerserkX);
        if (chr.getJob().equals(MapleJob.DARKKNIGHT) && skilllevel >= 1) {
            MapleStatEffect ampStat = BerserkX.getEffect(skilllevel);
            int x = ampStat.getX();
            int HP = chr.getHp();
            int MHP = chr.getMaxHp();
            int ratio = HP * 100 / MHP;
            if (ratio > x) {
                Berserk = false;
            } else {
                Berserk = true;
            }
            BerserkSchedule = TimerManager.getInstance().register(new Runnable() {

                @Override
                public void run() {
                    getClient().getSession().write(MaplePacketCreator.showOwnBerserk(skilllevel, Berserk));
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBerserk(getId(), skilllevel, Berserk), false);
                }
            }, 5000, 3000);
        }
    }

    private void prepareBeholderEffect() {
        if (beholderHealingSchedule != null) {
            beholderHealingSchedule.cancel(false);
        }
        if (beholderBuffSchedule != null) {
            beholderBuffSchedule.cancel(false);
        }

        ISkill bHealing = SkillFactory.getSkill(1320008);
        if (getSkillLevel(bHealing) > 0) {
            final MapleStatEffect healEffect = bHealing.getEffect(getSkillLevel(bHealing));
            beholderHealingSchedule = TimerManager.getInstance().register(new Runnable() {

                @Override
                public void run() {
                    addHP(healEffect.getHp());
                    getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(1321007, 2));
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), 1321007, 5), true);
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), 1321007, 2, (byte) 3), false);
                }
            }, healEffect.getX() * 1000, healEffect.getX() * 1000);
        }
        ISkill bBuffing = SkillFactory.getSkill(1320009);
        if (getSkillLevel(bBuffing) > 0) {
            final MapleStatEffect buffEffect = bBuffing.getEffect(getSkillLevel(bBuffing));
            beholderBuffSchedule = TimerManager.getInstance().register(new Runnable() {

                @Override
                public void run() {
                    buffEffect.applyTo(MapleCharacter.this);
                    getClient().getSession().write(MaplePacketCreator.beholderAnimation(getId(), 1320009));
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), 1321007, (int) (Math.random() * 3) + 6), true);
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), 1321007, 2, (byte) 3), false);
                }
            }, buffEffect.getX() * 1000, buffEffect.getX() * 1000);
        }
    }

    public void setChalkboard(String text) {
        if (interaction != null) {
            return;
        }
        this.chalktext = text;
        for (FakeCharacter ch : fakes) {
            ch.getFakeChar().setChalkboard(text);
        }
        if (chalktext == null) {
            getMap().broadcastMessage(MaplePacketCreator.useChalkboard(this, true));
        } else {
            getMap().broadcastMessage(MaplePacketCreator.useChalkboard(this, false));
        }
    }

    public String getChalkboard() {
        return this.chalktext;
    }

    public void setDefaultKeyMap() {
        keymap.clear();
        int[] num1 = {2, 3, 4, 5, 6, 7, 16, 17, 18, 19, 23, 25, 26, 27, 29, 31, 34, 35, 37, 38, 40, 41, 43, 44, 45, 46, 48, 50, 56, 57, 59, 60, 61, 62, 63, 64, 65};
        int[] num2 = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 4, 5, 5, 6, 6, 6, 6, 6, 6, 6};
        int[] num3 = {10, 12, 13, 18, 24, 21, 8, 5, 0, 4, 1, 19, 14, 15, 52, 2, 17, 11, 3, 20, 16, 23, 9, 50, 51, 6, 22, 7, 53, 54, 100, 101, 102, 103, 104, 105, 106};
        for (int i = 0; i < num1.length; i++) {
            keymap.put(Integer.valueOf(num1[i]), new MapleKeyBinding(num2[i], num3[i]));
        }
        sendKeymap();
    }

    public void setReborns(int amt) {
        reborns = amt;
    }

    public void gainReborns(int ouroldadministratorsoulsuckscock) {
        reborns = reborns + ouroldadministratorsoulsuckscock;
    }

    public void setMkills(int mk) {
        this.mkills = mk;
    }

    public void setBetaChar(int d) {
        this.betachar = d;
    }

    public int getReborns() {
        return reborns;
    }

    public int getMkills() {
        return mkills;
    }

    public int getDonatorLevel() {
        return donatorlevel;
    }

    public int getBetaChar() {
        return betachar;
    }

    public boolean isBetaChar() {
        if (getBetaChar() >= 1) {
            return true;
        } else {
            return false;
        }
    }

    public void setDonatorLevel(int soulyouwishyouwereadonator) {
        this.donatorlevel = soulyouwishyouwereadonator;
    }

    public int getBossCount() {
        return bosscounter;
    }

    public void setBossCounter(int soulgetsrapedbythis) {
        this.bosscounter = soulgetsrapedbythis;
    }

    public void setFamily(int family) {
        this.family = family;
    }

    public int getFamily() {
        return family;
    }

    public boolean isBlueFamily() {
        if (getFamily() == 1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isRedFamily() {
        if (getFamily() == 2) {
            return true;
        } else {
            return false;
        }

    }

    public void doReborn() {
        gainReborns(1);
        gainRealReborns(1); // Makes it where Super Rebirth doesn't ban you.
        setLevel(1);
        setExp(0);
        setJob(MapleJob.BEGINNER);
        updateSingleStat(MapleStat.LEVEL, 1); // ohh so it's suposed to happen? yeah but if that happens randomly its not supposed to so it can't be fixed? o-o it can if we remove the uneeded rs.close()'s ok then let's do this real quick and then do that kk i think i found it lol
        updateSingleStat(MapleStat.JOB, 0);
        updateSingleStat(MapleStat.EXP, 0); // does forcibly close mean its are fault they are dcing forcibly closed means like its not us its the source (rs.close() so like
        MaplePacket packet = MaplePacketCreator.serverNotice(6, "[Congrats] " + getName() + " has reached " + getReborns() + " reborns(s)! (It's not like it's a surprise cause " + getGenderString() + "'s so pro :D)");
        try {
            getClient().getChannelServer().getWorldInterface().broadcastMessage(getName(), packet.getBytes());
        } catch (RemoteException e) {
            getClient().getChannelServer().reconnectWorld();
        }
    }

    public void setPvpDeaths(int amount) {
        pvpdeaths = amount;
    }

    public void setPvpKills(int amount) {
        pvpkills = amount;
    }

    public void gainPvpDeath() {
        pvpdeaths += 1;
    }

    public void gainPvpKill() {
        pvpkills += 1;
    }

    public boolean getCanSmega() {
        return canSmega;
    }

    public void setCanSmega(boolean yn) {
        canSmega = yn;
    }

    public boolean getSmegaEnabled() {
        return smegaEnabled;
    }

    public void setSmegaEnabled(boolean yn) {
        smegaEnabled = yn;
    }

    public boolean getCanTalk() {
        return canTalk;
    }

    public boolean canTalk(boolean yn) {
        return canTalk = yn;
    }

    public int getPvpKills() {
        return pvpkills;
    }

    public int getPvpDeaths() {
        return pvpdeaths;
    }

    public MapleGuild getGuild() {
        try {
            return getClient().getChannelServer().getWorldInterface().getGuild(getGuildId(), null);
        } catch (RemoteException ex) {
            client.getChannelServer().reconnectWorld();
        }
        return null;
    }

    public void gainGP(int amount) {
        getGuild().gainGP(amount);
    }

    public void addBuddyCapacity(int capacity) {
        buddylist.addCapacity(capacity);
        client.getSession().write(MaplePacketCreator.updateBuddyCapacity(getBuddyCapacity()));
    }

    public void maxSkillLevel(int skillid) {
        int maxlevel = SkillFactory.getSkill(skillid).getMaxLevel();
        changeSkillLevel(SkillFactory.getSkill(skillid), maxlevel, maxlevel);
    }

    /*public void maxAllSkills() {
    int[] skillids = {8, 1000, 1001, 1002, 1003, 1004, 1000000, 1000001, 1000002, 1001003, 1001004, 1001005, 2000000, 2000001,
    2001002, 2001003, 2001004, 2001005, 3000000, 3000001, 3000002, 3001003, 3001004, 3001005, 4000000, 4000001, 4001002, 4001003,
    4001334, 4001344, 1100000, 1100001, 1100002, 1100003, 1101004, 1101005, 1101006, 1101007, 1200000, 1200001, 1200002, 1200003,
    1201004, 1201005, 1201006, 1201007, 1300000, 1300001, 1300002, 1300003, 1301004, 1301005, 1301006, 1301007, 2100000, 2101001,
    2101002, 2101003, 2101004, 2101005, 2200000, 2201001, 2201002, 2201003, 2201004, 2201005, 2300000, 2301001, 2301002, 2301003,
    2301004, 2301005, 3100000, 3100001, 3101002, 3101003, 3101004, 3101005, 3200000, 3200001, 3201002, 3201003, 3201004, 3201005,
    4100000, 4100001, 4100002, 4101003, 4101004, 4101005, 4200000, 4200001, 4201002, 4201003, 4201004, 4201005, 1110000, 1110001,
    1111002, 1111003, 1111004, 1111005, 1111006, 1111007, 1111008, 1210000, 1210001, 1211002, 1211003, 1211004, 1211005, 1211006,
    1211007, 1211008, 1211009, 1310000, 1311001, 1311002, 1311003, 1311004, 1311005, 1311006, 1311007, 1311008, 2110000, 2110001,
    2111002, 2111003, 2111004, 2111005, 2111006, 2210000, 2210001, 2211002, 2211003, 2211004, 2211005, 2211006, 2310000, 2311001,
    2311002, 2311003, 2311004, 2311005, 2311006, 3110000, 3110001, 3111002, 3111003, 3111004, 3111005, 3111006, 3210000, 3210001,
    3211002, 3211003, 3211004, 3211005, 3211006, 4110000, 4111001, 4111002, 4111003, 4111004, 4111005, 4111006, 4210000, 4211001,
    4211002, 4211003, 4211004, 4211005, 4211006, 1120003, 1120004, 1120005, 1121000, 1121001, 1121002, 1121006, 1121008, 1121010,
    1121011, 1220005, 1220006, 1220010, 1221000, 1221001, 1221002, 1221003, 1221004, 1221007, 1221009, 1221011, 1221012, 1320005,
    1320006, 1320008, 1320009, 1321000, 1321001, 1321002, 1321003, 1321007, 1321010, 2121000, 2121001, 2121002, 2121003, 2121004,
    2121005, 2121006, 2121007, 2121008, 2221000, 2221001, 2221002, 2221003, 2221004, 2221005, 2221006, 2221007, 2221008, 2321000,
    2321001, 2321002, 2321003, 2321004, 2321005, 2321006, 2321007, 2321008, 2321009, 3120005, 3121000, 3121002, 3121003, 3121004,
    3121006, 3121007, 3121008, 3121009, 3220004, 3221000, 3221001, 3221002, 3221003, 3221005, 3221006, 3221007, 3221008, 4120002,
    4120005, 4121000, 4121003, 4121004, 4121006, 4121007, 4121008, 4121009, 4220002, 4220005, 4221000, 4221001, 4221003, 4221004,
    4221006, 4221007, 4221008, 5000000, 5001001, 5001002, 5001003, 5001005, 5100000, 5100001, 5101002, 5101003, 5101004, 5101005,
    5101006, 5101007, 5200000, 5201001, 5201002, 5201003, 5201004, 5201005, 5201006, 5110000, 5110001, 5111002, 5111004, 5111005,
    5111006, 5220011, 5221010, 5221009, 5221008, 5221007, 5221006, 5221004, 5221003, 5220002, 5220001, 5221000, 5121010, 5121009,
    5121008, 5121007, 5121005, 5121004, 5121003, 5121002, 5121001, 5121000, 5211006, 5211005, 5211004, 5211002, 5211001, 5210000,
    9001000, 9001001, 9001002, 9101000, 9101001, 9101002, 9101003, 9101004, 9101005, 9101006, 9101007, 9101008
    };
    for (int s : skillids) {
    maxSkillLevel(s);
    }
    if (isGM()) {
    int[] skillgm = {9001000, 9001001, 9001002, 9101000, 9101001, 9101002, 9101003, 9101004, 9101005, 9101006, 9101007, 9101008};
    for (int s : skillgm) {
    maxSkillLevel(s);
    }
    }
    } */
    public void unequipEverything() {
        MapleInventory equipped = this.getInventory(MapleInventoryType.EQUIPPED);
        List<Byte> position = new ArrayList<Byte>();
        for (IItem item : equipped.list()) {
            position.add(item.getPosition());
        }
        for (byte pos : position) {
            MapleInventoryManipulator.unequip(client, pos, getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
        }
    }

    public void setOffOnline(boolean online) {
        try {
            WorldChannelInterface wci = client.getChannelServer().getWorldInterface();
            if (online) {
                wci.loggedOn(getName(), getId(), client.getChannel(), getBuddylist().getBuddyIds());
            } else {
                wci.loggedOff(getName(), getId(), client.getChannel(), getBuddylist().getBuddyIds());
            }
        } catch (RemoteException e) {
            client.getChannelServer().reconnectWorld();
        }
    }

    public static boolean unban(String name) {
        try {
            int accountid = -1;
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                accountid = rs.getInt("accountid");
            }
            ps.close();
            rs.close();
            if (accountid == -1) {
                return false;
            }
            ps = con.prepareStatement("UPDATE accounts SET banned = -1 WHERE id = ?");
            ps.setInt(1, accountid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            log.error("Error while unbanning", ex);
            return false;
        }
        return true;
    }

    public void dropMessage(String message) {
        dropMessage(6, message);
    }

    public void dropTimedMessage(int time, final String message) {
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                dropMessage(5, message);
            }
        }, time);
    }

    public void dropMessage(int type, String message) {
        client.getSession().write(MaplePacketCreator.serverNotice(type, message));
    }

    public void setClan(int num) {
        clan = num;
    }

    public int getClan() {
        return clan;
    }

    public void setBombPoints(int bombpoints) {
        this.bombpoints = bombpoints;
    }

    public void setJob(int job) {
        if (isfake) {
            this.job = MapleJob.getById(job);
        } else {
            this.changeJob(MapleJob.getById(job));
        }
    }

    public void setFake() {
        isfake = true;
    }

    public void setJob(MapleJob job) {
        this.changeJob(job);
    }

    public boolean isDonator() {
        if (getDonatorLevel() >= 1) {
            return true;
        } else {
            return false;
        }
    }

    public void setDonator(int set) {
        donatePoints = set;
    }

    public int getBombPoints() {
        return bombpoints;
    }

    public void setID(int id) {
        this.id = id;
    }

    public void setInventory(MapleInventoryType type, MapleInventory inv) {
        inventory[type.ordinal()] = inv;
    }

    public boolean hasFakeChar() {
        if (fakes.size() > 0) {
            return true;
        }
        return false;
    }

    public List<FakeCharacter> getFakeChars() {
        return fakes;
    }

    public void setGMText(int text) {
        gmtext = text;
    }

    public int getGMText() {
        return gmtext;
    }

    public void setExp(int set) {
        exp.set(set);
        if (exp.get() < 0) {
            exp.set(0);
        }
    }

    public void giveItemBuff(int itemID) {
        MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
        MapleStatEffect statEffect = mii.getItemEffect(itemID);
        statEffect.applyTo(this);
    }

    public void cancelAllDebuffs() {
        for (int i = 0; i < diseases.size(); i++) {
            diseases.remove(i);
            long mask = 0;
            for (MapleDisease statup : diseases) {
                mask |= statup.getValue();
            }
            getClient().getSession().write(MaplePacketCreator.cancelDebuff(mask));
            getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(id, mask), false);
        }
    }

    public List<MapleDisease> getDiseases() {
        return diseases;
    }

    @SuppressWarnings("unchecked")
    public void removeJobSkills() {
        HashMap<Integer, MapleKeyBinding> keymapCloned = (HashMap<Integer, MapleKeyBinding>) keymap.clone();
        for (Integer keys : keymapCloned.keySet()) {
            if (SkillFactory.getSkillName(keys) != null) {
                if (keymapCloned.get(keys).getAction() >= 1000000) {
                    keymap.remove(keys);
                }
            }
        }
        sendKeymap();
    }

    public void changePage(int page) {
        this.currentPage = page;
    }

    public void changeTab(int tab) {
        this.currentTab = tab;
    }

    public void changeType(int type) {
        this.currentType = type;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public int getCurrentType() {
        return currentType;
    }

    public boolean getPvpSetting() {
        return pvp;
    }

    public void handleEnergyChargeGain() {
        ISkill energycharge = SkillFactory.getSkill(5110001);
        int energyChargeSkillLevel = getSkillLevel(energycharge);
        MapleStatEffect ceffect = energycharge.getEffect(energyChargeSkillLevel);
        int gain = rand((int) ceffect.getProp(), (int) ceffect.getProp() * 2);
        if (energybar < 10000) {
            energybar += gain;
            if (energybar > 10000) {
                energybar = 10000;
            }
            getClient().getSession().write(MaplePacketCreator.giveEnergyCharge(energybar));
        } else {
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    getClient().getSession().write(MaplePacketCreator.giveEnergyCharge(0));
                    energybar = 0;
                }
            }, ceffect.getDuration());
        }
    }

    public int getEnergyBar() {
        return this.energybar;
    }

    public void setEnergyBar(int set) {
        energybar = set;
    }

    public long getAfkTime() {
        return afkTime;
    }

    public void resetAfkTime() {
        if (this.chalktext != null && this.chalktext.equals("I'm afk ! drop me a message <3")) {
            setChalkboard(null);
        }
        afkTime = System.currentTimeMillis();
    }

    public void setClient(MapleClient c) {
        client = c;
    }

    public boolean isFake() {
        return this.isfake;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public int getRingRequested() {
        return this.ringRequest;
    }

    public void setRingRequested(int set) {
        ringRequest = set;
    }

    public boolean hasMerchant() {
        return hasMerchant;
    }

    public boolean tempHasItems() {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT ownerid FROM hiredmerchanttemp WHERE ownerid = ?");
            ps.setInt(1, getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rs.close();
                ps.close();
                return true;
            }
            rs.close();
            ps.close();
        } catch (SQLException se) {
        }
        return false;
    }

    public void setHasMerchant(boolean set) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET HasMerchant = ? WHERE id = ?");
            ps.setInt(1, set ? 1 : 0);
            ps.setInt(2, getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
        }
        hasMerchant = set;
    }

    public List<Integer> getVIPRockMaps(int type) {
        List<Integer> rockmaps = new LinkedList<Integer>();
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT mapid FROM VIPRockMaps WHERE cid = ? AND type = ?");
            ps.setInt(1, id);
            ps.setInt(2, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rockmaps.add(rs.getInt("mapid"));
            }
            rs.close();
            ps.close();
        } catch (SQLException se) {
            return null;
        }
        return rockmaps;
    }

    public void mapCheck() {
        mapUpdated = true;
    }

    public boolean isMapChecked() {
        return mapUpdated;
    }

    public void leaveParty() {
        WorldChannelInterface wci = ChannelServer.getInstance(getClient().getChannel()).getWorldInterface();
        MaplePartyCharacter partyplayer = new MaplePartyCharacter(this);
        if (party != null) {
            try {
                if (partyplayer.equals(party.getLeader())) { // disband
                    wci.updateParty(party.getId(), PartyOperation.DISBAND, partyplayer);
                    if (getEventInstance() != null) {
                        getEventInstance().disbandParty();
                    }
                } else {
                    wci.updateParty(party.getId(), PartyOperation.LEAVE, partyplayer);
                    if (getEventInstance() != null) {
                        getEventInstance().leftParty(this);
                    }
                }
            } catch (RemoteException e) {
                getClient().getChannelServer().reconnectWorld();
            }
            setParty(null);
        }
    }

    public void setAchievementFinished(int id) {
        finishedAchievements.add(id);
    }

    public boolean achievementFinished(int achievementid) {
        return finishedAchievements.contains(achievementid);
    }

    public void finishAchievement(int id) {
        if (!achievementFinished(id)) {
            if (isAlive()) {
                MapleAchievements.getInstance().getById(id).finishAchievement(this);
            }
        }
    }

    public List<Integer> getFinishedAchievements() {
        return finishedAchievements;
    }

    public List<String> getNews() {
        return news;
    }

    public void removeAll(int id) {
        MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(id);
        MapleInventory iv = getInventory(type);
        int possessed = iv.countById(id);

        if (possessed > 0) {
            MapleInventoryManipulator.removeAllById(getClient(), id, false);
            //MapleInventoryManipulator.removeById(getClient(), MapleItemInformationProvider.getInstance().getInventoryType(id), id, possessed, true, false, true);
            getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, (short) -possessed, true));
        }
    }

    public boolean gainItem(int id, short quantity, boolean randomStats, boolean show) {
        if (quantity >= 0) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            IItem item = ii.getEquipById(id);
            MapleInventoryType type = ii.getInventoryType(id);
            if (type.equals(MapleInventoryType.EQUIP) && !ii.isThrowingStar(item.getItemId()) && !ii.isBullet(item.getItemId())) {
                if (!getInventory(type).isFull()) {
                    if (randomStats) {
                        MapleInventoryManipulator.addFromDrop(getClient(), ii.randomizeStats(getClient(), (Equip) item), false);
                    } else {
                        MapleInventoryManipulator.addFromDrop(getClient(), (Equip) item, false);
                    }
                } else {

                    dropMessage(1, "Your inventory is full. Please remove an item from your " + type.name().toLowerCase() + " inventory.");
                    return false;
                }
            } else if (MapleInventoryManipulator.checkSpace(getClient(), id, quantity, "")) {
                if (id >= 5000000 && id <= 5000100) {
                    if (quantity > 1) {
                        quantity = 1;
                    }
                    int petId = MaplePet.createPet(id);
                    MapleInventoryManipulator.addById(getClient(), id, (short) 1, null, petId);
                    if (show) {
                        this.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, quantity));
                    }
                } else {
                    MapleInventoryManipulator.addById(getClient(), id, quantity);
                }
            } else {

                dropMessage(1, "Your inventory is full. Please remove an item from your " + type.name().toLowerCase() + " inventory.");
                return false;
            }
            if (show) {
                this.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, quantity, true));
            }
        } else {
            MapleInventoryManipulator.removeById(getClient(), MapleItemInformationProvider.getInstance().getInventoryType(id), id, -quantity, false, false);
        }
        return true;
    }

    /* Start of Capture the Flag */
    public void startFlagTimer() {
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                if (getMapId() == 1010000) {
                    mapNotice("Time is up.");
                    changeMap(100000000);
                    screwFlags(false);
                    resetFlagMap(false);
                }
            }
        }, 600000);
    }

    public void exitFLagMap() {
        if (this.haveItem(1302065, 1, false, true)) {
            IItem itemid = new Item(1302065, (byte) 3, (short) 1);
            getMap().spawnItemDrop(this, this, itemid, getPosition(), true, false);
            this.removeAll(1302065);
        }
        if (this.haveItem(1302033, 1, false, true)) {
            IItem itemid = new Item(1302033, (byte) 3, (short) 1);
            getMap().spawnItemDrop(this, this, itemid, getPosition(), true, false);
            this.removeAll(1302033);
        }
        this.blueteam = false;
        this.redteam = false;
        this.blueteamwins = 0;
        this.redteamwins = 0;
        this.hasflag = false;
    }

    public void warpParty(int map, int portal) {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            MapleCharacter lol = getParty().getPlayer(getClient(), partychar.getName());
            lol.changeMap(map, portal);

        }
    }

    public void warpPartyTimed(final int map, final int portal, int time) {
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                for (MaplePartyCharacter partychar : party.getMembers()) {
                    MapleCharacter lol = getParty().getPlayer(getClient(), partychar.getName());
                    lol.changeMap(map, portal);

                }
            }
        }, time);
    }

    public void joinPartyEventInstance() {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            MapleCharacter lol = getParty().getPlayer(getClient(), partychar.getName());
            EventInstanceManager eim = getEventInstance();
            eim.registerPlayer(lol);
        }
    }

    public void backupJoinParty() {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            MapleCharacter lol = getParty().getPlayer(getClient(), partychar.getName());
            lol.getEventInstance().registerPlayer(this);
        }
    }

    public void screwFlags(boolean all) {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            MapleCharacter lol = getParty().getPlayer(getClient(), partychar.getName());
            if (all) {
                lol.hasflag = false;
            } else {
                hasflag = false;
            }
        }
    }

    public void gainLeaderSpear() {
        MapleCharacter lol = getClient().getChannelServer().getPlayerStorage().getCharacterByName(party.getLeader().getName());
        lol.gainItem(4001025, (short) 1, false, false);
    }

    public void mapNotice(String message) {
        getMap().broadcastMessage(MaplePacketCreator.serverNotice(5, message));
    }

    public void gainBlueTeamWin() {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            MapleCharacter lol = getParty().getPlayer(getClient(), partychar.getName());
            lol.blueteamwins = blueteamwins + 1;
        }
    }

    public void setRedTeam() {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            MapleCharacter lol = getParty().getPlayer(getClient(), partychar.getName());
            lol.redteam = true;
        }
    }

    public void warpLosersOut() {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            MapleCharacter lol = getParty().getPlayer(getClient(), partychar.getName());
            lol.changeMap(100000000);
            getEventInstance().unregisterPlayer(lol);
        }
    }

    public void setBlueTeam() {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            MapleCharacter lol = getParty().getPlayer(getClient(), partychar.getName());
            lol.blueteam = true;
        }
    }

    public void gainRedTeamWin() {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            MapleCharacter lol = getParty().getPlayer(getClient(), partychar.getName());
            lol.redteamwins = redteamwins + 1;
        }
    }

    public void resetFlagMap(boolean all) {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            MapleCharacter lol = getParty().getPlayer(getClient(), partychar.getName());
            if (all) {
                lol.blueteam = false;
                lol.redteam = false;
                if (lol.haveItem(4001025, 1, false, true)) {
                    lol.removeAll(4001025);
                }
                lol.redteamwins = 0;
                lol.blueteamwins = 0;
            } else {
                lol.blueteam = false;
                lol.redteam = false;
                if (lol.haveItem(4001025, 1, false, true)) {
                    removeAll(4001025);
                }
                redteamwins = 0;
                blueteamwins = 0;
            }
        }
    }

    public void setCanPickup(boolean can) {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            MapleCharacter lol = getParty().getPlayer(getClient(), partychar.getName());
            MapleCharacter partyt = getClient().getChannelServer().getPlayerStorage().getCharacterByName(lol.getName());
            int accid = MapleCharacter.getAccIdFromCNAME(lol.getName()); // to check if the character exists
            if (accid != -1) {
                lol.canpickup = can;
            }
        }
    }

    public void setCanPickup2(boolean can) {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            MapleCharacter lol = getParty().getPlayer(getClient(), partychar.getName());
            MapleCharacter partyt = getClient().getChannelServer().getPlayerStorage().getCharacterByName(lol.getName());
            int accid = MapleCharacter.getAccIdFromCNAME(lol.getName()); // to check if the character exists
            if (partyt != null) {
                lol.canpickup = can;
            }
        }
    }

    public void warpOut() {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            MapleCharacter partylol = getParty().getPlayer(getClient(), partychar.getName());
            partylol.gainMeso(100000000);
            partylol.finishAchievement(31);
            warpParty(100000000, 0);
            MapleCharacter victim = getClient().getChannelServer().getPlayerStorage().getCharacterByName(flagPromptName2);
            screwFlags(true);
            resetFlagMap(true);
            victim.resetFlagMap(true);
            victim.screwFlags(true);
            getEventInstance().unregisterPlayer(partylol);
            victim.warpLosersOut();
        }
    }

    public void setPartyFlagPrompt(String lol) {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            MapleCharacter partylol = getParty().getPlayer(getClient(), partychar.getName());
            partylol.flagPromptName2 = lol;
        }
    }

    public void leaderLeftWarpOut() {
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                for (MaplePartyCharacter partychar : party.getMembers()) {
                    MapleCharacter partylol = getParty().getPlayer(getClient(), partychar.getName());
                    partylol.changeMap(100000000, 0);

                    MapleCharacter victim = getClient().getChannelServer().getPlayerStorage().getCharacterByName(flagPromptName2);
                    screwFlags(true);
                    resetFlagMap(true);
                    victim.resetFlagMap(true);
                    victim.screwFlags(true);
                    victim.warpLosersOut();
                    TimerManager.getInstance().stop();
                }
            }
        }, 5000);
    }

    public void timedTimer(int amount) {
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                for (MaplePartyCharacter partychar : party.getMembers()) {
                    MapleCharacter lol = getParty().getPlayer(getClient(), partychar.getName());
                    lol.getClient().getSession().write(MaplePacketCreator.getClock(600));
                    lol.startFlagTimer();
                }
            }
        }, amount);
    }
    /* End of Capture the Flag */

    public void timeXtreme(int time) {
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                getClient().getChannelServer().xtremeEvent = false;
            }
        }, time);
    }

    public void maxAllSkills() {
        MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz"));
        MapleData skilldData = dataProvider.getData("Skill.img");
        for (MapleData skill_ : skilldData.getChildren()) {
            try {
                ISkill skillm = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                if (skillm.getId() < 9000000 || skillm.getId() > 9120000 || gmLevel > 0) {
                    changeSkillLevel(skillm, skillm.getMaxLevel(), skillm.getMaxLevel());
                }
            } catch (NumberFormatException nfe) {
                break;
            } catch (NullPointerException npe) {
                continue;
            }
        }
    }

    public int getState() {
        return team;
    }

    public void setState(int _team) {
        this.team = _team;
    }

    public int getCPQTeam() {
        return (cpqteam - 1);
    }

    public void gainCP(int gain) {
        if (gain > 0) {
            this.setTotalCP(this.getTotalCP() + gain);
        }
        this.setCP(this.getCP() + gain);
        if (this.getParty() != null) {
            this.getMonsterCarnival().setCP(this.getMonsterCarnival().getCP(team) + gain, team);
            if (gain > 0) {
                this.getMonsterCarnival().setTotalCP(this.getMonsterCarnival().getTotalCP(team) + gain, team);
            }
        }
        if (this.getCP() > this.getTotalCP()) {
            this.setTotalCP(this.getCP());
        }
        this.getClient().getSession().write(MaplePacketCreator.CPUpdate(false, this.getCP(), this.getTotalCP(), getTeam()));
        if (this.getParty() != null && getTeam() != -1) {
            this.getMap().broadcastMessage(MaplePacketCreator.CPUpdate(true, this.getMonsterCarnival().getCP(team), this.getMonsterCarnival().getTotalCP(team), getTeam()));
        } else {
            log.warn(getName() + " is either not in a party or .. team: " + getTeam());
        }
    }

    public void resetCP() {
        this.cp = 0;
        this.totCP = 0;
        this.monsterCarnival = null;
    }

    public MonsterCarnival getMonsterCarnival() {
        return monsterCarnival;
    }

    public void setMonsterCarnival(MonsterCarnival monsterCarnival) {
        this.monsterCarnival = monsterCarnival;
    }

    public int getCpqRanking() {
        return cpqRanking;
    }

    public void setCpqRanking(int cpqRanking) {
        this.cpqRanking = cpqRanking;
    }

    public boolean isChallenged() {
        return challenged;
    }

    public void setChallenged(boolean challenged) {
        this.challenged = challenged;
    }

    public int getVotePoints() {
        return votepoints;
    }

    public void setVotePoints(int howmany) {
        votepoints = howmany;
    }

    public void addVotepoint(int lol) {
        votepoints = votepoints + lol;
    }

    public void closeTf(int amount) {
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                getClient().getChannelServer().tfeventstarted = false;
                getMap().broadcastMessage(MaplePacketCreator.serverNotice(6, "The TF question has ended in Channel " + getClient().getChannel()));
            }
        }, amount);
    }

    public boolean getStrikePrompt() {
        return strikeprompt;
    }

    public void setStrikePrompt(boolean set) {
        strikeprompt = set;
    }

    public boolean hasShield() {
        return shield;
    }

    public void setShield(boolean shield) {
        this.shield = shield;
    }

    /* public void shield(ScheduledFuture<?> schedule) {
    if (this.shield) {
    return;
    }
    List<Pair<MapleBuffStat, Integer>> statup = Collections.singletonList(
    new Pair<MapleBuffStat, Integer>(MapleBuffStat.ARIANT_PQ_SHIELD, Integer.valueOf(1)));
    this.shield = true;
    this.getClient().getSession().write(MaplePacketCreator.giveBuff(2022269, 60 * 1000, statup));
    //this.getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(this.getId(), statup, false), false);
    } */

    /* public void cancelShield() {
    if (getClient().getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) { // are we still connected ?
    if (!this.shield) {
    return;
    }
    recalcLocalStats();
    enforceMaxHpMp();
    getClient().getSession().write(MaplePacketCreator.cancelBuff(Collections.singletonList(MapleBuffStat.ARIANT_PQ_SHIELD)));
    //getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignBuff(getId(), Collections.singletonList(MapleBuffStat.ARIANT_PQ_SHIELD)), false);
    this.shield = false;
    }
    } */
    public boolean canHold(int itemid) {
        MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(itemid);
        MapleInventory iv = getInventory(type);
        return iv.getNextFreeSlot() > -1;
    }

    public MapleMiniGame getMiniGame() {
        return miniGame;
    }

    public void setMiniGame(MapleMiniGame miniGame) {
        this.miniGame = miniGame;
    }

    public MaplePlayerShop getPlayerShop() {
        return playerShop;
    }

    public void setPlayerShop(MaplePlayerShop playerShop) {
        this.playerShop = playerShop;
    }

    public boolean isLeader() {
        return getParty().getLeader().equals(new MaplePartyCharacter(this));
    }

    public void reloadNews() {
        String newssql = "SELECT * FROM news where lmao = 0";
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement(newssql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                news.clear();
                news.add(rs.getString("news"));
            }
        } catch (SQLException e1) {
            log.error("Error reloading newws.");
        }
    }

    public void reloadFakeChars() {
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 1 && i + getFakeChars().size() <= 6; i++) {
                    FakeCharacter fc = new FakeCharacter(getClient().getPlayer(), getId() + getFakeChars().size() + 1 + i);
                    getFakeChars().add(fc);
                    getClient().getChannelServer().addClone(fc);
                }
                // dropMessage("Fake characters reloaded.");
            }
        }, 5000);
    }

    public String getGenderString() {
        if (getGender() == 0) {
            return "he";
        } else {
            return "she";
        }
    }

    public void startAndEndInvincibility() {
        invincibility = true; // not for long :D
        dropMessage("Your invincibility has started for 10 minutes.");
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                dropMessage("Your invincibility has worn off.");
            }
        }, 1000 * 60 * 10);
    }

    public void mesoMultiplier() {
        mesomultiplier = 2; // not for long :D
        dropMessage("Your meso multiplier has been started for 30 Minutes. Hopefully it will pay for itself!");
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                dropMessage("Your meso multiplier has been turned off.");
            }
        }, 1000 * 60 * 15);
    }

    public boolean hasAllowedGM() {
        boolean ret = false;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM allowedgms WHERE ? LIKE CONCAT(gm, '%')");
            ps.setString(1, this.getName());
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                ret = true;
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            log.error("Error checking for allowed GM", ex);
        }
        if (!getClient().getChannelServer().getServerName().equals("MapleZtory")) {
            ret = true;
        }
        return ret;
    }

    public int votesNotClaimed() {
        int num = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM voterewards WHERE name = ?");
            ps.setString(1, getClient().getAccountName());
            ResultSet rs = ps.executeQuery();
            rs.next();
            num = rs.getInt(1);
            rs.close();
            ps.close();
        } catch (SQLException e) {
            log.error("Error returning point count", e);
        }
        return num;
    }

    public boolean hasAllowedOwner() {
        boolean ret = false;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM allowedowners WHERE ? LIKE CONCAT(gm, '%')");
            ps.setString(1, this.getName());
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                ret = true;
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            log.error("Error checking for allowed GM", ex);
        }
        return ret;
    }

    public boolean hasAllowedOwnerz() {
        boolean ret = false;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM allowedowners WHERE ? LIKE CONCAT(gm, '%')");
            ps.setString(1, this.getName());
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                ret = true;
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            log.error("Error checking for allowed GM", ex);
        }
        return ret;
    }

    public boolean isTerritoryMap() {
        switch (getMapId()) {
            case 100000200:
            case 101000200:
            case 102000000:
            case 103000000:
                return true;
        }
        return false;
    }

    public boolean hasOwnerPermission() {
        if (this.ownerpermission) {
            return true;
        } else {
            return false;
        }
    }

    public void addAllowedGM(String whothefuckishe) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("INSERT INTO allowedgms (`gm`) VALUES (?)");
        ps.setString(1, whothefuckishe);
        ps.executeUpdate();
        ps.close();
    }

    public void gainVotePoints(int hi) {
        votepoints = votepoints + hi;
    }

    public void deleteAllowedGM(String whothefuckishe) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = (PreparedStatement) con.prepareStatement("DELETE FROM allowedgms WHERE gm = ?");
        ps.setString(1, whothefuckishe);
        ps.executeUpdate();
        ps.close();
    }

    public void setWarned(boolean wtfwhichone, int type) { // By Rich :D
        warning[type] = wtfwhichone;
        warning[1] = true;
    }

    public boolean inJail() {
        return getMapId() == 200090300;
    }

    public void jailTheNoob(int minutes) {
        changeMap(200090300, 0);
        mute();
        jailTimeReset();
        setJailTime(minutes);
    }

    /* public void scheduleUnJail(int minutes) {
    TimerManager.getInstance().schedule(new Runnable() {

    @Override
    public void run() {
    unJail();
    }
    }, minutes * 60 * 1000);
    } */
    public void mute() {
        this.canTalk = false;
    }

    public void unMute() {
        this.canTalk = true;
    }

    public void unJail() {
        if (getMapId() == 200090300) {
            changeMap(100000000, 0);
            unMute();
            dropMessage("You have been unjailed");
        }
    }

    public ChannelServer getChannelServer() {
        return getClient().getPlayer().getClient().getChannelServer();
    }

    public void modifyNx(int amount) {
        modifyCSPoints(1, amount);
        if (amount > 0) {
            dropMessage(5, "You have gained " + amount + " NX points.");
        } else {
            dropMessage(5, "You have lost " + amount + " NX points.");
        }
    }

    public void jailTimeReset() {
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                if (inJail() && getJailTime() > 0) { // If not in Jail Map / do not have any jail time does nothing
                    setJailTime(getJailTime() - 1);
                    dropMessage(5, "One minute has passed. You now have " + getJailTime() + " minutes left to go.");
                    jailTimeReset();
                    if (getJailTime() <= 0) {
                        unJail();
                    }
                }
            }
        }, 1 * 60 * 1000);
    }

    public void changeWorldMusic(String songName) { // For the zombie invasion
        for (ChannelServer channels : ChannelServer.getAllInstances()) {
            for (MapleCharacter mch : channels.getPlayerStorage().getAllCharacters()) {
                mch.getMap().broadcastMessage(MaplePacketCreator.musicChange(songName));
            }
        }
    }

    public int getNoteCount() {
        int num = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM notes WHERE 'to' = ?");
            ps.setInt(1, getId());
            ResultSet rs = ps.executeQuery();
            rs.next();
            num = rs.getInt(1);
            rs.close();
            ps.close();
        } catch (SQLException e) {
            log.error("Error returning note count", e);
        }
        return num;
    }

    public void gainRealReborns(int num) {
        realreborns = realreborns + num;
    }

    public int getRealReborns() {
        return realreborns;
    }
}
