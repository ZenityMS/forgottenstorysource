var pqs = Array("AquariumPQ","BossHunterPQ","KerningPQ","AriantPQ","CarnivalPQ","ZakumPQ","HorntailPQ","HenesysPQ","BossQuest");
var npc = Array(2060009,9201082,9020000,2101014,2042000,2032002,2083004,1012112,9000011);

function start() {
	var text = "What PQ do you want to try out?\r\n#r";
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