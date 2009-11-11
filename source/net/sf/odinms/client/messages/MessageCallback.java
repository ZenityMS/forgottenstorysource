package net.sf.odinms.client.messages;
public interface MessageCallback {
    void dropMessage(String message);
    void dropMessageYellow(String message);
}
