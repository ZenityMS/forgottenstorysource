
package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.scripting.quest.QuestScriptManager;
import net.sf.odinms.server.quest.MapleQuest;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class QuestActionHandler extends AbstractMaplePacketHandler {

    //Creates a new instance of QuestActionHandler
    public QuestActionHandler() {
    }

    //the wz files has startscript and endscript but its not like we care about it anyways
    //for now we just use questid.js with functions start and end in them
    //instead of creating two separate scripts
    //and questaction extends npcconversation, since you cant have both at once
    //and questaction is basically an npc and thus can use the same functions
    //this class only starts the script itself, whereas npcmoretalkhandler extends it
    //but more on that later
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getPlayer().resetAfkTime();
        // [62 00] [01] [69 08] [86 71 0F 00] 7D 09 3E 02
        byte action = slea.readByte();
        short quest = slea.readShort();
        MapleCharacter player = c.getPlayer();
        if (action == 1) { // start quest
            int npc = slea.readInt();
            slea.readInt(); // ?
            MapleQuest.getInstance(quest).start(player, npc);
        } else if (action == 2) { // complete quest
            int npc = slea.readInt();
            slea.readInt(); // dont know *o*
            if (slea.available() >= 4) {
                int selection = slea.readInt();
                MapleQuest.getInstance(quest).complete(player, npc, selection);
            } else {
                MapleQuest.getInstance(quest).complete(player, npc);
            }
            c.getSession().write(MaplePacketCreator.showOwnBuffEffect(0, 9)); // Quest completion
            player.getMap().broadcastMessage(player, MaplePacketCreator.showBuffeffect(player.getId(), 0, 9, (byte) 0), false);
        // 6 = start quest
        // 7 = unknown error
        // 8 = equip is full
        // 9 = not enough mesos
        // 11 = due to the equipment currently being worn wtf o.o
        // 12 = you may not posess more than one of this item
        } else if (action == 3) { // forfeit quest
            MapleQuest.getInstance(quest).forfeit(player);
        } else if (action == 4) { // scripted start quest
            int npc = slea.readInt();
            slea.readInt(); // ?
            QuestScriptManager.getInstance().start(c, npc, quest);
        } else if (action == 5) { // scripted end quests
            int npc = slea.readInt();
            slea.readInt(); // ?
            QuestScriptManager.getInstance().end(c, npc, quest);
        }
    }
}
