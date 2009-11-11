var status = 0;
var secondJob = Array("#L0#Fighter#l\r\n#L1#Page#l\r\n#L2#Spearman#l", "#L3#Fire Wizard#l\r\n#L4#Ice Wizard#l\r\n#L5#Cleric#l", "#L6#Hunter#l\r\n#L7#Crossbow man#l", "#L8#Assassin#l\r\n#L9#Bandit#l", "#L10#Brawler#l\r\n#L11#Gunslinger#l");
var jobs = Array(110, 120, 130, 210, 220, 230, 310, 320, 410, 420, 510, 520);

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
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (cm.getPlayer().getLevel() == 10 || (cm.getPlayer().getJob().getId() == 0 && cm.getPlayer().getLevel() >= 10)) {
                cm.sendSimple("#b#L0#Warrior#l\r\n#L1#Mage#l\r\n#L2#Bowman#l\r\n#L3#Theif#l\r\n#L4#Pirate#l#k");
            } else if (cm.getPlayer().getLevel() == 30) {
                cm.sendSimple("#b" + secondJob[(cm.getPlayer().getJob().getId() / 100) - 1] + "#k");
                status++;
            } else {
                cm.sendOk("Hi there, having a nice day?");
                cm.dispose();
            }
        } else{
            if (status == 1)
                cm.changeJobById((selection + 1) * 100);
            else if (status == 2)
                cm.changeJobById(jobs[selection]);
            cm.sendOk("You have now advanced in job!");
            cm.dispose();
        }
    }
}
