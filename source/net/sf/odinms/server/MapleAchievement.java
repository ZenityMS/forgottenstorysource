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

import java.rmi.RemoteException;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.server.MapleAchievements;

/**
 *
 * @author Patrick/PurpleMadness
 */
public class MapleAchievement {
    private String name;
    MapleClient c;
    private int reward;
    private int reward2;
    public MapleAchievements ach;
    private boolean notice;
    private boolean prizemeso;
    private int reward3;
    
    public MapleAchievement(String name, int reward, int reward2){
        this.name = name;
        this.reward = reward;
        this.reward2 = reward2;
        this.notice = true;
    }
    
    public MapleAchievement(String name, int reward, int reward2, boolean notice){
       this.name = name;
       this.reward = reward;
       this.reward2 = reward2;
       this.notice = notice;
    }

      public MapleAchievement(String name, int reward, int reward2, boolean notice, boolean prizemeso, int reward3){
       this.name = name;
       this.reward = reward;
       this.reward2 = reward2;
       this.notice = notice;
       this.prizemeso = prizemeso;
       this.reward3 = reward3;
    }

    public String getName(){
        return name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public int getReward(){
        return reward;
    }

        public int getReward2(){
        return reward2;
    }
    
    public void setReward(int reward){
        this.reward = reward;
    }

        public void setReward2(int reward2){
        this.reward = reward2;
    }
    
    public void finishAchievement(MapleCharacter player){
        try {
          // MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
          // IItem item = ii.getEquipById(?);
         //  MapleInventoryManipulator.addFromDrop(c, item, true);
            player.setPassionPoints(player.getPassionPoints() + reward2);
            player.modifyCSPoints(4, reward);
            player.setAchievementFinished(MapleAchievements.getInstance().getByMapleAchievement(this));
            player.getClient().getSession().write(MaplePacketCreator.serverNotice(5, "[Achievement] You've gained " + reward + " NX and " + reward2 + " Passion Points as you " + name + "."));
            if (prizemeso) {
                if (reward3 + player.getMeso() < Integer.MAX_VALUE) {
                    player.gainMeso(reward3);
                } else {
                    player.setMeso(2147483647);
                }
            }
            if (notice && !player.isGM())
                ChannelServer.getInstance(player.getClient().getChannel()).getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, "[Achievement] Congratulations to " + player.getName() + " as they just " + name + "!").getBytes());
        } catch (RemoteException e) {
            player.getClient().getChannelServer().reconnectWorld();
        }
    }
}  