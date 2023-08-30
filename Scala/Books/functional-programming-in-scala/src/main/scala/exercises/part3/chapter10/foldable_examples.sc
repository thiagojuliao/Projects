import exercises.part1.chapter3.Tree
import exercises.part1.chapter3.Tree.*
import exercises.part3.chapter10.Foldable
import exercises.part3.chapter10.Foldable.given
import exercises.part3.chapter10.instances.int.given

/** List */
val foldableList = Foldable[List]

val ls = List.range(1, 6)

foldableList.foldLeft(ls)(0)(_ + _)  // 1 + 2 + 3 + 4 + 5 = 15
foldableList.foldRight(ls)(Nil: List[Int])(_ :: _)
foldableList.foldRight(ls)(0)(_ - _) // (1 - (2 - (3 - (4 - (5 - 0))))) = 1 - (2 - (3 - (4 - 5))) = 1 - (2 - (3 + 1)) = 1 - (2 - 4) = 1 + 2 = 3
foldableList.foldMap(ls)(_ * 2)      // 2 + 4 + 6 + 8 + 10 = 30

/** Indexed Seq */
val foldableIndexSeq = Foldable[IndexedSeq]

val seq = IndexedSeq.range(1, 6)

foldableIndexSeq.foldLeft(seq)(0)(_ + _)
foldableIndexSeq.foldRight(seq)(Nil: List[Int])(_ :: _)
foldableIndexSeq.foldRight(seq)(0)(_ - _)
foldableIndexSeq.foldMap(seq)(_ * 2)

/** Lazy List */
val foldableLazyList = Foldable[LazyList]

val lz = LazyList.from(1).take(10)

foldableLazyList.foldLeft(lz)(0)(_ + _)
foldableLazyList.foldRight(lz)(LazyList.empty[Int])(_ #:: _).take(10).toList
foldableLazyList.foldMap(lz)(_ + 10)

/** Trees */
val foldableTree = Foldable[Tree]

val tree = Branch(Branch(Leaf(2), Leaf(5)), Branch(Leaf(1), Branch(Leaf(10), Leaf(7))))

foldableTree.foldLeft(tree)(0)(_ + _)
foldableTree.foldRight(tree)(Nil: List[Int])(_ :: _)
foldableTree.foldMap(tree)(_ * 10)
