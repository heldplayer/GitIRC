
package me.heldplayer.GitIRC.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.heldplayer.GitIRC.ConsoleMessageReciever;
import me.heldplayer.GitIRC.MessageReciever;

public class IncomingMessage {
    private String source = "";
    private String type = "";
    private String[] args = null;
    private String argString = "";

    public IncomingMessage(String base) {
        String editable = base.trim();

        if (editable.startsWith(":")) {
            if (editable.indexOf(" ") < 0) {
                return;
            }

            this.source = editable.substring(1, editable.indexOf(" "));

            editable = editable.substring(this.source.length() + 1).trim();
        }

        String[] split = editable.split(" ");

        if (split.length <= 0) {
            return;
        }

        this.type = split[0];

        editable = editable.substring(this.type.length()).trim();

        this.argString = editable;
        this.args = this.argString.split(" ");

        //System.err.println("Source: " + source + "\tType: " + type + "\tArgs: " + argString);
    }

    public boolean parse(MessageReciever reciever) {
        if (this.type.equalsIgnoreCase("PING")) {
            reciever.send("PONG " + this.argString);
            return true;
        }
        if (this.type.equalsIgnoreCase("PONG")) {
            return true;
        }
        if (this.type.equalsIgnoreCase("NOTICE")) {
            String result = "";
            for (int i = 1; i < this.args.length; i++) {
                if (i != 1) {
                    result += " ";
                }
                result += this.args[i];
            }

            if (result.startsWith(":")) {
                result = result.substring(1);
            }

            System.err.println("[NOTICE: " + this.args[0] + "]: " + result);
            return true;
        }
        if (this.type.equalsIgnoreCase("CAP")) {
            if (this.args.length < 3) {
                return false;
            }
            if (this.args[0].equalsIgnoreCase("*") && this.args[1].equalsIgnoreCase("LS")) {
                String result = "";
                for (int i = 2; i < this.args.length; i++) {
                    if (i != 2) {
                        result += " ";
                    }
                    result += this.args[i];
                }

                if (result.startsWith(":")) {
                    result = result.substring(1);
                }

                String[] caps = result.split(" ");

                for (String cap : caps) {
                    if (cap.equalsIgnoreCase("multi-prefix")) {
                        reciever.send("CAP REQ :multi-prefix");
                        return true;
                    }
                }
                return false;
            }
            else if (this.args[0].equalsIgnoreCase(reciever.getNick()) && this.args[1].equalsIgnoreCase("ACK")) {
                reciever.send("CAP END");
                return true;
            }
            return false;
        }
        if (this.type.equalsIgnoreCase("PRIVMSG")) {
            String result = "";
            for (int i = 1; i < this.args.length; i++) {
                if (i != 1) {
                    result += " ";
                }
                result += this.args[i];
            }

            if (result.startsWith(":")) {
                result = result.substring(1);
            }

            System.out.println("[" + this.args[0] + "] <" + this.source.split("!")[0] + "> " + result);
            //reciever.send("PRIVMSG " + args[0] + " :" + result);
            return true;
        }
        if (this.type.equalsIgnoreCase("MODE")) {
            String result = "";
            for (int i = 1; i < this.args.length; i++) {
                if (i != 1) {
                    result += " ";
                }
                result += this.args[i];
            }

            if (result.startsWith(":")) {
                result = result.substring(1);
            }

            System.out.println("[" + this.args[0] + "] " + this.source.split("!")[0] + " sets mode " + result);
            return true;
        }
        if (this.type.equalsIgnoreCase("KICK")) {
            String result = "";
            for (int i = 2; i < this.args.length; i++) {
                if (i != 2) {
                    result += " ";
                }
                result += this.args[i];
            }

            if (result.startsWith(":")) {
                result = result.substring(1);
            }

            System.out.println("[" + this.args[0] + "] " + this.source.split("!")[0] + " has kicked " + this.args[1] + " (" + result + ")");
            return true;
        }
        if (this.type.equalsIgnoreCase("QUIT")) {
            String result = "";
            for (int i = 0; i < this.args.length; i++) {
                if (i != 0) {
                    result += " ";
                }
                result += this.args[i];
            }

            if (result.startsWith(":")) {
                result = result.substring(1);
            }

            System.out.println(this.source.split("!")[0] + " has quit (" + result + ")");
            return true;
        }
        if (this.type.equalsIgnoreCase("PART")) {
            String result = "";
            for (int i = 1; i < this.args.length; i++) {
                if (i != 1) {
                    result += " ";
                }
                result += this.args[i];
            }

            if (result.startsWith(":")) {
                result = result.substring(1);
            }

            System.out.println("[" + this.args[0] + "] " + this.source.split("!")[0] + " parts (" + result + ")");
            return true;
        }
        if (this.type.equalsIgnoreCase("JOIN")) {
            System.out.println("[" + this.args[0] + "] " + this.source.split("!")[0] + " has joined");
            return true;
        }

        if (this.type.equalsIgnoreCase("001")) {
            reciever.send("USERHOST " + reciever.getNick());
            return true;
        }
        if (this.type.equalsIgnoreCase("332")) {
            String result = "";
            for (int i = 2; i < this.args.length; i++) {
                if (i != 2) {
                    result += " ";
                }
                result += this.args[i];
            }

            if (result.startsWith(":")) {
                result = result.substring(1);
            }

            System.out.println("[" + this.args[1] + "] Topic: " + result);
            return true;
        }
        if (this.type.equalsIgnoreCase("333")) {
            if (this.args.length < 3) {
                return false;
            }

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            System.out.println("[" + this.args[1] + "] Set by " + this.args[2] + " on " + format.format(new Date(Long.parseLong(this.args[3]) * 1000L)));
            return true;
        }
        if (this.type.equalsIgnoreCase("372")) {
            String result = "";
            for (int i = 1; i < this.args.length; i++) {
                if (i != 1) {
                    result += " ";
                }
                result += this.args[i];
            }

            if (result.startsWith(":")) {
                result = result.substring(1);
            }

            System.out.println(result);
            return true;
        }
        if (this.type.equalsIgnoreCase("302")) {
            String host = "";

            for (int i = 1; i < this.args.length; i++) {
                if (i == 1) {
                    host += this.args[i].substring(1);
                }
                else {
                    host += " " + this.args[i];
                }
            }

            System.out.println(this.args[0] + " is " + host);

            System.out.println("Performing startup commands...");

            try {
                FileReader fileReader = new FileReader(new File(IncomingMessage.class.getResource("/perform.txt").toURI()));

                BufferedReader reader = new BufferedReader(fileReader);

                while (reader.ready()) {
                    String line = reader.readLine();

                    ConsoleMessageReciever rec = (ConsoleMessageReciever) reciever;

                    rec.inputBuffer.put(rec.index++, String.format(line, reciever.getNick()));
                }

                reader.close();
                fileReader.close();
            }
            catch (URISyntaxException e) {}
            catch (IOException e) {}

            return true;
        }

        return false;
    }
}
