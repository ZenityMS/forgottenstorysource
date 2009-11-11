package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleBuffStat;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.maps.MapleSummon;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import java.util.Iterator;
import net.sf.odinms.client.MapleCharacter;

public class DamageSummonHandler extends AbstractMaplePacketHandler {

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // Ramon 93 00 69 00 00 00 FF 32 02 00 00 98 40 7C 00 01 -> 562 dmg
        // Ramon 93 00 8E 00 00 00 FF 00 00 00 00 05 87 01 00 00 -> miss

        slea.readInt(); //Bugged? might not be skillid.
        int unkByte = slea.readByte();
        int damage = slea.readInt();
        int monsterIdFrom = slea.readInt();
        slea.readByte(); // stance
        MapleCharacter player = c.getPlayer();

        Iterator<MapleSummon> iter = player.getSummons().values().iterator();
        while (iter.hasNext()) {
            MapleSummon summon = iter.next();
            if (summon.isPuppet() && summon.getOwner() == player) { //We can only have one puppet(AFAIK O.O) so this check is safe.
                summon.addHP(-damage);
                if (summon.getHP() <= 0) {
                    player.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
                }
                c.getPlayer().getMap().broadcastMessage(player, MaplePacketCreator.damageSummon(player.getId(), summon.getSkill(), damage, unkByte, monsterIdFrom), summon.getPosition());
                break;
            }
        }
    }
}
