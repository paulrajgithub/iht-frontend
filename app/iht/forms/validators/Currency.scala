/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package iht.forms.validators

import play.api.data.FormError

/**
  * Created by grant on 14/12/16.
  */
trait Currency {
  protected lazy val moneyFormatSimple = """^(\d*+([.]\d{1,2})?)$""".r
  protected lazy val stringWithSpacesRegEx = "\\s+".r
  protected lazy val valueWithCommaFormatRegEx =
                              """(^(\d*)(\.\d{0,2})?$)|(^(\d{1,3},(\d{3},)*\d{3}(\.\d{1,2})?|\d{1,3}(\.\d{1,2})?)$)""".r
  protected lazy val isInvalidLength: String => Boolean = _.length > 10
  protected lazy val isInvalidPence: String => Boolean = s =>
    s.count(_ == '.') match {
      case numberDecimalPoints if numberDecimalPoints > 1 => true
      case numberDecimalPoints if numberDecimalPoints == 1 && s.substring(s.indexOf(".") + 1).length > 2 => true
      case _ => false
    }
  protected lazy val isInvalidNumericCharacters: String => Boolean = s => s.exists(c => c != '.' && !c.isDigit && c != ' ' && c!=',')
  protected lazy val isInvalidCommaPosition:String => Boolean = s => valueWithCommaFormatRegEx.findFirstMatchIn(s.trim).fold(true)(_ => false)
  protected lazy val hasSpaces: String => Boolean = s => stringWithSpacesRegEx.findFirstIn(s.trim).fold(false)(_ => true)

  protected def cleanMoneyString(moneyString: String) =
    moneyFormatSimple.findFirstIn(convertToValidCurrencyValue(moneyString.trim.replace(",", "").replace("-", ""))).getOrElse("")

  protected def validationErrors(key: String,
                                 value: String,
                                 errorLengthKey: String,
                                 errorInvalidCharsKey: String,
                                 errorInvalidPenceKey: String,
                                 errorInvalidSpacesKey: String,
                                 errorInvalidCommaPositionKey: String): Option[Seq[FormError]] = value match {
    case v if isInvalidLength(v) => Some(Seq(FormError(key, errorLengthKey)))
    case v if isInvalidNumericCharacters(v) => Some(Seq(FormError(key, errorInvalidCharsKey)))
    case v if isInvalidPence(v) => Some(Seq(FormError(key, errorInvalidPenceKey)))
    case v if hasSpaces(v) => Some(Seq(FormError(key, errorInvalidSpacesKey)))
    case v if isInvalidCommaPosition(v) => Some(Seq(FormError(key, errorInvalidCommaPositionKey)))
    case _ => None
  }

  /**
    * Convert the value like dd. to dd.00
    * @param moneyValue
    * @return
    */
  private def convertToValidCurrencyValue(moneyValue: String) = {
    val hasDecimal = moneyValue.contains('.')
    hasDecimal match {
      case true => {
        val index = moneyValue.indexOf(".")
        val stringAfterDecimal = moneyValue.substring(index + 1)
        if (stringAfterDecimal.isEmpty) {
          moneyValue.replace(".", ".00")
        } else {
          moneyValue
        }
      }
      case _ => moneyValue
    }

  }
}
