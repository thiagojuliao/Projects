import java.time.LocalDate

import exercises.part1.chapter4.Validated
import exercises.part3.chapter12.Applicative
import exercises.part3.chapter12.Applicative.*
import exercises.part3.chapter12.NonEmptyList
import exercises.part3.chapter12.instances.nel.given
import exercises.part3.chapter12.instances.validated.given
import exercises.part3.chapter12.instances.option.given

/** Applicative Functionality Testing */
val fmp1 = Map("a" -> Some("Alexander"), "b" -> Some("Britney"))
Applicative[Option].sequenceMap(fmp1)

val fmp2 = Map("a" -> Some("Alexander"), "b" -> None)
Applicative[Option].sequenceMap(fmp2)

/** Listing 12.5 Validating user input in a web form */
case class WebForm(name: String, birthdate: LocalDate, phoneNumber: String)

def validName(name: String): Validated[NonEmptyList[String], String] =
  if name != "" then Validated.Valid(name)
  else Validated.Invalid(NonEmptyList("Name cannot be empty"))

def validBirthdate(birthdate: String): Validated[NonEmptyList[String], LocalDate] =
  try Validated.Valid(LocalDate.parse(birthdate))
  catch
    case _: java.time.format.DateTimeParseException =>
      Validated.Invalid(NonEmptyList("Birthdate must be in the form yyyy-MM-dd"))

def validPhone(phoneNumber: String): Validated[NonEmptyList[String], String] =
  if phoneNumber.matches("[0-9]{10}") then Validated.Valid(phoneNumber)
  else Validated.Invalid(NonEmptyList("Phone number must be 10 digits"))

def validateWebForm(name: String, birthdate: String, phone: String): Validated[NonEmptyList[String], WebForm] =
  validName(name).map3(validBirthdate(birthdate), validPhone(phone))(WebForm.apply)

validateWebForm("", "18-04-1989", "1234567890")
