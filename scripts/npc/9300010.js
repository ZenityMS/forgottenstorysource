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

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Mr. Moneybags - Stat Resetter
-- By --------------------------------------------------------------------------------------------------
	xQuasar
**/

importPackage(net.sf.odinms.client);

var status = 0;
var str;
var dex;
var inte;
var luk;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else if (status == -1) {
		status = 0;
		cm.sendNext("Hello, I'm the AP re-seter.");
	} else if (status == 0) {
		status = 1;
		cm.sendYesNo("Would you like to have your AP reset? It'll cost #b5,000,000 mesos#k.");
	} else if (status == 1) {
		if (mode == 1 && (cm.getMeso() >= 5000000)) {
			cm.gainMeso(-5000000);
			var statup = new java.util.ArrayList();
			str = cm.getChar().getStr();
			dex = cm.getChar().getDex();
			inte = cm.getChar().getInt();
			luk = cm.getChar().getLuk();
			currentAp = cm.getPlayer().getRemainingAp();
			totalAddAp = str + dex + inte + luk - 16;
			newAp = currentAp + totalAddAp;
			cm.getPlayer().setStr(4);
			cm.getPlayer().setDex(4);
			cm.getPlayer().setInt(4);
			cm.getPlayer().setLuk(4);
			cm.getPlayer().setRemainingAp(newAp);
			statup.add(new net.sf.odinms.tools.Pair(MapleStat.STR, java.lang.Integer.valueOf(4)));
			statup.add(new net.sf.odinms.tools.Pair(MapleStat.DEX, java.lang.Integer.valueOf(4)));
			statup.add(new net.sf.odinms.tools.Pair(MapleStat.LUK, java.lang.Integer.valueOf(4)));
			statup.add(new net.sf.odinms.tools.Pair(MapleStat.INT, java.lang.Integer.valueOf(4)));
			statup.add(new net.sf.odinms.tools.Pair(MapleStat.AVAILABLEAP, java.lang.Integer.valueOf(cm.getPlayer().getRemainingAp())));
			cm.getC().getSession().write (net.sf.odinms.tools.MaplePacketCreator.updatePlayerStats(statup));
			cm.sendOk("Your stats were reset.");
			cm.dispose();
		} else if (mode == 1 && (cm.getMeso() < 5000000)) {
			cm.sendNext("Sorry, you don't have enough mesos. You'll need #b5,000,000#k to get your stats reset...");
			cm.dispose();
		} else {
			cm.sendNext("Alright see you later!");
			cm.dispose();
		}
	}
}