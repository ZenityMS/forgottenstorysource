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
	King Clang Spawner
-- By --------------------------------------------------------------------------------------------------
	xQuasar (Based on Santa/Purple's destinyTips, with help from Groat & MrMysterious)
-- Version Info -------------------------------------------------------------------------------------
	1.0 First version by xQuasar
**/

importPackage(net.sf.odinms.client);

function init() {
	scheduleNew();
}

function scheduleNew() {
	em.schedule("start", 35*60*1000); //every 35 min
}

function start() {
	scheduleNew();
	var hotSand = em.getChannelServer().getMapFactory().getMap(110040000, false, false);
	var kingClang = net.sf.odinms.server.life.MapleLifeFactory.getMonster(5220001);
	var current1 = em.getChannelServer().getMapFactory().getMap(110040000).countMobOnMap(5220001);
	var current2 = em.getChannelServer().getMapFactory().getMap(110040000).countMobOnMap(5220000);
	if (current1 == 0 && current2 == 0) {
		var random = Math.floor(Math.random()*7);
		var posX;
		var posY;
		switch (random) { //king clang can spawn in a variety of different spots
			case 1:
				posX = -116;
				posY = -833;
				break;
			case 2:
				posX = -1056;
				posY = 173;
				break;
			case 3:
				posX = -300;
				posY = 176;
				break;
			case 4:
				posX = 964;
				posY = 173;
				break;
			case 5:
				posX = 286;
				posY = -473;
				break;
			default:
				posX = 709;
				posY = -353;
		}
		hotSand.spawnMonsterOnGroundBelow(kingClang, new java.awt.Point(posX, posY));
		hotSand.broadcastMessage(net.sf.odinms.tools.MaplePacketCreator.serverNotice(6, "[Event] A large, blue shell seems to have been washed up onto the beach..."));
	}
	scheduleNew();
}