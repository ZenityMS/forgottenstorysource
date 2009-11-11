package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.scripting.event.EventManager;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class EnteredBoatMapHandler extends AbstractMaplePacketHandler {

	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		//BB 00 (header)
		//6C 24 05 06 00 (Ellinia) || 6F C2 EB 0B 00 (Orbis)
		int mapid = slea.readInt();
		EventManager bm = null;
		if (mapid == 101000300 || mapid == 200000111) { //Elinia Station and Orbis Station<To Ellinia>
			bm = c.getChannelServer().getEventSM().getEventManager("Boats");
		} else if (mapid == 220000110 || mapid == 200000121) { //Ludibirum/Orbis
			bm = c.getChannelServer().getEventSM().getEventManager("Trains");
		} else if (mapid == 240000110 || mapid == 200000131) { //Leafre/Orbis
			bm = c.getChannelServer().getEventSM().getEventManager("Cabin");
		}
		if (bm != null && bm.getProperty("docked").equals("true")) {
			c.getSession().write(MaplePacketCreator.boatPacket(true));
		}
	}
}
