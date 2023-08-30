package playgrounds

import data.Kleisli
import data.Kleisli.*
import instances.all.given
import syntax.all.*

object KleisliPlayground extends App {
  trait Server
  case class ServerConfig(hostname: String, port: String)

  val reader: Reader[ServerConfig, Server] = Kleisli(_ => new Server {})

  reader.ask
    .flatMap { config =>
      config.show

      Kleisli.pure(config)
    }
    .run(ServerConfig("test", "22"))
}
