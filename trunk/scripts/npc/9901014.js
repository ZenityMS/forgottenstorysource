importPackage(java.util);
importPackage(net.sf.odinms.client);
importPackage(net.sf.odinms.server);
importPackage(net.sf.odinms.net.channel);
importPackage(net.sf.odinms.tools);

var status = 0;

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
        cm.sendYesNo("YES WE CAN! Hi I'm Obama and I am the president manager of all channels in #bMapleZtory#k. Would you like to spend #r50,000,000 mesos to be one of the presidents in #bMapleZtory?");
      } else if (status == 1) {
if (!cm.getPlayer().getClient().getChannelServer().getInstance(cm.getPlayer().getClient().getChannel()).president.equalsIgnoreCase(cm.getPlayer().getName())) {
                        if (!cm.getPlayer().getClient().getChannelServer().getInstance(cm.getPlayer().getClient().getChannel()).president.equalsIgnoreCase("None")) {
 if (cm.getPlayer().getMeso() > 50000000) {
cm.getPlayer().gainMeso(-50000000);
cm.getPlayer().getClient().getChannelServer().getWorldInterface().broadcastMessage(cm.getPlayer().getName(), MaplePacketCreator.serverNotice(6, cm.getName() + " is now the president of  Channel " + cm.getPlayer().getClient().getChannel() + " and has kicked " + cm.getPlayer().getClient().getChannelServer().president + " out of office!").getBytes());
cm.getPlayer().getClient().getChannelServer().getInstance(cm.getPlayer().getClient().getChannel()).president = cm.getPlayer().getName();
cm.dispose();
                            } else {
                                cm.sendOk("You don't have 50 Million mesos!");
                                cm.dispose();
                            }
                        } else {
                            if (cm.getPlayer().getMeso() > 50000000) {
                                cm.getPlayer().gainMeso(-50000000);
                                cm.getPlayer().getClient().getChannelServer().getWorldInterface().broadcastMessage(cm.getPlayer().getName(), MaplePacketCreator.serverNotice(6, cm.getPlayer().getName() + " is now the president of Channel " + cm.getPlayer().getClient().getChannel()).getBytes());
                                cm.getPlayer().getClient().getChannelServer().president = cm.getPlayer().getName();
                                cm.dispose();
                            } else {
                                cm.sendOk("You don't have 50 Million mesos!");
                                cm.dispose();
                            }
                        }
                    } else {
                        cm.sendOk("You are already the president of this channel");
                        cm.dispose();

                    }
                }
}
}
