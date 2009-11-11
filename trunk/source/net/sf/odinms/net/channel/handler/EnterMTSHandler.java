package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.messages.ServernoticeMapleClientMessageCallback;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.server.maps.SavedLocationType;

public class EnterMTSHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if ((c.getPlayer().getMapId() != 910000000)) {
            if (c.getPlayer().isAlive()) {
                new ServernoticeMapleClientMessageCallback(5, c).dropMessage("You have been transported to Free Market Entrance.");
                c.getSession().write(MaplePacketCreator.enableActions());
                MapleMap to;
                MaplePortal pto;
                to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(910000000);
                c.getPlayer().saveLocation(SavedLocationType.FREE_MARKET);
                pto = to.getPortal("out00");
                c.getPlayer().changeMap(to, pto);
            } else {
                new ServernoticeMapleClientMessageCallback(5, c).dropMessage("You cannot enter the Free Market if you are not alive.");
                c.getSession().write(MaplePacketCreator.enableActions());
            }
        } else {
            new ServernoticeMapleClientMessageCallback(5, c).dropMessage("You are already in the Free Market Entrance.");
            c.getSession().write(MaplePacketCreator.enableActions());
        }
    }
}
