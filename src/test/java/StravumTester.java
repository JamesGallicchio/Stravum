import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class StravumTester {
    public static void main(String[] args) {

//        System.out.println();
//
//        StravumConnection c = new StravumConnection("us-east.stratum.slushpool.com", 3333);
//
//        Worker w = new Worker("jamesgallicchio", "worker1", j -> {
//            System.out.println(j.getJobID());
//        });
//        c.authorizeWorker(w);

        try {
            Socket s = new Socket(InetAddress.getByName("us-east.stratum.slushpool.com"), 3333, InetAddress.getLocalHost(), 0);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            out.write("{\"id\": 1, \"method\": \"mining.subscribe\", \"params\": []}\n");
            out.write("{\"params\": [\"jamesgallicchio.worker1\", \"pass\"], \"id\": 2, \"method\": \"mining.authorize\"}\n");
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
