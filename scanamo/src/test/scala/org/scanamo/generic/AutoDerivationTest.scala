package org.scanamo.generic

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scanamo._

class AutoDerivationTest extends AnyFunSuite with Matchers {

  test("Derivation should correctly encode Option with Map inside") {
    import org.scanamo.generic.auto._

    case class OptionWrapper(theOption: Option[InnerWrapper])
    case class InnerWrapper(innerMap: Map[String, String])

    val value = OptionWrapper(Some(InnerWrapper(Map())))

    val result = DynamoFormat[OptionWrapper].write(value)

    result should ===(
      DynamoValue.fromDynamoObject(
        DynamoObject(
          "theOption" -> DynamoValue.fromDynamoObject(
            DynamoObject("innerMap" -> DynamoValue.fromDynamoObject(DynamoObject.empty))
          )
        )
      )
    )
  }

  test("Derivation should correctly encode Option with List inside") {
    import org.scanamo.generic.auto._

    case class OptionWrapper(theOption: Option[InnerWrapper])
    case class InnerWrapper(innerList: List[String])

    val value = OptionWrapper(Some(InnerWrapper(List())))

    val result = DynamoFormat[OptionWrapper].write(value)

    result should ===(
      DynamoValue.fromDynamoObject(
        DynamoObject(
          "theOption" -> DynamoValue.fromDynamoObject(
            DynamoObject("innerList" -> DynamoValue.fromDynamoArray(DynamoArray(List())))
          )
        )
      )
    )
  }

  test("Derivation should prioritise implicits from DynamoFormat companion") {
    import org.scanamo.generic.auto._

    val value = Some("umbrella")
    val expected = DynamoFormat.optionFormat[String].write(value)

    val actual = DynamoFormat[Option[String]].write(value)

    actual should ===(expected)
  }

  test("Derivation should prioritise implicits from user specified companions") {
    import org.scanamo.generic.auto._

    case class Foobar(value: Unit)
    object Foobar {
      def fromString(s: String): Either[TypeCoercionError, Foobar] = s match {
        case "foo" => Right(Foobar(()))
        case _     => Left(TypeCoercionError(new RuntimeException(s"$s is not a foo")))
      }

      implicit val dynamoFormatFoo: DynamoFormat[Foobar] =
        DynamoFormat.xmap[Foobar, String](fromString, (_: Foobar) => "foo")
    }

    val result = DynamoFormat[Foobar].write(Foobar(()))

    result should ===(DynamoValue.fromString("foo"))
  }

}
