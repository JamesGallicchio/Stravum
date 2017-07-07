package com.github.jamesgallicchio.stravum;

import java.util.function.Consumer;

public class Worker {

    public final String user;
    public final String worker;
    public final Consumer<MiningJob> updateJob;

    public Worker(String user, String worker, Consumer<MiningJob> updateJob) {
        this.user = user;
        this.worker = worker;
        this.updateJob = updateJob;
    }

    public void setJob(MiningJob j) {
        updateJob.accept(j);
    }
}
