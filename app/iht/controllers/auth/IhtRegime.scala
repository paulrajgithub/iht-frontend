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

package iht.controllers.auth

import uk.gov.hmrc.play.frontend.auth.connectors.domain.Accounts
import uk.gov.hmrc.play.frontend.auth.{AuthenticationProvider, TaxRegime}

/**
  * Created by yasar on 22/09/16.
  */
trait IhtRegime extends TaxRegime{
  override def isAuthorised(accounts: Accounts): Boolean = true
}

object IhtRegimeForRegistration extends IhtRegime{
  override def authenticationType: AuthenticationProvider = GovernmentGatewayProviderForRegistration
}

object IhtRegimeForApplication extends IhtRegime{
  override def authenticationType: AuthenticationProvider = GovernmentGatewayProviderForApplication
}