package com.github.jamesgallicchio.stravum.jsonrpc

import play.api.libs.json._

import scala.util.{Success, Try}

sealed trait Message {
  override def toString: String = Message.stringify(this)
}

object Message {
  def parse(json: String): Option[Message] = {
    // Try to parse json
    Try(Json.parse(json)) match {
      case Success(raw) =>
        // Try to make a Response, otherwise a Request, otherwise a Notify
        raw.validate[Response].orElse(raw.validate[Request] match {
          case JsSuccess(v, p) if v.id == JsNull => JsSuccess(Notify(v.method, v.params), p)
          case r => r
        }).orElse(raw.validate[Notify]).asOpt
      case _ => None
    }
  }

  def stringify(msg: Message): String = Json.stringify(msg match {
    case r: Request => Json.toJson(r)
    case r: Response => Json.toJson(r)
    case r: Notify => Json.toJson(r)
  })

  case class Request(id: JsValue, method: String, params: Array[JsValue]) extends Message
  object Request {
    def apply[T](id: JsValue, method: String, params: T*)(implicit conv: T => JsValue): Request =
      Request(id, method, params.map(conv).toArray)
  }
  implicit val requestRead: Reads[Request] = Json.reads[Request]
  implicit val requestWrite: Writes[Request] = Json.writes[Request]

  type Error = Response.Error
  case class Response(id: JsValue, result: JsValue = JsNull, error: JsValue = JsNull) extends Message {
    def getError: Option[Error] = error.validate[Error].asOpt
  }
  object Response{
    case class Error(code: Int, message: String, data: JsValue)
  }
  implicit val responseRead: Reads[Response] = Json.reads[Response]
  implicit val responseWrite: Writes[Response] = Json.writes[Response]
  implicit val errorRead: Reads[Error] = Json.reads[Error]
  implicit val errorWrite: Writes[Error] = Json.writes[Error]

  case class Notify(method: String, params: Array[JsValue] = Array.empty[JsValue]) extends Message
  implicit val notifyRead: Reads[Notify] = Json.reads[Notify]
  implicit val notifyWrite: Writes[Notify] = Json.writes[Notify]
}