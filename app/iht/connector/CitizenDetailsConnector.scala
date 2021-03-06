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

package iht.connector

import iht.config.WSHttp
import iht.models.CidPerson
import play.api.Logger
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CitizenDetailsConnector {
  def http: HttpGet with HttpPost with HttpPut with HttpDelete

  def serviceUrl: String

  def getCitizenDetails(nino: Nino)(implicit hc: HeaderCarrier): Future[CidPerson]
}

object CitizenDetailsConnector extends CitizenDetailsConnector with ServicesConfig {
  override def http = WSHttp

  lazy val serviceUrl = baseUrl("citizen-details")

  def getCitizenDetails(nino: Nino)(implicit hc: HeaderCarrier): Future[CidPerson] = {
    Logger.info("Calling Citizen Details service to retrieve personal details")
    http.GET[CidPerson](s"$serviceUrl/citizen-details/nino/$nino").recover{
      case throwable: Throwable => {
        Logger.warn(s"Error calling Citizen Details service to retrieve personal details")
        throw new RuntimeException("Person details could not be retrieved!")
      }
    }
  }
}
