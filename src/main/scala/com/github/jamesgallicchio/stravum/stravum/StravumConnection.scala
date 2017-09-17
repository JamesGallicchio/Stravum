package com.github.jamesgallicchio.stravum.stravum

import java.util.concurrent.atomic.AtomicInteger

import com.github.jamesgallicchio.stravum.HashUtils
import com.github.jamesgallicchio.stravum.HashUtils.HexConversions
import com.github.jamesgallicchio.stravum.jsonrpc.JsonRPCSocket
import com.github.jamesgallicchio.stravum.jsonrpc.Message.{Notify, Request, Response}
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import play.api.libs.json.{JsString, Json, Reads}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class StravumConnection(host: String, port: Int) {
  val s = new JsonRPCSocket(host, port)
  private val requestIdCounter = new AtomicInteger() // ID for requests, auto incremented
  private def nextID = Json.toJson(requestIdCounter.incrementAndGet())

  // Subscribe
  println("Subscribing")
  private val subscribeRes = Await.result(s.request(Request(nextID, "mining.subscribe")), Duration.Inf)
  println("Subscribed")
  private val subInfo = subscribeRes.getError match {
    case Some(Response.Error(c, m, _)) => throw new Exception(s"Error on subscribe ($c): $m")
    case _ => subscribeRes.result
  }

  val exn1: Array[Byte] = (subInfo \ 1).validate[String].get.unhex
  val exn2Length: Int = (subInfo \ 2).validate[Int].get

  // Difficulty level mutable storage
  private val diff = new AtomicInteger(1)
  def diffLevel: Int = diff.get

  // Handle notifications
  val jobStream: Observable[MiningJob] = s.notifyStream.map {

    // Set difficulty handling
    case Notify(m, p) if m == "mining.set_difficulty" =>
      diff.set(p(0).validate[Int].getOrElse(diff.get))
      None

    // Mining job handling (map values to mining job)
    case Notify(m, p) if m == "mining.notify" =>
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
        exn1,
        exn2Length,
        diffLevel
      )

    // Ignore any other notifications?? *shouldn't happen* *sweats nervously*
    case j => println(s"Received weird notification: ${j.method}(${j.params})"); None
  }.behavior(None).filter(_.isDefined).map(_.get) // Remove all the Nones and extract the MiningJobs


  // For handling futures validation
  private def validate[T](future: Future[Response])(implicit format: Reads[T]): Future[T] = future.flatMap(r => r.getError match {
    // Error on request
    case Some(Response.Error(c, m, _)) => Future.failed(new Exception(s"$c: $m"))
    // No error
    case None => r.result.validate[T].asOpt match {
        // Unexpected type of result
      case None => Future.failed(new ClassCastException)
        // Everything is good we were success A+
      case Some(t) => Future.successful(t)
    }
  })

  // Handle worker auth
  def authorize(w: Worker): Future[Boolean] = {
    validate[Boolean](
      s.request(Request(nextID, "mining.authorize", w.name, "pwd")(JsString))
    )
  }

  // Handle sending back shares
  def submitShare(w: Worker, jobID: String, exn2: Array[Byte], nTime: Int, nonce: Int): Future[Boolean] = {
    validate[Boolean](
      s.request(Request(nextID, "mining.submit", w.name, jobID, HashUtils.hex(exn2), Integer.toHexString(nTime), Integer.toHexString(nonce))(JsString))
    )
  }
}