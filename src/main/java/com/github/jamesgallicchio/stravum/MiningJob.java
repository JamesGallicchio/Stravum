package com.github.jamesgallicchio.stravum;

import java.util.List;

public class MiningJob {

    private String jobID;
    private String prevhash;
    private String coinb1;
    private String coinb2;
    private String exn1;
    private String exn2;
    private int exn2Length;
    private List<String> merkleBranch;
    private byte[] merkleRoot;
    private String version;
    private String nbits;
    private String ntime;
    private boolean cleanJobs;

    private String blockHeaderStart;
    private byte[] networkDifficulty;
    private int difficultyLevel;
    private byte[] target;

    public MiningJob(String jobID, String prevhash, String coinb1, String coinb2, List<String> merkleBranch, String version,
                     String nbits, String ntime, boolean cleanJobs, String ex1, int exn2Length, int difficulty, byte[] networkDifficulty) {
        this.jobID = jobID;
        this.prevhash = prevhash;
        this.coinb1 = coinb1;
        this.coinb2 = coinb2;
        this.exn1 = ex1;
        this.exn2 = generateExn(jobID, exn2Length);
        this.exn2Length = exn2Length;
        this.merkleBranch = merkleBranch;
        this.version = version;
        this.nbits = nbits;
        this.ntime = ntime;
        this.cleanJobs = cleanJobs;
        this.networkDifficulty = networkDifficulty;
        this.difficultyLevel = difficulty;
        this.target = HashUtils.getTargetForDifficulty(difficulty);

        merkleRoot = HashUtils.doubleSHA256(HashUtils.hexToBytes(getCoinbase()));
        for (String s : merkleBranch) {
            merkleRoot = HashUtils.doubleSHA256(merkleRoot, HashUtils.hexToBytes(s));
        }

        blockHeaderStart = version + prevhash + HashUtils.bytesToHex(HashUtils.bytewiseReverse(merkleRoot)) + ntime  + nbits;
    }

    public String getBlockHeaderStart() {
        return blockHeaderStart;
    }

    public String getExtranonce2() {
        return exn2;
    }

    public String getCoinbase() {
        return coinb1 + exn1 + exn2 + coinb2;
    }

    private static String generateExn(String jobID, int exn2Length) {
        exn2Length--;
        while (exn2Length > 0) {
            jobID = hexGen() + jobID + hexGen();
        }
        return jobID;
    }

    private static String hexGen() {
        return Integer.toString((int) (Math.random()*16), 16);
    }

    public String getJobID() {
        return jobID;
    }

    public String getPrevhash() {
        return prevhash;
    }

    public int getExn2Length() {
        return exn2Length;
    }

    public List<String> getMerkleBranch() {
        return merkleBranch;
    }

    public String getVersion() {
        return version;
    }

    public String getNbits() {
        return nbits;
    }

    public String getNtime() {
        return ntime;
    }

    public boolean isCleanJobs() {
        return cleanJobs;
    }

    public byte[] getNetworkDifficulty() {
        return networkDifficulty;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public byte[] getTarget() {
        return target;
    }
}
