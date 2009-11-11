package net.sf.odinms.net.world.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.net.channel.remote.ChannelWorldInterface;
import net.sf.odinms.net.login.remote.LoginWorldInterface;
import net.sf.odinms.net.world.guild.MapleGuild;
import net.sf.odinms.net.world.guild.Territory;
import net.sf.odinms.net.world.guild.TerritoryStorage;

public interface WorldRegistry extends Remote {

    public WorldChannelInterface registerChannelServer(String authKey, ChannelWorldInterface cb) throws RemoteException;
    public void deregisterChannelServer(int channel) throws RemoteException;
    public WorldLoginInterface registerLoginServer(String authKey, LoginWorldInterface cb) throws RemoteException;
    public void deregisterLoginServer(LoginWorldInterface cb) throws RemoteException;
    public String getStatus() throws RemoteException;
    public void updateTerritory(Territory t, int id) throws RemoteException;
    public void resetTerritoryPoints(int id) throws RemoteException;
    public TerritoryStorage getTerritoryStorage() throws RemoteException;
    public void broadcastPacketToWorld(MaplePacket packet) throws RemoteException;
    public MapleGuild getGuildById(int guildid) throws RemoteException;
}
