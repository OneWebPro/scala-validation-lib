package pl.onewebpro.validation.core.data

import pl.onewebpro.validation.core.entity.ValidationMap
import pl.onewebpro.validation.core.{FieldName, Validation}

/**
  * Represents source of data like json, xml file etc.
  *
  * @tparam T Type of source, like json or xml
  */
class Source[T](source: T) {
  /**
    * Extracts data from source by name, for example we want to get string
    *
    * @return
    */
  def extract[A](fieldName: FieldName)(implicit extractor: Extractor[T, A]): Validation[A] =
    extractor(fieldName, source)

  /**
    * Validate value using validation map
    *
    * @return
    */
  def validate[A](map: ValidationMap[A])(implicit extractor: Extractor[T, A]): Validation[A] =
    map.validate[T](this)
}
