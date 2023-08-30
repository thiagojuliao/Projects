package exercises.part3.chapter10

import exercises.part3.chapter10.Monoid

enum WC:
  case Stub(chars: String)
  case Part(lStub: String, words: Int, rStub: String)

/** 10.10: Write a monoid instance for WC, and make sure it meets the monoid laws: */
val wcMonoid: Monoid[WC] = new:
  val empty: WC = WC.Stub("")

  def combine(wc1: WC, wc2: WC): WC = wc1 -> wc2 match
    case WC.Stub(a) -> WC.Stub(b)                   => WC.Stub(a + b)
    case WC.Stub(a) -> WC.Part(l, w, r)             => WC.Part(l + a, w, r)
    case WC.Part(l, w, r) -> WC.Stub(b)             => WC.Part(l, w, r + b)
    case WC.Part(l1, w1, r1) -> WC.Part(l2, w2, r2) =>
      WC.Part(l1, w1 + (if (r1 + l2).isEmpty then 0 else 1) + w2, r2)

  /** 10.11: Use the WC monoid to implement a function that counts words in a String by recursively splitting it into substrings and counting the
    * words in those substrings.
    */
  def wc(c: Char): WC =
    if c.isWhitespace then WC.Part("", 0, "")
    else WC.Stub(c.toString)

  def unstub(s: String): Int =
    if s.isEmpty then 0 else 1

  def count(s: String): Int =
    Monoid.foldMapV(s.toIndexedSeq)(wc)(using wcMonoid) match
      case WC.Stub(s)       => unstub(s)
      case WC.Part(l, w, r) => unstub(l) + w + unstub(r)
