package net.sf.odinms.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.tools.MaplePacketCreator;

public class MapleLottery {

    private static List<MapleCharacter> entries;
    private static int prize = 100000;
    private static int ticketId = 4000213; //Coca-Cola Card
    private static Random r = new Random();
    private static ScheduledFuture<?> active;
    private static final long DAY = 1000 * 60 * 60 * 24;
    private static final long HOUR = 1000 * 60 * 60;

    // Make sure checks are used inside the NPC if there is enough mesos
    public static String buyTicket(MapleCharacter mc, int ticketCost) {
        entries.add(mc); // why would that cause an error? o-o
        prize += (Integer) ticketCost * .55;
        MapleInventoryManipulator.addById(mc.getClient(), ticketId, (short) 1, "Lotto Ticket");
        return "There is currently a 1 in " + entries.size() + " chance to win, with a jackpot of " + prize + ". Lottery is drawn every day at 4 PM Server Time. Remember, you must be online during the drawing, if you miss it your ticket will become invalid and there are no refunds.";
    }

    public static void initLotto() {
        String s = entries.get(r.nextInt(entries.size())).getName();
        MapleCharacter win = null;
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            if (cserv.getPlayerStorage().getCharacterByName(s) != null) {
                win = cserv.getPlayerStorage().getCharacterByName(s);
            }
        }
        if (win != null) {
            win.gainMeso(prize, true);
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, "[Event] " + win.getName() + ""));
            }

            active = null;
            prize = 100000;
        } else {
            initLotto();
        }
    }

    public static void scheduleLotto() {
        int curTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int dif = 16 - curTime;
        active = TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                initLotto();
            }
        }, ((dif * HOUR) + DAY));
    }

    public static List<MapleCharacter> getEntries() {
        return entries;
    }

    public static boolean isActive() {
        return active != null;
    }

    public static void Backup() { // Incase server crash and people qq.
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM player_items WHERE itemid = ?");
            ps.setInt(1, ticketId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException sqle) {
        }
    }
}
