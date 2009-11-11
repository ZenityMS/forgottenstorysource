/*function start() {
if (cm.eventOn())
cm.doEvent();
cm.dispose();   
}*/
function start() {
if (cm.getPlayer().getClient().getChannelServer().eventOn == true) {
cm.warp(cm.getPlayer().getClient().getChannelServer().eventMap, 0);
cm.dispose();
} else {
cm.sendOk("There is no event to take you too.");
cm.dispose();
}
}