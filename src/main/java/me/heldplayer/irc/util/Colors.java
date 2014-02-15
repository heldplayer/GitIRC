
package me.heldplayer.irc.util;

public enum Colors {

    BOLD("\u0002", false),
    UNDERLINE("\u001f", false),
    REVERSE("\u0016", false),
    RESET("\u000f", false),
    WHITE("0", true),
    BLACK("1", true),
    DARK_BLUE("2", true),
    DARK_GREEN("3", true),
    RED("4", true),
    BROWN("5", true),
    PURPLE("6", true),
    ORANGE("7", true),
    YELLOW("8", true),
    GREEN("9", true),
    TEAL("10", true),
    CYAN("11", true),
    BLUE("12", true),
    MAGENTA("13", true),
    DARK_GREY("14", true),
    LIGHT_GREY("15", true);

    public final String code;
    public final boolean isColor;

    private Colors(String code, boolean isColor) {
        this.code = code;
        this.isColor = isColor;
    }

    public String combine(Colors background) {
        if (this.isColor && background.isColor) {
            return this.toString() + "," + background.code;
        }
        else {
            return this.toString();
        }
    }

    public String toString() {
        return (this.isColor ? "\u0003" : "") + this.code;
    }

}
