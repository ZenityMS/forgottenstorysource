/**
Vote Point NPC
 @author Rich / WlZET
*/
importPackage(net.sf.odinms.server);

var status = 0;
var twoitems = new Array(1002357, 1452044, 1302081, 1312037, 1322060, 1402046, 1412033, 1422037, 1482023, 1122000, 1012071, 1382036, 1332026, 1072211, 1040112, 1060101, 1072198, 1041120, 1061119, 1312015, 1072221, 1072178, 1051097, 1050094, 1051101, 1072183, 1462018, 1051085, 1002405, 1082127, 1050090, 1050106, 1072173, 1060106, 1040117, 1041118, 1332027, 1432038);
var threeitems = new Array(1382045, 1382046, 1382047, 1382048, 1382049, 1382050, 1382051, 1382052, 1382053, 1372035, 1372036, 1372037, 1372038, 1372039, 1372040, 1372041, 1372042);
// var superequips = new Array(4001126, 4001126);
var bombpacks = new Array (20, 50, 100, 200);
var bombprice = new Array (1, 2, 3, 5);
var erasers = new Array(50, 100, 200);

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1){
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1){
        status++;
        } else {
        status--;
        }
        if (status == 0) {
          cm.sendGetText("#b#eHello! I'm the Lupin Eraser trading NPC! \r\n #rThe stats of the item you choose will be randomized. #g\r\nThe more Lupin Erasers you give the more chance you will get a really good item!");
// cm.randomizeStats(1082230, 20)
             //   cm.sendSimple("Please pick your item! \b\r\n#L0#1 points for #v4031183#\n\#l " +
// "\r\n#L1#1 points for #v2044703#\n\#l" +
// "\r\n#L2#1 points for #v2044503#\n\#l");
// cm.getPlayer().gainVotePoints(10);
} else if (status == 1) {
                if (cm.haveItem(4001040, cm.getText())) {
if (cm.getText() > 4 && cm.getText() < 101) {
			var awesomeness = "Which item do you wish to randomize?#b";
			for (var i = 0; i < twoitems.length; i++) {
				// awesomeness += "\r\n#L" + i + "##z" + twoitems[i] + "# #l";
awesomeness += "\r\n#L" + i + "##v" + twoitems[i] + "# #l";
}
cm.sendSimple(awesomeness);			
status = 100;
} else {
cm.sendOk("Your number must be higher than 5 and less than 100.");
cm.dispose();
}
} else {
                    cm.sendOk("You don't have that many Lupin Erasers.");
                    cm.dispose();
                } 




} else if (status == 101) {
if (cm.canHold(1382045)) {
                    cm.randomizeStats(twoitems[selection], cm.getText() * 2);
                    cm.gainItem(4001040, -cm.getText());
                    cm.dispose();
// cm.randomizeStats(twoitems[selection], cm.getText());
} else {
cm.sendOk("You do not have enough space in your inventory!");

}

        
    }
}
}
