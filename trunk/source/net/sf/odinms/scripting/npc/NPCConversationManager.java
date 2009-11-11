package net.sf.odinms.scripting.npc;

import java.awt.Point;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleJob;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.scripting.AbstractPlayerInteraction;
import net.sf.odinms.scripting.event.EventManager;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MapleShopFactory;
import net.sf.odinms.server.quest.MapleQuest;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.net.world.guild.MapleGuild;
import net.sf.odinms.server.MapleSquad;
import net.sf.odinms.server.MapleSquadType;
import net.sf.odinms.server.maps.MapleMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import net.sf.odinms.client.Equip;
import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.MapleKeyBinding;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.net.world.guild.TerritoryStorage;
import net.sf.odinms.net.world.remote.WorldRegistry;
import net.sf.odinms.server.ClanHolder;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.MapleLottery;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.server.MapleStatEffect;
import net.sf.odinms.server.MonsterCarnival;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.server.life.MapleLifeFactory;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.life.MapleMonsterStats;
import net.sf.odinms.server.life.MapleNPC;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.server.maps.MapleMapObjectType;
import net.sf.odinms.tools.Pair;

/**
 *
 * @author Matze
 */
public class NPCConversationManager extends AbstractPlayerInteraction {

    private MapleClient c;
    private int npc;
    private String fileName = null;
    private String getText;
    private List<MaplePartyCharacter> otherParty;
    private MapleCharacter chr;

    public NPCConversationManager(MapleClient c, int npc, MapleCharacter chr, String fileName) {
        super(c);
        this.c = c;
        this.npc = npc;
        this.chr = chr;
        this.fileName = fileName;
    }

    public NPCConversationManager(MapleClient c, int npc, List<MaplePartyCharacter> otherParty, int b) {
        super(c);
        this.c = c;
        this.npc = npc;
        this.otherParty = otherParty;
    }

    public void dispose() {
        NPCScriptManager.getInstance().dispose(this);
    }

