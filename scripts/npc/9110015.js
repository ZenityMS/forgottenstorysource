var status = 0;
var mobs = new Array(3, 3);

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
cm.sendSimple("Hello #b#h ##k! I am the mushroom inside your summoning bag. I can spawn other monsters in #rMapleZtory#k for you. Which monsters would you like to spawn? " +
   "\r\n#L1##b10 Cube Slimes\n\#l" +
   "\r\n#L2##b10 Silver Slimes\n\#l");
} else {
cm.sendOk("You can't summon monsters in the #rFree Market#k");
cm.dispose();
}
} else if (selection == 1) {
   cm.summonMob(3110300, 5000, 400000, 10);
   cm.dispose();
} else if (selection == 2) {
   cm.summonMob(9400203, 5000, 400000, 10);
   cm.dispose();
}
}
}