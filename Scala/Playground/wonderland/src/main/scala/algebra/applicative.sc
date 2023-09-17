import algebra.*

import algebra.Applicative
import algebra.Applicative.syntax.*
import algebra.instances.option.given

// Testing pure
Applicative[Option].pure(18)

// Testing ap
val sum3 = (x: Int, y: Int, z: Int) => x + y + z
val o1 = Some(5)
val o2 = Some(3)
val o3 = Some(2)

o3 <*> (o2 <*> o1.map(sum3.curried))

case class Point(x: Int, y: Int)

val x = Some(0)
val y = Some(1)

def map2[A, B, C](o1: Option[A], o2: Option[B])(f: (A, B) => C): Option[C] =
  o2 <*> o1.map(f.curried)
  
map2(x, y)(Point.apply)

// Testing *> operator (keepLeft)
Option(5) *> Option(6)
Option.empty[Int] *> Option(7)

// Testing <* operator (keepRight)
Option(5) <* Option(6)
Option(7) <* Option.empty[Int]
