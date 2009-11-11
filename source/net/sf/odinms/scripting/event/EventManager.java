package net.sf.odinms.scripting.event;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptException;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleCharacterUtil;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.world.MapleParty;
import net.sf.odinms.server.MapleSquad;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.tools.MaplePacketCreator;

public class EventManager {

    private Invocable iv;
    private ChannelServer cserv;
    private MapleCharacter player;
    private Map<String, EventInstanceManager> instances = new HashMap<String, EventInstanceManager>();
    private Properties props = new Properties();
    private String name;

    public EventManager(ChannelServer cserv, Invocable iv, String name) {
        this.iv = iv;
        this.cserv = cserv;
        this.name = name;
    }

    public void cancel() {
        try {
            iv.invokeFunction("cancelSchedule", (Object) null);
        } catch (ScriptException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void schedule(String methodName, long delay) {
        schedule(methodName, null, delay);
    }

    public void schedule(final String methodName, final EventInstanceManager eim, long delay) {
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                try {
                    iv.invokeFunction(methodName, eim);
                } catch (ScriptException ex) {
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, delay);
    }

    public ScheduledFuture<?> scheduleAtTimestamp(final String methodName, long timestamp) {
        return TimerManager.getInstance().scheduleAtTimestamp(new Runnable() {

            public void run() {
                try {
                    iv.invokeFunction(methodName, (Object) null);
                } catch (ScriptException ex) {
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, timestamp);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(final String methodName, long delay) { // Ehh Fully copied and pasted, im lazy.
        return TimerManager.getInstance().register(new Runnable() {

            public void run() {
                try {
                    iv.invokeFunction(methodName, (Object) null);
                } catch (ScriptException ex) {
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, delay);
    }

    public ChannelServer getChannelServer() {
        return cserv;
    }

    public MapleCharacter getPlayer() {
        return player;
    }

    public EventInstanceManager getInstance(String name) {
        return instances.get(name);
    }

    public Collection<EventInstanceManager> getInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public EventInstanceManager newInstance(String name) {
        EventInstanceManager ret = new EventInstanceManager(this, name);
        instances.put(name, ret);
        return ret;
    }

    public void disposeInstance(String name) {
        /*    try {
        iv.invokeFunction("dispose", (Object) null);
        } catch (ScriptException ex) {
        Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
        Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        instances.remove(name); */
        instances.remove(name);
    }

    public void disposeInstance2(String name) {
        instances.remove(name);
    }

    public Invocable getIv() {
        return iv;
    }

    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public String getName() {
        return name;
    }

    //PQ method: starts a PQ
    public void startInstance(MapleParty party, MapleMap map) {
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
            eim.registerParty(party, map);
        } catch (ScriptException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void startInstance(MapleSquad squad, MapleMap map) {
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
            eim.registerSquad(squad, map);
        } catch (ScriptException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //non-PQ method for starting instance
    public void startInstance(EventInstanceManager eim) {
        try {
            iv.invokeFunction("setup", eim);
        } catch (ScriptException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void worldMessage(String text) {
        try {
            player.getClient().getChannelServer().getWorldInterface().broadcastMessage(getPlayer().getName(), MaplePacketCreator.serverNotice(6, text).getBytes());
        } catch (RemoteException e) {
            player.getClient().getChannelServer().reconnectWorld();
        }
    }

    public void save() {
        for (ChannelServer chan : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : chan.getPlayerStorage().getAllCharacters()) {
                chr.saveToDB(true, false);
                //BackUpper.backUpSQL(chr.getClient(), "");
            }
        }
    }

    /**
     * Gives a list of all the Jr. GMs, GMs, SuperGMs, and Admins online.
     */
    /**
     * Gives a list of every player with a certain GM level there is online.
     * @param helper if true, adds GM helpers to the list, otherwise skips them
     * @param junior if true, adds Junior GMs to the list, otherwise skips them
     * @param gm if true, adds GMs (level 3) to the list, otherwise skips them
     * @param supergm if true, adds SuperGMs to the list, otherwise skips them
     * @param admin if true, adds Admins to the list, otherwise skips them
     */
    public String getAllGMs() {
        StringBuilder builder = new StringBuilder("");
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cs.getPlayerStorage().getAllCharacters()) {
                if (chr.isGM()) {
                    builder.append(MapleCharacterUtil.makeMapleReadable(chr.getName()));
                    builder.append(", ");
                }
            }
        }
        return builder.toString();
    }
}
