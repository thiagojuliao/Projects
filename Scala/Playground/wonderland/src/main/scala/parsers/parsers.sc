import parsers.*

// Default Parsers Testing
charParser('a').run("aabbxx")
stringParser("test").run("testing")
numberParser.run("1234testingHello45")
stringLiteralParser.run("\"hello world\"")

// Json Parsers Testing
import parsers.JsonParser.*

jsonNullParser.run("nullable")
jsonBoolParser.run("truenull")
jsonNumberParser.run("1234testingHello45")
jsonStringParser.run("\"hello world\"12345")

jsonParser.run("\"hello world\"")
jsonParser.run("1234567890")
jsonParser.run("null")
jsonParser.run("false")
