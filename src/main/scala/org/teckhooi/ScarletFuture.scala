package org.teckhooi

import scala.util.{Success, Failure, Try}
import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._

import org.teckhooi.concurrent.Foo._

object ScarletFuture extends App {
  def optionToTry[A](o: Option[A]) = o match {
    case Some(a) => Success(a)
    case None => Failure(new RuntimeException("Empty value"))
  }

  override def main(args: Array[String]) = {
    println("Scarlet running...")
    
    val sleepMillis = 3000

    val bigF = Future {
      Try(Thread.sleep(sleepMillis))
      optionToTry(Option(10)).flatMap(t => Try(foo(t))).get
    }

    val smallF = Future {
      Try(Thread.sleep(sleepMillis))
      optionToTry(Option(-200)).flatMap(t => Try(foo(t))).get
    }

    Try(Thread.sleep(2000));

    val bigSmallF = bigF.zip(smallF)

    bigF.map{t => "BigF is fine"}
      .recover{case t => "BigF is facing some trouble"}
      .foreach {s => println(s)}

    smallF.map{t => "SmallF is fine"}
      .recover{case t => "SmallF is facing some trouble"}
      .foreach {s => println(s)}

    Await.ready(bigSmallF, 10 seconds)
  }
}

