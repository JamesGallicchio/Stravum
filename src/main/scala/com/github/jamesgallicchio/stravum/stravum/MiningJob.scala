package com.github.jamesgallicchio.stravum.stravum

import com.github.jamesgallicchio.stravum.HashUtils.{Hash, hash2}

case class MiningJob(jobID: Array[Byte], prevhash: Hash, coinb1: Array[Byte], coinb2: Array[Byte], merkleBranch: List[Hash], version: Int,
                nBits: Int, nTime: Int, cleanJobs: Boolean, exn1: Array[Byte], exn2Len: Int, diff: Int) {
  def genExn2(exn: Int): Array[Byte] = {
    val arr = new Array[Byte](exn2Len)
    for(i <- 0 until exn2Len) {
      arr.update(i,
        if (i < jobID.length)
          jobID(i)
        else
          (exn >> (8 * (i-jobID.length))).asInstanceOf[Byte])
    }
    arr
  }


  def genMerkRoot(exn: Int): Hash = merkleBranch.foldLeft(hash2(coinb1, exn1, genExn2(exn), coinb2)) { hash2(_, _) }

  def generateHeader(exn: Int): Header = Header(version, prevhash, genMerkRoot(exn), nTime, nBits)
}