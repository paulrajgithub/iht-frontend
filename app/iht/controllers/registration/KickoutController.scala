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

package iht.controllers.registration

import iht.connector.CachingConnector
import iht.constants.IhtProperties
import iht.controllers.ControllerHelper.Mode
import iht.controllers.IhtConnectors
import iht.metrics.Metrics
import iht.models.enums.KickOutSource
import iht.utils.CommonHelper
import iht.utils.RegistrationKickOutHelper._
import iht.views.html.registration.kickout._
import play.api.i18n.Messages
import play.api.mvc.Request
import play.twirl.api.HtmlFormat

import scala.concurrent.Future

object KickoutController extends KickoutController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait KickoutController extends RegistrationController {
  def cachingConnector: CachingConnector

  override def guardConditions: Set[Predicate] = Set.empty

  def metrics: Metrics

  lazy val probateLocationPageLoad = iht.controllers.registration.applicant.routes.ProbateLocationController.onPageLoad()
  lazy val probateLocationEditPageLoad = iht.controllers.registration.applicant.routes.ProbateLocationController.onEditPageLoad()

  lazy val deceasedPermanentHomePageLoad = iht.controllers.registration.deceased.routes.DeceasedPermanentHomeController.onPageLoad()
  lazy val deceasedPermanentHomeEditPageLoad = iht.controllers.registration.deceased.routes.DeceasedPermanentHomeController.onEditPageLoad()

  lazy val deceasedDateOfDeathPageLoad= iht.controllers.registration.deceased.routes.DeceasedDateOfDeathController.onPageLoad()
  lazy val deceasedDateOfDeathEditPageLoad= iht.controllers.registration.deceased.routes.DeceasedDateOfDeathController.onEditPageLoad()

  lazy val applyingForProbatePageLoad = iht.controllers.registration.applicant.routes.ApplyingForProbateController.onPageLoad()
  lazy val applyingForProbateEditPageLoad = iht.controllers.registration.applicant.routes.ApplyingForProbateController.onEditPageLoad()


  private def probateKickoutView(contentLines: Seq[String], mode: Mode.Value = Mode.Standard)(request: Request[_]) =
    kickout_template(Messages("page.iht.registration.applicantDetails.kickout.probate.summary"),
      if (mode == Mode.Edit) probateLocationEditPageLoad else probateLocationPageLoad )(contentLines)(request)

  private def locationKickoutView(contentLines: Seq[String], mode: Mode.Value = Mode.Standard)(request: Request[_]) =
    kickout_template(Messages("page.iht.registration.deceasedDetails.kickout.location.summary"),
      if (mode == Mode.Edit) deceasedPermanentHomeEditPageLoad else  deceasedPermanentHomePageLoad )(contentLines)(request)

  private def capitalTaxKickoutView(contentLines: Seq[String], mode: Mode.Value = Mode.Standard)(request: Request[_]) =
    kickout_template(Messages("page.iht.registration.deceasedDateOfDeath.kickout.date.capital.tax.summary"),
      if (mode == Mode.Edit) deceasedDateOfDeathEditPageLoad else deceasedDateOfDeathPageLoad,
      Messages("iht.registration.kickout.returnToTheDateOfDeath"))(contentLines)(request)

  private def dateOtherKickoutView(contentLines: Seq[String], mode: Mode.Value = Mode.Standard)(request: Request[_]) =
    kickout_template(Messages("page.iht.registration.deceasedDateOfDeath.kickout.date.other.summary"),
      if (mode == Mode.Edit) deceasedDateOfDeathEditPageLoad else deceasedDateOfDeathPageLoad)(contentLines)(request)

  private def notApplyingForProbateKickoutView(contentLines: Seq[String], mode: Mode.Value = Mode.Standard)(request: Request[_]) =
    kickout_template(Messages("page.iht.registration.notApplyingForProbate.kickout.summary"),
      if (mode == Mode.Edit) applyingForProbateEditPageLoad else applyingForProbatePageLoad)(contentLines)(request)

  private def getContent(mode: Mode.Value = Mode.Standard):Map[String, Request[_] => HtmlFormat.Appendable] = {
    if(mode == Mode.Edit) {
      contentInEditMode
    } else {
      content
    }
  }


