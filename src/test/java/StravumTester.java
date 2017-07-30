import com.github.jamesgallicchio.stravum.StravumConnection;
import com.github.jamesgallicchio.stravum.Worker;

public class StravumTester {
    public static void main(String[] args) {

        System.out.println();

        StravumConnection c = new StravumConnection("us-east.stratum.slushpool.com", 8080);

        Worker w = new Worker("jamesgallicchio", "worker1", j -> {
            System.out.println(j.getJobID());
        });
        c.authorizeWorker(w);
    }
}
