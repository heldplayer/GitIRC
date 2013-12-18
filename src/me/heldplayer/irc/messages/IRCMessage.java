
package me.heldplayer.irc.messages;

public class IRCMessage {

    /*
     * message = [ ":" prefix SPACE ] command [ params ] crlf
     * prefix = servername / ( nickname [ [ "!" user ] "@" host ] )
     * command = 1*letter / 3digit
     * params = *14( SPACE middle ) [ SPACE ":" trailing ]
     * =/ 14( SPACE middle ) [ SPACE [ ":" ] trailing ]
     * 
     * nospcrlfcl = %x01-09 / %x0B-0C / %x0E-1F / %x21-39 / %x3B-FF; any octet
     * except NUL, CR, LF, " " and ":"
     * middle = nospcrlfcl *( ":" / nospcrlfcl )
     * trailing = *( ":" / " " / nospcrlfcl )
     * 
     * SPACE = %x20 ; space character
     * crlf = %x0D %x0A ; "carriage return" "linefeed"
     */

    public String message;
    public String prefix;
    public String command;
    public String trailing;
    public String[] params;

    public IRCMessage(String input) {
        String processing = input;
        this.message = input;
        if (processing.startsWith(":")) {
            this.prefix = processing.substring(1, processing.indexOf(' '));
            processing = processing.substring(processing.indexOf(' ') + 1);
        }
        else {
            this.prefix = null;
        }
        if (processing.indexOf(' ') > 0) {
            this.command = processing.substring(0, processing.indexOf(' '));
            processing = processing.substring(processing.indexOf(' ') + 1);
        }
        else {
            this.command = processing;
        }
        if (processing.indexOf(':') > 0) {
            this.trailing = processing.substring(processing.indexOf(" :") + 2);
            processing = processing.substring(0, processing.indexOf(" :"));
        }
        else {
            this.trailing = null;
        }
        this.params = processing.split(" ");
    }

}
