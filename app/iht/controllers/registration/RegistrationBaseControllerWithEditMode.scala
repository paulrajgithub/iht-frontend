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

import java.util.UUID

import iht.constants.Constants
import iht.controllers.ControllerHelper.Mode
import iht.utils.{CommonHelper, RegistrationKickOutHelper}
import iht.utils.RegistrationKickOutHelper._
import play.api.data.Form
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.SessionKeys

import scala.concurrent.Future

trait RegistrationBaseControllerWithEditMode[T] extends RegistrationBaseController[T] {

  def okForEditPageLoad(form: Form[T])(implicit request: Request[AnyContent]): Result

  def badRequestForEditSubmit(form: Form[T])(implicit request: Request[AnyContent]): Result

  def onEditPageLoad = pageLoad(Mode.Edit)

  def onEditSubmit = submit(Mode.Edit)

  override def pageLoad(mode: Mode.Value) = authorisedForIht {
    implicit user => implicit request =>
      withRegistrationDetailsRedirectOnGuardCondition { rd =>
        val f = fillForm(rd)
        val okResult: Result = if (mode == Mode.Standard) {
          okForPageLoad(f)
        } else {
          okForEditPageLoad(f)
        }
        val result = okResult.withSession(CommonHelper.ensureSessionHasNino(request.session, user))
        Future.successful(result)
      }
  }

  override def submit(mode: Mode.Value) =
    authorisedForIht {
      implicit user => implicit request => {
        withRegistrationDetailsRedirectOnGuardCondition { rd =>
          val boundForm = performAdditionalValidation(form.bindFromRequest, rd, mode)

          boundForm.fold(
            formWithErrors => {
              if (mode == Mode.Standard) {
                Future.successful(badRequestForSubmit(formWithErrors))
              } else {
                Future.successful(badRequestForEditSubmit(formWithErrors))
              }
            },
            details => {
              val copyOfRd = applyChangesToRegistrationDetails(rd, details, mode)

              val route =
                if(mode == Mode.Standard) {
                  onwardRoute(copyOfRd)
                } else {
                  onwardRouteInEditMode(copyOfRd)
                }

              storeAndRedirectWithKickoutCheck(cachingConnector, copyOfRd, getKickoutReason, route, storageFailureMessage, mode)
            }
          )
        }
      }
    }
}
