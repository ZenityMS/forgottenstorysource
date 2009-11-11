/**
Vote Point NPC
 @author Rich / WlZET
*/
var status = 0;
var oneitems = new Array(4031183, 2044703, 2044503, 2043303, 2043103, 2043203, 2043003, 2044403, 2044303, 2043803, 2044103, 2044203, 2044003, 2043703, 2040807, 2040806, 2040007, 2040506, 2040710, 2040711);
var twoitems = new Array(4001126, 1302081, 1312037, 1322060, 1402046, 1412033, 1422037, 1482023);
var threeitems = new Array(1382045, 1382046, 1382047, 1382048, 1382049, 1382050, 1382051, 1382052, 1382053, 1372035, 1372036, 1372037, 1372038, 1372039, 1372040, 1372041, 1372042);
// var superequips = new Array(4001126, 4001126);
var bombpacks = new Array (20, 50, 100, 200);
var bombprice = new Array (1, 2, 3, 5);

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
// cm.getPlayer().gainVotePoints(100);
        cm.sendSimple ("Hello, I am the Vote Point NPC. Every time you use the vote point system on the website and use @votepoints you gain #b1#k Vote Point.  \n You currently have " + cm.getPlayer().getVotePoints() + " Vote Points. \b\r\n#L0#1 point shop\n\#l " +
"\r\n#L1#3 Point shop\n\#l" +
"\r\n#L2#5 Point shop\n\#l" +
"\r\n\r\n#L3#Bomb Packages\n\#l");
        } else if (status == 1) {
            switch(selection) {
                case 0:
             //   cm.sendSimple("Please pick your item! \b\r\n#L0#1 points for #v4031183#\n\#l " +
// "\r\n#L1#1 points for #v2044703#\n\#l" +
// "\r\n#L2#1 points for #v2044503#\n\#l");
// cm.getPlayer().gainVotePoints(10);
			var awesomeness = "Which item would you like to buy? \r\n #rLeaf = 100 Million Mesos\r\n These scrolls are GM scrolls#b";
			for (var i = 0; i < oneitems.length; i++) {
				awesomeness += "\r\n#L" + i + "##z" + oneitems[i] + "# (Price :  1 Point)#l";
}
cm.sendSimple(awesomeness);			
status = 100;
break;

                case 1:
			var awesomeness = "Which item would you like to buy?#b";
			for (var i = 0; i < threeitems.length; i++) {
				awesomeness += "\r\n#L" + i + "##z" + threeitems[i] + "# (Price :  3 Points)#l";
}
cm.sendSimple(awesomeness);			
status = 300;
break;

case 2:
			var awesomeness = "Which item would you like to buy? \r\n #rMaple Leaf = 1 Billion Mesos#b";
			for (var i = 0; i < twoitems.length; i++) {
				awesomeness += "\r\n#L" + i + "##z" + twoitems[i] + "# (Price :  5 Points)#l";
}
cm.sendSimple(awesomeness);			
status = 200;
break;

case 3:
			var awesomeness = "How many bombs would you like to buy?#b";
			for (var i = 0; i < bombpacks.length; i++) {
			for (var i = 0; i < bombprice.length; i++) {
				awesomeness += "\r\n#L" + i + "#" + bombpacks[i] + " Bombs (Price : " + bombprice[i] + " Points)#l";
}
}
cm.sendSimple(awesomeness);			
status = 700;
break;
}
} else if (status == 101) {
                if (cm.getPlayer().getVotePoints() > 0) {
                   // cm.gainItem(oneitems[selection]);
                    cm.randomizeStats(oneitems[selection], 20);
                    cm.getPlayer().gainVotePoints(-1);
                    cm.dispose();
                } else {
                    cm.sendOk("You don't have 1 #bVote Point#k.");
                    cm.dispose();
                }
} else if (status == 201) {
// for (var i = 0; i < superequips.length; i++) {
                if (cm.getPlayer().getVotePoints() > 4) {
                if (twoitems[selection] != 000) {
                   // cm.gainItem(twoitems[selection]);
                    cm.randomizeStats(twoitems[selection], 35);
                    cm.getPlayer().gainVotePoints(-5);
                    cm.dispose();
} else { 
cm.sendYesNo ("You will receive 50 Bombs for 5 vote points if you press the 'Yes' button.");
status = 500;
}
               } else {
                    cm.sendOk("You don't have 5 #bVote Points#k.");
                    cm.dispose();
                }
} else if (status == 301) {
                if (cm.getPlayer().getVotePoints() > 2) {
                    // cm.gainItem(threeitems[selection]);
                    cm.randomizeStats(threeitems[selection], 45);
                    cm.getPlayer().gainVotePoints(-3);
                    cm.dispose();
                } else {
                    cm.sendOk("You don't have 3 #bVote Points#k.");
                    cm.dispose();
                }
} else if (status == 501) {
cm.sendOk ("HOLY SHIT HOW'D YOU GET HERE?!?!");
} else if (status == 701) {
                    if (bombpacks[selection] == 20) {
                if (cm.getPlayer().getVotePoints() > 1 - 1) {
                    cm.gainItem(2100067, bombpacks[selection]);
                    cm.getPlayer().gainVotePoints(-1);
                    cm.dispose();
                } else {
                    cm.sendOk("You don't have 1 #bVote Point#k.");
                    cm.dispose();
}                
} else {
                    if (bombpacks[selection] == 50) {
                if (cm.getPlayer().getVotePoints() > 2 - 1) {
                    cm.gainItem(2100067, bombpacks[selection]);
                    cm.getPlayer().gainVotePoints(-2);
                    cm.dispose();
                } else {
                    cm.sendOk("You don't have 2 #bVote Points#k.");
                    cm.dispose();
}                
} else {
                    if (bombpacks[selection] == 100) {
                if (cm.getPlayer().getVotePoints() > 3 - 1)  {
                    cm.gainItem(2100067, bombpacks[selection]);
                    cm.getPlayer().gainVotePoints(-3);
                    cm.dispose();
                } else {
                    cm.sendOk("You don't have 3 #bVote Points#k.");
                    cm.dispose();
              }  
} else {
                if (cm.getPlayer().getVotePoints() > 5 - 1)  {
                    cm.gainItem(2100067, bombpacks[selection]);
                    cm.getPlayer().gainVotePoints(-5);
                    cm.dispose();
} else {
cm.sendOk("You don't have 5 vote points!");
cm.dispose();
}
}
}





}
}
        
    }
}
