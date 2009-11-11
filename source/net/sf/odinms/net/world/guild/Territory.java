package net.sf.odinms.net.world.guild;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.net.world.remote.WorldRegistry;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import net.sf.odinms.database.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Territory implements Serializable {

    private int id;
    private int ownerId;
    private int ownerLevel;
    private String name;
    private int townMapId;
    private int capturePoints;
    private TerritoryStorage storage;

    public Territory(int id, String name, int ownerId, int ownerLevel, int townMapId, int capturePoints, TerritoryStorage storage) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerLevel = ownerLevel;
        this.name = name;
        this.townMapId = townMapId;
        this.capturePoints = capturePoints;
        this.storage = storage;
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getOwnerLevel() {
        return ownerLevel;
    }

    public String getName() {
        return name;
    }

    public int getTownMapId() {
        return townMapId;
    }

    public int getCapturePoints() {
        return capturePoints;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setOwnerLevel(int level) {
        this.ownerLevel = level;
    }

    public WorldRegistry getWorldRegistry() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
            WorldRegistry worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
            return worldRegistry;
        } catch (Exception r) {
            r.printStackTrace();
            return null;
        }
    }

    private void resetDbPointsInternal() {
        //Then wipe the points from the database...
        Connection con;
        Logger log = LoggerFactory.getLogger(this.getClass());

        try {
            con = DatabaseConnection.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            PreparedStatement ps = null;
            ps = con.prepareStatement("UPDATE territorypoints SET points = " + 0 + " WHERE territoryid = " + id);
            ps.execute();
            ps.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void resetPointsForCapture(MaplePacket broadcast) {
        resetDbPointsInternal();

        WorldRegistry worldRegistry = getWorldRegistry();
        try {
            worldRegistry.resetTerritoryPoints(id);

            if (broadcast != null) {
                worldRegistry.broadcastPacketToWorld(broadcast);
            }
        } catch (Exception r) {
            r.printStackTrace();
        }
    }

    public void territoryCaptureMessage(MaplePacket broadcast) {
        WorldRegistry worldRegistry = getWorldRegistry();
        try {
            if (broadcast != null) {
                worldRegistry.broadcastPacketToWorld(broadcast);
            }
        } catch (Exception r) {
            r.printStackTrace();

        }
    }

    public void saveToDb() {
        Connection con;
        Logger log = LoggerFactory.getLogger(this.getClass());

        try {
            con = DatabaseConnection.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            PreparedStatement ps = null;
            ps = con.prepareStatement("UPDATE territories SET guildid = " + ownerId + ", level = " + ownerLevel + " WHERE id = " + id);
            ps.execute();
            ps.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public String getOwnerName() {
        MapleGuild guild = null;

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
            WorldRegistry worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");

            guild = worldRegistry.getGuildById(ownerId);
        } catch (Exception r) {
            r.printStackTrace();
        }

        if (guild == null) {
            return getOwnerNameFromDb();
        } else {
            return guild.getName();
        }
    }

    public String getOwnerNameFromDb() {
        Connection con;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        try {
            ps = con.prepareStatement("SELECT * FROM guilds WHERE guildid = " + ownerId);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException se) {
            se.printStackTrace();
            return null;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException sqle) {
            }
        }

        return null;
    }

    public int getTerritoryOwnerFromDb(int whicht) {
        Connection con;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

        try {
            ps = con.prepareStatement("SELECT * FROM territories WHERE guildid = " + ownerId + " AND id = '?'");
            ps.setInt(1, whicht);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("guildid");
            }
        } catch (SQLException se) {
            se.printStackTrace();
            return 0;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
            }
        }

        return 0;
    }
}
