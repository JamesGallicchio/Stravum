package com.github.jamesgallicchio.stravum.jsonrpc

import java.io.{BufferedReader, BufferedWriter, InputStreamReader, OutputStreamWriter}
import java.net.{InetAddress, Socket}
import java.util.concurrent.{Executors, TimeUnit}

import com.github.jamesgallicchio.stravum.jsonrpc.Message.{Notify, Request, Response}
import monix.reactive.Observable
import monix.execution.Scheduler.Implicits.global
import play.api.libs.json.JsValue

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.concurrent.duration.TimeUnit

class JsonRPCSocket(host: String, port: Int, notifyCacheMax: Int = 5) {
  //private val socket = new Socket(InetAddress.getByName(host), port, InetAddress.getLocalHost, 0)
  private val out = new BufferedWriter(new OutputStreamWriter(System.out)) //socket.getOutputStream))

  // Holds pending requests
  private val requests = mutable.Map[JsValue, Promise[Response]]()

  // Observes socket lines
  private val obs = Observable.fromLinesReader(new BufferedReader(new InputStreamReader(System.in))) //socket.getInputStream)))
    .map(log(_, outbound = false))
    .map(Message.parse).filter(_.isDefined).map(_.get).share

  // Process Responses
  obs.collect { case r: Response => r }.foreach(r => requests.remove(r.id).foreach(_.success(r)))

  // Process notifications
  val notifyStream: Observable[Notify] = obs.collect { case n: Notify => n }.cache(notifyCacheMax)

  private val executor = Executors.newScheduledThreadPool(10)

  def request(request: Request, timeout: Long = 15, unit: TimeUnit = TimeUnit.SECONDS): Future[Message.Response] = {
    // Store a promise to be responded to
    val promise = Promise[Response]
    requests.put(request.id, promise)

    // Send request
    println(request.toString)
    out.write(request.toString)
    out.flush()

    // Remove request from list on timeout
    executor.schedule((() => requests.remove(request.id).foreach(_.failure(new InterruptedException()))): Runnable, timeout, unit)

    promise.future
  }

  def notify(notif: Notify): Unit = {
    out.write(log(notif.toString, outbound = true))
    out.flush()
  }

  private def log(s: String, outbound: Boolean): String = {
    println((if(outbound) "Outbound: " else "Inbound: ") + s)
    s
  }
}