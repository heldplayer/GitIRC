
package com.mojang.api.http;

public class HttpBody {

    private String bodyString;

    public HttpBody(String bodyString) {
        this.bodyString = bodyString;
    }

    public byte[] getBytes() {
        return this.bodyString != null ? this.bodyString.getBytes() : new byte[0];
    }

}
