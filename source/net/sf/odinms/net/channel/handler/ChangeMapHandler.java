package net.sf.odinms.net.channel.handler;

import java.net.InetAddress;

import net.sf.odinms.client.MapleBuffStat;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeMapHandler extends AbstractMaplePacketHandler {

    private static Logger log = LoggerFactory.getLogger(ChangeMapHandler.class);

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getPlayer().resetAfkTime();
        if (slea.available() == 0) {
            int channel = c.getChannel();
            String ip = ChannelServer.getInstance(c.getChannel()).getIP(channel);
            String[] socket = ip.split(":");
            if (c.getPlayer().inCS() || c.getPlayer().inMTS()) {
                c.getPlayer().saveToDB(true, true);
                c.getPlayer().setInCS(false);
                c.getPlayer().setInMTS(false);
            } else {
                c.getPlayer().saveToDB(true, false);
            }
            ChannelServer.getInstance(c.getChannel()).removePlayer(c.getPlayer());
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
            try {
                MaplePacket packet = MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]));
                c.getSession().write(packet);
                c.getSession().close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            byte type = slea.readByte(); // 1 = from dying 2 = regular portals
            int targetid = slea.readInt(); // FF FF FF FF

            String startwp = slea.readMapleAsciiString();
            MaplePortal portal = c.getPlayer().getMap().getPortal(startwp);

            MapleCharacter player = c.getPlayer();
            if (player.getBuffedValue(MapleBuffStat.MORPH) != null && player.getBuffedValue(MapleBuffStat.COMBO) != null) {
                player.cancelEffectFromBuffStat(MapleBuffStat.MORPH);
                player.cancelEffectFromBuffStat(MapleBuffStat.COMBO);
            }
            if (player.getBuffedValue(MapleBuffStat.PUPPET) != null) {
                player.cancelBuffStats(MapleBuffStat.PUPPET);
            }

            if (targetid != -1 && !c.getPlayer().isAlive()) {
                boolean executeStandardPath = true;
                if (player.getEventInstance() != null) {
                    executeStandardPath = player.getEventInstance().revivePlayer(player);
                }
                if (executeStandardPath) {
                    player.cancelAllBuffs();
                    player.setHp(50);
                    MapleMap to = player.getMap().getReturnMap();
                    MaplePortal pto = to.getPortal(0);
                    player.setStance(0);
                    player.changeMap(to, pto);
                    if (player.getMap().getReturnMap().getId() == 0) {
                        player.changeMap(100000000, 0);
                        player.dropMessage("Due to an error you are being redirected to Henesys");
                    }
                }
            } else if (targetid != -1 && player.isGM()) {
                MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
                MaplePortal pto = to.getPortal(0);
                player.changeMap(to, pto);
            } else {
                if (portal != null) {
                    portal.enterPortal(c);
                } else {
                    c.getSession().write(MaplePacketCreator.enableActions());
                    log.warn("Portal {} not found on map {}", startwp, player.getMap().getId());
                }
            }
        }
    }
}
