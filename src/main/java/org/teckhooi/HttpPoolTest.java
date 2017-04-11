package org.teckhooi;

import javaslang.control.Try;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.teckhooi.HttpPoolTest.RunOption.RunB1;


public class HttpPoolTest {

    // Refactor RunOption to use direct methods
    public enum RunOption {
        RunA, RunB, RunB1, RunAll
    }

    public static void main(String[] args) throws Exception {
        System.out.println("http.maxConnections (default: 5): " + System.getProperty("http.maxConnections"));
        System.out.println("http.keepAlive (default: true): " + System.getProperty("http.keepAlive"));
        new HttpPoolTest().run(new URL("http://myrest.getsandbox.com/users/"), RunB1, 20, Optional.of(new ProxyAddress("localhost", 8080)));
    }

    public void run(URL url, RunOption option, int numOfConnections, Optional<ProxyAddress> proxyAddress) throws Exception {
        switch (option) {
            case RunA:
                reportElaspedTime(() -> runWithHttpClient(url, numOfConnections, proxyAddress.map(p -> new HttpHost(p.getHost(), p.getPort()))));
                break;
            case RunB:
                reportElaspedTime(() -> runWithURL(url, numOfConnections,
                    proxyAddress.map(p -> new Proxy(Proxy.Type.HTTP, new InetSocketAddress(p.getHost(), p.getPort()))), true));
                break;
            case RunB1:
                reportElaspedTime(() -> runWithURL(url, numOfConnections,
                    proxyAddress.map(p -> new Proxy(Proxy.Type.HTTP, new InetSocketAddress(p.getHost(), p.getPort()))), false));
                break;
            default:
                reportElaspedTime(() -> runWithHttpClient(url, numOfConnections, proxyAddress.map(p -> new HttpHost(p.getHost(), p.getPort()))));
                reportElaspedTime(() -> runWithURL(url, numOfConnections,
                    proxyAddress.map(p -> new Proxy(Proxy.Type.HTTP, new InetSocketAddress(p.getHost(), p.getPort()))), true));
                break;
        }
    }

    private boolean runWithHttpClient(URL url, int numOfClients, Optional<HttpHost> proxy) {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(1);

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
            .setConnectionManager(cm)
            .setMaxConnTotal(1)
            .setMaxConnPerRoute(5);

        CloseableHttpClient httpClient = proxy.map(p -> httpClientBuilder.setProxy(p).build())
            .orElse(httpClientBuilder.build());

        /*
          Option A: Using Apache HTTP Client

          Preferred way to do HTTP connection in term of ease of use and predictability.

          NOTE: Connecting to a different path of the same server does not create another connection socket.
        */
        try {
            HttpPost request = new HttpPost(url.toURI());
            request.addHeader("Content-type", "application/json");
            request.addHeader("Accept", "application/json");

            generateAndStart(numOfClients, httpClient, request, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean runWithURL(URL url, int numClients, Optional<Proxy> proxy, boolean runConcurrent) {

        /*
          Option B: JDK 8 URL connections

          Bad connection will result closing current socket and use a new socket. Using URLConnection instances created
          from the same URL instance in a multi threaded environment will create multiple sockets to the server. To use
          persistent connection, close the input stream and request for a new URLConnection instance from the URL
          instance again. Closing the URLConnection does not equal to closing the socket.

          NOTE: Connecting to a different path of the same server does not create another connection socket.
         */
        try {
            if (runConcurrent) {
                concurrentGenerateAndStart(numClients, url, proxy, 0);
            } else {
                generateAndStart(numClients, url, proxy, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void concurrentGenerateAndStart(int num, URL url, Optional<Proxy> proxy, long delay) throws IOException {
        Thread[] workers = new Thread[num];
        for (int i = 0; i < num; i++) {
            URLConnection urlConnection = proxy.map(p -> {
                try {
                    return url.openConnection(p);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).orElse(url.openConnection());
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Content-type", "application/json");
            urlConnection.setDoOutput(true);

            System.out.println("Starting JDK URL worker: " + i);
            int ndx = i;

            sleep(500);

            workers[i] = new Thread(() -> connect(urlConnection, delay, ndx));
            workers[i].start();
        }

        for (int j = 0; j < workers.length; j++) {
            try {
                workers[j].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateAndStart(int num, URL url, Optional<Proxy> proxy, long delay) throws IOException {
        for (int i = 0; i < num; i++) {
            URLConnection urlConnection = proxy.map(p -> {
                try {
                    return url.openConnection(p);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).orElse(url.openConnection());
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Content-type", "application/json");
            urlConnection.setDoOutput(true);

            System.out.println("Starting JDK URL worker: " + i);
            int ndx = i;

            sleep(500);

            connect(urlConnection, delay, ndx);

        }
    }

    private void connect(URLConnection urlConnection, long delay, int ndx) {
        try {
            System.out.println(urlConnection.getURL().toString() + " started.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String message = "{\"name\":\"" + UUID.randomUUID().toString() + "\"}";

        PrintWriter writer = null;
        BufferedReader reader = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
            writer.print(message);
            writer.flush();
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            printResponse(reader, message, ndx);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void generateAndStart(int num, CloseableHttpClient httpClient, HttpPost request, long delay) throws Exception {
        Thread[] workers = new Thread[num];

        for (int i = 0; i < num; i++) {
            System.out.println("Starting worker: " + i);
            final int j = i;

            sleep(500);

            workers[i] = new Thread(() -> {
                try {
                    connect(httpClient, request, delay, j);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            workers[i].start();
        }

        for (int i = 0; i < workers.length; i++) {
            try {
                workers[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void connect(CloseableHttpClient httpClient, HttpPost request, long delay, int ndx) throws IOException {
        System.out.println(request.getURI().toString() + " started.");
        String message = "{\"name\":\"" + UUID.randomUUID().toString() + "\"}";
        request.setEntity(new StringEntity(message));

        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(httpClient.execute(request, HttpClientContext.create()).getEntity().getContent()))) {

            printResponse(reader, message, ndx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printResponse(BufferedReader reader, String message, int ndx) throws IOException {
        String line;
        System.out.println("Status return for " + ndx + ": " + message);
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }

    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reportElaspedTime(Supplier<Boolean> supplier) {
        long start = System.currentTimeMillis();
        System.out.println("Time elapsed: " + (supplier.get() ? (System.currentTimeMillis() - start) : "** Exception **"));
    }

    static class ProxyAddress {
        private String host;
        private int port;

        public ProxyAddress(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }

    private void toRuntimeException(Try.CheckedRunnable runnable) throws RuntimeException {
        try {
            runnable.run();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}