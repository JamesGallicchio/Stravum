package com.github.jamesgallicchio.stravum.stravum

case class Worker(user: String, worker: String) {
  val name: String = user + "." + worker
}