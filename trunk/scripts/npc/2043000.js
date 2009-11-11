var pqs = Array("Passion Points","Rebirth Points","Boss Points","Jump Points","Donator Points");
var npc = Array(1092000,2003,9100203,9010000,9201067);

function start() {
	var text = "Hello, don't fear me I won't harm you. I am incharge of the #dPoint System#k, such as #dRebirth Points#k, #dAqua Points#k, #dJump Points#k, and #dDonator Points#k \r\n#b";
	for (var i = 0; i < pqs.length; i++) {
		text += ("#L" + i + "#" + pqs[i] + "#l\r\n");
	}
	cm.sendSimple(text);
}

function action(mode, type, selection) {
	if (mode > 0)
		cm.openNpc(npc[selection]);
	else
		cm.dispose();
}