
package me.heldplayer.web.server.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import me.heldplayer.web.server.RequestSource;

public class FileResponse extends WebResponse {

    private File file;

    public FileResponse(File file) throws IOException {
        super();
        this.file = file;
    }

    @Override
    public WebResponse writeResponse(RequestSource source) throws IOException {
        Extension extension = Extension.fromFileName(this.file.getName());

        this.header.writeBytes("HTTP/1.0 200 OK\r\n");
        this.header.writeBytes("Connection: close\r\n");
        this.header.writeBytes("Server: HeldBot\r\n");
        this.header.writeBytes("Content-Type: " + extension.type + "\r\n");

        FileInputStream input = new FileInputStream(this.file);

        if (source.method.hasBody) {
            while (true) {
                int b = input.read();
                if (b == -1) {
                    break;
                }
                this.body.write(b);
            }
        }

        input.close();

        return this;
    }

    private static enum Extension {

        TextPlain("text/plain"),
        TextHtml("text/html", "htm", "html", "xhtm", "xhtml"),
        TextCss("text/css", "css"),
        TextJavascript("text/javascript", "js");

        public final String type;
        public final Set<String> extensions;

        private Extension(String type, String... extensions) {
            this.type = type;

            HashSet<String> set = new HashSet<String>();
            for (String extension : extensions) {
                set.add(extension);
            }

            this.extensions = Collections.unmodifiableSet(set);
        }

        public static Extension fromFileName(String name) {
            name = name.toLowerCase();

            if (name.lastIndexOf(".") < 0) {
                return TextPlain;
            }
            String ext = name.substring(name.lastIndexOf(".") + 1);

            for (Extension extension : values()) {
                if (extension.extensions.contains(ext)) {
                    return extension;
                }
            }

            return TextPlain;
        }
    }

}
