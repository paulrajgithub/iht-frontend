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

package iht.controllers.application.debts

import iht.connector.{CachingConnector, IhtConnector}
import iht.connector.IhtConnectors
import iht.controllers.application.ApplicationController
import iht.models.application.debts.AllLiabilities
import iht.utils._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

/**
 * Created by jamestuttle on 20/10/15.
 */
object DebtsOverviewController extends DebtsOverviewController with IhtConnectors

trait DebtsOverviewController extends ApplicationController {

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      withApplicationDetails { rd => ad =>
        val debts = ad.allLiabilities.fold(new AllLiabilities())(l => l)
        Future.successful(Ok(iht.views.html.application.debts.debts_overview(ad, debts, rd)))
      }
    }
  }
}
