
package me.heldplayer.test;

import java.util.jar.JarException;

import me.heldplayer.irc.api.BotAPI;
import me.heldplayer.irc.api.IRCUser;
import me.heldplayer.irc.api.event.Event;
import me.heldplayer.irc.api.event.IEventBus;
import me.heldplayer.irc.base.java.ISandboxDelegate;
import me.heldplayer.irc.base.java.JavaObject;
import me.heldplayer.irc.base.java.JavaReader;
import me.heldplayer.irc.base.java.SandboxManager;

public class JavaTest {

    public static void main(String[] args) {
        // Decimal
        assert testNumber("0", 0, Integer.class); // Ok
        assert testNumber("604162", 604162, Integer.class); // Ok
        assert testNumber("80", 80, Integer.class); // Ok
        assert !testNumber("6A", null, Integer.class); // Error
        // Decimal Long
        assert testNumber("0L", 0L, Long.class); // Ok
        assert testNumber("604162L", 604162L, Long.class); // Ok
        assert testNumber("80L", 80L, Long.class); // Ok
        assert !testNumber("6AL", null, Long.class); // Error
        // Octagonal
        assert testNumber("0", 0, Integer.class); // Ok
        assert testNumber("07", 07, Integer.class); // Ok
        assert testNumber("074712", 074712, Integer.class); // Ok
        assert !testNumber("08", null, Integer.class); // Error
        assert !testNumber("0F", null, Integer.class); // Error
        // Octagonal Long
        assert testNumber("0L", 0L, Long.class); // Ok
        assert testNumber("07L", 07L, Long.class); // Ok
        assert testNumber("074712L", 074712L, Long.class); // Ok
        assert !testNumber("08L", null, Long.class); // Error
        assert !testNumber("0FL", null, Long.class); // Error
        // Hexadecimal
        assert !testNumber("x", null, Integer.class); // Error
        assert !testNumber("0x", null, Integer.class); // Error
        assert !testNumber("x0", null, Integer.class); // Error
        assert testNumber("0x0", 0x0, Integer.class); // Ok
        assert testNumber("0x8", 0x8, Integer.class); // Ok
        assert testNumber("0xF", 0xF, Integer.class); // Ok
        assert testNumber("0xBED", 0xBED, Integer.class); // Ok
        assert testNumber("0xCAFEBABE", 0xCAFEBABE, Integer.class); // Ok
        assert testNumber("0xcAfEbAbE", 0xcAfEbAbE, Integer.class); // Ok
        assert !testNumber("0xG", null, Integer.class); // Error
        // Hexadecimal Long
        assert !testNumber("xL", null, Long.class); // Error
        assert !testNumber("0xL", null, Long.class); // Error
        assert !testNumber("x0L", null, Long.class); // Error
        assert testNumber("0x0L", 0x0L, Long.class); // Ok
        assert testNumber("0x8L", 0x8L, Long.class); // Ok
        assert testNumber("0xFL", 0xFL, Long.class); // Ok
        assert testNumber("0xBEDL", 0xBEDL, Long.class); // Ok
        assert testNumber("0xCAFEBABEL", 0xCAFEBABEL, Long.class); // Ok
        assert testNumber("0xcAfEbAbEl", 0xcAfEbAbEl, Long.class); // Ok
        assert !testNumber("0xGL", null, Long.class); // Error
        // Binary
        assert !testNumber("0b", null, Integer.class); // Error
        assert testNumber("0b0", 0, Integer.class); // Ok
        assert testNumber("0b1", 1, Integer.class); // Ok
        assert testNumber("0b1100101", 101, Integer.class); // Ok
        assert !testNumber("0b2", null, Integer.class); // Error
        // Binary Long
        assert !testNumber("0bL", null, Long.class); // Error
        assert testNumber("0b0L", 0L, Long.class); // Ok
        assert testNumber("0b1L", 1L, Long.class); // Ok
        assert testNumber("0b1100101L", 101L, Long.class); // Ok
        assert !testNumber("0b2L", null, Long.class); // Error
        // Float
        assert !testNumber(".F", null, Float.class); // Error
        assert testNumber(".0F", .0F, Float.class); // Ok
        assert testNumber(".0535F", .0535F, Float.class); // Ok
        assert testNumber("0.F", 0.F, Float.class); // Ok
        assert testNumber("0.0F", 0.0F, Float.class); // Ok
        assert testNumber("50.034F", 50.034F, Float.class); // Ok
        assert testNumber("0F", 0F, Float.class); // Ok
        assert testNumber("35450F", 35450F, Float.class); // Ok
        // Double
        assert !testNumber(".D", null, Double.class); // Error
        assert testNumber(".0D", .0D, Double.class); // Ok
        assert testNumber(".0535D", .0535D, Double.class); // Ok
        assert testNumber("0.D", 0.D, Double.class); // Ok
        assert testNumber("0.0D", 0.0D, Double.class); // Ok
        assert testNumber("50.034D", 50.034D, Double.class); // Ok
        assert testNumber("0D", 0D, Double.class); // Ok
        assert testNumber("35450D", 35450D, Double.class); // Ok

        prepareBotAPI();

        IRCUser user = new IRCUser("heldplayer");
        ISandboxDelegate sandbox = SandboxManager.createSandbox(user);
        sandbox.addCommand("test");

        String input = "    this.is$valid";
        JavaObject object = new JavaObject(input);

        System.out.println(object.value.toString());
    }

    private static boolean testNumber(String input, Number value, Class<? extends Number> expected) {
        try {
            Number number = new JavaReader(input).readNumber();
            if (!number.equals(value)) {
                throw new JarException("Expected " + value + " but got " + number);
            }
            if (number.getClass() != expected) {
                throw new JarException("Expected " + expected.getSimpleName() + " but got " + number.getClass().getSimpleName());
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
