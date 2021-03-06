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

package iht.controllers.application.declaration

import iht.connector.{CachingConnector, IhtConnector, IhtConnectors}
import iht.constants.IhtProperties
import iht.controllers.ControllerHelper
import iht.controllers.application.ApplicationController
import iht.forms.ApplicationForms
import iht.metrics.Metrics
import iht.models._
import iht.models.application.{ApplicationDetails, ProbateDetails}
import iht.models.enums.StatsSource
import iht.utils.CommonHelper._
import iht.utils.{CommonHelper, _}
import iht.viewmodels.application.DeclarationViewModel
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.{GatewayTimeoutException, HeaderCarrier}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by vineet on 01/12/16.
  */

object DeclarationController extends DeclarationController with IhtConnectors {
  lazy val metrics: Metrics = Metrics
}

trait DeclarationController extends ApplicationController {

  import play.api.mvc.Request

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  val metrics: Metrics

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { regDetails =>
          for {
            appDetails <- getApplicationDetails(
              getOrException(regDetails.ihtReference), regDetails.acknowledgmentReference)
          } yield {

            Ok(iht.views.html.application.declaration.declaration(
              DeclarationViewModel(ApplicationForms.declarationForm,
                appDetails,
                regDetails,
                CommonHelper.getNino(user),
                ihtConnector)))

          }
        }
      }
  }

  def onSubmit = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { rd =>
          if (rd.coExecutors.nonEmpty) {
            val boundForm = ApplicationForms.declarationForm.bindFromRequest
            boundForm.fold(
              formWithErrors => {
                LogHelper.logFormError(formWithErrors)
                for {
                  appDetails <- getApplicationDetails(
                    getOrException(rd.ihtReference), rd.acknowledgmentReference)
                } yield {
                  BadRequest(iht.views.html.application.declaration.declaration(
                    DeclarationViewModel(ApplicationForms.declarationForm,
                      appDetails,
                      rd,
                      CommonHelper.getNino(user),
                      ihtConnector)
                  ))
                }
              }, {
                case true =>
                  processApplicationOrRedirect
                case _ =>
                  Logger.warn("isDeclared is false. Redirecting to InternalServerError")
                  Future.successful(InternalServerError)
              }
            )
          } else {
            processApplicationOrRedirect
          }
        }
      }
  }

  private def processApplicationOrRedirect(implicit request: Request[_], hc: HeaderCarrier, user: AuthContext) = {
    withRegistrationDetails { rd =>
      val ihtReference = CommonHelper.getOrException(rd.ihtReference)
      ihtConnector.getCaseDetails(CommonHelper.getNino(user), ihtReference) flatMap { rd =>
        if (rd.status == ApplicationStatus.AwaitingReturn) {
          processApplication(CommonHelper.getNino(user))
        } else {
          Future.successful(Redirect(
            iht.controllers.home.routes.IhtHomeController.onPageLoad()))
        }
      }
    }
  }

  private def processApplication(nino: String)(implicit request: Request[_], hc: HeaderCarrier, user: AuthContext): Future[Result] = {
    val errorHandler: PartialFunction[Throwable, Result] = {
      case ex: Throwable => Ok(iht.views.html.application.application_error(submissionException(ex))(request, applicationMessages))
    }
    withRegistrationDetails { regDetails =>
      val ihtAppReference = regDetails.ihtReference
      val acknowledgement = regDetails.acknowledgmentReference

      val applicationDetails = Await.result(ihtConnector.getApplication(nino, ihtAppReference, acknowledgement),
        Duration.Inf)

      val ad1 = CommonHelper.getOrExceptionNoApplication(applicationDetails)

      fillMetricsData(ad1, regDetails)

      val updatedAppDetails: ApplicationDetails = ad1.copy(reasonForBeingBelowLimit = calculateReasonForBeingBelowLimit(ad1))

      ihtConnector.saveApplication(nino, updatedAppDetails, acknowledgement)
        .flatMap(optionSavedApplication => {
          if (optionSavedApplication.isEmpty) {
            Logger.debug("Unable to save application details: reasonForBeingBelowLimit not saved")
          }
          Logger.debug("Processing submission of application with IHT reference " + ihtAppReference + ":-\n" + updatedAppDetails.toString)
          ihtConnector.submitApplication(ihtAppReference, nino, updatedAppDetails)
        })
        .flatMap(returnId => {
          Logger.debug("Submission completed successfully with return id ::: " + returnId)
          Future(metrics.generalStatsCounter(StatsSource.COMPLETED_APP)).onFailure {
            case _ => Logger.info("Unable to write to StatsSource metrics repository")
          }
          Logger.info("Processing to get Probate details")
          getProbateDetails(nino, ihtAppReference, returnId
            .fold(throw new RuntimeException("Unable to submit application"))(identity).trim)
        })
        .flatMap {
          case Some(probateObject) =>
            Logger.info("Saving probate details in session")
            cachingConnector.storeProbateDetails(probateObject)
          case _ =>
            Logger.warn("Probate details could not be retrieved")
            Future.successful(None)
        }
        .map { _ =>
          Redirect(iht.controllers.application.declaration.routes.DeclarationReceivedController.onPageLoad())
        } recover errorHandler
    }
  }

  private def getProbateDetails(nino: String, ihtReference: String, ihtReturnId: String)
                               (implicit hc: HeaderCarrier): Future[Option[ProbateDetails]] = {

    ihtConnector.getProbateDetails(nino, ihtReference, ihtReturnId.trim) map {
      case Some(probateObject) =>
        Logger.info("Probate details received successfully")
        Some(probateObject)
      case _ =>
        Logger.warn("Problem occured while retrieving Probate details ")
        None
    } recover {
      case e: Exception =>
        Logger.warn(s"Problem getting probate details: ${e.getMessage}")
        None
    }
  }

  def submissionException(exception: Throwable): String = {
    exception match {
      case ex: GatewayTimeoutException => {
        Logger.debug("Request has been timed out while submitting application")
        ControllerHelper.errorServiceUnavailable
      }
      case ex: Exception => {
        if (ex.getMessage.contains("Request timed out") || ex.getMessage.contains("Connection refused")
          || ex.getMessage.contains("Service Unavailable") || ex.getMessage.contains(ControllerHelper.desErrorCode503)) {
          Logger.debug("Request has been timed out while submitting application")
          ControllerHelper.errorServiceUnavailable
        } else if (ex.getMessage.contains(ControllerHelper.desErrorCode502) || ex.getMessage.contains(ControllerHelper.desErrorCode504)) {
          Logger.debug("System error while submitting application")
          ControllerHelper.errorRequestTimeOut
        } else {
          Logger.debug("System error while submitting application")
          ControllerHelper.errorSystem
        }
      }
      case _ => {
        Logger.debug("System error while submitting application")
        ControllerHelper.errorSystem
      }
    }
  }

  /*
   * Calculate the reason for declaration
   */
  def calculateReasonForBeingBelowLimit(appDetails: ApplicationDetails): Option[String] = {

    val grossEstateValue = appDetails.totalValue
    val grossEstateValueMinusExemptionsAndLiabilities = if (appDetails.totalExemptionsValue > 0) {
      grossEstateValue - appDetails.totalExemptionsValue - appDetails.totalLiabilitiesValue
    } else {
      grossEstateValue
    }

    if (grossEstateValue <= IhtProperties.taxThreshold) {
      Some(ControllerHelper.ReasonForBeingBelowLimitExceptedEstate)
    } else if (grossEstateValueMinusExemptionsAndLiabilities <= IhtProperties.taxThreshold &&
      grossEstateValue <= IhtProperties.grossEstateLimit &&
      grossEstateValue > IhtProperties.taxThreshold) {
      Some(ControllerHelper.ReasonForBeingBelowLimitSpouseCivilPartnerOrCharityExemption)
    } else if (grossEstateValueMinusExemptionsAndLiabilities <= IhtProperties.transferredNilRateBand) {
      Some(ControllerHelper.ReasonForBeingBelowLimitTNRB)
    } else {
      None
    }
  }

  /**
    * Creates various metric data from ApplicationDetails object
    */
  private def fillMetricsData(appDetails: ApplicationDetails, regDetails: RegistrationDetails) = {
    val assetValue = appDetails.totalAssetsValue
    val giftValue = appDetails.totalGiftsValue
    val debtsValue = appDetails.totalLiabilitiesValue
    val exemptionsValue = appDetails.totalExemptionsValue
    val totalAssets = assetValue + giftValue

    //Getting stats for Application that has additional executors
    if (regDetails.coExecutors.size > 0) {
      Future(metrics.generalStatsCounter(StatsSource.ADDITIONAL_EXECUTOR_APP)).onFailure {
        case _ => Logger.info("Unable to write to StatsSource metrics repository")
      }
    }

    statsSource(appDetails, giftValue, debtsValue, exemptionsValue, totalAssets) foreach { stats =>
      Future(metrics.generalStatsCounter(stats)).onFailure {
        case _ => Logger.info("Unable to write to StatsSource metrics repository")
      }
    }
  }

  def statsSource(appDetails: ApplicationDetails,
                  giftValue: BigDecimal,
                  debtsValue: BigDecimal,
                  exemptionsValue: BigDecimal,
                  totalAssets: BigDecimal): Option[StatsSource.Value] = {
    val total = totalAssets + giftValue + debtsValue + exemptionsValue
    if (total == 0) {
      Some(StatsSource.NO_ASSETS_DEBTS_EXEMPTIONS_APP)
    } else if (totalAssets > 0 && debtsValue > 0 && exemptionsValue > 0 && appDetails.increaseIhtThreshold.isDefined) {
      Some(StatsSource.ASSET_DEBTS_EXEMPTIONS_TNRB_APP)
    } else if (totalAssets > 0 && debtsValue > 0 && exemptionsValue == 0) {
      Some(StatsSource.ASSETS_AND_DEBTS_ONLY_APP)
    } else if (totalAssets > 0) {
      Some(StatsSource.ASSETS_ONLY_APP)
    } else {
      None
    }
  }
}