package com.github.jamesgallicchio.stravum

import java.io.{InputStreamReader, PrintStream}
import java.net.{InetAddress, Socket}

import com.github.jamesgallicchio.stravum.stravum.{StravumConnection, Worker}
import monix.execution.Scheduler.Implicits.global

object Test {
  def main(args: Array[String]): Unit = {
    /*val head = Header("00000002".intUnhex,
      "000000000000000082ccf8f1557c5d40b21edabb18d2d691cfbf87118bac7254".unhex,
      "7cbbf3148fe2407123ae248c2de7428fa067066baee245159bf4a37c37aa0aab".unhex,
      1399704683, "1900896c".intUnhex)

    val hash = head.hash(Integer.parseUnsignedInt("3476871405"))*/

    val c = new StravumConnection("hi", 0)

    c.jobStream.foreach(j => println(s"Job received: ${j.jobID}"))

    c.authorize(Worker("jamesgallicchio", "worker1"))
  }
}