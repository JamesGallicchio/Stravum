package com.github.jamesgallicchio.stravum;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    private static final byte[] HEADERPADDING = hexToBytes("000000800000000000000000000000000000000000000000000000000000000000000000000000000000000080020000");

    private static final MessageDigest HASHER;

    static {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        HASHER = md;
    }

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i+= 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String bytesToHex(byte[] d) {
        StringBuilder sb = new StringBuilder(d.length * 2);
        int lim = d.length * 2;
        for (int i = 0; i < lim; i += 2) {
            sb.setCharAt(i, Character.forDigit(d[i / 2] >>> 4, 16));
            sb.setCharAt(i + 1, Character.forDigit(d[i / 2] & 0xF,  16));
        }
        return sb.toString();
    }

    public static byte[] bytewiseReverse(byte[] b) {
        byte[] d = new byte[b.length];
        for (int i = 0; i < b.length; i++) {
            d[b.length-1-i] = b[i];
        }
        return d;
    }

    public static byte[] sha256(byte[] data, byte[]... moreData) {
        HASHER.update(data);
        for (byte[] b : moreData) {
            HASHER.update(b);
        }
        return HASHER.digest();
    }

    public static byte[] doubleSHA256(byte[] data, byte[]... moreData) {
        return sha256(sha256(data, moreData));
    }

    public static byte[] blockHash(byte[] blockHeaderStart, int nonce) {
        HASHER.update(blockHeaderStart);
        HASHER.update((byte) (nonce >> 24));
        HASHER.update((byte) (nonce >> 16));
        HASHER.update((byte) (nonce >> 8));
        HASHER.update((byte) nonce);
        HASHER.update(HEADERPADDING);
        return HASHER.digest();
    }

    public static final BigInteger TARGET_DIFFICULTY_ONE = new BigInteger("00000000ffff0000000000000000000000000000000000000000000000000000");

    public static byte[] getTargetForDifficulty(int d) {
        return TARGET_DIFFICULTY_ONE.divide(BigInteger.valueOf(d)).toByteArray();
    }

    public static boolean hashLessThanTarget(byte[] hash, byte[] target) {
        if (hash.length != target.length) {
            throw new IllegalArgumentException("Hash and target not same length!");
        }

        for (int i = 0; i < hash.length; i++) {
            if (hash[i] < target[i]) return true;
            else if (hash[i] > target[i]) return false;
        }
        return false;
    }
}