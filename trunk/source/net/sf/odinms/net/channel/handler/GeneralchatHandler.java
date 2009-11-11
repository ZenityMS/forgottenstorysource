package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.messages.CommandProcessor;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.server.PublicChatHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class GeneralchatHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getPlayer().resetAfkTime();
        String text = slea.readMapleAsciiString();
        int show = slea.readByte();
        if (!CommandProcessor.getInstance().processCommand(c, text) && c.getPlayer().getCanTalk() && !PublicChatHandler.doChat(c, text)) {
            if (!c.getPlayer().isHidden()) {
                if (c.getPlayer().getGMText() == 0) {
                    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getChatText(c.getPlayer().getId(), text, false, show));
                } else if (c.getPlayer().getGMText() == 7) {
                    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getChatText(c.getPlayer().getId(), text, true, show));
                } else {
                    switch (c.getPlayer().getGMText()) {
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                            //MultiChat
                            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.multiChat(c.getPlayer().getName(), text, c.getPlayer().getGMText() - 1));
                            break;
                        case 5:
                        case 6:
                            //Server Notice
                            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(c.getPlayer().getGMText(), c.getPlayer().getName() + " : " + text));
                            break;
                        case 8:
                            //Whisper
                            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getWhisper(c.getPlayer().getName(), c.getChannel(), text));
                            break;
                        case 9:
                            //MapleTip
                            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.sendYellowTip(c.getPlayer().getName() + " : " + text));
                            break;
                    }
                    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getChatText(c.getPlayer().getId(), text, false, 1));
                }
                if (text.equalsIgnoreCase("cc plz")) {
                    c.getPlayer().finishAchievement(14);
                }
                if (text.contains("wtf")) {
                    c.getPlayer().finishAchievement(30);
                    //    c.getPlayer().dropMessage("You have said your first wtf! I'm sure you'll have more wtfs in the future!");
                    }

                if (text.contains("gtfo noob")) {
                    c.getPlayer().finishAchievement(32);
                }

                if (text.contains("i suck")) {
                    c.getPlayer().finishAchievement(37);
                }

                if (text.contains("abcdefghijklmnopqrstuvwxyz")) {
                    c.getPlayer().finishAchievement(33);
                    //    c.getPlayer().dropMessage("You have said your first wtf! I'm sure you'll have more wtfs in the future!");
                    }

                if (text.equalsIgnoreCase("yes") && c.getPlayer().getChannelServer().pollstarted && c.getPlayer().warning[2] == false) {
                    c.getPlayer().getChannelServer().firstoption = c.getPlayer().getChannelServer().firstoption + 1;
                    c.getPlayer().warning[2] = true;

                    try {
                        c.getPlayer().getChannelServer().getWorldInterface().broadcastMessage(c.getPlayer().getName(), MaplePacketCreator.serverNotice(6, c.getPlayer().getName() + " voted 'yes' for the poll! That makes " + ChannelServer.getInstance(c.getChannel()).firstoption + " votes for yes").getBytes());
                    } catch (Exception e) {
                        // do nothing
                    }
                }

                if (text.equalsIgnoreCase("no") && c.getPlayer().getChannelServer().pollstarted && c.getPlayer().warning[2] == false) {
                    c.getPlayer().getChannelServer().secondoption = c.getPlayer().getChannelServer().secondoption + 1;
                    c.getPlayer().warning[2] = true;
                    try {
                        c.getPlayer().getChannelServer().getWorldInterface().broadcastMessage(c.getPlayer().getName(), MaplePacketCreator.serverNotice(6, c.getPlayer().getName() + " voted 'no' for the poll! That makes " + ChannelServer.getInstance(c.getChannel()).secondoption + " votes for no").getBytes());
                    } catch (Exception e) {
                        // do nothing
                    }
                }

                if (text.equalsIgnoreCase("true")) {
                    if (c.getPlayer().getClient().getChannelServer().tfeventtrueorfalse && c.getPlayer().getClient().getChannelServer().tfeventstarted) {
                        if (c.getPlayer().setanswered == false) {
                            if (c.getPlayer().getMeso() + c.getPlayer().getClient().getChannelServer().tfworth < Integer.MAX_VALUE) {
                                c.getPlayer().gainMeso(c.getPlayer().getClient().getChannelServer().tfworth);
                                c.getPlayer().dropMessage("Correct");
                                c.getPlayer().setanswered = true;
                            } else {
                                c.getPlayer().setMeso(2147483647);
                                c.getPlayer().dropMessage("Correct, you now have max mesos.");
                                c.getPlayer().setanswered = true;
                            }
                            c.getPlayer().setanswered = true;
                        } else {
                            c.getPlayer().dropMessage("You have already answered.");
                        }
                    } else {
                        c.getPlayer().dropMessage("Either the event is not on or you answered the question wrong");
                        c.getPlayer().setanswered = true;
                    }
                }

                if (text.equalsIgnoreCase("false")) {
                    if (c.getPlayer().getClient().getChannelServer().tfeventtrueorfalse == false && c.getPlayer().getClient().getChannelServer().tfeventstarted) {
                        if (c.getPlayer().setanswered == false) {
                            if (c.getPlayer().getMeso() + c.getPlayer().getClient().getChannelServer().tfworth < Integer.MAX_VALUE) {
                                c.getPlayer().gainMeso(c.getPlayer().getClient().getChannelServer().tfworth);
                                c.getPlayer().dropMessage("Correct");
                                c.getPlayer().setanswered = true;
                            } else {
                                c.getPlayer().setMeso(Integer.MAX_VALUE);
                                c.getPlayer().dropMessage("Correct, you now have max mesos.");
                                c.getPlayer().setanswered = true;
                            }
                            c.getPlayer().setanswered = true;
                        } else {
                            c.getPlayer().dropMessage("You have already answered.");
                        }
                    } else {
                        c.getPlayer().dropMessage("Either the event is not on or you answered the question wrong");
                        c.getPlayer().setanswered = true;
                    }
                }
            } else {
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(5, c.getPlayer().getName() + " : " + text));
            }
        }
    }
}
