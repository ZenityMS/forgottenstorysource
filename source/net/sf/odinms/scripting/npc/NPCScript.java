package net.sf.odinms.scripting.npc;

import java.util.List;
import net.sf.odinms.net.world.MaplePartyCharacter;

public interface NPCScript {

    public void start();
    public void start(List<MaplePartyCharacter> chrs);
    public void action(byte mode, byte type, int selection);
}
