package com.github.jamesgallicchio.stravum.stravum

import java.nio.{ByteBuffer, ByteOrder}

import com.github.jamesgallicchio.stravum.HashUtils.{ByteArrayHasher, Hash}

class Header private (val bytes: ByteBuffer) extends AnyVal {
  def hash(nonce: Int): Hash = {
    bytes.putInt(76, nonce)
    bytes.array().hash2
  }
}

object Header {
  def apply(version: Int, lastHash: Hash, merkleRoot: Hash, nTime: Int, nBits: Int) =
    new Header(ByteBuffer.allocate(80).order(ByteOrder.LITTLE_ENDIAN)
      .putInt(version).put(lastHash.reverse).put(merkleRoot.reverse).putInt(nTime).putInt(nBits))
}