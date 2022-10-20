package com.net128.oss.web.webshell.commands.picocli;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.toilelibre.libe.curl.Curl;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * net commands
 */
@Slf4j
public class NetCommands {
    @Command(description = "A java implementation of curl.",
            mixinStandardHelpOptions = true)
    public String jcurl(
            @Option(names = "-v", description = "Be verbose") boolean verbose,
            @Parameters(description = "The URI to make a request against.", paramLabel = "URI") String uri
    ) {
        try {
            if (uri.trim().length() == 0) {
                throw new RuntimeException("Host name is required.");
            }
            if (!uri.matches("^[A-Za-z]*://.*")) {
                uri = "http://" + uri;
            }
            HttpResponse response = Curl.curl(uri);
            HttpEntity entity = response.getEntity();
            StringBuilder sb = new StringBuilder();
            if (verbose) {
                Header[] headers = response.getAllHeaders();
                for (Header responseHeader : headers) {
                    sb.append(responseHeader.getName());
                    sb.append(": ");
                    sb.append(responseHeader.getValue());
                    sb.append("\n");
                }
                if (headers.length > 0) {
                    sb.append("\n");
                }
            }
            sb.append(EntityUtils.toString(entity));
            return sb.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
