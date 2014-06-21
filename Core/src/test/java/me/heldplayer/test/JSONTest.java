
package me.heldplayer.test;

import me.heldplayer.util.json.JSONException;
import me.heldplayer.util.json.JSONObject;
import me.heldplayer.util.json.JSONWriter;

public class JSONTest {

    public static void main(String[] args) {
        try {
            String input = "{true:false,\"null\":null,\"some\\\\ stuff\":[true, true, \"false\", 'true', 'null',{true:false,null:null}]}";
            System.out.println(input);
            System.out.println();
            JSONObject object = new JSONObject(input);

            String resultString = JSONWriter.write(object);
            System.out.println(resultString);
            System.out.println();
            JSONObject newObject = new JSONObject(input);

            String newResultString = JSONWriter.write(newObject);
            System.out.println(newResultString);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
