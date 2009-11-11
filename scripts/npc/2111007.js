/*
@    Author : Rich
@
@    NPC = NAME
@    Map =  MAP
@    NPC MapId = MAPID
@    Function = Rebirth Player
@
*/

var status = 0;
var item = new Array(1112802,
                    1112802,
                    1112801,
                    1112800,
                    1112006,
                    1112005,
                    1112003,
                    1112002,
                    4031360,
                    4031358,
                    4031362,
                    4031364,
                    1112803,
                    1112806,
                    1112807,
                    1112809,
                    1112001);


function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {  
                cm.sendSimple ("Hey! I'm here to remove rings that you don't want anymore :) Choose which Ring you Don't want! " +
			"#k\r\n#L1##r#v1112802#" +
			"#k\r\n#L2##r#v1112801#" +
			"#k\r\n#L3##r#v1112800#" +
			"#k\r\n#L4##r#v1112006#" +
			"#k\r\n#L5##r#v1112005#" +
			"#k\r\n#L6##r#v1112003#" +
			"#k\r\n#L7##r#v1112002#" +
			"#k\r\n#L8##r#v4031360#" +
			"#k\r\n#L9##r#v4031358#" +
			"#k\r\n#L10##r#v4031362#" +
			"#k\r\n#L11##r#v4031364#" +
			"#k\r\n#L12##r#v1112803#" +
			"#k\r\n#L13##r#v1112806#" +
			"#k\r\n#L14##r#v1112807#" +
			"#k\r\n#L15##r#v1112809#" +
			"#k\r\n#L16##r#v1112001#");
    } else if (status == 1) { 
          if (cm.haveItem (item[selection])) {
            cm.gainItem(item[selection], -1); 
            cm.sendOk ("Done!");
          cm.dispose();
          } else { 
          cm.sendOk ("You don't have that ring in your inventory!");
          cm.dispose();
          }
}
}
}                     