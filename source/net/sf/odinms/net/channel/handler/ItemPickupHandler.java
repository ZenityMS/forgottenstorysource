package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.maps.MapleMapItem;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class ItemPickupHandler extends AbstractMaplePacketHandler {

    /** Creates a new instance of ItemPickupHandler */
    public ItemPickupHandler() {
    }

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getPlayer().resetAfkTime();
        @SuppressWarnings("unused")
        byte mode = slea.readByte(); // or something like that... but better ignore it if you want
        // mapchange to work! o.o!
        slea.readInt(); //?
        slea.readInt(); // position, but we dont need it o.o
        int oid = slea.readInt();
        MapleMapObject ob = c.getPlayer().getMap().getMapObject(oid);
        if (ob == null) {
            c.getSession().write(MaplePacketCreator.getInventoryFull());
            c.getSession().write(MaplePacketCreator.getShowInventoryFull());
            return;
        }
        if (ob instanceof MapleMapItem) {
            MapleMapItem mapitem = (MapleMapItem) ob;
            synchronized (mapitem) {
                if (mapitem.isPickedUp()) {
                    c.getSession().write(MaplePacketCreator.getInventoryFull());
                    c.getSession().write(MaplePacketCreator.getShowInventoryFull());
                    return;
                }
                double distance = c.getPlayer().getPosition().distanceSq(mapitem.getPosition());
                // c.getPlayer().getCheatTracker().checkPickupAgain();
                if (mapitem.getMeso() > 0) {
                    if (c.getPlayer().getParty() != null) {
                        ChannelServer cserv = c.getChannelServer();
                        int mesosamm = mapitem.getMeso();
                        int partynum = 0;
                        for (MaplePartyCharacter partymem : c.getPlayer().getParty().getMembers()) {
                            if (partymem.isOnline() && partymem.getMapid() == c.getPlayer().getMap().getId() && partymem.getChannel() == c.getChannel()) {
                                partynum++;
                            }
                        }
                        int mesosgain = mesosamm / partynum;
                        for (MaplePartyCharacter partymem : c.getPlayer().getParty().getMembers()) {
                            if (partymem.isOnline() && partymem.getMapid() == c.getPlayer().getMap().getId()) {
                                MapleCharacter somecharacter = cserv.getPlayerStorage().getCharacterById(partymem.getId());
                                if (somecharacter != null) {
                                    somecharacter.gainMeso(mesosgain, true, true);
                                }
                            }
                        }
                    } else {
                        c.getPlayer().gainMeso(mapitem.getMeso(), true, true);
                    }
                    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, c.getPlayer().getId()), mapitem.getPosition());
                    c.getPlayer().getCheatTracker().pickupComplete();
                    c.getPlayer().getMap().removeMapObject(ob);


                } else {
                    if (mapitem.getItem().getItemId() >= 5000000 && mapitem.getItem().getItemId() <= 5000100) {
                        int petId = MaplePet.createPet(mapitem.getItem().getItemId());
                        if (petId == -1) {
                            return;
                        }
                        MapleInventoryManipulator.addById(c, mapitem.getItem().getItemId(), mapitem.getItem().getQuantity(), null, petId);
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, c.getPlayer().getId()), mapitem.getPosition());
                        // c.getPlayer().getCheatTracker().pickupComplete();
                        c.getPlayer().getMap().removeMapObject(ob);
                    } else {
                        if (mapitem.getItem().getItemId() == 1302065) {
                            if (c.getPlayer().canpickup && c.getPlayer().blueteam) {
                                try {
                                    c.getPlayer().hasflag = true;
                                    c.getPlayer().setCanPickup(false);
                                } catch (NullPointerException manfred) {
                                    //Do nothing
                                }
                            } else {
                                c.getPlayer().dropMessage("One of your party members already has a flag or this isn't your team's flag.");
                                return;
                            }
                        }

                        if (mapitem.getItem().getItemId() == 1302033) {
                            if (c.getPlayer().canpickup && c.getPlayer().redteam) {
                                try {
                                    c.getPlayer().hasflag = true;
                                    c.getPlayer().setCanPickup(false);
                                } catch (NullPointerException manfred) {
                                    //Do nothing
                                }
                            } else {
                                c.getPlayer().dropMessage("One of your party members already has a flag or this isn't your team's flag.");
                                return;
                            }
                        }

                        if (MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true)) {
                            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, c.getPlayer().getId()), mapitem.getPosition());
                            c.getPlayer().getMap().removeMapObject(ob);
                        } else {
                            c.getPlayer().getCheatTracker().pickupComplete();
                            return;
                        }
                    }
                }
            }
            mapitem.setPickedUp(true);
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }
}

