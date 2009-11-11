package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class MobDamageMobHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // Balrog attacking tylus - [A3 00] [9D 54 AF 00] [0D 46 23 00] [0E 54 AF 00]
        int attackerOid = slea.readInt();
        int cid = slea.readInt();
        int damagedOid = slea.readInt();

        MapleMonster damaged = c.getPlayer().getMap().getMonsterByOid(damagedOid);
        MapleMonster attacker = c.getPlayer().getMap().getMonsterByOid(attackerOid);

        if (damaged == null || attacker == null) {
            return; // Hax.
        }

        int damage = (int) (Math.random() * (damaged.getMaxHp() / 13 + attacker.getPADamage() * 10)) * 2 + 500;  // TODO. Make a formula for damage LOL T____T
        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.MobDamageMob(damaged, damage), damaged.getPosition());
    }
}
