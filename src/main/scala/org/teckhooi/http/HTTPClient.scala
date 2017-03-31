package org.teckhooi.http

import java.net.{InetSocketAddress, Proxy, URL}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}
import scalaz.Reader

/**
  *
  * Created by Lim, Teck Hooi on 31/3/17.
  *
  */

case class ConnectionContext(url: URL, numOfClients: Int, proxyURL: Option[URL])

object HTTPClient extends App {
  val hostF = Future(new URL("http://myrest.getsandbox.com/users"))
  val proxyF = Future(Some(new URL("http://localhost:8080")))
//  val proxyF = Future.successful(None)

  val result = Reader((context: ConnectionContext) => {
    val connections = (1 to context.numOfClients).map(ndx => Future {
      println(s"Connection $ndx running...")

      val urlConnection = context.proxyURL
        .map(p => context.url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(p.getHost, p.getPort))))
        .getOrElse(context.url.openConnection())

      urlConnection.addRequestProperty("Accept", "application/json")
      urlConnection.addRequestProperty("Content-type", "application/json")

      io.Source.fromInputStream(urlConnection.getInputStream).getLines().foreach(println)
    })

    Future.sequence(connections)
  })


  val clientTry = for {
    host <- hostF
    proxy <- proxyF
    _ <- result.run(ConnectionContext(host, 20, proxy))
  } yield "Done"

  clientTry.onComplete {
    case Success(x) => println(x)
    case Failure(t) => t.printStackTrace()
  }

  Await.ready(clientTry, 5 seconds)
}