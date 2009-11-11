/* 
 * This file is part of the OdinMS Maple Story Server
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

/*
 * @Author Rich
 * 
 * Capture the Flag - fail
 */


importPackage(net.sf.odinms.world);
importPackage(net.sf.odinms.client);
importPackage(net.sf.odinms.server.maps);

var exitMap;
var flagmap;
var instanceId;
var minPlayers = 1;

function init() {
	instanceId = 1;
}

function monsterValue(eim, mobId) {
	return 1;
}

function setup(eim) {
	instanceId = em.getChannelServer().getInstanceId();
	exitMap = em.getChannelServer().getMapFactory().getMap(100000000); // Loser's Exit Map
        flagmap = em.getChannelServer().getMapFactory().getMap(1010000); // IDK LOL
	exitMap2 = em.getChannelServer().getMapFactory().getMap(980000303); // Winner's Exit - why the hell would we need this?
	var instanceName = "COF" + instanceId;
	var eim = em.newInstance(instanceName);
	var mf = eim.getMapFactory();
	em.getChannelServer().addInstanceId();
	var map = mf.getMap(980000301);//wutt :D
	return eim;
}

function playerEntry(eim, player) {
	var map = eim.getMapInstance(1010001); // waiting room before capture the flag
	player.changeMap(map, map.getPortal(0));
	player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getClock(600)); // It's just going to dissapear anyway :\
}

function playerDead(eim, player) {
  if (player.getMap().hasTimer() == false) {
    player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getClock(600));
    player.getMap().setTimer(true);
  }
}

function playerRevive(eim, player) {
	if (eim.isLeader(player)) { //check for party leader
		//boot whole party and end
		var party = eim.getPlayers();
		for (var i = 0; i < party.size(); i++) {
			playerExit(eim, party.get(i));
		}
		eim.dispose2();
	}
	else { //boot dead player
		// If only 5 players are left, uncompletable:
		var party = eim.getPlayers();
		if (party.size() <= minPlayers) {
			for (var i = 0; i < party.size(); i++) {
				playerExit(eim,party.get(i));
			}
			eim.dispose2();
		}
		else
			playerExit(eim, player);
	}
}

function playerDisconnected(eim, player) {
	if (eim.isLeader(player)) { //check for party leader
		//PWN THE PARTY (KICK OUT)
		var party = eim.getPlayers();
		for (var i = 0; i < party.size(); i++) {
			if (party.get(i).equals(player)) {
				removePlayer(eim, player);
			}			
			else {
				playerExit(eim, party.get(i));
			}
		}
		eim.dispose2();
	}
	else { //KICK THE D/CED CUNT
		// If only 5 players are left, uncompletable:
		var party = eim.getPlayers();
		if (party.size() < minPlayers) {
			for (var i = 0; i < party.size(); i++) {
				playerExit(eim,party.get(i));
			}
			eim.dispose2();
		}
		else
			playerExit(eim, player);
	}
}

function leftParty(eim, player) {			
	// If only 5 players are left, uncompletable:
	var party = eim.getPlayers();
	if (party.size() <= minPlayers) {
		for (var i = 0; i < party.size(); i++) {
			playerExit(eim,party.get(i));
		}
		eim.dispose2();
	}
	else
		playerExit(eim, player);
}

function disbandParty(eim) {
	//boot whole party and end
	var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++) {
		playerExit(eim, party.get(i));
	}
	eim.dispose2();
}

function playerExit(eim, player) {
	eim.unregisterPlayer(player);
	player.changeMap(exitMap, exitMap.getPortal(0));
}

function playerEntry2(eim, player) {
	var map = eim.getMapInstance(1010000); // yay :D
	player.changeMap(map, map.getPortal(0));
	player.getClient().getSession().write(net.sf.odinms.tools.MaplePacketCreator.getClock(600)); // It's just going to dissapear anyway :\
}

//Those offline cuntts
function removePlayer(eim, player) {
	eim.unregisterPlayer(player);
	player.getMap().removePlayer(player);
	player.setMap(exitMap);
}

function clearPQ(eim) {
	//HTPQ does nothing special with winners
	var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++) {
		playerExit(eim, party.get(i));
	}
	eim.dispose2();
}

function allMonstersDead(eim) {
        //Open Portal? o.O
}

function cancelSchedule() {
}

function timeOut() {
	var iter = em.getInstances().iterator();
	while (iter.hasNext()) {
		var eim = iter.next();
		if (eim.getPlayerCount() > 0) {
			var pIter = eim.getPlayers().iterator();
			while (pIter.hasNext()) {
				playerExit(eim, pIter.next());
			}
		}
		eim.dispose2();
	}
}
