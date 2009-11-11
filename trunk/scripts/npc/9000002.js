
function start() {
	cm.sendOk("Congratulations on finishing the jump quest, I have given you #b1#k jump point#k");
        cm.gainJumpPoints(1);
        cm.warp(100000000);
	cm.dispose();
}

function action(mode, type, selection) {
}
