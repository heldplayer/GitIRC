
package me.heldplayer.test;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.IRCChannel;
import me.heldplayer.irc.api.IRCUser;
import me.heldplayer.irc.api.event.Event;
import me.heldplayer.irc.api.event.IEventBus;
import me.heldplayer.irc.base.java.ISandboxDelegate;
import me.heldplayer.irc.base.java.JavaException;
import me.heldplayer.irc.base.java.JavaExpression;
import me.heldplayer.irc.base.java.JavaReader;
import me.heldplayer.irc.base.java.SandboxManager;

public class JavaTest {

    public static void main(String[] args) {
        // Decimal
        assert JavaTest.testNumber("0", 0, Integer.class); // Ok
        assert JavaTest.testNumber("604162", 604162, Integer.class); // Ok
        assert JavaTest.testNumber("80", 80, Integer.class); // Ok
        assert !JavaTest.testNumber("6A", null, Integer.class); // Error
        // Decimal Long
        assert JavaTest.testNumber("0L", 0L, Long.class); // Ok
        assert JavaTest.testNumber("604162L", 604162L, Long.class); // Ok
        assert JavaTest.testNumber("80L", 80L, Long.class); // Ok
        assert !JavaTest.testNumber("6AL", null, Long.class); // Error
        // Octagonal
        assert JavaTest.testNumber("0", 0, Integer.class); // Ok
        assert JavaTest.testNumber("07", 07, Integer.class); // Ok
        assert JavaTest.testNumber("074712", 074712, Integer.class); // Ok
        assert !JavaTest.testNumber("08", null, Integer.class); // Error
        assert !JavaTest.testNumber("0F", null, Integer.class); // Error
        // Octagonal Long
        assert JavaTest.testNumber("0L", 0L, Long.class); // Ok
        assert JavaTest.testNumber("07L", 07L, Long.class); // Ok
        assert JavaTest.testNumber("074712L", 074712L, Long.class); // Ok
        assert !JavaTest.testNumber("08L", null, Long.class); // Error
        assert !JavaTest.testNumber("0FL", null, Long.class); // Error
        // Hexadecimal
        assert !JavaTest.testNumber("x", null, Integer.class); // Error
        assert !JavaTest.testNumber("0x", null, Integer.class); // Error
        assert !JavaTest.testNumber("x0", null, Integer.class); // Error
        assert JavaTest.testNumber("0x0", 0x0, Integer.class); // Ok
        assert JavaTest.testNumber("0x8", 0x8, Integer.class); // Ok
        assert JavaTest.testNumber("0xF", 0xF, Integer.class); // Ok
        assert JavaTest.testNumber("0xBED", 0xBED, Integer.class); // Ok
        assert JavaTest.testNumber("0xCAFEBABE", 0xCAFEBABE, Integer.class); // Ok
        assert JavaTest.testNumber("0xcAfEbAbE", 0xcAfEbAbE, Integer.class); // Ok
        assert !JavaTest.testNumber("0xG", null, Integer.class); // Error
        // Hexadecimal Long
        assert !JavaTest.testNumber("xL", null, Long.class); // Error
        assert !JavaTest.testNumber("0xL", null, Long.class); // Error
        assert !JavaTest.testNumber("x0L", null, Long.class); // Error
        assert JavaTest.testNumber("0x0L", 0x0L, Long.class); // Ok
        assert JavaTest.testNumber("0x8L", 0x8L, Long.class); // Ok
        assert JavaTest.testNumber("0xFL", 0xFL, Long.class); // Ok
        assert JavaTest.testNumber("0xBEDL", 0xBEDL, Long.class); // Ok
        assert JavaTest.testNumber("0xCAFEBABEL", 0xCAFEBABEL, Long.class); // Ok
        assert JavaTest.testNumber("0xcAfEbAbEl", 0xcAfEbAbEl, Long.class); // Ok
        assert !JavaTest.testNumber("0xGL", null, Long.class); // Error
        // Binary
        assert !JavaTest.testNumber("0b", null, Integer.class); // Error
        assert JavaTest.testNumber("0b0", 0, Integer.class); // Ok
        assert JavaTest.testNumber("0b1", 1, Integer.class); // Ok
        assert JavaTest.testNumber("0b1100101", 101, Integer.class); // Ok
        assert !JavaTest.testNumber("0b2", null, Integer.class); // Error
        // Binary Long
        assert !JavaTest.testNumber("0bL", null, Long.class); // Error
        assert JavaTest.testNumber("0b0L", 0L, Long.class); // Ok
        assert JavaTest.testNumber("0b1L", 1L, Long.class); // Ok
        assert JavaTest.testNumber("0b1100101L", 101L, Long.class); // Ok
        assert !JavaTest.testNumber("0b2L", null, Long.class); // Error
        // Float
        assert !JavaTest.testNumber(".F", null, Float.class); // Error
        assert JavaTest.testNumber(".0F", .0F, Float.class); // Ok
        assert JavaTest.testNumber("0.5F", 0.5F, Float.class); // Ok
        assert JavaTest.testNumber(".0535F", .0535F, Float.class); // Ok
        assert JavaTest.testNumber("0.F", 0.F, Float.class); // Ok
        assert JavaTest.testNumber("0.0F", 0.0F, Float.class); // Ok
        assert JavaTest.testNumber("50.034F", 50.034F, Float.class); // Ok
        assert JavaTest.testNumber("0F", 0F, Float.class); // Ok
        assert JavaTest.testNumber("35450F", 35450F, Float.class); // Ok
        // Float (tagless)
        assert !JavaTest.testNumber(".", null, Float.class); // Error
        assert JavaTest.testNumber(".0", .0F, Float.class); // Ok
        assert JavaTest.testNumber("0.5", 0.5F, Float.class); // Ok
        assert JavaTest.testNumber(".0535", .0535F, Float.class); // Ok
        assert JavaTest.testNumber("0.", 0.F, Float.class); // Ok
        assert JavaTest.testNumber("0.0", 0.0F, Float.class); // Ok
        assert JavaTest.testNumber("50.034", 50.034F, Float.class); // Ok
        // Double
        assert !JavaTest.testNumber(".D", null, Double.class); // Error
        assert JavaTest.testNumber(".0D", .0D, Double.class); // Ok
        assert JavaTest.testNumber(".0535D", .0535D, Double.class); // Ok
        assert JavaTest.testNumber("0.D", 0.D, Double.class); // Ok
        assert JavaTest.testNumber("0.0D", 0.0D, Double.class); // Ok
        assert JavaTest.testNumber("50.034D", 50.034D, Double.class); // Ok
        assert JavaTest.testNumber("0D", 0D, Double.class); // Ok
        assert JavaTest.testNumber("35450D", 35450D, Double.class); // Ok

        JavaTest.prepareBotAPI();

        IRCUser user = new IRCUser("heldplayer");
        IRCChannel channel = new IRCChannel("#test");
        ISandboxDelegate sandbox = SandboxManager.createSandbox(user, channel);
        sandbox.addCommand("test");

        String input = "    this.is$valid";
        JavaExpression object = new JavaExpression(input);

        System.out.println(object.value.toString());
    }

    private static boolean testNumber(String input, Number value, Class<? extends Number> expected) {
        try {
            Number number = new JavaReader(input).readNumber();
            if (!number.equals(value)) {
                throw new JavaException("Expected %s but got %s", value, number);
            }
            if (number.getClass() != expected) {
                throw new JavaException("Expected %s but got %s", expected.getSimpleName(), number.getClass().getSimpleName());
            }
            System.out.println(input + " = (" + number.getClass().getSimpleName() + ") " + number);
            return true;
        }
        catch (Throwable e) {
            System.err.println(input + " = " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
    }

    private static void prepareBotAPI() {
        BotAPI.eventBus = new IEventBus() {

            @Override
            public void unregisterEventHandler(Object obj) {}

            @Override
            public void registerEventHandler(Object obj) {}

            @Override
            public boolean postEvent(Event event) {
                return false;
            }

            @Override
            public void cleanup() {}
        };

        System.out.println("Loaded " + BotAPI.pluginLoader.loadPlugins() + " plugins");
    }

}
