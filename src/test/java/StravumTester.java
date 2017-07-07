import com.github.jamesgallicchio.stravum.HashUtils;
import com.github.jamesgallicchio.stravum.MiningJob;
import com.github.jamesgallicchio.stravum.StravumConnection;
import com.github.jamesgallicchio.stravum.Worker;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class StravumTester {
    public static void main(String[] args) {

        System.out.println();

        StravumConnection c = new StravumConnection("us-east.stratum.slushpool.com");

        AtomicReference<String> blockHead = new AtomicReference<>();
        AtomicInteger nonce = new AtomicInteger();
        AtomicInteger ntime = new AtomicInteger();

        Worker w = new Worker("jamesgallicchio", "worker1", j -> {
            System.out.println(j.getJobID());
        });
        c.authorizeWorker(w);
    }
}
