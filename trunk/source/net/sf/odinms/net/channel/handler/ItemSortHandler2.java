package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class ItemSortHandler2 extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getPlayer().resetAfkTime();
        // [41 00] [E5 1D 55 00] [01]
        // [32 00] [01] [01] // Sent after
        slea.readInt();
        byte mode = slea.readByte();
        c.getSession().write(MaplePacketCreator.finishedSort2(mode));
    }
}