package net.sf.odinms.net.login.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.maps.FakeCharacter;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class PlayerDCHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) { // This probably won't work.
        if (c.getPlayer().hasFakeChar()) {
            for (FakeCharacter ch : c.getPlayer().getFakeChars()) {
                if (ch.follow()) {
                    c.getPlayer().howmanyfakechars = c.getPlayer().getFakeChars().size();
                    ch.getFakeChar().getMap().removePlayer(ch.getFakeChar());
                }
            }
        }
        c.getPlayer().saveToDB(true, true);
        c.getPlayer().getMap().removePlayer(c.getPlayer());
        /* Connection con = DatabaseConnection.getConnection();
        try {
        PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin WHERE name = ?");
        ps.setString(1, c.getAccountName());
        ps.executeUpdate();
        ps.close();
        } catch (SQLException se) {
        }
        }*/
    }
}
