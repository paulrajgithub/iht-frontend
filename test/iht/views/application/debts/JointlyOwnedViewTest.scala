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

package iht.views.application.debts

import iht.forms.ApplicationForms._
import iht.models.application.debts.BasicEstateElementLiabilities
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.utils.CommonHelper
import play.api.i18n.Messages.Implicits._
import iht.views.html.application.debts.jointly_owned
import iht.views.html.application.debts.{funeral_expenses, jointly_owned}
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable
import iht.controllers.application.debts.routes
import iht.constants.Constants._
import iht.constants.IhtProperties._

/**
  * Created by vineet on 15/11/16.
  */

class JointlyOwnedViewTest extends DebtsElementViewBehaviour[BasicEstateElementLiabilities]{

  val ihtReference = Some("ABC1A1A1A")
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
                                                      deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
                                                               maritalStatus = Some(TestHelper.MaritalStatusMarried))),
                                                      deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))

  override def form:Form[BasicEstateElementLiabilities] = jointlyOwnedDebts
  override def formToView:Form[BasicEstateElementLiabilities] => Appendable = form => jointly_owned(form, regDetails)

  override def pageTitle = messagesApi("iht.estateReport.debts.owedOnJointAssets")
  override def browserTitle = messagesApi("page.iht.application.debts.jointlyOwned.browserTitle")

  override def guidance  = guidance(
    Set(messagesApi("page.iht.application.debts.jointlyOwned.description.p1",
      CommonHelper.getDeceasedNameOrDefaultString(regDetails)))
  )

  override def yesNoQuestionText = messagesApi("page.iht.application.debts.jointlyOwned.isOwned")
  override def inputValueFieldLabel = messagesApi("iht.estateReport.debts.owedOnJointAssets.value")
  override def inputValueFieldHintText = messagesApi("page.iht.application.debts.jointlyOwned.description.p2")
  override def linkHash = TestHelper.DebtsOwedJointlyID

  override def formTarget = Some(routes.JointlyOwnedDebtsController.onSubmit)

  "JointlyOwnedView" must {
    behave like debtsElement
  }
}
