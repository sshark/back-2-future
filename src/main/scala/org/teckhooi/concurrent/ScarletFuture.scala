package org.teckhooi.concurrent

import org.teckhooi.concurrent.Foo._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

/**
  * This code demonstrates how Future and Await work hand in hand
  * to disallow the application to exit without producing the
  * output it is expected
  */
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

    bigF.map(_ => "BigF is fine")
      .recover{case t => "BigF is facing some trouble"}
      .foreach(println)

    /**
      * It is important to assign a value to the new instance created
      * from `smallF.map(...)`. `Await.ready(...)` cannot tell if the
      * code in the second `map(...)` has completed if
      * `smallF` is passed to `Await` instead of `newSmallF`
      */
    val newSmallF = smallF.map(_ => {
      Try(Thread.sleep(sleepMillis))
      "SmallF is fine"
    }).recoverWith{case t => Future.failed(t)}

    /**
      * Changing this line to
      *
      * val bothF = bigF.zip(newSmallF)
      *
      * will have a different output,
      *
      * [info] Foo running with 200
      * [info] Foo running with -10
      * [info] Fail => Must be +ve
      *
      * because bigF is a Failure and therefore bothF is a Failure without
      * execute what in newSmallF.map(...)
      *
      * On the other hand, the current output is
      *
      * [info] Foo running with -10
      * [info] Foo running with 200
      * [info] BigF is facing some trouble
      * [info] Fail => Must be +ve
      *
      * bigF still a `Failure` because its value is -ve but the content of
      * `newSmallF` is exexcuted and will take additional `sleepMillis`
      */
    val bothF = newSmallF.zip(bigF)

    /**
      * Assuming `sleepMillis` is set to 2s, the application will complete
      * in approximately 4.5s where 2s for bigF and smallF running
      * simultaneously, another 2s waiting in `newSmallF.map(...)` and
      * finally few tens of a second for overhead. It should not taken
      * 10s to complete.However, if the `Await` time is set to, say, 3s,
      * an exception will we thrown from `Await.ready(...)` because there
      * is not enough time for `bothF` to complete.
      *
      * If `newSmallF` is used over `bothF` and a +ve value is used in bigF
      * i.e. `Option(10)` the output of `Await.ready(...)` would be
      *
      * [info] Pass: SmallF is fine
      */
    Await.ready(newSmallF, 10 seconds).onComplete{
      case Success(x) => println(s"Pass: $x")
      case Failure(t) => println(s"Fail => ${t.getMessage}")
    }
  }
}
