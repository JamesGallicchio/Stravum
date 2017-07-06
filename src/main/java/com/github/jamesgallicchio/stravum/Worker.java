package com.github.jamesgallicchio.stravum;

import java.util.function.Consumer;

public class Worker {

    public final String user;
    public final String pass;
    public final Consumer<MiningJob> updateJob;

    public Worker(String user, String pass, Consumer<MiningJob> updateJob) {
        this.user = user;
        this.pass = pass;
        this.updateJob = updateJob;
    }

    public void setJob(MiningJob j) {
        updateJob.accept(j);
    }
}
