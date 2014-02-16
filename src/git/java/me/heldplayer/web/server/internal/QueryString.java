
package me.heldplayer.web.server.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.TreeMap;

public class QueryString {

    public Map<String, String> values;

    public QueryString() {
        this.values = new TreeMap<String, String>();
    }

    public QueryString(String data) {
        this();

        if (data.indexOf('?') >= 0) {
            data = data.substring(data.indexOf('?') + 1);
        }

        String[] pairs = data.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            String key = keyValue[0];
            String value = "";
            if (keyValue.length > 1) {
                try {
                    value = URLDecoder.decode(keyValue[1], "UTF-8");
                }
                catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            this.values.put(key, value);
        }
    }

}
