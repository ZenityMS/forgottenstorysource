var status = 0;

function start(){
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection){
    if(mode == -1){
        cm.dispose();
    }else{
        if(mode == 1){
            status++;
        }else{
            status--;
        }
        if(status == 0){
		cm.sendOk("Sup #b" + cm.getPlayer().getName() + ",#k \r\n I was just smoking some weed like the usual when Professor Foxwit came and asked me to hand out the #rEaster charm#k. I asked him if I could charge you bitches for it, he said yes so I charge 100 Easter Eggs for the #rEaster Charm#k. \r\n You can get the Easter eggs from the monsters below. You might notice these monsters drop more mesos than usual.");
		} else if (status == 1) {
		cm.sendYesNo("So do you have #b100#k #v2022065#?");
		} else if (status == 2) {
		if (cm.haveItem(2022065, 100)) {
		cm.gainItem(2022065, -100);
		cm.gainItem(4140903, 1);
		cm.sendOk("Thank you for helping me collect #bEaster Eggs!#k Have fun in MapleZtory");
		cm.dispose();
		} else {
		cm.sendOk("Who are you trying to fool?");
		cm.setHp(0);
		cm.dispose();
		}
		}
		}
		}