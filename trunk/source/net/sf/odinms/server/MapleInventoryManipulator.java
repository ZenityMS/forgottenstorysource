/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * MapleInventoryManipulator.java
 * 
 * Created on 27. November 2007, 16:19
 */
package net.sf.odinms.server;

import java.awt.Point;
import java.util.Iterator;
import java.util.List;

import net.sf.odinms.client.Equip;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.InventoryException;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleBuffStat;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleJob;
import net.sf.odinms.tools.MaplePacketCreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Matze
 */
public class MapleInventoryManipulator {

    private static Logger log = LoggerFactory.getLogger(MapleInventoryManipulator.class);

    /** Creates a new instance of MapleInventoryManipulator */
    private MapleInventoryManipulator() {
    }

    public static boolean addRing(MapleCharacter chr, int itemId, int ringId) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleInventoryType type = ii.getInventoryType(itemId);
        IItem nEquip = ii.getEquipById(itemId, ringId);

        byte newSlot = chr.getInventory(type).addItem(nEquip);
        if (newSlot == -1) {
            return false;
        }
        chr.getClient().getSession().write(MaplePacketCreator.addInventorySlot(type, nEquip));
        return true;
    }

    public static boolean addById(MapleClient c, int itemId, short quantity) {
        return addById(c, itemId, quantity, null);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner) {
        return addById(c, itemId, quantity, owner, -1);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner, int petid) {
        if (quantity >= 4000 || quantity < 0) {
            AutobanManager.getInstance().autoban(c.getPlayer().getClient(), "PE Item: " + quantity + "x " + itemId);
            return false;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleInventoryType type = ii.getInventoryType(itemId);
        if (!type.equals(MapleInventoryType.EQUIP)) {
            short slotMax = ii.getSlotMax(c, itemId);
            List<IItem> existing = c.getPlayer().getInventory(type).listById(itemId);
            if (!ii.isThrowingStar(itemId) && !ii.isBullet(itemId)) {
                if (existing.size() > 0) { // first update all existing slots to slotMax

                    Iterator<IItem> i = existing.iterator();
                    while (quantity > 0) {
                        if (i.hasNext()) {
                            Item eItem = (Item) i.next();
                            short oldQ = eItem.getQuantity();
                            if (oldQ < slotMax && (eItem.getOwner().equals(owner) || owner == null)) {
                                short newQ = (short) Math.min(oldQ + quantity, slotMax);
                                quantity -= (newQ - oldQ);
                                eItem.setQuantity(newQ);
                                c.getSession().write(MaplePacketCreator.updateInventorySlot(type, eItem));
                            }
                        } else {
                            break;
                        }
                    }
                }
                // add new slots if there is still something left
                while (quantity > 0 || ii.isThrowingStar(itemId) || ii.isBullet(itemId)) {
                    short newQ = (short) Math.min(quantity, slotMax);
                    if (newQ != 0) {
                        quantity -= newQ;
                        Item nItem = new Item(itemId, (byte) 0, newQ, petid);
                        byte newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                        if (newSlot == -1) {
                            c.getSession().write(MaplePacketCreator.getInventoryFull());
                            c.getSession().write(MaplePacketCreator.getShowInventoryFull());
                            return false;
                        }
                        if (owner != null) {
                            nItem.setOwner(owner);
                        }
                        c.getSession().write(MaplePacketCreator.addInventorySlot(type, nItem));
                        if ((ii.isThrowingStar(itemId) || ii.isBullet(itemId)) && quantity == 0) {
                            break;
                        }
                    } else {
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return false;
                    }
                }
            } else {
                // Throwing Stars and Bullets - Add all into one slot regardless of quantity.
                Item nItem = new Item(itemId, (byte) 0, quantity);
                byte newSlot = c.getPlayer().getInventory(type).addItem(nItem);

                if (newSlot == -1) {
                    c.getSession().write(MaplePacketCreator.getInventoryFull());
                    c.getSession().write(MaplePacketCreator.getShowInventoryFull());
                    return false;
                }
                c.getSession().write(MaplePacketCreator.addInventorySlot(type, nItem));
                c.getSession().write(MaplePacketCreator.enableActions());
            }
        } else {
            if (quantity == 1) {
                IItem nEquip = ii.getEquipById(itemId);
                if (owner != null) {
                    nEquip.setOwner(owner);
                }

                byte newSlot = c.getPlayer().getInventory(type).addItem(nEquip);
                if (newSlot == -1) {
                    c.getSession().write(MaplePacketCreator.getInventoryFull());
                    c.getSession().write(MaplePacketCreator.getShowInventoryFull());
                    return false;
                }
                c.getSession().write(MaplePacketCreator.addInventorySlot(type, nEquip));
            } else {
                throw new InventoryException("Trying to create equip with non-one quantity");
            }
        }
        return true;
    }

    public static boolean addFromDrop(MapleClient c, IItem item) {
        return addFromDrop(c, item, true);
    }

    public static boolean addFromDrop(MapleClient c, IItem item, boolean show) {
        return addFromDrop(c, item, show, null);
    }

    public static boolean addFromDrop(MapleClient c, IItem item, boolean show, String owner) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleInventoryType type = ii.getInventoryType(item.getItemId());

        if (!c.getChannelServer().allowMoreThanOne() && ii.isPickupRestricted(item.getItemId()) && c.getPlayer().haveItem(item.getItemId(), 1, true, false)) {
            c.getSession().write(MaplePacketCreator.getInventoryFull());
            c.getSession().write(MaplePacketCreator.showItemUnavailable());
            return false;
        }
        short quantity = item.getQuantity();
        if (quantity >= 4000 || quantity < 0) {
            AutobanManager.getInstance().autoban(c.getPlayer().getClient(), "PE Item: " + quantity + "x " + item.getItemId());
            return false;
        }

        if (!type.equals(MapleInventoryType.EQUIP)) {
            short slotMax = ii.getSlotMax(c, item.getItemId());
            List<IItem> existing = c.getPlayer().getInventory(type).listById(item.getItemId());
            if (!ii.isThrowingStar(item.getItemId()) && !ii.isBullet(item.getItemId())) {
                if (existing.size() > 0) { // first update all existing slots to slotMax

                    Iterator<IItem> i = existing.iterator();
                    while (quantity > 0) {
                        if (i.hasNext()) {
                            Item eItem = (Item) i.next();
                            short oldQ = eItem.getQuantity();
                            if (oldQ < slotMax && item.getOwner().equals(eItem.getOwner())) {
                                short newQ = (short) Math.min(oldQ + quantity, slotMax);
                                quantity -= (newQ - oldQ);
                                eItem.setQuantity(newQ);
                                c.getSession().write(MaplePacketCreator.updateInventorySlot(type, eItem, true));
                            }
                        } else {
                            break;
                        }
                    }
                }
                // add new slots if there is still something left
                while (quantity > 0 || ii.isThrowingStar(item.getItemId()) || ii.isBullet(item.getItemId())) {
                    short newQ = (short) Math.min(quantity, slotMax);
                    quantity -= newQ;
                    Item nItem = new Item(item.getItemId(), (byte) 0, newQ);
                    nItem.setOwner(item.getOwner());
                    byte newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                    if (newSlot == -1) {
                        c.getSession().write(MaplePacketCreator.getInventoryFull());
                        c.getSession().write(MaplePacketCreator.getShowInventoryFull());
                        item.setQuantity((short) (quantity + newQ));
                        return false;
                    }
                    c.getSession().write(MaplePacketCreator.addInventorySlot(type, nItem, true));
                }
            } else {
                // Throwing Stars and Bullets - Add all into one slot regardless of quantity.
                Item nItem = new Item(item.getItemId(), (byte) 0, quantity);
                byte newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                if (newSlot == -1) {
                    c.getSession().write(MaplePacketCreator.getInventoryFull());
                    c.getSession().write(MaplePacketCreator.getShowInventoryFull());
                    return false;
                }
                c.getSession().write(MaplePacketCreator.addInventorySlot(type, nItem));
                c.getSession().write(MaplePacketCreator.enableActions());
            }
        } else {
            if (quantity == 1) {
                byte newSlot = c.getPlayer().getInventory(type).addItem(item);

                if (newSlot == -1) {
                    c.getSession().write(MaplePacketCreator.getInventoryFull());
                    c.getSession().write(MaplePacketCreator.getShowInventoryFull());
                    return false;
                }
                c.getSession().write(MaplePacketCreator.addInventorySlot(type, item, true));
            } else {
                throw new RuntimeException("Trying to create equip with non-one quantity");
            }
        }
        if (owner != null) {
            item.setOwner(owner);
        }
        if (show) {
            c.getSession().write(MaplePacketCreator.getShowItemGain(item.getItemId(), item.getQuantity()));
        }
        return true;
    }

    public static boolean checkSpace(MapleClient c, int itemid, int quantity, String owner) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleInventoryType type = ii.getInventoryType(itemid);

        if (!type.equals(MapleInventoryType.EQUIP)) {
            short slotMax = ii.getSlotMax(c, itemid);
            List<IItem> existing = c.getPlayer().getInventory(type).listById(itemid);
            if (!ii.isThrowingStar(itemid) && !ii.isBullet(itemid)) {
                if (existing.size() > 0) { // first update all existing slots to slotMax

                    for (IItem eItem : existing) {
                        short oldQ = eItem.getQuantity();
                        if (oldQ < slotMax && owner.equals(eItem.getOwner())) {
                            short newQ = (short) Math.min(oldQ + quantity, slotMax);
                            quantity -= (newQ - oldQ);
                        }
                        if (quantity <= 0) {
                            break;
                        }
                    }
                }
            }
            // add new slots if there is still something left
            final int numSlotsNeeded;
            if (slotMax > 0) {
                numSlotsNeeded = (int) (Math.ceil(((double) quantity) / slotMax));
            } else if (ii.isThrowingStar(itemid) || ii.isBullet(itemid)) {
                numSlotsNeeded = 1;
            } else {
                numSlotsNeeded = 1;
                log.error("SUCK ERROR - FIX ME! - 0 slotMax");
            }
            return !c.getPlayer().getInventory(type).isFull(numSlotsNeeded - 1);
        } else {
            return !c.getPlayer().getInventory(type).isFull();
        }
    }

    public static void removeFromSlot(MapleClient c, MapleInventoryType type, byte slot, short quantity, boolean fromDrop) {
        removeFromSlot(c, type, slot, quantity, fromDrop, false);
    }

    public static void removeFromSlot(MapleClient c, MapleInventoryType type, byte slot, short quantity, boolean fromDrop, boolean consume) {
        IItem item = c.getPlayer().getInventory(type).getItem(slot);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        boolean allowZero = consume && (ii.isThrowingStar(item.getItemId()) || ii.isBullet(item.getItemId()));
        c.getPlayer().getInventory(type).removeItem(slot, quantity, allowZero);
        if (item.getQuantity() == 0 && !allowZero) {
            c.getSession().write(MaplePacketCreator.clearInventoryItem(type, item.getPosition(), fromDrop));
        } else {
            c.getSession().write(MaplePacketCreator.updateInventorySlot(type, (Item) item, fromDrop));
        }
    }

    public static int editEquipById(MapleCharacter chr, int max, int itemid, String stat, int newval) {
        return editEquipById(chr,max,itemid,stat,(short)newval);
    }

    public static int editEquipById(MapleCharacter chr, int max, int itemid, String stat, short newval) {
        // Is it an equip?
        if (!MapleItemInformationProvider.getInstance().isEquip(itemid)) {
            return -1;
        }

        // Get List
        List<IItem> equips = chr.getInventory(MapleInventoryType.EQUIP).listById(itemid);
        List<IItem> equipped = chr.getInventory(MapleInventoryType.EQUIPPED).listById(itemid);

        // Do you have any?
        if (equips.size() == 0 && equipped.size() == 0) {
            return 0;
        }

        int edited = 0;

        // edit items
        for(IItem itm : equips) {
            Equip e = (Equip)itm;
            if (edited >= max) {
                break;
            }
            edited++;
            if (stat.equals("str")) {
                e.setStr(newval);
            } else if (stat.equals("dex")) {
                e.setDex(newval);
            } else if (stat.equals("int")) {
                e.setInt(newval);
            } else if (stat.equals("luk")) {
                e.setLuk(newval);
            } else if (stat.equals("watk")) {
                e.setWatk(newval);
            } else if (stat.equals("matk")) {
                e.setMatk(newval);
            } else {
                return -2;
            }
        }
        for(IItem itm : equipped) {
            Equip e = (Equip)itm;
            if (edited >= max) {
                break;
            }
            edited++;
            if (stat.equals("str")) {
                e.setStr(newval);
            } else if (stat.equals("dex")) {
                e.setDex(newval);
            } else if (stat.equals("int")) {
                e.setInt(newval);
            } else if (stat.equals("luk")) {
                e.setLuk(newval);
            } else if (stat.equals("watk")) {
                e.setWatk(newval);
            } else if (stat.equals("matk")) {
                e.setMatk(newval);
            } else {
                return -2;
            }
        }

        // Return items edited
        return (edited);
    }

    public static void removeById(MapleClient c, MapleInventoryType type, int itemId, int quantity, boolean fromDrop, boolean consume) {
        List<IItem> items = c.getPlayer().getInventory(type).listById(itemId);
        int remremove = quantity;
        for (IItem item : items) {
            if (remremove <= item.getQuantity()) {
                removeFromSlot(c, type, item.getPosition(), (short) remremove, fromDrop, consume);
                remremove = 0;
                break;
            } else {
                remremove -= item.getQuantity();
                removeFromSlot(c, type, item.getPosition(), item.getQuantity(), fromDrop, consume);
            }
        }
        if (remremove > 0) {
            throw new InventoryException("[h4x] Not enough cheese available (" + itemId + ", " + (quantity - remremove) +
                    "/" + quantity + ")");
        }
    }

    public static void removeAllById(MapleClient c, int itemId, boolean checkEquipped) {
        MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(itemId);
        for (IItem item : c.getPlayer().getInventory(type).listById(itemId)) {
            if (item != null) {
                removeFromSlot(c, type, item.getPosition(), item.getQuantity(), true, false);
            }
        }
        if (checkEquipped) {
            IItem ii = c.getPlayer().getInventory(type).findById(itemId);
            if (ii != null) {
                c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeItem(ii.getPosition());
                c.getPlayer().equipChanged();
            }
        }
    }

    public static void move(MapleClient c, MapleInventoryType type, byte src, byte dst) {
        if (src < 0 || dst < 0) {
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        IItem source = c.getPlayer().getInventory(type).getItem(src);
        IItem initialTarget = c.getPlayer().getInventory(type).getItem(dst);
        if (source == null) {
            return;
        }
        short olddstQ = -1;
        if (initialTarget != null) {
            olddstQ = initialTarget.getQuantity();
        }
        short oldsrcQ = source.getQuantity();
        short slotMax = ii.getSlotMax(c, source.getItemId());
        c.getPlayer().getInventory(type).move(src, dst, slotMax);
        if (!type.equals(MapleInventoryType.EQUIP) && initialTarget != null && initialTarget.getItemId() == source.getItemId() && !ii.isThrowingStar(source.getItemId()) && !ii.isBullet(source.getItemId())) {
            if ((olddstQ + oldsrcQ) > slotMax) {
                c.getSession().write(MaplePacketCreator.moveAndMergeWithRestInventoryItem(type, src, dst, (short) ((olddstQ + oldsrcQ) - slotMax), slotMax));
            } else {
                c.getSession().write(MaplePacketCreator.moveAndMergeInventoryItem(type, src, dst, ((Item) c.getPlayer().getInventory(type).getItem(dst)).getQuantity()));
            }
        } else {
            c.getSession().write(MaplePacketCreator.moveInventoryItem(type, src, dst));
        }
    }

    public static void equip(MapleClient c, byte src, byte dst) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Equip source = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(src);
        Equip target = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
        if (source == null) {
            return;
        }
        if (!c.getPlayer().isGM() && !c.getChannelServer().CanGMItem()) {
            switch (source.getItemId()) {
                case 1002140: // Wizet Invincible Hat
                case 1042003: // Wizet Plain Suit
                case 1062007: // Wizet Plain Suit Pants
                case 1322013: // Wizet Secret Agent Suitcase
                    removeAllById(c, source.getItemId(), false);
                    c.getPlayer().dropMessage(1, "You're not a GM");
                    return;
            }
        }

        if (source.getItemId() == 1302065 || source.getItemId() == 1302033) {
            c.getPlayer().dropMessage(6, "You cannot equip the Capture the Flag items.");
            return;
        }
        
        if (source.getItemId() == 1812006) {
            removeAllById(c, source.getItemId(), false);
            c.getPlayer().dropMessage(1, "Magic Scale Has Been Blocked");
            return;
        }

        if (source.getItemId() == 1002425 && c.getPlayer().getLevel() == 200) {
            c.getPlayer().setLevel(2);
            c.getPlayer().setJob(0);
            c.getPlayer().setReborns(1);
            c.getPlayer().doReborn();
            c.getPlayer().setRebirthPoints(c.getPlayer().getRebirthPoints() + 1);
            c.getPlayer().dropMessage("Congratulations on your rebirth! I have given you one rebrth point.");
        }

        if (source.getItemId() == 1002083 && c.getPlayer().getLevel() >= 120) {
            c.getPlayer().saveToDB(true, true);
            c.getPlayer().changeJob(MapleJob.getById(412));// Night Lord
            removeAllById(c, source.getItemId(), false);
        }
        if (source.getItemId() == 1002082 && c.getPlayer().getLevel() >= 120) {
            c.getPlayer().saveToDB(true, true);
            c.getPlayer().changeJob(MapleJob.getById(422));// Bandit
            removeAllById(c, source.getItemId(), false);
        }
        if (source.getItemId() == 1002081 && c.getPlayer().getLevel() >= 120) {
            c.getPlayer().saveToDB(true, true);
            c.getPlayer().changeJob(MapleJob.getById(112));//hero
            removeAllById(c, source.getItemId(), false);
        }
        if (source.getItemId() == 1002080 && c.getPlayer().getLevel() >= 120) {
            c.getPlayer().saveToDB(true, true);
            c.getPlayer().changeJob(MapleJob.getById(122));//paladin
            removeAllById(c, source.getItemId(), false);
        }
        if (source.getItemId() == 1002393 && c.getPlayer().getLevel() >= 120) {
            c.getPlayer().saveToDB(true, true);
            c.getPlayer().changeJob(MapleJob.getById(132));//DK
            removeAllById(c, source.getItemId(), false);
        }
        if (source.getItemId() == 1002394 && c.getPlayer().getLevel() >= 120) {
            c.getPlayer().saveToDB(true, true);
            c.getPlayer().changeJob(MapleJob.getById(212));//Bishop
            removeAllById(c, source.getItemId(), false);
        }
        if (source.getItemId() == 1002392 && c.getPlayer().getLevel() >= 120) {
            c.getPlayer().saveToDB(true, true);
            c.getPlayer().changeJob(MapleJob.getById(222));// FP mage
            removeAllById(c, source.getItemId(), false);
        }
        if (source.getItemId() == 1002391 && c.getPlayer().getLevel() >= 120) {
            c.getPlayer().saveToDB(true, true);
            c.getPlayer().changeJob(MapleJob.getById(232));//IL mage
            removeAllById(c, source.getItemId(), false);
        }
        if (source.getItemId() == 1002395 && c.getPlayer().getLevel() >= 120) {
            c.getPlayer().saveToDB(true, true);
            c.getPlayer().changeJob(MapleJob.getById(512));// brawler
            removeAllById(c, source.getItemId(), false);
        }
        if (source.getItemId() == 1002515 && c.getPlayer().getLevel() >= 120) {
            c.getPlayer().saveToDB(true, true);
            c.getPlayer().changeJob(MapleJob.getById(312));// bowman
            removeAllById(c, source.getItemId(), false);
        }
        if (source.getItemId() == 1002397 && c.getPlayer().getLevel() >= 120) {
            c.getPlayer().saveToDB(true, true);
            c.getPlayer().changeJob(MapleJob.getById(322));// xbow
            removeAllById(c, source.getItemId(), false);
        }

        if (dst == -6) {
            // unequip the overall
            IItem top = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -5);
            if (top != null && ii.isOverall(top.getItemId())) {
                if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()) {
                    c.getSession().write(MaplePacketCreator.getInventoryFull());
                    c.getSession().write(MaplePacketCreator.getShowInventoryFull());
                    return;
                }
                unequip(c, (byte) -5, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
            }
        } else if (dst == -5) {
            // unequip the bottom and top
            IItem top = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -5);
            IItem bottom = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -6);
            if (top != null && ii.isOverall(source.getItemId())) {
                if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).isFull(bottom != null && ii.isOverall(source.getItemId()) ? 1 : 0)) {
                    c.getSession().write(MaplePacketCreator.getInventoryFull());
                    c.getSession().write(MaplePacketCreator.getShowInventoryFull());
                    return;
                }
                unequip(c, (byte) -5, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
            }
            if (bottom != null && ii.isOverall(source.getItemId())) {
                if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()) {
                    c.getSession().write(MaplePacketCreator.getInventoryFull());
                    c.getSession().write(MaplePacketCreator.getShowInventoryFull());
                    return;
                }
                unequip(c, (byte) -6, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
            }
        } else if (dst == -10) {
            // check if weapon is two-handed
            IItem weapon = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
            if (weapon != null && ii.isTwoHanded(weapon.getItemId())) {
                if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()) {
                    c.getSession().write(MaplePacketCreator.getInventoryFull());
                    c.getSession().write(MaplePacketCreator.getShowInventoryFull());
                    return;
                }
                unequip(c, (byte) -11, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
            }
        } else if (dst == -11) {
            IItem shield = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -10);
            if (shield != null && ii.isTwoHanded(source.getItemId())) {
                if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()) {
                    c.getSession().write(MaplePacketCreator.getInventoryFull());
                    c.getSession().write(MaplePacketCreator.getShowInventoryFull());
                    return;
                }
                unequip(c, (byte) -10, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
            }
        } else if (dst == -18) {
            c.getPlayer().getMount().setItemId(source.getItemId());
        }
        source = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(src);
        target = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
        c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeSlot(src);
        if (target != null) {
            c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeSlot(dst);
        }
        source.setPosition(dst);
        c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).addFromDB(source);
        if (target != null) {
            target.setPosition(src);
            c.getPlayer().getInventory(MapleInventoryType.EQUIP).addFromDB(target);
        }
        if (c.getPlayer().getBuffedValue(MapleBuffStat.BOOSTER) != null && ii.isWeapon(source.getItemId())) {
            c.getPlayer().cancelBuffStats(MapleBuffStat.BOOSTER);
        }
        c.getSession().write(MaplePacketCreator.moveInventoryItem(MapleInventoryType.EQUIP, src, dst, (byte) 2));
        c.getPlayer().equipChanged();
    }

    public static void unequip(MapleClient c, byte src, byte dst) {
        Equip source = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(src);
        Equip target = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(dst);
        if (dst < 0) {
            log.warn("Unequipping to negative slot. ({}: {}->{})", new Object[]{c.getPlayer().getName(), src, dst});
        }
        if (source == null) {
            return;
        }
        if (target != null && src <= 0) {
            // do not allow switching with equip
            c.getSession().write(MaplePacketCreator.getInventoryFull());
            return;
        }
        c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeSlot(src);
        if (target != null) {
            c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeSlot(dst);
        }
        source.setPosition(dst);
        c.getPlayer().getInventory(MapleInventoryType.EQUIP).addFromDB(source);
        if (target != null) {
            target.setPosition(src);
            c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).addFromDB(target);
        }
        c.getSession().write(MaplePacketCreator.moveInventoryItem(MapleInventoryType.EQUIP, src, dst, (byte) 1));
        c.getPlayer().equipChanged();
    }

    public static void drop(MapleClient c, MapleInventoryType type, byte src, short quantity) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (src < 0) {
            type = MapleInventoryType.EQUIPPED;
        }
        IItem source = c.getPlayer().getInventory(type).getItem(src);
        int itemId = source.getItemId();
        if (itemId == 1302065 || itemId == 1302033) {
            c.getPlayer().hasflag = false;
            c.getPlayer().setCanPickup(true);
        }

        if (itemId == 1112000) {
            c.getPlayer().dropMessage("Super Rebirth rings cannot be dropped or traded.");
            return;
        }

        if (itemId == 1012076) {
            c.getPlayer().dropMessage("The Beta Mask cannot be traded or dropped.");
            return;
        } 

        if (c.getPlayer().getItemEffect() == itemId && source.getQuantity() == 1) {
            c.getPlayer().setItemEffect(0);
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.itemEffect(c.getPlayer().getId(), 0));
        } else if (itemId == 5370000 || itemId == 5370001) {
            if (c.getPlayer().getItemQuantity(itemId, false) == 1) {
                c.getPlayer().setChalkboard(null);
            }
        }

        if (itemId == 4000076) {
            c.getPlayer().mapCheck();
        }

        if (quantity < 0 || source == null || quantity == 0 && !ii.isThrowingStar(itemId) && !ii.isBullet(itemId)) {
            String message = "Dropping " + quantity + " " + (source == null ? "?" : itemId) + " (" +
                    type.name() + "/" + src + ")";
            //AutobanManager.getInstance().addPoints(c, 1000, 0, message);
            log.info(MapleClient.getLogMessage(c, message));
            c.getSession().close(); // disconnect the client as is inventory is inconsistent with the serverside inventory -> fuck

            return;
        }
        Point dropPos = new Point(c.getPlayer().getPosition());
        //dropPos.y -= 99;
        if (quantity < source.getQuantity() && !ii.isThrowingStar(itemId) && !ii.isBullet(itemId)) {
            IItem target = source.copy();
            target.setQuantity(quantity);
            source.setQuantity((short) (source.getQuantity() - quantity));
            c.getSession().write(MaplePacketCreator.dropInventoryItemUpdate(type, source));
            boolean weddingRing = source.getItemId() == 1112803 || source.getItemId() == 1112806 || source.getItemId() == 1112807 || source.getItemId() == 1112809;
            if (weddingRing) {
                c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos);
            } else if (c.getPlayer().getMap().getEverlast()) {
                if (!c.getChannelServer().allowUndroppablesDrop() && ii.isDropRestricted(target.getItemId())) {
                    c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos);
                } else {
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, false);
                }
            } else {
                if (!c.getChannelServer().allowUndroppablesDrop() && ii.isDropRestricted(target.getItemId())) {
                    c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos);
                } else {
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
                }
            }
        } else {
            c.getPlayer().getInventory(type).removeSlot(src);
            c.getSession().write(MaplePacketCreator.dropInventoryItem(
                    (src < 0 ? MapleInventoryType.EQUIP : type), src));
            if (src < 0) {
                c.getPlayer().equipChanged();
            }
            if (c.getPlayer().getMap().getEverlast()) {
                if (!c.getChannelServer().allowUndroppablesDrop() && ii.isDropRestricted(itemId)) {
                    c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos);
                } else {
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos, true, false);
                }
            } else {
                if (!c.getChannelServer().allowUndroppablesDrop() && ii.isDropRestricted(itemId)) {
                    c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos);
                } else {
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos, true, true);
                }
            }
        }
    }
}
