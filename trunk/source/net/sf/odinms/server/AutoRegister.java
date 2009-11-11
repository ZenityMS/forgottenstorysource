package net.sf.odinms.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import net.sf.odinms.client.LoginCrypto;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.login.LoginServer;
import net.sf.odinms.database.DatabaseConnection;

public class AutoRegister {

    public static boolean success;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MapleClient.class);

    public static boolean getAccountExists(String login) {
        boolean accountExists = false;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT name FROM accounts WHERE name = ?");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.first()) {
                accountExists = true;
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            log.warn("Error acquiring the account of (" + login + ") check AutoRegister.");
        }
        return accountExists;
    }

    public static void createAccount(String login, String pwd, String eip) {
        try {
            PreparedStatement ipq = DatabaseConnection.getConnection().prepareStatement("SELECT lastknownip FROM accounts WHERE lastknownip = ?");
            ipq.setString(1, eip.substring(1, eip.lastIndexOf(':')));
            ResultSet rs = ipq.executeQuery();
            if (!rs.first() || rs.last() && rs.getRow() < LoginServer.getInstance().AutoRegLimit()) {
                try {
                    PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO accounts (name, password, email, birthday, macs, lastknownip) VALUES (?, ?, ?, ?, ?, ?)");
                    ps.setString(1, login);
                    ps.setString(2, LoginCrypto.hexShaOne(pwd));
                    ps.setString(3, "no@email.provided");
                    ps.setString(4, "0000-00-00");
                    ps.setString(5, "00-00-00-00-00-00");
                    ps.setString(6, eip.substring(1, eip.lastIndexOf(':')));
                    ps.executeUpdate();
                    ps.close();
                    success = true;
                } catch (Exception ex) {
                    log.warn("Error creating the account of (" + login + " | " + pwd + " | " + eip + ").");
                    ipq.close();
                    rs.close();
                    return;
                }
            }
            ipq.close();
            rs.close();
        } catch (Exception ex) {
            log.warn("Error in creating " + login + "'s account.");
        }
    }
}
