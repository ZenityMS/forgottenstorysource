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
        if (status == 0) 
        cm.sendOk("Hi I'm Michael Jackson! You might notice I come here alot. Anyways, welcome to #bMapleZtory!#k");
        cm.dispose();
}
}
