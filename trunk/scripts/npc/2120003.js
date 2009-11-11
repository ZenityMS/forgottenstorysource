/* Made by Rich of MapleZtory */

importPackage(net.sf.odinms.net.login);

var status = 0;;

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
cm.sendYesNo("Hello! I am the Donator NPC of #bMapleZtory#k " + LoginServer.getInstance().getServerName() + " I can take you to the donator map! \r\n\#rWhat a donator has to offer you?#k \r\n\r\n #e#b Extra EXP while killing monsters\r\n  A special donator map to summon monsters\r\n  A few donator commands to help you in game. \r\n\r\n  Price range: \r\n#r 5$ - Level 1\r\n 10$ - Level 2\r\n 15$ - Level 3\r\n 25$ - Level 4\r\n 50 or more $ - Level 5 Donator\r\n\r\n The price range is in USA Dollars #g#l\r\n To become a donator talk to a GM about donating. \r\n\r\n #k Would you like to enter the Donator Map?");
} else if (status == 1) {
if (cm.getPlayer().getDonatorLevel() >= 1) {
cm.warp(912000000, 0);
cm.sendOk("Have fun!");
} else {
cm.sendOK("You are not a donator! To become a Donator you should talk to a GM about donating. Have fun in MapleZtory!");
cm.dispose();
}
}
}
}