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
	Pumpking Spawner
-- By --------------------------------------------------------------------------------------------------
	Rich for the Black Charm.
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
	var hotSand = em.getChannelServer().getMapFactory().getMap(682000304, false, false);
	var kingClang = net.sf.odinms.server.life.MapleLifeFactory.getMonster(9500302);
	var current = em.getChannelServer().getMapFactory().getMap(682000304).countMobOnMap(9500302);
	if (current == 0) {
		var posX;
		var posY;
		posX = -24;
		posY = 172;
	var overrideStats = new net.sf.odinms.server.life.MapleMonsterStats();
	overrideStats.setHp(1500);
	overrideStats.setExp(40000000);
	overrideStats.setMp(kingClang.getMaxMp());
	kingClang.setOverrideStats(overrideStats);
	kingClang.setHp(overrideStats.getHp());
		hotSand.spawnMonsterOnGroundBelow(kingClang, new java.awt.Point(posX, posY)); 
                   em.getChannelServer().broadcastPacket(net.sf.odinms.tools.MaplePacketCreator.serverNotice(5, "[Event] The Pumpkin Monster has appeared in the Piano Room! Talk to Tyre in Henesys to find him!"));
	}	
	scheduleNew();
}