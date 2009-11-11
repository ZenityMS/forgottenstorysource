/*
 * This file is part of the OdinMS Maple Story Server
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

package net.sf.odinms.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.odinms.provider.MapleData;
import net.sf.odinms.provider.MapleDataProvider;
import net.sf.odinms.provider.MapleDataProviderFactory;
import net.sf.odinms.provider.MapleDataTool;
import net.sf.odinms.tools.StringUtil;

/**
 *
 * @author Lerk
 * Edited by : Xerixe
 *
 */

public class CashItemFactory {
    private static Map<Integer, Integer> snLookup = new HashMap<Integer,Integer>();
    private static Map<Integer, CashItemInfo> itemStats = new HashMap<Integer, CashItemInfo>();
    private static MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Etc.wz"));
    private static MapleData commodities = data.getData(StringUtil.getLeftPaddedStr("Commodity.img", '0', 11));

    public static CashItemInfo getItem(int sn) {
        if (itemStats.containsKey(sn)) {
            return itemStats.get(sn);
        }
        CashItemInfo stats = itemStats.get(sn);
        int cid = getCommodityFromSN(sn);
        int itemId = MapleDataTool.getIntConvert(cid + "/ItemId",commodities);
        int count = MapleDataTool.getIntConvert(cid + "/Count",commodities,1);
        int price = MapleDataTool.getIntConvert(cid + "/Price",commodities,0);
        stats = new CashItemInfo(itemId, count, price);
        itemStats.put(sn, stats);
        return stats;
    }

    private static int getCommodityFromSN(int sn) {
        if (snLookup.containsKey(sn))
            return snLookup.get(sn);
        int curr = snLookup.size() - 1;
        int currSN = 0;
        if (curr == -1) {
            curr = 0;
            currSN = MapleDataTool.getIntConvert("0/SN",commodities);
            snLookup.put(currSN, curr);
        }
        for (int i = snLookup.size() - 1; currSN != sn; i++) {
            curr = i;
            currSN = MapleDataTool.getIntConvert(curr + "/SN",commodities);
            snLookup.put(currSN, curr);
        }
        return curr;
    }

    public static List<Integer> getPackageItems(int itemId) {
        List<Integer> packageItems = new ArrayList<Integer>();
        MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File("wz/Etc.wz"));
        MapleData a = dataProvider.getData("CashPackage.img");
        if (packageItems.contains(itemId))
            return packageItems;
        for (MapleData b : a.getChildren()) {
            if (itemId == Integer.parseInt(b.getName())) {
                for (MapleData c : b.getChildren()) {
                    for (MapleData d : c.getChildren()) {
                        int sn = MapleDataTool.getIntConvert("" + Integer.parseInt(d.getName()), c);
                        CashItemInfo item = getItem(sn);
                        packageItems.add(item.getId());
                    }
                }
                break;
            }
        }
        return packageItems;
    }

    public static void clearCache() {
        itemStats.clear();
        snLookup.clear();
    }
}