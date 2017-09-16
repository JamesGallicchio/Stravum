package com.github.jamesgallicchio.stravum

import java.io.{InputStreamReader, PrintStream}
import java.net.{InetAddress, Socket}
import java.nio.{ByteBuffer, ByteOrder}

import com.github.jamesgallicchio.stravum.stravum.{Header, StravumConnection, Worker}
import com.github.jamesgallicchio.stravum.HashUtils._
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Test {
  def main(args: Array[String]): Unit = {

    /*val head = Header("00000002".intUnhex,
      "000000000000000082ccf8f1557c5d40b21edabb18d2d691cfbf87118bac7254".unhex,
      "7cbbf3148fe2407123ae248c2de7428fa067066baee245159bf4a37c37aa0aab".unhex,
      1399704683, "1900896c".intUnhex)
    val hash = hasherLock{head.hash(Integer.parseUnsignedInt("3476871405"), _)}

    println(hash)

    println(HashUtils.underTarget(hash, HashUtils.targetForDiff(1000000)))

    val target = HashUtils.targetForDiff(1)

    val threads = 8
    val jump = (1024L*1024*1024*4/threads).asInstanceOf[Int]

    val time = System.currentTimeMillis()
    (0 until threads par).map(_*jump).foreach { start =>
      hasherLock { hasher =>
        for (nonce <- start until start + jump) {
          val h = head.hash(nonce, hasher)
          if (HashUtils.underTarget(h, target)) println(s"$nonce: ${h.hex}")
        }
      }
    }
    println(s"Took ${System.currentTimeMillis()-time}")*/

    //Await.result(Observable.range(Integer.MIN_VALUE, Integer.MAX_VALUE, 1).mapAsync(10){ nonce => Task(head.hash(nonce.asInstanceOf[Int]))}.filter(HashUtils.underTarget(_, target)).toListL.foreach(println), Duration.Inf)

    /*val c = new StravumConnection("hi", 0)

    c.jobStream.foreach(j => println(s"Job received: ${j.jobID}"))

    c.authorize(Worker("jamesgallicchio", "worker1"))

    c.jobStream.*/


  }
}