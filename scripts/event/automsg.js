var setupTask;

function init() {
    scheduleNew();
}

function scheduleNew() {
	setupTask = em.scheduleAtFixedRate("start", 1000 * 60 * 10);
}

function cancelSchedule() {
    setupTask.cancel(true);
}

function start() {
    var Message = new Array("To answer true and false questions simply say True or False when the event starts!","Don't forget to vote every 12 hours for vote points and prizes!,","@help or @commands will show you a list of useful commands.", "Online GMs : "+em.getAllGMs()+".");
    em.getChannelServer().broadcastPacket(net.sf.odinms.tools.MaplePacketCreator.sendYellowTip("[PassionTip] " + Message[Math.floor(Math.random() * Message.length)]));
}