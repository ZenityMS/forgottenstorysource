package net.sf.odinms.net.channel.handler;

import java.rmi.RemoteException;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import java.sql.*;
import net.sf.odinms.client.MapleCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.net.world.remote.WorldChannelInterface;
import net.sf.odinms.tools.MaplePacketCreator;

public class ReportHandler extends AbstractMaplePacketHandler {

    final String[] reasons = {
        "Hacking",
        "Botting",
        "Scamming",
        "Fake GM",
        "Harassment",
        "Advertising"
    };

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getPlayer().resetAfkTime();
        int reportedCharId = slea.readInt();
        byte reason = slea.readByte();
        String chatlog = "No chatlog";
        short clogLen = slea.readShort();
        if (clogLen > 0) {
            chatlog = slea.readAsciiString(clogLen);
        }
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info(c.getPlayer().getName() + " reported charid " + reportedCharId);
        int cid = reportedCharId;

        if (addReportEntry(c.getPlayer().getId(), reportedCharId, reason, chatlog)) {
            c.getSession().write(MaplePacketCreator.reportReply((byte) 0));
        } else {
            c.getSession().write(MaplePacketCreator.reportReply((byte) 4));
        }
        try {
            WorldChannelInterface wci = c.getChannelServer().getWorldInterface();
            wci.broadcastGMMessage(null, MaplePacketCreator.serverNotice(5, c.getPlayer().getName() + " reported " + MapleCharacter.getNameById(cid, 0) + " for " + reasons[reason] + ".").getBytes());
        } catch (RemoteException ex) {
            c.getChannelServer().reconnectWorld();
        }
    }

    private boolean addReportEntry(int reporterId, int victimId, byte reason, String chatlog) {
        try {
            Connection dcon = DatabaseConnection.getConnection();
            PreparedStatement ps;
            ps = dcon.prepareStatement("INSERT INTO reports VALUES (NULL, CURRENT_TIMESTAMP, ?, ?, ?, ?, 'UNHANDLED')");
            ps.setInt(1, reporterId);
            ps.setInt(2, victimId);
            ps.setInt(3, reason);
            ps.setString(4, chatlog);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            return false;
        }
        return true;
    }
}
