package com.github.jamesgallicchio.stravum;

import com.thetransactioncompany.jsonrpc2.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class JsonRPCSocket implements Closeable {

    private Socket s;
    private BufferedReader in;
    private BufferedWriter out;

    private List<Consumer<JSONRPC2Notification>> handlers;
    private Map<Object, JSONRPC2Response> waiting;

    private boolean polling;

    public JsonRPCSocket(String url, int port) {
        try {
            s = new Socket(InetAddress.getByName(url), port, InetAddress.getLocalHost(), 0);
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        polling = true;
        new Thread(() -> {
            try {
                while (polling) {
                    try {
                        String incoming = in.readLine();
                        JSONRPC2Message m = JSONRPC2Message.parse(incoming);

                        if (m instanceof JSONRPC2Response) {
                            waiting.put(((JSONRPC2Response) m).getID(), (JSONRPC2Response) m);
                        } else if (m instanceof JSONRPC2Notification) {
                            for (Consumer<JSONRPC2Notification> handler : handlers) {
                                handler.accept((JSONRPC2Notification) m);
                            }
                        }
                    } catch (JSONRPC2ParseException ignored) {}
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).run();
    }

    public JSONRPC2Response request(JSONRPC2Request r) throws IOException {
        out.write(r.toJSONString());
        out.flush();

        Object key = r.getID();
        try {
            while (!waiting.containsKey(key)) Thread.sleep(25);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return waiting.remove(key);
    }

    public void addNotificationHandler(Consumer<JSONRPC2Notification> handler) {
        handlers.add(handler);
    }

    public void notify(JSONRPC2Notification n) throws IOException {
        out.write(n.toJSONString());
        out.flush();
    }

    public void close() {
        try {
            polling = false;
            in.close();
            out.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
