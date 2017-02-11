package pl.onewebpro

import cats.data.{NonEmptyList, Validated => CatsValidated}
import pl.onewebpro.validation.basic.validator.{Max, Min, NonEmptyStringValidator, TextFieldValidator}
import pl.onewebpro.validation.core.Validated
import pl.onewebpro.validation.core.data.{Extractor, OptionalExtractor}
import pl.onewebpro.validation.core.entity.ValidationMap
import pl.onewebpro.validation.core.error.ComposedError
import pl.onewebpro.validation.core.schema.Schema
import pl.onewebpro.validation.core.validator._
import scala.collection.mutable.{Map => MutableMap}

import scala.language.implicitConversions

package object validation {

  lazy val schema = Schema

  lazy val nonEmptyString = NonEmptyStringValidator

  def textValidator(min: Int = 0, max: Int = 0): TextFieldValidator = new TextFieldValidator(min, max)

  lazy val textValidator: TextFieldValidator = textValidator()

  def optional[T](validator: Validator[T]): OptionalValidator[T] = new OptionalValidator(validator)

  def collection[T](validator: Validator[T]): CollectionValidator[T] = new CollectionValidator(validator)

  def min(min: Int): Min = new Min(min)

  def max(max: Int): Max = new Max(max)

  def minAndMax(minValue: Int, maxValue: Int): MultiValidator[Int] = multi(min(minValue), max(maxValue))

  def nonEmptyCollection[T](validator: Validator[T]): NonEmptyCollectionValidator[T] =
    new NonEmptyCollectionValidator(validator)

  def multi[T](validators: Validator[T]*): MultiValidator[T] = new MultiValidator[T](validators)

  def of[T]: TypeValidator[T] = new TypeValidator[T]

  implicit def pairToMap[T](pair: (String, Validator[T])): ValidationMap[T] = {
    val (key, validator) = pair
    ValidationMap(key, validator)
  }

  implicit def optionalExtractor[S, R](implicit extractor: Extractor[S, R]): Extractor[S, Option[R]] =
    new OptionalExtractor(extractor)


  implicit class ValidatedErrorsImplicits(errors: NonEmptyList[ComposedError]) {
    def groupedByKey: Map[String, NonEmptyList[ComposedError]] = {
      type Reducer = MutableMap[String, NonEmptyList[ComposedError]]

      def foldToMutableMap: (Reducer, ComposedError) => Reducer = {
        case (container, error) =>
          val value: NonEmptyList[ComposedError] =
            if (container.contains(error.key)) container(error.key).::(error) else NonEmptyList.of(error)
          container += (error.key -> value)
      }

      errors.foldLeft[Reducer](MutableMap.empty)(foldToMutableMap).toMap
    }
  }

  implicit class ValidatedImplicits[T](result: Validated[T]) {
    def groupedErrors: CatsValidated[Map[String, NonEmptyList[ComposedError]], T] = {
      result.leftMap(_.groupedByKey)
    }
  }

}
