package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleInventory;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class ItemSortHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getPlayer().resetAfkTime();
        /*
        40 00 4D 2B A1 00 05 cash
        40 00 F7 17 A1 00 04 etc
        40 00 6F 36 A1 00 03 setup
        40 00 1F 4A A1 00 02 use
        40 00 EB 50 A1 00 02 use
        40 00 8D 54 A1 00 02 use
        40 00 AD 61 A1 00 01 eqp
        
        MapleStory just sorts items at the top
         */

        slea.readInt();
        byte mode = slea.readByte();

        boolean sorted = false;
        MapleInventoryType pInvType = MapleInventoryType.getByType(mode);
        MapleInventory pInv = c.getPlayer().getInventory(pInvType);

        while (!sorted) {
            byte freeSlot = pInv.getNextFreeSlot();
            if (freeSlot != -1) {
                byte itemSlot = -1;
                for (byte i = (byte) (freeSlot + 1); i <= 100; i++) {
                    if (pInv.getItem(i) != null) {
                        itemSlot = i;
                        break;
                    }
                }
                if (itemSlot <= 100 && itemSlot > 0) {
                    MapleInventoryManipulator.move(c, pInvType, itemSlot, freeSlot);
                } else {
                    sorted = true;
                }
            }
        }
        c.getSession().write(MaplePacketCreator.finishedSort(mode));
        c.getSession().write(MaplePacketCreator.enableActions());
    }
}
