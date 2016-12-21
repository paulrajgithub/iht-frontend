/*
 * Copyright 2016 HM Revenue & Customs
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

package iht.models.application

import org.joda.time.LocalDate
import play.api.libs.json.Json

/**
 *
 * Created by Vineet Tyagi on 19/06/15.
 *
 */
case class IhtApplication(ihtRefNo: String,
                          firstName:String,
                          lastName:String,
                          dateOfBirth: LocalDate,
                          dateOfDeath: LocalDate,
                          nino: String,
                          entryType: String,
                          role:String,
                          registrationDate: LocalDate,
                          currentStatus: String,
                          acknowledgmentReference:String)

object IhtApplication {
  implicit val formats = Json.format[IhtApplication]
}
