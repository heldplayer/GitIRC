
package me.heldplayer.irc.base.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public final class JavaReader {

    private Reader reader;

    private long index;

    private char previous;
    private boolean usePrevious;

    public JavaReader(String input) {
        this(new StringReader(input));
    }

    public JavaReader(Reader reader) {
        if (reader.markSupported()) {
            this.reader = reader;
        }
        else {
            this.reader = new BufferedReader(reader);
        }

        this.index = 0;

        this.previous = 0;
        this.usePrevious = false;
    }

    public JavaPart readPart(JavaPart parent) {
        StringBuffer result = new StringBuffer();
        char first = this.readChar();

        while (first == ' ' || first == 0) {
            if (first == 0) {
                return null;
            }
            first = this.readChar();
        }

        this.goBack();

        if (parent == null && "0123456789.".indexOf(first) >= 0) {
            return new NumberPart(parent, this.readNumber());
        }
        if (parent != null) {
            if (first == '(') {

            }
            if (first == '[' && parent instanceof FieldArrayPart) {
                this.readChar();
                // Read Integer only
                FieldArrayPart array = (FieldArrayPart) parent;
                Number number = this.readNumber();
                if (!(number instanceof Integer)) {
                    throw new JavaException("Array index must be an integer");
                }
                array.index = number.intValue();
            }
        }

        boolean firstChar = true;
        while (true) {
            char c = this.readChar();

            if (c == 0) {
                break;
            }
            if (firstChar) {
                if (Character.isDigit(c)) {
                    throw new JavaException("Identifier cannot start with a number: " + c);
                }
                else if (".()[]{}".indexOf(c) >= 0) {
                    throw new JavaException("Identifier mustn't start with " + c);
                }
                firstChar = false;
            }

            if (c == '.') {
                return new NamedPart(parent, result.toString());
            }
            if (c == '(') {
                this.goBack();
                return new MethodPart(parent, result.toString());
            }
            if (c == '[') {
                this.goBack();
                return new FieldArrayPart(parent, result.toString());
            }
            if (c == ' ') {
                this.goBack();
                if (parent == null) {
                    return new TextPart(parent, result.toString());
                }

                return new FieldPart(parent, result.toString());
            }

            if (!(Character.isLetter(c) || Character.isDigit(c) || c == '$' || c == '_' || c == '.' || c == 0)) {
                throw new JavaException("Invalid identifier character: '" + c + "'");
            }

            result.append(c);
        }

        String resultString = result.toString();

        if (resultString == null || resultString.isEmpty()) {
            return null;
        }

        if (parent == null) {
            return new TextPart(parent, resultString);
        }

        return new FieldPart(parent, resultString);
    }

    public String readChars(int count) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < count; i++) {
            result.append(this.readChar());
        }

        return result.toString();
    }

    public char readNormalChar() {
        while (true) {
            char c = this.readChar();

            if (c == 0 || c > ' ') {
                return c;
            }
        }
    }

    public Number readNumber() {
        boolean hasPoint = false;
        boolean isExtended = false;
        boolean terminated = false;
        boolean hex = false;
        boolean bit = false;
        boolean negative = false;
        StringBuilder result = new StringBuilder();

        char first = this.readChar();

        while (first == ' ' || first == 0) {
            first = this.readChar();
        }
        this.goBack();

        boolean firstChar = true;
        char c = this.readChar();
        char prev = 0;

        final String characters = "=!*+-/|&<>%^;:[](){}";

        while (c != 0 && c != ' ') {
            if (characters.indexOf(c) >= 0) {
                break;
            }
            if (terminated) {
                throw new JavaException("Number should be terminated but got '" + c + "'");
            }
            if (Character.isDigit(c) || (!hasPoint && hex && "ABCDEFabcdef".indexOf(c) >= 0)) {
                if (firstChar && c == '0') {
                    char next = this.readChar();
                    boolean goBack = true;
                    if (next == 0 || characters.indexOf(next) >= 0) {
                        result.append(c);
                        terminated = true;
                        goBack = false;
                    }
                    if ("FDLfdl".indexOf(next) >= 0) {
                        if (next == 'F' || next == 'f') {
                            hasPoint = true;
                        }
                        if (next == 'D' || next == 'd') {
                            hasPoint = true;
                            isExtended = true;
                        }
                        if (next == 'L' || next == 'l') {
                            isExtended = true;
                        }

                        result.append(c);
                        terminated = true;
                        goBack = false;
                    }
                    if (next == '.') {
                        result.append(c);
                    }
                    if (Character.isDigit(next) || (!hasPoint && hex && "ABCDEFabcdef".indexOf(next) >= 0)) {
                        hex = true;
                        bit = true;
                    }
                    if (next == 'X' || next == 'x') {
                        hex = true;
                        bit = false;
                    }
                    if (next == 'B' || next == 'b') {
                        bit = true;
                        hex = false;
                    }
                    if (goBack) {
                        this.goBack();
                    }
                    firstChar = false;

                    prev = c;
                    c = this.readChar();

                    continue;
                }
                else if (firstChar && c == '-') {
                    negative = true;

                    continue;
                }
                else if (firstChar) {
                    firstChar = false;
                    if (prev == '0' && (c == 'b' || c == 'B')) {
                        bit = true;
                        hex = false;

                        prev = c;
                        c = this.readChar();

                        continue;
                    }
                }

                result.append(c);
            }
            else if (c == '.') {
                if (hasPoint) {
                    throw new JavaException("Invalid number character: '" + c + "'");
                }
                hasPoint = true;
                hex = false;
                firstChar = false;

                result.append(c);
            }
            else {
                if (c == 'f' || c == 'F') {
                    hasPoint = true;
                    bit = false;
                    hex = false;
                    terminated = true;
                }
                else if (c == 'd' || c == 'D') {
                    hasPoint = true;
                    isExtended = true;
                    bit = false;
                    hex = false;
                    terminated = true;
                }
                else if (c == 'l' || c == 'L') {
                    isExtended = true;
                    terminated = true;
                }
                else if (!hasPoint && (c == 'x' || c == 'X') && prev == '0') {
                    hex = true;
                    result = new StringBuilder();
                    firstChar = false;
                }
                else if (!hasPoint && (c == 'b' || c == 'B') && prev == '0') {
                    bit = true;
                    result = new StringBuilder();
                    firstChar = false;
                }
                else {
                    throw new JavaException("Invalid number character: '" + c + "'");
                }
            }

            prev = c;
            c = this.readChar();
        }

        String output = (negative ? "-" : "") + result.toString();

        // System.out.println(hasPoint + " " + isExtended + " " + hex + " " + bit + " " + terminated + " " + output);

        if (output == null || output.isEmpty()) {
            throw new JavaException("No number specified");
        }

        if (hasPoint) {
            if (isExtended) {
                return Double.parseDouble(output);
            }
            else {
                return Float.parseFloat(output);
            }
        }
        else {
            if (isExtended) {
                if (hex && bit) { // 001234567L
                    return Long.parseLong(output, 8);
                }
                else if (hex) { // 0x0123456789ABCDEFL
                    return Long.parseLong(output, 16);
                }
                else if (bit) { // 0b01L
                    return Long.parseLong(output, 2);
                }
                else { // 0123457689L
                    return Long.parseLong(output);
                }
            }
            else {
                try {
                    if (hex && bit) { // 001234567
                        return Integer.parseInt(output, 8);
                    }
                    else if (hex) { // 0x0123456789ABCDEF
                        return Integer.parseInt(output, 16);
                    }
                    else if (bit) { // 0b01
                        return Integer.parseInt(output, 2);
                    }
                    else { // 0123457689
                        return Integer.parseInt(output);
                    }
                }
                catch (NumberFormatException e) {
                    if (hex && bit) { // 001234567L
                        return (int) Long.parseLong(output, 8);
                    }
                    else if (hex) { // 0x0123456789ABCDEFL
                        return (int) Long.parseLong(output, 16);
                    }
                    else if (bit) { // 0b01L
                        return (int) Long.parseLong(output, 2);
                    }
                    else { // 0123457689L
                        return (int) Long.parseLong(output);
                    }
                }
            }
        }
    }

    public void goBack() {
        if (this.usePrevious || this.index < 0) {
            throw new JavaException("Failed going back along the input");
        }
        this.index--;
        this.usePrevious = true;
    }

    public char readChar() {
        int c = 0;

        if (this.usePrevious) {
            this.usePrevious = false;
            c = this.previous;
        }
        else {
            try {
                if (this.reader.ready()) {
                    c = this.reader.read();
                }
            }
            catch (IOException e) {
                throw new JavaException(e);
            }

            if (c < 0) {
                c = 0;
            }
        }

        this.index++;

        this.previous = (char) c;

        return this.previous;
    }

}
