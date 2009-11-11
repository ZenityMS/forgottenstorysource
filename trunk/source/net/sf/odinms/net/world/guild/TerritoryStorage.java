package net.sf.odinms.net.world.guild;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.odinms.database.DatabaseConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerritoryStorage implements Serializable {

    private Map territories = new LinkedHashMap<Integer, Territory>();
    private TerritoryMonster[] monsters;

    public TerritoryStorage() {
    }

    public Territory getTerritoryById(int id) {
        return (Territory) territories.get(id);
    }

    public Territory[] getTerritories() {
        return (Territory[]) territories.values().toArray(new Territory[territories.size()]);
    }

    public TerritoryMonster[] getAllMonsters() {
        return monsters;
    }

    public TerritoryMonster[] getMonsters(int territory) {
        ArrayList<TerritoryMonster> ret = new ArrayList<TerritoryMonster>();

        for (int i = 0; i < monsters.length; i++) {
            if (monsters[i].getTerritoryId() == territory) {
                ret.add(monsters[i]);
            }
        }

        return ret.toArray(new TerritoryMonster[ret.size()]);
    }

    public void updateTerritory(Territory t, int id) {
        territories.put(id, t);
    }

    public TerritoryMonster getMonsterByMonsterId(int monsterid) {
        for (int i = 0; i < monsters.length; i++) {
            if (monsters[i].getMonsterId() == monsterid) {
                return monsters[i];
            }
        }

        return null;
    }

    public void loadTerritoriesFromDb() {
        System.out.println("Loading territories from db.");

        Logger log = LoggerFactory.getLogger(this.getClass());

        Connection con;

        try {
            con = DatabaseConnection.getConnection();
        } catch (Exception e) {
            log.error("Unable to connect to database to read territory information.", e);
            return;
        }

        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM territories");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.println("Loading territory " + rs.getString("name"));
                territories.put(rs.getInt("id"), new Territory(rs.getInt("id"), rs.getString("name"), rs.getInt("guildid"), rs.getInt("level"), rs.getInt("mapid"), rs.getInt("capturepoints"), this));
            }

            ps.close();
            rs.close();
        } catch (SQLException se) {
            log.error("Unable to read territory information from database", se);
            return;
        }
    }

    public void loadMonstersFromDb() {
        System.out.println("Loading territory monsters from db...");

        Logger log = LoggerFactory.getLogger(this.getClass());
        ArrayList<TerritoryMonster> loadedMonsters = new ArrayList<TerritoryMonster>();

        Connection con;

        try {
            con = DatabaseConnection.getConnection();
        } catch (Exception e) {
            log.error("Unable to connect to database to read territory information.", e);
            return;
        }

        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM territorymonsters");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.println("Loading monster " + rs.getString("monsterid") + " for territory " + rs.getString("territoryid"));
                loadedMonsters.add(new TerritoryMonster(rs.getInt("id"), rs.getInt("monsterid"), rs.getInt("territoryid"), rs.getInt("points")));
            }

            ps.close();
            rs.close();
        } catch (SQLException se) {
            log.error("Unable to read territory information from database", se);
            return;
        }

        monsters = new TerritoryMonster[loadedMonsters.size()];
        loadedMonsters.toArray(monsters);
    }

    public void saveEverything() {
    }
}
