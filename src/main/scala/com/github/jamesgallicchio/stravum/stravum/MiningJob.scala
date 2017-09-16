package com.github.jamesgallicchio.stravum.stravum

import com.github.jamesgallicchio.stravum.HashUtils.{Hash, hash2}

import scala.util.Random

case class MiningJob(jobID: String, prevhash: Hash, coinb1: Array[Byte], coinb2: Array[Byte], merkleBranch: List[Hash], version: Int,
                nBits: Int, nTime: Int, cleanJobs: Boolean, exn1: Array[Byte], exn2Len: Int, diff: Int) {
  def genExn2(): Array[Byte] =
    (jobID.grouped(2).map(s => (s.charAt(0) ^ s.charAt(1)).toByte) ++ MiningJob.bytePad).take(exn2Len).toArray

  def genMerkRoot(): Hash = merkleBranch.foldLeft(hash2(coinb1, exn1, genExn2(), coinb2)) { hash2(_, _) }

  def generateHeader(): Header = Header(version, prevhash, genMerkRoot(), nTime, nBits)
}

object MiningJob {
  val bytePad: Iterator[Byte] = Iterator.continually(Random.nextInt().toByte)
}