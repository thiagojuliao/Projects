package year2016.day2

trait KeyPad[T]:
  def get: Array[Array[T]]
  def dimensions: (Int, Int)
  def getButton(pos: Position): Option[T]

object KeyPad:
  val simpleKeyPad: KeyPad[Int] = new KeyPad[Int]:
    override val dimensions: (Int, Int) = (3, 3)

    override def get: Array[Array[Int]] =
      Array(
        Array(1, 2, 3),
        Array(4, 5, 6),
        Array(7, 8, 9)
      )

    override def getButton(pos: Position): Option[Int] =
      val Position(x, y) = pos
      val condition      = (0 until dimensions._1 contains x) && (0 until dimensions._2 contains y)

      if condition then Some(get(x)(y)) else None

  val bathroomKeyPad: KeyPad[String] = new KeyPad[String]:
    private val empty: String = ""

    override val dimensions: (Int, Int) = (5, 5)

    override def get: Array[Array[String]] =
      Array(
        Array(empty, empty, "1", empty, empty),
        Array(empty, "2", "3", "4", empty),
        Array("5", "6", "7", "8", "9"),
        Array(empty, "A", "B", "C", empty),
        Array(empty, empty, "D", empty, empty)
      )

    override def getButton(pos: Position): Option[String] =
      val Position(x, y)   = pos
      val condition00      = (0 until dimensions._1 contains x) && (0 until dimensions._2 contains y)
      lazy val condition01 = get(x)(y) != empty

      if condition00 && condition01 then Some(get(x)(y)) else None
