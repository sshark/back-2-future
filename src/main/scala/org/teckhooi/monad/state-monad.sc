trait StateMonad[+T, S] {
  self =>
  def apply(state: S): (T, S)

  def flatMap[U](f: T => StateMonad[U, S]) = new StateMonad[U, S] {
    override
    def apply(state: S) = {
      val (v, s) = self(state)
      f(v)(s)
    }
  }

  def map[U](f: T => U) = new StateMonad[U, S] {
    override
    def apply(state: S) = {
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

  def pop[A](state: List[A]): StateMonad[Option[A], List[A]] = new StateMonad[Option[A], List[A]] {
    override def apply(state: List[A]): (Option[A], List[A]) = state match {
      case x :: xs => (Some(x), xs)
      case _ => (None, state)
    }
  }
}

