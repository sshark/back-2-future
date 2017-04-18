trait StateMonad[+T, S] { self =>
  def apply(state: S): (T, S)

  def flatMap[U](f: T => StateMonad[U, S]) = new StateMonad[U, S] {
    override def apply(state: S) = {
      val (v, s) = self(state)
      f(v)(s)
    }
  }

  def map[U](f: T => U) = new StateMonad[U, S] {
    override def apply(state: S) = {
      val (v, s) = self(state)
      (f(v), s)
    }
  }
}

object StateMonad {
  def apply[T, S](value: T) = new StateMonad[T, S] {
    override def apply(state: S) = (value, state)
  }
}

object Stack {
  def push[A](a: A) = new StateMonad[Unit, List[A]] {
    override def apply(state: List[A]) = (Unit, a :: state)
  }

  def pop[A] = new StateMonad[Option[A], List[A]] {
    override def apply(state: List[A]): (Option[A], List[A]) = state match {
      case x :: xs => (Some(x), xs)
      case _ => (None, state)
    }
  }
}

import Stack._

val result = for {
  _ <- push(2)
  _ <- push(3)
  _ <- push(4)
  x <- pop
} yield x

println(result(List()))

/*
val res = push(2).flatMap(_ => push(3)).flatMap(_ => push(4)).flatMap(_ => pop).map(identity)
println(res(List()))
*/
