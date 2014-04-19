
package me.heldplayer.util.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

class JSONParser {

    private Reader reader;

    // To find errors
    private long column;
    private long row;
    private long index;

    public String createErrorLocation() {
        return "char " + this.index + " (row " + this.row + "; column " + this.column + ")";
    }

    private char previous;
    private boolean usePrevious;

    public JSONParser(String input) {
        this(new StringReader(input));
    }

    public JSONParser(Reader reader) {
        if (reader.markSupported()) {
            this.reader = reader;
        }
        else {
            this.reader = new BufferedReader(reader);
        }

        this.column = 0;
        this.index = 0;

        this.previous = 0;
        this.usePrevious = false;
    }

    public void goBack() {
        if (this.usePrevious || this.index < 0) {
            throw new JSONException("Failed going back along the input");
        }
        this.index--;
        this.column--;
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
                c = this.reader.read();
            }
            catch (IOException e) {
                throw new JSONException(e);
            }

            if (c < 0) {
                c = 0;
            }
        }

        this.index++;

        if (this.previous == '\r') {
            this.row++;
            this.column = c == '\n' ? 0 : 1;
        }
        else if (c == '\n') {
            this.row++;
            this.column = 0;
        }
        else {
            this.column++;
        }

        this.previous = (char) c;

        return this.previous;
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

    public Object readValue() {
        char c = this.readNormalChar();

        switch (c) {
        case '"':
        case '\'':
            return this.readString(c);
        case '{':
            this.goBack();
            return new JSONObject(this);
        case '[':
            this.goBack();
            return new JSONArray(this);
        }

        StringBuffer result = new StringBuffer();
        while (c >= ' ' && ",:{}[];=#\\\"'".indexOf(c) < 0) {
            result.append(c);
            c = this.readChar();
        }

        this.goBack();

        String resultString = result.toString();
        if (resultString.isEmpty()) {
            throw new JSONException("Expected value but got nothing at " + this.createErrorLocation());
        }

        return JSONParser.strToObject(resultString);
    }

    public String readString(char opening) {
        StringBuffer result = new StringBuffer();
        while (true) {
            char c = this.readChar();
            switch (c) {
            case 0:
            case '\r':
            case '\n':
                throw new JSONException("Expected '" + opening + "' but got newline at " + this.createErrorLocation());
            case '\\':
                c = this.readChar();
                switch (c) {
                case 'b':
                    result.append('\b');
                break;
                case 't':
                    result.append('\t');
                break;
                case 'n':
                    result.append('\n');
                break;
                case 'f':
                    result.append('\f');
                break;
                case 'r':
                    result.append('\r');
                break;
                case 'u':
                    result.append((char) Integer.parseInt(this.readChars(4), 4));
                break;
                case '"':
                case '\'':
                case '\\':
                case '/':
                    result.append(c);
                break;
                default:
                    throw new JSONException("Received invalid escape code '\\" + c + "' at " + this.createErrorLocation());
                }
            break;
            default:
                if (c == opening) {
                    return result.toString();
                }
                result.append(c);
            }
        }
    }

    public static Object strToObject(String input) {
        if (input.isEmpty()) {
            return input;
        }
        if (input.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (input.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (input.equalsIgnoreCase("null")) {
            return JSONParser.NULL;
        }

        char first = input.charAt(0);
        if ((first >= '0' && first <= '9') || first == '-') {
            try {
                if (input.indexOf('.') > -1 || input.indexOf('e') > -1 || input.indexOf('E') > -1) {
                    Double d = Double.valueOf(input);
                    if (!d.isInfinite() && !d.isNaN()) {
                        return d;
                    }
                }
                else {
                    Long l = Long.valueOf(input);
                    if (input.equals(l.toString())) {
                        if (l.longValue() == l.intValue()) {
                            return Integer.valueOf(l.intValue());
                        }
                        else {
                            return l;
                        }
                    }
                }
            }
            catch (Throwable e) {}
        }

        return input;
    }

    public static Object NULL = new NULL();

    private static class NULL {

        private NULL() {}

        @Override
        public String toString() {
            return "null";
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == null || obj == this;
        }

    }

}
