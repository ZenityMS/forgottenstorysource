package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class MapleTVHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        /*
        D1 00 - header
        07 00 4F 72 69 67 69 6E 32 - "Origin2"
        00 00 00 00 - ?
        80 7F 3D 36 - (910000000 -> ?) Any suggestions? */
        slea.readMapleAsciiString();
        slea.readInt();
        slea.readInt();
    }
}
