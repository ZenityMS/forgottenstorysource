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
package net.sf.odinms.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.database.DatabaseConnection;

public class ClanHolder {

    private Map<MapleCharacter, Integer> online = new LinkedHashMap<MapleCharacter, Integer>(); // Only for each channel sadly..
    private static Map<String, Integer> offline = new LinkedHashMap<String, Integer>(); // Only contains name..

    public void registerPlayer(MapleCharacter chr) {
        if (!offline.containsKey(chr)) {
            offline.put(chr.getName(), chr.getClan());
        }
        if (!online.containsKey(chr)) {
            online.put(chr, chr.getClan());
        }

    }

    public void playerOnline(MapleCharacter chr) {
        online.put(chr, chr.getClan());
    }

    public void deregisterPlayer(MapleCharacter chr) {
        online.remove(chr);
    }

    public static int countOfflineByClan(int clan) {
        int size = 0;
        for (String name : offline.keySet()) {
            if (offline.get(name) == clan) {
                size++;
            }
        }
        return size;
    }

    public int countOnlineByClan(int clan) {
        int size = 0;
        for (MapleCharacter chr : online.keySet()) {
            if (online.get(chr) == clan) {
                size++;
            }
        }
        return size;
    }

    public List<MapleCharacter> getAllOnlinePlayersFromClan(int clan) {
        List<MapleCharacter> players = new LinkedList<MapleCharacter>();
        for (MapleCharacter player : online.keySet()) {
            if (online.get(player) == clan) {
                players.add(player);
            }
        }
        return players;
    }

    public List<String> getAllOfflinePlayersFromClan(int clan) {
        List<String> players = new LinkedList<String>();
        for (String name : offline.keySet()) {
            if (offline.get(name) == clan) {
                players.add(name);
            }
        }
        return players;
    }

    public static void loadAllClans() {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT name, clan FROM characters WHERE clan >= 0");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                offline.put(rs.getString("name"), rs.getInt("clan"));
			}
            ps.close();
            rs.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
}
