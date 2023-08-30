package exercises.part2.chapter9

/** 9.4.1 The JSON format
  *
  * { "Company name" : "Microsoft Corporation", "Ticker" : "MSFT", "Active" : true, "Price" : 30.66, "Shares outstanding" : 8.38e9, "Related
  * companies" : [ "HPQ", "IBM", "YHOO", "DELL", "GOOG" ] }
  */
enum JSON:
  case JNull
  case JNumber(get: Double)
  case JString(get: String)
  case JBool(get: Boolean)
  case JArray(get: IndexedSeq[JSON])
  case JObject(get: Map[String, JSON])

/** 9.4 Writing a JSON parser */
object JSON:
  /** 9.9: Hard: At this point, you will be taking over the process. You’ll be creating a Parser[JSON] from scratch using the primitives we’ve
    * defined. You don’t need to worry (yet) about the representation of Parser. As you go, you’ll undoubtedly discover additional combinators and
    * idioms, notice and factor out common patterns, and so on. Use the skills you’ve been developing throughout this book, and have fun! If you get
    * stuck, you can always consult the answers.
    */
  def jsonParser[Err, Parser[+_]](P: Parsers[Parser]): Parser[JSON] =
    import P.*

    def token(s: String) = string(s).token

    def array: Parser[JSON] = (
      token("[") *> value.sep(token(",")).map(vs => JArray(vs.toIndexedSeq)) <* token("]")
    ).scope("array")

    def obj: Parser[JSON] = (
      token("{") *> keyval.sep(token(",")).map(kvs => JObject(kvs.toMap)) <* token("}")
    ).scope("object")

    def keyval: Parser[(String, JSON)] = escapedQuoted ** (token(":") *> value)

    def lit: Parser[JSON] = (
      token("null").as(JNull) |
        double.map(JNumber(_)) |
        escapedQuoted.map(JString(_)) |
        token("true").as(JBool(true)) |
        token("false").as(JBool(false))
    ).scope("literal")

    def value: Parser[JSON] = lit | obj | array

    (whitespace *> (obj | array)).root
