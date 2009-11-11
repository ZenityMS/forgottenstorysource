/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
					   Matthias Butz <matze@odinms.de>
					   Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Vending Machine Spawner
-- By --------------------------------------------------------------------------------------------------
	xQuasar (Based on Santa/Purple's destinyTips, with help from Groat & MrMysterious)
-- Version Info -------------------------------------------------------------------------------------
	1.0 First version by xQuasar
**/

importPackage(net.sf.odinms.client);
importPackage(net.sf.odinms.tools);

function init() {
	scheduleNew();
}

function scheduleNew() {
	em.schedule("start", 60*60*1000); //every hour
}

function start() {
	scheduleNew();
	var hotSand = em.getChannelServer().getMapFactory().getMap(100000000, false, false);
	var kingClang = net.sf.odinms.server.life.MapleLifeFactory.getMonster(9410013);
	var current = em.getChannelServer().getMapFactory().getMap(100000000).countMobOnMap(9410013);
	if (current == 0) {
		var posX;
		var posY;
		posX = 460;
		posY = 4;
		hotSand.spawnMonsterOnGroundBelow(kingClang, new java.awt.Point(posX, posY)); 
		// hotSand.broadcastMessage(net.sf.odinms.tools.MaplePacketCreator.serverNotice(6, "[Event] The Vending machine is attacking Henesys!"));
                // em.worldMessage("[Event] The Vending machine is attacking Henesys!");
                   em.getChannelServer().broadcastPacket(net.sf.odinms.tools.MaplePacketCreator.serverNotice(5, "[Event] The Vending Machine is attacking Henesys!"));
	}	
	scheduleNew();
}