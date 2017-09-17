package com.github.jamesgallicchio.stravum.jsonrpc

import java.io.{BufferedReader, BufferedWriter, InputStreamReader, OutputStreamWriter}
import java.net.{InetAddress, Socket}
import java.util.concurrent.{Executors, TimeUnit}

import com.github.jamesgallicchio.stravum.jsonrpc.Message.{Notify, Request, Response}
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import play.api.libs.json.JsValue

import scala.collection.mutable
import scala.concurrent.duration.TimeUnit
import scala.concurrent.{Future, Promise}

class JsonRPCSocket(host: String, port: Int, notifyCacheMax: Int = 5) {
  private val socket = new Socket(InetAddress.getByName(host), port, InetAddress.getLocalHost, 0)
  private val outW = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))
  private object out {
    def write(s: String): Unit = {println(s"out: $s"); outW.write(s)}
    def flush(): Unit = outW.flush()
  }

  // Holds pending requests
  private val requests = mutable.Map[JsValue, Promise[Response]]()

  // Observes socket lines
  private val obs = Observable.fromLinesReader(new BufferedReader(new InputStreamReader(socket.getInputStream)))
    .dump("in: ").map(Message.parse).filter(_.isDefined).map(_.get).publish

  println("pre response handler")

  obs.collect { case r: Response => r }.foreach(r => requests.remove(r.id).foreach(_.success(r)))

  println("past response handler")

  // Process notifications
  val notifyStream: Observable[Notify] = obs.collect { case n: Notify => n }.cache(notifyCacheMax)

  println("past notify stream")

  private val executor = Executors.newScheduledThreadPool(10)

  def request(request: Request, timeout: Long = 15, unit: TimeUnit = TimeUnit.SECONDS): Future[Message.Response] = {
    // Store a promise to be responded to
    val promise = Promise[Response]
    requests.put(request.id, promise)

    // Send request
    out.write(request.toString)
    out.flush()

    // Remove request from list on timeout
    executor.schedule((() => requests.remove(request.id).foreach(_.failure(new InterruptedException()))): Runnable, timeout, unit)

    promise.future
  }

  def notify(notif: Notify): Unit = {
    out.write(notif.toString)
    out.flush()
  }
}