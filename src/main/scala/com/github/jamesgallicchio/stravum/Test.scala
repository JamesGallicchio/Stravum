package com.github.jamesgallicchio.stravum

import com.github.jamesgallicchio.stravum.HashUtils._
import com.github.jamesgallicchio.stravum.jsonrpc.Message
import com.github.jamesgallicchio.stravum.jsonrpc.Message.Notify
import com.github.jamesgallicchio.stravum.stravum.MiningJob

object Test {
  def main(args: Array[String]): Unit = {

    val res = Message.parse("{\"params\":[\"3a417\",\"47382094d1e4b353b4a2c36c43e5cf9dd992a8f2002249220000000000000000\",\"01000000010000000000000000000000000000000000000000000000000000000000000000ffffffff4503346907fabe6d6d8bb024ca97bc837cf0db5de4022a99d3891f61d9b1003ef22f6b4a9bcca377db0100000000000000\",\"17a4032f736c7573682f0000000002afd6bf4a000000001976a9147c154ed1dc59609e3d26abb2df2ea3d587cd8c4188ac0000000000000000266a24aa21a9ede0a9cbad3feb11a3f32554b01830be3ac5d6c46769ead848e7d78a1b0501e2b900000000\",[\"788b000479c982726be1997c9581b2dcf4fe39544c20adfb498f5e653a698cf5\",\"dde155d9d0be3e1b762c1deb740b52559cbb120b24d9946ef1bbc4da8f177e6d\",\"70579224731f6f08c75d0ef7cbe11cf348ce43649b60aba595cd1c7d59ba8374\",\"6d81a530a9a91bfd1e758d34b2df00ac37f21af67162129691f877790b4db58f\",\"213c8bb14e859542af3add2434c9c61f17e392bace0e48c7d5b0192e4a3ff3a4\",\"38efc53735c96fa8bde8d7341543ceec16446fd2e23938422aa1d87d5992719c\",\"0a25643516077c6bf4e1f332e876021fe96e0af10081095babaafcf8616409af\"],\"20000000\",\"1801310b\",\"59bdff93\",false],\"id\":null,\"method\":\"mining.notify\"}")

    res flatMap {
      case Notify(m, p) =>
        for {
        jobID <- p(0).validate[String].asOpt
        prevhash <- p(1).validate[String].asOpt
        coinb1 <- p(2).validate[String].asOpt
        coinb2 <- p(3).validate[String].asOpt
        merkle <- p(4).validate[List[String]].asOpt
        version <- p(5).validate[String].asOpt
        nBits <- p(6).validate[String].asOpt
        nTime <- p(7).validate[String].asOpt
        clean <- p(8).validate[Boolean].asOpt
      } yield MiningJob(
        jobID.unhex,
        prevhash.unhex,
        coinb1.unhex,
        coinb2.unhex,
        merkle.map(_.unhex),
        version.intUnhex,
        nBits.intUnhex,
        nTime.intUnhex,
        clean,
        "016503001f6ca3".unhex,
          4,
          128
      )
      case _ => None
    } foreach { j =>
      for(i <- 0 to 10) println(j.generateHeader(i).bytes.hex)
    }
    /*val head = Header("00000002".intUnhex,
      "000000000000000082ccf8f1557c5d40b21edabb18d2d691cfbf87118bac7254".unhex,
      "7cbbf3148fe2407123ae248c2de7428fa067066baee245159bf4a37c37aa0aab".unhex,
      1399704683, "1900896c".intUnhex)
    val hash = hasherLock{head.hash(Integer.parseUnsignedInt("3476871405"), _)}

    println(hash)

    println(HashUtils.underTarget(hash, HashUtils.targetForDiff(1000000)))

    val target = HashUtils.targetForDiff(1)

    val time = System.currentTimeMillis()
    (0 until threads par).map(_*jump).foreach { start =>
      hasherLock { hasher =>
        for (nonce <- start until start + jump) {
          val h = head.hash(nonce, hasher)
          if (HashUtils.underTarget(h, target)) println(s"$nonce: ${h.hex}")
        }
      }
    }
    println(s"Took ${System.currentTimeMillis()-time}")

    Await.result(Observable.range(Integer.MIN_VALUE, Integer.MAX_VALUE, 1).mapAsync(10){ nonce => Task(head.hash(nonce.asInstanceOf[Int]))}.filter(HashUtils.underTarget(_, target)).toListL.foreach(println), Duration.Inf)

    val c = new StravumConnection("stratum.slushpool.com", 3333)

    println("printing jobs")
    c.jobStream.foreach(j => println(s"Job received: ${j.jobID}"))

    println("auth worker")
    c.authorize(Worker("jamesgallicchio", "worker1"))

    println("handling jobs")
    val threads = 8
    val jump = (1024L*1024*1024*4/threads).asInstanceOf[Int]

    private class JobRunner(job: MiningJob) {
      private var running = true
      def stop(): Unit = running = false

      new Thread { override def run() = {
        while(running) {
          // Get target and header
          val target = HashUtils.targetForDiff(job.diff)
          val header = job.generateHeader()

          //
          (0 until threads par).map(_ * jump).foreach { start =>
            hasherLock { hasher =>
              for (nonce <- start until start + jump) {
                val h = header.hash(nonce, hasher)
                if (HashUtils.underTarget(h, target)) {
                  println(s"$nonce: ${h.hex}")

                }
              }
            }
          }
        }
      }}
    }

    var job: Thread = null // current running job

    // cancel previous running job and start running on new job !
    c.jobStream foreach (newJob => job synchronized {
      if (job != null) job
      job
      job.start()
    })*/
  }
}