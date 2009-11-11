package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.client.anticheat.CheatingOffense;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class GiveFameHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getPlayer().resetAfkTime();
        int who = slea.readInt();
        int mode = slea.readByte();
        int famechange = mode == 0 ? -1 : 1;
        MapleCharacter target = (MapleCharacter) c.getPlayer().getMap().getMapObject(who);
        if (target == c.getPlayer()) { // Faming self.
            c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.FAMING_SELF);
            return;
        } else if (c.getPlayer().getLevel() < 15) {
            c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.FAMING_UNDER_15);
            return;
        }
        if (target.getFame() >= 50) {
            target.finishAchievement(9);
        }
        switch (c.getPlayer().canGiveFame(target)) {
            case OK:
                if (Math.abs(target.getFame()) < 30000) { // Forces player fame to a positive amount and checks :) Credits Moogra <3
                    target.addFame(famechange);
                    target.updateSingleStat(MapleStat.FAME, target.getFame());
                }
                if (!c.getPlayer().isGM()) {
                c.getPlayer().hasGivenFame(target);
                }
                c.getSession().write(MaplePacketCreator.giveFameResponse(mode, target.getName(), target.getFame()));
                target.getClient().getSession().write(MaplePacketCreator.receiveFame(mode, c.getPlayer().getName()));
                break;
            case NOT_TODAY:
                break;
            case NOT_THIS_MONTH:
                c.getSession().write(MaplePacketCreator.giveFameErrorResponse(4));
                break;
        }
    }
}
