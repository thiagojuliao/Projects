import parsers.*
import algebra.Applicative
import algebra.instances.parser.given
import algebra.Functor.syntax.*
import algebra.Applicative.syntax.*
import parsers.JsonParser.{jsonNumberParser, jsonParser}

// Default Parsers Testing
charParser('a').run("aabbxx")
stringParser("test").run("testing")
numberParser.run("1234testingHello45")
stringLiteralParser.run("\"hello world\"")
arrayParser(numberParser).run("[1,2,3,4,5]")
arrayParser(stringLiteralParser).run("[\"thiago\", \"lollys\"]")

keyValueParser(stringLiteralParser).run("\"key\":\"value\"")
mapParser(numberParser).run("{\"a\":12345, \"b\": 54321}")

// Json Parsers Testing
import parsers.JsonParser.*

jsonNullParser.run("nullable")
jsonBoolParser.run("truenull")
jsonNumberParser.run("1234testingHello45")
jsonStringParser.run("\"hello world\"12345")
jsonArrayParser.run("[1, null, true, \"hello world\"]")

jsonParser.run("\"hello world\"")
jsonParser.run("1234567890")
jsonParser.run("null")
jsonParser.run("false")
jsonParser.run("[1, null, true, \"hello world\"]")
jsonParser.run("{\"name\": \"Thiago Juliao\", \"age\": 34}")

Parser.many(jsonParser).run("12345\"hello world\"truefalsenull[1,2,3,4,5]")

// Testing on a true JSON input
val json =
  "{\"data\": [{\"type\": \"articles\", \"id\": \"1\", \"attributes\": {\"title\": \"JSON:API paints my bikeshed!\", \"body\": \"The shortest article. Ever.\", \"created\": \"2015-05-22T14:56:29.000Z\", \"updated\": \"2015-05-22T14:56:28.000Z\"}, \"relationships\": {\"author\": {\"data\": {\"id\": \"42\", \"type\": \"people\"}}}}], \"included\": [{\"type\": \"people\", \"id\": \"42\", \"attributes\": {\"name\": \"John\", \"age\": 80, \"gender\": \"male\"}}]}"

println(json)
jsonParser.run(json)