    public void sendNext(String text) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01"));
    }

    public void sendPrev(String text) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00"));
    }

    public void sendNextPrev(String text) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01"));
    }

    public void sendOk(String text) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00"));
    }

    public void sendTimedOk(int time, final String text) {
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00"));
            }
        }, time); // miliseconds
    }

    public void sendYesNo(String text) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 1, text, ""));
    }

    public void sendAcceptDecline(String text) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0x0C, text, ""));
    }

    public void sendSimple(String text) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 4, text, ""));
    }

    public void sendStyle(String text, int styles[]) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalkStyle(npc, text, styles));
    }

    public void sendGetNumber(String text, int def, int min, int max) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalkNum(npc, text, def, min, max));
    }

    public void sendGetText(String text) {
        getClient().getSession().write(MaplePacketCreator.getNPCTalkText(npc, text));
    }

    public void setGetText(String text) {
        this.getText = text;
    }

    public String getText() {
        return this.getText;
    }

    public void openShop(int id) {
        MapleShopFactory.getInstance().getShop(id).sendShop(getClient());
    }

    @Override
    public void openNpc(int id) {
        dispose();
        NPCScriptManager.getInstance().start(getClient(), id);
    }

    public void changeJob(MapleJob job) {
        getPlayer().changeJob(job);
    }

    public MapleJob getJob() {
        return getPlayer().getJob();
    }

    public void startQuest(int id) {
        MapleQuest.getInstance(id).start(getPlayer(), npc);
    }

    public void completeQuest(int id) {
        MapleQuest.getInstance(id).complete(getPlayer(), npc);
    }

    public void forfeitQuest(int id) {
        MapleQuest.getInstance(id).forfeit(getPlayer());
    }

    /**
     * use getPlayer().getMeso() instead
     * @return
     */
    @Deprecated
    public int getMeso() {
        return getPlayer().getMeso();
    }

    public void gainMeso(int gain) {
        getPlayer().gainMeso(gain, true, false, true);
    }

    public void gainExp(int gain) {
        getPlayer().gainExp(gain, true, true);
    }

    public int getNpc() {
        return npc;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * use getPlayer().getLevel() instead
     * @return
     */
    @Deprecated
    public int getLevel() {
        return getPlayer().getLevel();
    }

    public void unequipEverything() {
        getPlayer().unequipEverything();
    }

    public void teachSkill(int id, int level, int masterlevel) {
        getPlayer().changeSkillLevel(SkillFactory.getSkill(id), level, masterlevel);
    }

    public void clearSkills() {
        Map<ISkill, MapleCharacter.SkillEntry> skills = getPlayer().getSkills();
        for (Entry<ISkill, MapleCharacter.SkillEntry> skill : skills.entrySet()) {
            getPlayer().changeSkillLevel(skill.getKey(), 0, 0);
        }
    }

    /**
     * Use getPlayer() instead (for consistency with MapleClient)
     * @return
     */
    @Deprecated
    public MapleCharacter getChar() {
        return getPlayer();
    }

    public MapleClient getC() {
        return getClient();
    }

    public void rechargeStars() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        IItem stars = getChar().getInventory(MapleInventoryType.USE).getItem((byte) 1);
        if (ii.isThrowingStar(stars.getItemId()) || ii.isBullet(stars.getItemId())) {
            stars.setQuantity(ii.getSlotMax(getClient(), stars.getItemId()));
            getC().getSession().write(MaplePacketCreator.updateInventorySlot(MapleInventoryType.USE, (Item) stars));
        }
    }

    public EventManager getEventManager(String event) {
        return getClient().getChannelServer().getEventSM().getEventManager(event);
    }

    public void showEffect(String effect) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.showEffect(effect));
    }

    public void playSound(String sound) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.playSound(sound));
    }

    @Override
    public String toString() {
        return "Conversation with NPC: " + npc;
    }

    public void updateBuddyCapacity(int capacity) {
        getPlayer().setBuddyCapacity(capacity);
    }

    public int getBuddyCapacity() {
        return getPlayer().getBuddyCapacity();
    }

    public void setHair(int hair) {
        getPlayer().setHair(hair);
        getPlayer().updateSingleStat(MapleStat.HAIR, hair);
        getPlayer().equipChanged();
    }

    public void setFace(int face) {
        getPlayer().setFace(face);
        getPlayer().updateSingleStat(MapleStat.FACE, face);
        getPlayer().equipChanged();
    }

    @SuppressWarnings("static-access")
    public void setSkin(int color) {
        getPlayer().setSkinColor(c.getPlayer().getSkinColor().getById(color));
        getPlayer().updateSingleStat(MapleStat.SKIN, color);
        getPlayer().equipChanged();
    }

    public void warpParty(int mapId) {
        warpParty(mapId, 0, 0);
    }

    public void warpParty(int mapId, int exp, int meso) {
        for (MaplePartyCharacter chr_ : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chr_.getName());
            if ((curChar.getEventInstance() == null && c.getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance()) {
                curChar.changeMap(mapId);
                if (exp > 0) {
                    curChar.gainExp(exp, true, false, true);
                }
                if (meso > 0) {
                    curChar.gainMeso(meso, true);
                }
            }
        }
    }

    public void warpRandom(int mapid) {
        MapleMap target = c.getChannelServer().getMapFactory().getMap(mapid);
        Random rand = new Random();
        MaplePortal portal = target.getPortal(rand.nextInt(target.getPortals().size())); //generate random portal
        getPlayer().changeMap(target, portal);
    }

    public int itemQuantity(int itemid) {
        int possesed = getPlayer().getItemQuantity(itemid, false);
        return possesed;
    }

    public MapleSquad createMapleSquad(MapleSquadType type) {
        MapleSquad squad = new MapleSquad(c.getChannel(), getPlayer());
        if (getSquadState(type) == 0) {
            c.getChannelServer().addMapleSquad(squad, type);
        } else {
            return null;
        }
        return squad;
    }

    public MapleCharacter getSquadMember(MapleSquadType type, int index) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        MapleCharacter ret = null;
        if (squad != null) {
            ret = squad.getMembers().get(index);
        }
        return ret;
    }

    public int getSquadState(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            return squad.getStatus();
        } else {
            return 0;
        }
    }

    public void setSquadState(MapleSquadType type, int state) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.setStatus(state);
        }
    }

    public boolean checkSquadLeader(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            if (squad.getLeader().getId() == getPlayer().getId()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void removeMapleSquad(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            if (squad.getLeader().getId() == getPlayer().getId()) {
                squad.clear();
                c.getChannelServer().removeMapleSquad(squad, type);
            }
        }
    }

    public int numSquadMembers(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        int ret = 0;
        if (squad != null) {
            ret = squad.getSquadSize();
        }
        return ret;
    }

    public boolean isSquadMember(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        boolean ret = false;
        if (squad.containsMember(getPlayer())) {
            ret = true;
        }
        return ret;
    }

    public void addSquadMember(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.addMember(getPlayer());
        }
    }

    public void removeSquadMember(MapleSquadType type, MapleCharacter chr, boolean ban) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.banMember(chr, ban);
        }
    }

    public void removeSquadMember(MapleSquadType type, int index, boolean ban) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            MapleCharacter chhr = squad.getMembers().get(index);
            squad.banMember(chhr, ban);
        }
    }

    public boolean canAddSquadMember(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            if (squad.isBanned(getPlayer())) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public void warpSquadMembers(MapleSquadType type, int mapId) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        MapleMap map = c.getChannelServer().getMapFactory().getMap(mapId);
        if (squad != null) {
            if (checkSquadLeader(type)) {
                for (MapleCharacter chhr : squad.getMembers()) {
                    chhr.changeMap(map, map.getPortal(0));
                }
            }
        }
    }

    public String searchItem(String item) {
        StringBuilder message = new StringBuilder("Choose the item you want:");
        getPlayer().getMap().broadcastMessage(getPlayer(), MaplePacketCreator.showJobChange(getPlayer().getId()), false);
        for (Pair<Integer, String> itemPair : MapleItemInformationProvider.getInstance().getAllItems()) {
            if (itemPair.getRight().toLowerCase().contains(item.toLowerCase())) {
                message.append("\r\n#L" + itemPair.getLeft() + "##i" + itemPair.getLeft() + "# - #b" + itemPair.getRight() + "#k#l");
            }
        }
        if (!message.toString().contains("#L")) {
            return "No Item's Found";
        }
        return message.toString();
    }

    public void resetReactors() {
        getPlayer().getMap().resetReactors();
    }

    public void displayGuildRanks() {
        MapleGuild.displayGuildRanks(getClient(), npc);
    }

    public boolean sendMessage(String recipient, String message) {
        MapleCharacter chr_ = getCharByName(recipient);
        if (chr_ != null) {
            chr_.dropMessage(6, getPlayer().getName() + ": " + message);
            return true;
        }
        return false;
    }

    public void gainFame(int amount) {
        getPlayer().addFame(amount);
        if (amount > 0) {
            getPlayer().dropMessage(1, "You have gained " + amount + " fame.");
        } else {
            getPlayer().dropMessage(1, "You have lost " + amount + " fame.");
        }
    }

    public void maxSkills() {
        getPlayer().maxAllSkills();
    }

    public int getSkillLevel(int skillid) {
        int skilllevel = getPlayer().getSkillLevel(SkillFactory.getSkill(skillid));
        return skilllevel;
    }

    public void giveBuff(int skillid) {
        MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
        MapleStatEffect statEffect = mii.getItemEffect(skillid);
        statEffect.applyTo(getPlayer());
    }

    public int getMkills() {
        return getPlayer().getMkills();
    }

    public void setMkills(int amount) {
        getPlayer().setMkills(amount);
    }

    public int partyMembersInMap() {
        int inMap = 0;
        for (MapleCharacter char2 : getPlayer().getMap().getCharacters()) {
            if (char2.getParty() == getPlayer().getParty()) {
                inMap++;
            }
        }
        return inMap;
    }

    public void modifyNx(int amount) {
        getPlayer().modifyCSPoints(1, amount);
        if (amount > 0) {
            getPlayer().dropMessage(5, "You have gained " + amount + " NX points.");
        } else {
            getPlayer().dropMessage(5, "You have lost " + amount + " NX points.");
        }
    }

    public int getTime(String type) {
        Calendar cal = Calendar.getInstance();
        if (type.startsWith("d")) {
            return cal.get(Calendar.DAY_OF_WEEK);
        } else if (type.startsWith("h")) {
            return cal.get(Calendar.HOUR_OF_DAY);
        } else if (type.startsWith("m")) {
            return cal.get(Calendar.MINUTE);
        } else if (type.startsWith("s")) {
            return cal.get(Calendar.SECOND);
        }
        return -1; // wrong input
    }

    public void addBuddyCapacity(int capacity) {
        getPlayer().addBuddyCapacity(capacity);
    }

    public void clearKeys() {
        getPlayer().setDefaultKeyMap();
    }

    public void scheduleWarp(int delay, int mapid) {
        final int fmapid = mapid;
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                getPlayer().changeMap(fmapid);
            }
        }, delay * 1000);
    }

    public void startClock(int limit, int endMap) {
        getPlayer().getMap().addMapTimer(limit, endMap);
    }

    public MapleCharacter getCharByName(String name) {
        try {
            return c.getChannelServer().getPlayerStorage().getCharacterByName(name);
        } catch (Exception e) {
            return null;
        }
    }

    public void warpAllInMap(int mapid) {
        warpAllInMap(mapid, 0);
    }

    public void warpAllInMap(int mapid, int portal) {
        for (MapleCharacter mch : getPlayer().getMap().getCharacters()) {
            if (mch.getEventInstance() != null) {
                mch.getEventInstance().unregisterPlayer(mch);
            }
            mch.changeMap(mapid, portal);
        }
    }

    public boolean createMarriage(String partner_) {
        MapleCharacter partner = getCharByName(partner_);
        if (partner == null) {
            return false;
        }
        partner.setMarried(true);
        getPlayer().setMarried(true);
        partner.setPartnerId(getPlayer().getId());
        getPlayer().setPartnerId(partner.getId());
        if (partner.getGender() > 0) {
            Marriage.createMarriage(getPlayer(), partner);
        } else {
            Marriage.createMarriage(partner, getPlayer());
        }
        return true;
    }

    public boolean createEngagement(String partner_) {
        MapleCharacter partner = getCharByName(partner_);
        if (partner == null) {
            return false;
        }
        if (partner.getGender() > 0) {
            Marriage.createEngagement(getPlayer(), partner);
        } else {
            Marriage.createEngagement(partner, getPlayer());
        }
        return true;
    }

    public void divorceMarriage() {
        getPlayer().setPartnerId(0);
        getPlayer().setMarried(false);
        Marriage.divorceMarriage(getPlayer());
    }

    public void changeKeyBinding(int key, byte type, int action) {
        MapleKeyBinding newbinding = new MapleKeyBinding(type, action);
        getPlayer().changeKeybinding(key, newbinding);
    }

    public Equip getEquipById(int id) { // we can do getEquipById(2349823).setStr(545); etc.
        MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(id);
        return (Equip) getPlayer().getInventory(type).findById(id);
    }

    public int getNpcTalkTimes() {
        return NPCScriptManager.getInstance().getNpcTalkTimes(getPlayer().getId(), npc);
    }

    public void setNpcTalkTimes(int amount) {
        NPCScriptManager.getInstance().setNpcTalkTimes(getPlayer().getId(), npc, amount);
    }

    public int talkedTimesByNpc() {
        return NPCScriptManager.getInstance().talkedTimesByNpc(npc);
    }

    public boolean makeProItem(int id, int hardcore) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        IItem item = ii.getEquipById(id);
        MapleInventoryType type = ii.getInventoryType(id);
        if (type.equals(MapleInventoryType.EQUIP)) {
            MapleInventoryManipulator.addFromDrop(c, ii.hardcoreItem((Equip) item, (short) hardcore));
            return true;
        } else {
            return false;
        }
    }

    public boolean isGuest() {
        if (c.isGuest()) {
            return true;
        }
        return false;
    }

    public void worldMessage(String text) {
        try {
            getPlayer().getClient().getChannelServer().getWorldInterface().broadcastMessage(getPlayer().getName(), MaplePacketCreator.serverNotice(6, text).getBytes());
        } catch (RemoteException e) {
            c.getChannelServer().reconnectWorld();
        }
    }

    public void broadcastMessage(int type, String message) {
        try {
            getPlayer().getClient().getChannelServer().getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(type, message).getBytes());
        } catch (RemoteException e) {
            c.getChannelServer().reconnectWorld();
        }
    }

    public void setClan(int set) {
        getPlayer().setClan(set);
        try {
            getPlayer().getClient().getChannelServer().getWorldInterface().broadcastToClan((getPlayer().getName() + " has entered the clan ! Give him a nice welcome.").getBytes(), set);
        } catch (RemoteException e) {
            c.getChannelServer().reconnectWorld();
        }
        c.getChannelServer().addToClan(getPlayer());
    }

    public String getAllOnlineNamesFromClan(int set) {
        StringBuilder sb = new StringBuilder();
        for (MapleCharacter names : c.getChannelServer().getClanHolder().getAllOnlinePlayersFromClan(set)) {
            sb.append(names.getName() + "\r\n");
        }
        return sb.toString();
    }

    public String getAllOfflineNamesFromClan(int set) {
        StringBuilder sb = new StringBuilder();
        for (String names : c.getChannelServer().getClanHolder().getAllOfflinePlayersFromClan(set)) {
            sb.append(names + "\r\n");
        }
        return sb.toString();
    }

    public int getOfflineClanCount(int clan) {
        return ClanHolder.countOfflineByClan(clan);
    }

    public int getOnlineClanCount(int clan) {
        try {
            return c.getChannelServer().getWorldInterface().onlineClanMembers(clan);
        } catch (RemoteException re) {
            c.getChannelServer().reconnectWorld();
        }
        return -1;
    }

    public String getJobById(int id) {
        return MapleJob.getJobName(id);
    }

    public List<MapleCharacter> getPartyMembers() {
        if (getPlayer().getParty() == null) {
            return null;
        }
        List<MapleCharacter> chars = new LinkedList<MapleCharacter>(); // creates an empty array full of shit..
        for (ChannelServer channel : ChannelServer.getAllInstances()) {
            for (MapleCharacter chhr : channel.getPartyMembers(getPlayer().getParty())) {
                if (chhr != null) { // double check <3
                    chars.add(chhr);
                }
            }
        }
        return chars;
    }

    public MapleCharacter getSender() {
        return this.chr;
    }

    public boolean hasTemp() {
        if (!getPlayer().hasMerchant() && getPlayer().tempHasItems()) {
            return true;
        } else {
            return false;
        }
    }

    public void resetMap(int mapid) {
        getClient().getChannelServer().getMapFactory().getMap(mapid).resetReactors();
    }

    public void removeHiredMerchantItem(boolean tempItem, int itemId) {
        String Table = "hiredmerchant";
        if (tempItem) {
            Table = "hiredmerchanttemp";
        }
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM " + Table + " WHERE itemid = ? AND ownerid = ? LIMIT 1");
            ps.setInt(1, itemId);
            ps.setInt(2, getPlayer().getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
        }
    }

    public int getHiredMerchantMesos() {
        Connection con = DatabaseConnection.getConnection();
        int mesos;
        try {
            PreparedStatement ps = con.prepareStatement("SELECT MerchantMesos FROM characters WHERE id = ?");
            ps.setInt(1, getPlayer().getId());
            ResultSet rs = ps.executeQuery();
            rs.next();
            mesos = rs.getInt("MerchantMesos");
            rs.close();
            ps.close();
        } catch (SQLException se) {
            return 0;
        }
        return mesos;
    }

    public void setHiredMerchantMesos(int set) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE characters SET MerchantMesos = ? WHERE id = ?");
            ps.setInt(1, set);
            ps.setInt(2, getPlayer().getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
        }
    }

    public boolean getHiredMerchantItems(boolean tempTable) {
        boolean temp = false, compleated = false;
        String Table = "hiredmerchant";
        if (tempTable) {
            Table = "hiredmerchanttemp";
        }
        if (tempTable) {
            temp = true;
        }
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM " + Table + " WHERE ownerid = ?");
            ps.setInt(1, getPlayer().getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("type") == 1) {
                    Equip spItem = new Equip(rs.getInt("itemid"), (byte) 0, -1);
                    spItem.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                    spItem.setLevel((byte) rs.getInt("level"));
                    spItem.setStr((short) rs.getInt("str"));
                    spItem.setDex((short) rs.getInt("dex"));
                    spItem.setInt((short) rs.getInt("int"));
                    spItem.setLuk((short) rs.getInt("luk"));
                    spItem.setHp((short) rs.getInt("hp"));
                    spItem.setMp((short) rs.getInt("mp"));
                    spItem.setWatk((short) rs.getInt("watk"));
                    spItem.setMatk((short) rs.getInt("matk"));
                    spItem.setWdef((short) rs.getInt("wdef"));
                    spItem.setMdef((short) rs.getInt("mdef"));
                    spItem.setAcc((short) rs.getInt("acc"));
                    spItem.setAvoid((short) rs.getInt("avoid"));
                    spItem.setHands((short) rs.getInt("hands"));
                    spItem.setSpeed((short) rs.getInt("speed"));
                    spItem.setJump((short) rs.getInt("jump"));
                    spItem.setOwner(rs.getString("owner"));
                    if (!getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()) {
                        MapleInventoryManipulator.addFromDrop(c, spItem, true);
                        removeHiredMerchantItem(temp, spItem.getItemId());
                    } else {
                        rs.close();
                        ps.close();
                        return false;
                    }
                } else {
                    Item spItem = new Item(rs.getInt("itemid"), (byte) 0, (short) rs.getInt("quantity"));
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    MapleInventoryType type = ii.getInventoryType(spItem.getItemId());
                    if (!getPlayer().getInventory(type).isFull()) {
                        MapleInventoryManipulator.addFromDrop(c, spItem, true);
                        removeHiredMerchantItem(temp, spItem.getItemId());
                    } else {
                        rs.close();
                        ps.close();
                        return false;
                    }
                }
            }
            rs.close();
            ps.close();
            compleated = true;
        } catch (SQLException se) {
            se.printStackTrace();
            return compleated;
        }
        return compleated;
    }

    public void summonMob(int mobid, int customHP, int customEXP, int amount) {
        spawnMonster(mobid, customHP, -1, -1, customEXP, 0, 0, amount, getPlayer().getPosition().x, getPlayer().getPosition().y);
    }

    public void spawnMonster(int mobid, int HP, int MP, int level, int EXP, int boss, int undead, int amount, int x, int y) {
        MapleMonsterStats newStats = new MapleMonsterStats();
        if (HP >= 0) {
            newStats.setHp(HP);
        }
        if (MP >= 0) {
            newStats.setMp(MP);
        }
        if (level >= 0) {
            newStats.setLevel(level);
        }
        if (EXP >= 0) {
            newStats.setExp(EXP);
        }
        newStats.setBoss(boss == 1); // LOLWAT
        newStats.setUndead(undead == 1);
        for (int i = 0; i < amount; i++) {
            MapleMonster npcmob = MapleLifeFactory.getMonster(mobid);
            npcmob.setOverrideStats(newStats);
            npcmob.setHp(npcmob.getMaxHp());
            npcmob.setMp(npcmob.getMaxMp());
            getPlayer().getMap().spawnMonsterOnGroundBelow(npcmob, new Point(x, y));
        }
    }

    public Point getNPCPosition() {
        MapleNPC thenpc = MapleLifeFactory.getNPC(this.npc);
        Point pos = thenpc.getPosition();
        return pos;
    }

    public void setNeededLeaves(int whatisdis) {
        for (ChannelServer channels : ChannelServer.getAllInstances()) {
            channels.neededleaves = whatisdis;
        }
    }

    public void gainReborn(int dicks) {
        getPlayer().setReborns(getPlayer().getReborns() + dicks);
    }

    public int getReborns() { // tjat
        return getPlayer().getReborns();
    }

    public void gain1Reborn(int reborn) {
        getPlayer().setReborns(getPlayer().getReborns() + 1);
    }

    public void reloadChar() {
        getPlayer().getClient().getSession().write(MaplePacketCreator.getCharInfo(getPlayer()));
        getPlayer().getMap().removePlayer(getPlayer());
        getPlayer().getMap().addPlayer(getPlayer());
    }

    public void changeJobById(int a) {
        getPlayer().changeJob(MapleJob.getById(a));
    }

    public void sendDonationNotice() {
        try {
            getPlayer().getClient().getChannelServer().getWorldInterface().broadcastMessage(getPlayer().getName(), MaplePacketCreator.serverNotice(6, "The donation event has started in Channel " + c.getChannel() + " Henesys by " + getPlayer().getName() + ".").getBytes());
        } catch (RemoteException e) {
            c.getChannelServer().reconnectWorld();
        }
    }

    public void summonMob(int mobid) {
        getPlayer().getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(mobid), getNPCPosition());
    }

    public void summonMobAtPosition(int mobid, int customHP, int customEXP, int amount, int posx, int posy) {
        MapleMonsterStats newStats = new MapleMonsterStats();
        if (customHP > 0) {
            newStats.setHp(customHP);
        }
        if (customEXP >= 0) {
            newStats.setExp(customEXP);
        }
        if (amount <= 1) {
            MapleMonster npcmob = MapleLifeFactory.getMonster(mobid);
            npcmob.setOverrideStats(newStats);
            npcmob.setHp(npcmob.getMaxHp());
            getPlayer().getMap().spawnMonsterOnGroudBelow(npcmob, new Point(posx, posy));
        } else {
            for (int i = 0; i < amount; i++) {
                MapleMonster npcmob = MapleLifeFactory.getMonster(mobid);
                npcmob.setOverrideStats(newStats);
                npcmob.setHp(npcmob.getMaxHp());
                getPlayer().getMap().spawnMonsterOnGroudBelow(npcmob, new Point(posx, posy));
            }
        }
    }

    public void summonMobAtPosition(int mobid, int amount, int posx, int posy) {
        if (amount <= 1) {
            MapleMonster npcmob = MapleLifeFactory.getMonster(mobid);
            npcmob.setHp(npcmob.getMaxHp());
            getPlayer().getMap().spawnMonsterOnGroudBelow(npcmob, new Point(posx, posy));
        } else {
            for (int i = 0; i < amount; i++) {
                MapleMonster npcmob = MapleLifeFactory.getMonster(mobid);
                npcmob.setHp(npcmob.getMaxHp());
                getPlayer().getMap().spawnMonsterOnGroudBelow(npcmob, new Point(posx, posy));
            }
        }
    }

    public void warpParty(int mapId, int portal) { // Just Warp
        MapleMap target = getMap(mapId);
        for (MaplePartyCharacter chhr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chhr.getName());
            if ((curChar.getEventInstance() == null && c.getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance()) {
                curChar.changeMap(target, target.getPortal(portal));
            }
        }
    }

    public void warpPartyWithExp(int mapid, int exp) {
        for (MaplePartyCharacter plaa : c.getPlayer().getParty().getMembers()) {
            MapleCharacter lol = getPlayer().getParty().getPlayer(getPlayer().getClient(), plaa.getName());
            lol.changeMap(lol.getClient().getChannelServer().getMapFactory().getMap(mapid, c.getPlayer().getWorld()), lol.getClient().getChannelServer().getMapFactory().getMap(mapid, c.getPlayer().getWorld()).getPortal(0));
            lol.gainExp(exp, true, false, true);
        }
    }

    public void warpPartyWithExpMeso(int mapid, int exp, int meso) {
        for (MaplePartyCharacter plaa : c.getPlayer().getParty().getMembers()) {
            MapleCharacter lol = getPlayer().getParty().getPlayer(getPlayer().getClient(), plaa.getName());
            lol.changeMap(lol.getClient().getChannelServer().getMapFactory().getMap(mapid, c.getPlayer().getWorld()), lol.getClient().getChannelServer().getMapFactory().getMap(mapid, c.getPlayer().getWorld()).getPortal(0));
            lol.gainExp(exp, true, false, true);
            lol.gainMeso(meso, true);
        }
    }

    public int checkAVGLevel(int map) {
        MapleMap mapz = c.getChannelServer().getMapFactory().getMap(map, c.getPlayer().getWorld());
        List<MapleMapObject> players = mapz.getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER));
        int level = 0;
        for (int a = 0; a < players.size(); a++) {
            MapleCharacter victim = (MapleCharacter) players.get(a);
            level += victim.getLevel();
        }
        return (level / players.size());
    }

    public int calcAvgLvl(int map) {
        int num = 0;
        int avg = 0;
        for (MapleMapObject mmo : c.getChannelServer().getMapFactory().getMap(map).getAllPlayer()) {
            avg += ((MapleCharacter) mmo).getLevel();
            num++;
        }
        avg /= num;
        return avg;
    }

    public void sendCPQMapLists() {
        String msg = "Pick a field:\\r\\n";
        for (int i = 0; i < 6; i++) {
            if (fieldTaken(i)) {
                if (fieldLobbied(i)) {
                    msg += "#b#L" + i + "#Monster Carnival Field " + (i + 1) + " Avg Lvl: " +
                            calcAvgLvl(980000100 + i * 100) + "#l\\r\\n";
                } else {
                    continue;
                }
            } else {
                msg += "#b#L" + i + "#Monster Carnival Field " + (i + 1) + "#l\\r\\n";
            }
        }
        sendSimple(msg);
    }

    public boolean fieldTaken(int field) {
        if (c.getChannelServer().getMapFactory().getMap(980000100 + field * 100).getAllPlayer().size() != 0) {
            return true;
        }
        if (c.getChannelServer().getMapFactory().getMap(980000101 + field * 100).getAllPlayer().size() != 0) {
            return true;
        }
        if (c.getChannelServer().getMapFactory().getMap(980000102 + field * 100).getAllPlayer().size() != 0) {
            return true;
        }
        return false;
    }

    public boolean fieldLobbied(int field) {
        if (c.getChannelServer().getMapFactory().getMap(980000100 + field * 100).getAllPlayer().size() != 0) {
            return true;
        }
        return false;
    }

    public void cpqLobby(int field) {
        try {
            MapleMap map;
            ChannelServer cs = c.getChannelServer();
            map = cs.getMapFactory().getMap(980000100 + 100 * field);
            for (MaplePartyCharacter mpc : c.getPlayer().getParty().getMembers()) {
                MapleCharacter mc;
                mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
                if (mc != null) {
                    mc.changeMap(map, map.getPortal(0));
                    mc.getClient().getSession().write(MaplePacketCreator.serverNotice(5,
                            "You will now recieve challenges from other parties. If you do not accept a challenge in 3 minutes, you will be kicked out."));
                    mc.getClient().getSession().write(MaplePacketCreator.getClock(3 * 60));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public MapleCharacter getChrById(int id) {
        ChannelServer cs = c.getChannelServer();
        return cs.getPlayerStorage().getCharacterById(id);
    }

    public void startCPQ(final MapleCharacter challenger, int field) {
        try {
            if (challenger != null) {
                if (challenger.getParty() == null) {
                    throw new RuntimeException("Challenger's party was null!");
                }
                for (MaplePartyCharacter mpc : challenger.getParty().getMembers()) {
                    MapleCharacter mc;
                    mc = c.getChannelServer().getPlayerStorage().getCharacterByName(mpc.getName());
                    if (mc != null) {
                        mc.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().getPortal(0));
                        mc.getClient().getSession().write(MaplePacketCreator.getClock(10));
                    }
                }
            }
            final int mapid = c.getPlayer().getMap().getId() + 1;
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    MapleMap map;
                    ChannelServer cs = c.getChannelServer();
                    map = cs.getMapFactory().getMap(mapid);
                    new MonsterCarnival(getPlayer().getParty(), challenger.getParty(), mapid);
                    map.broadcastMessage(MaplePacketCreator.serverNotice(5,
                            "Monster Carnival has begun!"));
                }
            }, 10000);
            mapMessage(5, "Monster Carnival will begin in 10 seconds!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void challengeParty(int field) {
        MapleCharacter leader = null;
        MapleMap map = c.getChannelServer().getMapFactory().getMap(980000100 + 100 * field);
        for (MapleMapObject mmo : map.getAllPlayer()) {
            MapleCharacter mc = (MapleCharacter) mmo;
            if (mc.getParty().getLeader().getId() == mc.getId()) {
                leader = mc;
                break;
            }
        }
        if (leader != null) {
            if (!leader.isChallenged()) {
                List<MaplePartyCharacter> fuckwits = new LinkedList<MaplePartyCharacter>();
                for (MaplePartyCharacter fucker : c.getPlayer().getParty().getMembers()) {
                    fuckwits.add(fucker);
                }
                NPCScriptManager.getInstance().start(c, npc, fileName, chr);
                NPCScriptManager.getInstance().start(
                        "cpqchallenge", leader.getClient(), npc, fuckwits);
            } else {
                sendOk("The other party is currently taking on a different challenge.");
            }
        } else {
            sendOk("Could not find leader!");
        }
    }

    public int getHair() {
        return getPlayer().getHair();
    }

    public int getFace() {
        return getPlayer().getFace();
    }

    public String getName() {
        return getPlayer().getName();
    }

    public int getGender() {
        return getPlayer().getGender();
    }

    public int getRebirthPoints() {
        return getPlayer().getRebirthPoints();
    }

    public void setRebirthPoints(int lol) {
        getPlayer().setRebirthPoints(getPlayer().getRebirthPoints() + lol);
    }

    public int getVotePoints() {
        return getPlayer().getVotePoints();
    }

    public void setVotePoints(int fuck) {
        getPlayer().setVotePoints(getPlayer().getVotePoints() + fuck);
    }

    public int getRandom(int start, int end) {
        return (int) Math.floor(Math.random() * end + start);
    }

    public void getNews() {
        for (String i : c.getPlayer().getNews()) { // why c.getPlayer().getNews() its stored in a variable at login oh but i think it dc's because of the fore statement wouldnt it just be easier to make an NPC and edit it from there? i tried that lol. i mean like this
            sendOk(i);
        }
    }

    public void WarpTo(String player) {
        MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(player);
        MapleMap target = victim.getMap();
        c.getPlayer().changeMap(target, target.findClosestSpawnpoint(victim.getPosition()));
    }

    public void randomizeStats(int item, int max) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Equip lol = ii.randomizeStats((Equip) ii.getEquipById(item), max);
        MapleInventoryManipulator.addFromDrop(c, lol, true);
    }

    public String buyLotto(int ticketCost) {
        if (!MapleLottery.isActive()) {
            MapleLottery.scheduleLotto();
        }
        return MapleLottery.buyTicket(getPlayer(), ticketCost);
    }

    public TerritoryStorage getTerritoryStorage() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
            WorldRegistry worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
            return worldRegistry.getTerritoryStorage();
        } catch (Exception r) {
            r.printStackTrace();
            return null;
        }
    }

    public void dropFlagAtPos(int flagid, int x, int y) { // By Rich to drop flags after getting a point.
        IItem itemid = new Item(flagid, (byte) 3, (short) 1);
        getPlayer().getMap().spawnItemDrop(getPlayer(), getPlayer(), itemid, new Point(x, y), true, false);
    }

    public void giveBetaItem(int equip, short stats) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        IItem item = ii.getEquipById(equip);
        MapleInventoryManipulator.addFromDrop(c, ii.hardcoreItem((Equip) item, stats));
    }

    public void changeStat(byte slot, int type, short amount) {
        Equip sel = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
        switch (type) {
            case 0:
                sel.setStr(amount);
                break;
            case 1:
                sel.setDex(amount);
                break;
            case 2:
                sel.setInt(amount);
                break;
            case 3:
                sel.setLuk(amount);
                break;
            case 4:
                sel.setWatk(amount);
                break;
            case 5:
                sel.setMatk(amount);
                break;
            case 6:
                sel.setHp(amount);
                break;
            case 7:
                sel.setMp(amount);
                break;
            case 8:
                sel.setWdef(amount);
                break;
            case 9:
                sel.setMdef(amount);
                break;
            case 10:
                sel.setAcc(amount);
                break;
            case 11:
                sel.setAvoid(amount);
                break;
            case 12:
                sel.setHands(amount);
                break;
            case 13:
                sel.setSpeed(amount);
                break;
            case 14:
                sel.setJump(amount);
                break;
            default:
                break;
        }
        c.getPlayer().equipChanged();
    }
}
