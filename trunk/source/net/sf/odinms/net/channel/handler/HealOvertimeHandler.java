package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class HealOvertimeHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        slea.readShort();
        slea.readByte();
        int healHP = slea.readShort();
        if (healHP != 0) {
            c.getPlayer().addHP(healHP);
        }
        int healMP = slea.readShort();
        if (healMP != 0) {
            c.getPlayer().addMP(healMP);
        }
    }
}
