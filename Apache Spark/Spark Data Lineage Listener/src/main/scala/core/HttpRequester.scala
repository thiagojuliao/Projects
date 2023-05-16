package core

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest

import scala.util.{Failure, Success}

class HttpRequester extends Actor with ActorLogging {

  implicit val system: ActorSystem = context.system
  import system.dispatcher

  override def receive: Receive = {
    case r: HttpRequest =>
      log.info(s"Sending a http request to ${r.uri}")

      val responseFuture = Http().singleRequest(r)

      responseFuture.onComplete {
        case Success(response) =>
          log.info(s"The request was successful and returned: ${response.status}")
          response.discardEntityBytes()

        case Failure(exception) =>
          log.info(s"The request has failed with the following exception: $exception")
      }

    case _ => log.warning("Please send a valid http request.")
  }
}
