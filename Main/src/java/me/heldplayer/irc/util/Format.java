
package me.heldplayer.irc.util;

public enum Format {

    BOLD("\u0002", false), UNDERLINE("\u001f", false), REVERSE("\u0016", false), RESET("\u000f", false), WHITE("00", true), BLACK("01", true), DARK_BLUE("02", true), DARK_GREEN("03", true), RED("04", true), BROWN("05", true), PURPLE("06", true), ORANGE("07", true), YELLOW("08", true), GREEN("09", true), TEAL("10", true), CYAN("11", true), BLUE("12", true), MAGENTA("13", true), DARK_GREY("14", true), LIGHT_GREY("15", true);

    public final String code;
    public final boolean isColor;

    private Format(String code, boolean isColor) {
        this.code = code;
        this.isColor = isColor;
    }

    public String combine(Format background) {
        if (this.isColor && background.isColor) {
            return this.toString() + "," + background.code;
        }
        else {
            return this.toString();
        }
    }

    @Override
    public String toString() {
        return (this.isColor ? "\u0003" : "") + this.code;
    }

}
