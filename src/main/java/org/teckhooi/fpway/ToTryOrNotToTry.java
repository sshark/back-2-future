package org.teckhooi.fpway;

import javaslang.control.Option;
import javaslang.control.Try;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by sshark on 2017/03/19.
 */

public class ToTryOrNotToTry {
    public void classicalPush(byte[] request, String targetURLString, Proxies proxies) {

        URLConnection connection;
        OutputStream os = null;

        try {
            URL targetURL = new URL(targetURLString);
            if (proxies == null) {
                connection = targetURL.openConnection();
            } else {
                connection = targetURL.openConnection(proxies.nextAvailableProxy());
            }

            connection.setRequestProperty("Content-Type", "Application/json");
            connection.setDoOutput(true);

            os = connection.getOutputStream();
            os.write(request);

        } catch (Exception e) {

        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

/*
    public Try<Void> push(byte[] request, String targetURLString, Option<Proxies> optionProxies) {
        optionProxies.toTry().flatMap(proxies -> Try.of(() -> new URL(targetURLString).openConnection(proxies.nextAvailableProxy())));

    }
*/

    public static void main(String[] args) {
        String targetURLString = "http://www.ibm.com";

        Try<URLConnection> tryConnection = Option.of(new ToTryOrNotToTry().getProxies(false))
            .toTry()
            .flatMap(proxies -> Try.of(() -> new URL(targetURLString).openConnection(proxies.nextAvailableProxy())))
            .recoverWith(t -> Try.of(() -> new URL(targetURLString).openConnection()));
        System.out.println(tryConnection);
    }

    private Proxies getProxies(boolean expectProxy) {
        return expectProxy ? new Proxies(new String[]{"proxy.ibm.com"}, new int[]{8080}) : null;
    }
}
