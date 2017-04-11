package org.teckhooi.fpway;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Created by sshark on 2017/03/19.
 */

public class Proxies {
    private String[] proxies;
    private int[] ports;
    private int ndx;

    public Proxies(String[] proxies, int[] ports) {
        this.proxies = proxies;
        this.ports = ports;
    }

    public Proxy nextAvailableProxy() {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxies[0], ports[0]));
    }
}
