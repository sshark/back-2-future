case class State[+T, S](run: S => (T, S))

sealed trait Input
case object Coin extends Input
case object Turn extends Input

case class Machine(locked: Boolean, candies: Int, coins: Int)

def simulateMachine(inputs: List[Input]): State[(Int, Int), Machine] = State[(Int, Int), Machine] (state => {
    val last = inputs.foldLeft(state) {
      case (s, Coin) => Machine(false, s.candies, s.coins + 1)
      case (s, Turn) => if (s.locked) s else Machine(true, s.candies - 1, s.coins)
    }
    ((last.coins, last.candies), last)
  })
    
simulateMachine(List(Coin, Turn, Coin, Turn, Coin, Turn, Coin, Turn)).run(Machine(true, 5, 10))._1
