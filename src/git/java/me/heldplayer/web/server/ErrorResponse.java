
package me.heldplayer.web.server;

import java.io.IOException;

public class ErrorResponse extends WebResponse {

    private ErrorType type;

    public ErrorResponse(ErrorType type) throws IOException {
        super();
        this.type = type;
    }

    @Override
    public WebResponse writeResponse(RequestFlags flags) throws IOException {
        out.writeBytes("HTTP/1.0 " + type.code + " " + type.reason + "\r\n");
        out.writeBytes("Connection: close\r\n");
        out.writeBytes("Server: ModeratorGui\r\n");
        out.writeBytes("Content-Type: text/plain\r\n");
        out.writeBytes("\r\n");

        if (flags.method.hasBody) {
            out.writeBytes("Error code " + type.code + " - " + type.reason + "\r\n");
            out.writeBytes("Unable to load the requested page :(\r\n");
            out.writeBytes("\r\n");
            out.writeBytes("Please try again later\r\n");
        }

        return this;
    }

    public static enum ErrorType {
        BadRequest("400", "Bad Request"),
        Unauthorized("401", "Unauthorized"),
        PaymentRequired("402", "Payment Required"),
        Forbidden("403", "Forbidden"),
        NotFound("404", "Not Found"),
        MethodNotAllowed("405", "Method Not Allowed"),
        NotAcceptable("406", "Not Acceptable"),
        ProxyAuthenticationRequired("407", "Proxy Authentication Required"),
        RequestTimeout("408", "Request Time-out"),
        Conflict("409", "Conflict"),
        Gone("410", "Gone"),
        LengthRequired("411", "Length Required"),
        PreconditionFailed("412", "Precondition Failed"),
        RequestEntityTooLarge("413", "Request Entity Too Large"),
        RequestURITooLarge("414", "Request-URI Too Large"),
        UnsupportedMediaType("415", "Unsupported Media Type"),
        RequestRangeNotSatisfiable("416", "Request Range Not Satisfiable"),
        ExpectationFailed("417", "Expectation Failed"),
        InternalServerError("500", "Internal Server Error"),
        NotImplemented("501", "Not Implemented"),
        BadGateway("502", "Bad Gateway"),
        ServiceUnavailable("503", "Service Unavailable"),
        GatewayTimeout("504", "Gateway Time-out"),
        HTTPVersionNotSupported("505", "HTTP Version Not Supported");

        public final String code, reason;

        private ErrorType(String code, String reason) {
            this.code = code;
            this.reason = reason;
        }
    }

}
