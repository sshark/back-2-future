package org.teckhooi

import org.teckhooi.concurrent.Foo._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

object ScarletFuture extends App {
  override def main(args: Array[String]) = {
    println(s"Running... ${ScarletFuture.getClass.getName}")
    
    val sleepMillis = 2000

    val bigF: Future[Unit] = Future {
      Try(Thread.sleep(sleepMillis))
      Option(-10).foreach(foo)
    }

    val smallF: Future[Unit] = Future {
      Try(Thread.sleep(sleepMillis))
      Option(200).foreach(foo)
    }

//    Try(Thread.sleep(2000))

    val bigSmallF = bigF.zip(smallF)

    bigF.map(_ => "BigF is fine")
      .recover{case t => "BigF is facing some trouble"}
      .foreach(println)

    smallF.map(_ => "SmallF is fine")
      .recover{case t => "SmallF is facing some trouble"}
      .foreach(println)

    Await.ready(bigSmallF, 10 seconds).onComplete {
      case Success(_) => println("SmallF done")
      case Failure(t) => println(s"** $t")
    }
  }
}

