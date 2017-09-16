package com.github.jamesgallicchio.stravum.stravum

import java.nio.{ByteBuffer, ByteOrder}
import java.security.MessageDigest

import com.github.jamesgallicchio.stravum.HashUtils.{Hash, hasherLock}

class Header private (val bytes: Array[Byte]) extends AnyVal {
  def hash(nonce: Int, h: MessageDigest): Hash = {
    h.update(bytes)
    h.update(nonce.asInstanceOf[Byte])
    h.update((nonce >> 8).asInstanceOf[Byte])
    h.update((nonce >> 16).asInstanceOf[Byte])
    h.update((nonce >> 24).asInstanceOf[Byte])
    h.digest(h.digest())
  }
}

object Header {
  def apply(version: Int, lastHash: Hash, merkleRoot: Hash, nTime: Int, nBits: Int) =
    new Header(ByteBuffer.allocate(76).order(ByteOrder.LITTLE_ENDIAN)
      .putInt(version).put(lastHash.reverse).put(merkleRoot.reverse).putInt(nTime).putInt(nBits).array())
}