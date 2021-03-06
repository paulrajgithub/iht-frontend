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

package iht.views.application.assets.insurancePolicy

import iht.controllers.application.assets.insurancePolicy.routes
import iht.testhelpers.CommonBuilder
import iht.views.application.{ApplicationPageBehaviour, CancelComponent, Guidance}
import play.api.mvc.Call
import iht.views.html.application.asset.insurancePolicy.insurance_policy_details_final_guidance
import play.api.i18n.Messages.Implicits.applicationMessages
import iht.testhelpers.TestHelper._

class InsurancePolicyDetailsGuidanceViewTest extends ApplicationPageBehaviour {

  val giftsLocation = CommonBuilder.DefaultCall1
  val deceasedName:String = "Deceased"

  override def pageTitle = messagesApi("iht.estateReport.assets.insurancePolicies.premiumsPaidByOther", deceasedName)

  override def browserTitle = messagesApi("iht.estateReport.assets.insurancePolicies.premiumsPaidByOther", messagesApi("iht.the.deceased"))

  override def view:String = insurance_policy_details_final_guidance(Some(giftsLocation), deceasedName)(
    createFakeRequest(isAuthorised = false), applicationMessages).toString()

  override def guidance: Guidance = guidance(Set(messagesApi("page.iht.application.insurance.policies.section7.guidance", deceasedName),
    messagesApi("page.iht.application.insurance.policies.section7.guidance2", deceasedName)))

  override def formTarget: Option[Call] = None

  override def cancelComponent: Option[CancelComponent] = Some(CancelComponent(
    routes.InsurancePolicyOverviewController.onPageLoad(),
    messagesApi("site.link.return.insurance.policies"),
    InsurancePlacedInTrustYesNoID
  ))

  "InsurancePolicyDetailsGuidanceView" must {

    behave like applicationPage

    "show the return link with to the gifts" in {
      val giftsButton = doc.getElementById("return-button-gifts")
      giftsButton.attr("href") shouldBe giftsLocation.url
      giftsButton.text() shouldBe messagesApi("site.link.go.to.gifts")
    }
  }
}
