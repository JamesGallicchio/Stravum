package com.github.jamesgallicchio.stravum;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class StravumConnection {

    private Socket s;
    private BufferedReader in;
    private BufferedWriter out;
    private boolean isListening;
    private Map<Object, Consumer<JSONRPC2Response>> waiting;
    private Consumer<JSONRPC2Notification> notifHandler;

    private int id = 0;

    private String notifID;

    private byte[] difficulty;
    private int shareDifficulty;

    private String exnonce1;
    private int exnonce2Size;

    private List<Worker> workers = new ArrayList<>();

    private Map<String, MiningJob> jobs = new HashMap<>();

    public StravumConnection(String url) {
        try {
            // Open connection
            s = new Socket(url, 3333);
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

            // Send subscription message
            out.write(new JSONRPC2Request("mining.subscribe", id++).toJSONString());
            JSONRPC2Response res = new JSONRPC2Response(in.readLine());
            if (res.getError() == null) {
                // Get response and break up all the resulting information
                List<Object> connRes = (List<Object>) res.getResult();

                Map<Object, Object> subDet = ((List<Object>) connRes.get(0)).stream().map(p -> (List) p).collect(Collectors.toMap(p -> p.get(0), p -> p.get(1)));
                notifID = (String) subDet.get("mining.notify");
                difficulty = HashUtils.hexToBytes((String) subDet.get("mining.set_difficulty"));

                exnonce1 = (String) connRes.get(1);
                exnonce2Size = (Integer) connRes.get(3);

                // Instantiate notifHandler
                notifHandler = n -> {
                    if ("mining.notify".equals(n.getMethod())) {
                        List<Object> params = n.getPositionalParams();
                        MiningJob j = new MiningJob(
                                (String) params.get(0),
                                (String) params.get(1),
                                (String) params.get(2),
                                (String) params.get(3),
                                (List<String>) params.get(4),
                                (String) params.get(5),
                                (String) params.get(6),
                                (String) params.get(7),
                                (boolean) params.get(8),
                                exnonce1, exnonce2Size,
                                shareDifficulty, difficulty
                        );
                        if (j.isCleanJobs()) {
                            jobs.clear();
                        }
                        jobs.put(j.getJobID(), j);
                        workers.forEach(w -> w.updateJob.accept(j));
                    } else if ("mining.set_difficulty".equals(n.getMethod())) {
                        shareDifficulty = (int) n.getPositionalParams().get(0);
                    }
                };

                // Start listening on the socket for responses and notifications
                isListening = true;
                waiting = new HashMap<>();
                new Thread(() -> {
                    try {
                        while (isListening) {
                            JSONRPC2Message msg = JSONRPC2Message.parse(in.readLine());
                            if (msg instanceof JSONRPC2Response) {
                                Consumer<JSONRPC2Response> h = waiting.remove(((JSONRPC2Response) msg).getID());
                                if (h != null) h.accept((JSONRPC2Response) msg);
                            } else if (msg instanceof JSONRPC2Notification) {
                                notifHandler.accept((JSONRPC2Notification) msg);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).run();
            } else {
                System.out.println("Error on subscription message! " + res.getError().getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void authorizeWorker(Worker w) {
        JSONRPC2Request r = new JSONRPC2Request("mining.authorize", Arrays.asList(w.user + "." + w.worker, "pass"), id++);
        try {
            out.write(r.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void submitShare(Worker w, String jobID, String ntime, String nonce) {

        JSONRPC2Request r = new JSONRPC2Request("mining.submit", Arrays.asList(w.user, jobID, jobs.get(jobID).getExtranonce2(), ntime, nonce), id++);
        try {
            out.write(r.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}