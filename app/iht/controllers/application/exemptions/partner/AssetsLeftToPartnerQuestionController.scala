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

package iht.controllers.application.exemptions.partner

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions._
import iht.utils.{ApplicationKickOutHelper, CommonHelper}
import iht.utils.CommonHelper._
import iht.views.html.application.exemption.partner.assets_left_to_partner_question
import play.api.Logger
import play.api.i18n.Messages
import play.api.mvc.{Call, Request, Result}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future
import iht.constants.IhtProperties._

object AssetsLeftToPartnerQuestionController extends AssetsLeftToPartnerQuestionController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait AssetsLeftToPartnerQuestionController extends EstateController {

  val partnerPermanentHomePage = routes.PartnerPermanentHomeQuestionController.onPageLoad()
  val exemptionsOverviewPage = addFragmentIdentifier(iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad(), Some(ExemptionsPartnerID))
  val partnerOverviewPage = addFragmentIdentifier(routes.PartnerOverviewController.onPageLoad(), Some(ExemptionsPartnerAssetsID))

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request =>

        withRegistrationDetails { registrationDetails =>
          for {
            applicationDetails <- ihtConnector.getApplication(CommonHelper.getNino(user),
              CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
              registrationDetails.acknowledgmentReference)
          } yield {
            applicationDetails match {
              case Some(appDetails) => {

                val filledForm = appDetails.allExemptions.flatMap(_.partner)
                  .fold(assetsLeftToSpouseQuestionForm)(assetsLeftToSpouseQuestionForm.fill)

                Ok(assets_left_to_partner_question(filledForm,
                  registrationDetails,
                  returnLabel(registrationDetails, appDetails),
                  returnUrl(registrationDetails, appDetails)
                ))
              }
              case _ => {
                Logger.warn("Application Details not found")
                InternalServerError("Application details not found")
              }
            }
          }
        }
  }


  def onSubmit = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { regDetails =>
          val boundForm = assetsLeftToSpouseQuestionForm.bindFromRequest

          val applicationDetailsFuture = ihtConnector.getApplication(CommonHelper.getNino(user),
            CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
            regDetails.acknowledgmentReference)

          applicationDetailsFuture.flatMap {
            case Some(appDetails) => {
              boundForm.fold(
                formWithErrors => {
                  Future.successful(BadRequest(assets_left_to_partner_question(formWithErrors,
                    regDetails,
                    returnLabel(regDetails, appDetails),
                    returnUrl(regDetails, appDetails))))
                },
                partnerExemption => {
                  saveApplication(CommonHelper.getNino(user), partnerExemption, regDetails, appDetails)
                }
              )
            }
            case None => {
              Logger.warn("Application Details not found")
              Future.successful(InternalServerError("Application details not found"))
            }
          }
        }
      }
  }

  def saveApplication(nino: String,
                      pe: PartnerExemption,
                      regDetails: RegistrationDetails,
                      appDetails: ApplicationDetails)(implicit request: Request[_],
                                                      hc: HeaderCarrier,
                                                      authContext: AuthContext): Future[Result] = {

    val updatedPartnerExemption = getUpdatedPartnerExemption(appDetails, pe)

    val applicationDetails = ApplicationKickOutHelper.updateKickout(checks = ApplicationKickOutHelper.checksEstate,
      prioritySection = applicationSection,
      registrationDetails = regDetails,
      applicationDetails = appDetails.copy(allExemptions = Some(appDetails.allExemptions.fold(new
          AllExemptions(partner = Some(updatedPartnerExemption)))(_.copy(partner = Some(updatedPartnerExemption))))))
    ihtConnector.saveApplication(nino, applicationDetails, regDetails.acknowledgmentReference).map(_ =>
      Redirect(applicationDetails.kickoutReason.fold(
        updatedPartnerExemption.isAssetForDeceasedPartner match {
          case Some(true) => {
            if (updatedPartnerExemption.isPartnerHomeInUK.isEmpty) partnerPermanentHomePage else partnerOverviewPage
          }
          case Some(false) => exemptionsOverviewPage
          case _ => throw new RuntimeException("Partner Exemption does not exist")
        }
      )(_ => kickoutRedirectLocation)))

  }

  /**
    * All PartnerExemption data will be wiped out if there is no asset left to partner
    *
    * @param appDetails
    * @param pe
    * @return
    */
  private def getUpdatedPartnerExemption(appDetails: ApplicationDetails,
                                         pe: PartnerExemption) = {

    val existingIsPartnerHomeInUK = appDetails.allExemptions.flatMap(_.partner.flatMap(_.isPartnerHomeInUK))
    val existingFirstName = appDetails.allExemptions.flatMap(_.partner.flatMap(_.firstName))
    val existingLastName = appDetails.allExemptions.flatMap(_.partner.flatMap(_.lastName))
    val existingDateOfBirth = appDetails.allExemptions.flatMap(_.partner.flatMap(_.dateOfBirth))
    val existingNino = appDetails.allExemptions.flatMap(_.partner.flatMap(_.nino))
    val existingTotalAssets = appDetails.allExemptions.flatMap(_.partner.flatMap(_.totalAssets))


    pe.isAssetForDeceasedPartner match {
      case Some(true) => pe.copy(isPartnerHomeInUK = existingIsPartnerHomeInUK,
        firstName = existingFirstName,
        lastName = existingLastName,
        dateOfBirth = existingDateOfBirth,
        nino = existingNino,
        totalAssets = existingTotalAssets)

      case Some(false) => pe.copy(isPartnerHomeInUK = None,
        firstName = None,
        lastName = None,
        dateOfBirth = None,
        nino = None,
        totalAssets = None)

      case _ => throw new RuntimeException("AssetsLeft to partner question has not been answered")
    }
  }

  private def returnLabel(regDetails: RegistrationDetails, appDetails: ApplicationDetails): String = {
    val deceasedName = regDetails.deceasedDetails.map(_.name)
    val partner = appDetails.allExemptions.flatMap(_.partner)
    partner match {
      case Some(x) => {
        if (x.isAssetForDeceasedPartner.isDefined && x.isPartnerHomeInUK.isDefined) {
          Messages("iht.estateReport.exemptions.partner.returnToAssetsLeftToSpouse")
        } else {
          Messages("page.iht.application.return.to.exemptionsOf", deceasedName.getOrElse(""))
        }
      }
      case None => {
        Messages("page.iht.application.return.to.exemptionsOf", deceasedName.getOrElse(""))
      }
    }
  }

  private def returnUrl(regDetails: RegistrationDetails, appDetails: ApplicationDetails): Call = {
    val partner = appDetails.allExemptions.flatMap(_.partner)
    partner match {
      case Some(x) => {
        if (x.isAssetForDeceasedPartner.isDefined && x.isPartnerHomeInUK.isDefined) {
          routes.PartnerOverviewController.onPageLoad()
        } else {
          exemptionsOverviewPage
        }
      }
      case None => exemptionsOverviewPage
    }
  }
}
