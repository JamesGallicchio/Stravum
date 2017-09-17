package com.github.jamesgallicchio.stravum

import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.MessageDigest

import scala.annotation.tailrec
import scala.collection.mutable

object HashUtils {
  private def newHasher = MessageDigest.getInstance("SHA-256")
  private var hashers = List[MessageDigest]()
  def hasherLock[T](f: MessageDigest => T) = {
    val hasher = hashers.synchronized(hashers match {
      case Nil => newHasher
      case head :: tail => hashers = tail; head
    })
    val res = f(hasher)
    hashers.synchronized(hashers = hasher :: hashers)
    res
  }

  type Hash = Array[Byte]

  /* ------ STRING HEX STUFF ------ */
  def hex(bytes: ByteBuffer): String = hex(bytes.array())
  def hex(bytes: Array[Byte]): String = bytes.foldLeft("") { (s, b) => s + Integer.toHexString(15 & (b >> 4)) + Integer.toHexString(15 & b)}

  implicit class BytesToHex(val bytes: Array[Byte]) extends AnyVal {
    def hex: String = HashUtils.hex(bytes)
  }

  implicit class HexConversions(val hex: String) extends AnyVal {
    def unhex: Array[Byte] = hex.grouped(2).map(b =>
        ((b.charAt(0).asDigit << 4) |
          (if (b.length < 2) 0 else b.charAt(1).asDigit))
        .asInstanceOf[Byte]
      ).toArray
    def intUnhex: Int = Integer.parseUnsignedInt(hex, 16)
  }

  /* ------ HASH STUFF ------ */
  implicit class ByteArrayHasher(val bytes: Array[Byte]) extends AnyVal {
    def hash: Hash = hasherLock(_.digest(bytes))
    def hash2: Hash = hasherLock{ h => h.digest(h.digest(bytes)) }
  }

  def hash(data: Array[Byte]*): Hash = hasherLock{ h =>
    data.foreach(h.update)
    h.digest
  }
  def hash2(data: Array[Byte]*): Hash = hasherLock{ h =>
    data.foreach(h.update)
    h.digest(h.digest)
  }

  /* ------ HASH TARGET STUFF ------ */
  private val target1 = new BigInteger("00000000ffff0000000000000000000000000000000000000000000000000000".unhex)
  private val targets = new mutable.HashMap[Long, Array[Byte]]() // cache of precalculated values

  def targetForDiff(diff: Long): Array[Byte] = targets.getOrElseUpdate(diff, {
    val newTarget = target1.divide(BigInteger.valueOf(diff)).toByteArray
    val arr = new Array[Byte](32)
    System.arraycopy(newTarget, 0, arr, 32 - newTarget.length, newTarget.length)
    arr
  })

  def underTarget(hash: Hash, target: Array[Byte]): Boolean = {
    @tailrec
    def rec(h: Hash, hi: Int, t: Hash, ti: Int): Boolean =
      if (hi < 0 || ti >= t.length) false // Check indexes in range
      else Integer.compareUnsigned(h(hi), t(ti)) match {
        case x if x > 0 => false
        case x if x < 0 => true
        case _ => rec(h, hi - 1, t, ti + 1) // Check if equal => keep comparing
      }

    rec(hash, hash.length - 1, target, 0)
  }
}