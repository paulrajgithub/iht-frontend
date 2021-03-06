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

package iht.controllers.application.exemptions

import iht.connector.{CachingConnector, IhtConnector}
import iht.connector.IhtConnectors
import iht.controllers.application.ApplicationController
import iht.utils.ExemptionsGuidanceHelper
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import scala.concurrent.Future

/**
 * Created by jon on 21/07/15.
 */
object ExemptionsGuidanceIncreasingThresholdController extends ExemptionsGuidanceIncreasingThresholdController with IhtConnectors

trait ExemptionsGuidanceIncreasingThresholdController extends ApplicationController {

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoad(ihtReference: String) = authorisedForIht {
    implicit user => implicit request => {
      Future.successful(Ok(iht.views.html.application.exemption.exemptions_guidance_increasing_threshold(ihtReference)))
    }
  }

  def onSubmit(ihtReference: String) = authorisedForIht {
    implicit user => implicit request => {
      ExemptionsGuidanceHelper.finalDestination(ihtReference, cachingConnector)
        .map( finalDestination => Redirect(finalDestination))
    }
  }
}