  lazy val content: Map[String, Request[_] => HtmlFormat.Appendable] = Map(
    KickoutApplicantDetailsProbateScotland ->
      (request => probateKickoutView(
        Seq(
          Messages("iht.registration.kickout.probateLocation.scotland"),
          Messages("iht.registration.kickout.ifWantChangeProbateLocation")))(request)),
    KickoutApplicantDetailsProbateNi ->
      (request => probateKickoutView(
        Seq(
          Messages("iht.registration.kickout.content"),
          Messages("iht.registration.kickout.ifWantChangeProbateLocation")))(request)),
    KickoutDeceasedDetailsLocationScotland ->
      (request => locationKickoutView(
        Seq(
          Messages("iht.registration.kickout.probateLocation.scotland"),
          Messages("iht.registration.kickout.ifWantChangeDeceasedLocation")))(request)),
    KickoutDeceasedDetailsLocationNI ->
      (request => locationKickoutView(
        Seq(
          Messages("iht.registration.kickout.content"),
          Messages("iht.registration.kickout.ifWantChangeDeceasedLocation")))(request)),
    KickoutDeceasedDetailsLocationOther ->
      (request => locationKickoutView(
        Seq(
          Messages("iht.registration.kickout.content"),
          Messages("iht.registration.kickout.ifWantChangeDeceasedLocation")))(request)),
    KickoutDeceasedDateOfDeathDateCapitalTax ->
      (request => capitalTaxKickoutView(Seq(
        Messages("iht.registration.kickout.message.phone"),
        Messages("iht.registration.kickout.message.phone2"),
        Messages("iht.registration.kickout.message.changeTheDate")
      ))(request)),
    KickoutDeceasedDateOfDeathDateOther ->
      (request => dateOtherKickoutView(Seq(
        Messages("iht.registration.kickout.content"),
        Messages("iht.registration.kickout.message.form2")
      ))(request)),
    KickoutNotApplyingForProbate ->
      (request => notApplyingForProbateKickoutView(Seq(
        Messages("page.iht.registration.notApplyingForProbate.kickout.p1"),
        Messages("iht.ifYouWantToChangeYourAnswer")
      ))(request)))


  lazy val contentInEditMode: Map[String, Request[_] => HtmlFormat.Appendable] = Map(
    KickoutApplicantDetailsProbateScotland ->
      (request => probateKickoutView(
        Seq(
          Messages("iht.registration.kickout.probateLocation.scotland"),
          Messages("iht.registration.kickout.ifWantChangeProbateLocation")), Mode.Edit)(request)),
    KickoutApplicantDetailsProbateNi ->
      (request => probateKickoutView(
        Seq(
          Messages("iht.registration.kickout.content"),
          Messages("iht.registration.kickout.ifWantChangeProbateLocation")), Mode.Edit)(request)),
    KickoutDeceasedDetailsLocationScotland ->
      (request => locationKickoutView(
        Seq(
          Messages("iht.registration.kickout.probateLocation.scotland"),
          Messages("iht.registration.kickout.ifWantChangeDeceasedLocation")), Mode.Edit)(request)),
    KickoutDeceasedDetailsLocationNI ->
      (request => locationKickoutView(
        Seq(
          Messages("iht.registration.kickout.content"),
          Messages("iht.registration.kickout.ifWantChangeDeceasedLocation")), Mode.Edit)(request)),
    KickoutDeceasedDetailsLocationOther ->
      (request => locationKickoutView(
        Seq(
          Messages("iht.registration.kickout.content"),
          Messages("iht.registration.kickout.ifWantChangeDeceasedLocation")), Mode.Edit)(request)),
    KickoutDeceasedDateOfDeathDateCapitalTax ->
      (request => capitalTaxKickoutView(Seq(
        Messages("iht.registration.kickout.message.phone"),
        Messages("iht.registration.kickout.message.phone2"),
        Messages("iht.registration.kickout.message.changeTheDate")
      ), Mode.Edit)(request)),
    KickoutDeceasedDateOfDeathDateOther ->
      (request => dateOtherKickoutView(Seq(
        Messages("iht.registration.kickout.content"),
        Messages("iht.registration.kickout.message.form2")
      ))(request)),
    KickoutNotApplyingForProbate ->
      (request => notApplyingForProbateKickoutView(Seq(
        Messages("page.iht.registration.notApplyingForProbate.kickout.p1"),
        Messages("iht.ifYouWantToChangeYourAnswer")
      ), Mode.Edit)(request)))

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      cachingConnector.getSingleValue(RegistrationKickoutReasonCachingKey) map { reason =>

        val content = getContent()
        Ok(content(CommonHelper.getOrException(reason))(request))
      }
    }
  }

  def onEditPageLoad = authorisedForIht {
    implicit user => implicit request => {
      cachingConnector.getSingleValue(RegistrationKickoutReasonCachingKey) map { reason =>

        val content = getContent(Mode.Edit)
        Ok(content(CommonHelper.getOrException(reason))(request))
      }
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request =>
      metrics.kickOutCounter(KickOutSource.REGISTRATION)
      Future.successful(Redirect(IhtProperties.linkRegistrationKickOut))
  }
}
