package org.teckhooi.concurrent

import java.util.concurrent.ForkJoinPool

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * Created by sshark on 2017/04/04.
  */

object JumpingJack extends App {

  implicit val ec = ExecutionContext.fromExecutorService(new ForkJoinPool(4))

  def runme(n: Int = 120) = (1 to n).grouped(2).flatMap { tuple =>
    val batches = Future.sequence(tuple.map { x =>
      Future {
        println(s"Running $x...")
        (1 to 100000).toList
      }
    })
    Await.result(batches, 10 second).map(_.sum.toLong)
  }

  def runmeFail(n: Int = 120) = (1 to n).toList.map { x =>
    Future[Long] {
      println(s"Running $x")
      (1 to 100000).toList.sum.toLong
    }
  }

  Await.ready(Future.sequence(runmeFail(25)), 10 second)

  // val result = runme().toList
  val result = runmeFail()
  //  println(result.sum)
  //  println(s"The total of size ${result.size} does " + (if (result.sum != 84609924480L) "NOT " else "") + "tally with 84609924480")
  //  println(s"The result does " + (if (result != 84609924480L) "NOT " else "") + "tally with 84609924480")
}
