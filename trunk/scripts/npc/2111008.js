/* Made by Rich of MapleZtory */

var status = 0;
var mobs = new Array(8800000, 3110300, 9400203, 9410013, 9400551, 9400569);
var reqlvl = new Array(1, 2, 3, 3, 4, 5, 3, 3, 3, 5, 5, 5);
var hp = new Array(66000000, 1000, 1000, 1200000000, 1300000000, 2147000000);
var exp = new Array(0, 400000, 800000, 800000, 1000000, 2147000000);
var howmany = new Array(1, 10, 15, 1, 1, 1);

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0 && status == 0)
            cm.dispose();
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
 if (cm.getPlayer().getMapId() != 910000000) {
cm.sendSimple("Hey I'm the Donator spawning NPC. What do you want to spawn? I only summon for tokens! They look like this: #v4031874# You can buy some for 50m ea/ using @token" +
   "\r\n#L0##bZakum 1st Body - Level 1\n\#l" +
   "\r\n#L1##b10 Cube Slimes - Level 2\n\#l" +
   "\r\n#L2##b15 Silver Slimes - Level 3 - 2x more EXP as Cube\n\#l" +
   "\r\n#L3##bVending Machine - Level 3 - As much EXP as a Silver Slime - Godly items\n\#l" +
   "\r\n#L4##bBob - Level 4 - 1.3 More EXP than Silver Slime - Drops Maple Leaves and Lupin Erasers\n\#l" +
   "\r\n#L5##bBPD - Level 5 - Drops Godly Items - Good Boss\n\#l");
} else {
cm.sendOk("You can't summon monsters in the #rFree Market#k");
cm.dispose();
}
} else if (status == 1) {
 if (cm.getPlayer().getDonatorLevel() >= reqlvl[selection] && cm.haveItem(4031874, 1)) {
   cm.gainItem(4031874, -1);
   cm.summonMob(mobs[selection], hp[selection], exp[selection], howmany[selection]);
   cm.dispose();
} else {
   cm.sendOk("You don't have the required donation level or you don't have a #v4031874#");
   cm.dispose();
}
}
}
}