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

package iht.views.application.assets.properties

import iht.models.UkAddress
import iht.models.application.assets.Properties
import iht.testhelpers.CommonBuilder
import iht.views.html.application.asset.properties.properties_overview
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

class PropertiesOverviewViewTest extends GenericNonSubmittablePageBehaviour {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidanceParagraphs = Set(
    Messages("page.iht.application.assets.deceased-permanent-home.description.p1", deceasedName),
    Messages("page.iht.application.assets.deceased-permanent-home.description.p2", deceasedName)
  )

  override def pageTitle = Messages("page.iht.application.assets.deceased-permanent-home.sectionTitle")

  override def browserTitle = Messages("iht.estateReport.assets.propertiesBuildingsAndLand")

  override def exitComponent = Some(
    ExitComponent(
      iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
      Messages("site.link.return.assets")
    )
  )

  override def view =
    properties_overview(List(CommonBuilder.property, CommonBuilder.property2),
      Some(Properties(isOwned = Some(true))),
      registrationDetails).toString()

  "Properties overview view" must {
    behave like nonSubmittablePage()

    behave like link("add-property",
      iht.controllers.application.assets.properties.routes.PropertyDetailsOverviewController.onPageLoad().url,
      Messages("iht.estateReport.assets.propertyAdd"))

    "show ownership question" in {
      elementShouldHaveText(doc, "home-in-uk-question", Messages("page.iht.application.assets.properties.question.question", deceasedName))
    }

    "show ownership question value" in {
      elementShouldHaveText(doc, "home-in-uk-value", Messages("iht.yes"))
    }

    behave like link("home-in-uk-link",
      iht.controllers.application.assets.properties.routes.PropertiesOwnedQuestionController.onPageLoad().url,
      Messages("iht.change"))

    def addressForDisplayDeleteAndModify(rowNo: Int, expectedUkAddress: UkAddress) = {
      def addressRow(colNo: Int, rowNo: Int) = {
        val propertiesUl = doc.getElementById("properties")
        val listItems = propertiesUl.getElementsByTag("li")
        listItems.get(rowNo).getElementsByTag("div").get(colNo)
      }
      s"show address number ${rowNo + 1}" in {
        addressRow(0, rowNo).ownText shouldBe formatAddressForDisplay(expectedUkAddress)
      }

      s"show address number ${rowNo + 1} delete link" in {
        val deleteDiv = addressRow(3, rowNo)
        val anchor = deleteDiv.getElementsByTag("a").first
        getAnchorVisibleText(anchor) shouldBe Messages("iht.delete")
      }

      s"show address number ${rowNo + 1} give details link" in {
        val deleteDiv = addressRow(4, rowNo)
        val anchor = deleteDiv.getElementsByTag("a").first
        getAnchorVisibleText(anchor) shouldBe Messages("iht.change")
      }
    }

    behave like addressForDisplayDeleteAndModify(0, CommonBuilder.DefaultUkAddress)

    behave like addressForDisplayDeleteAndModify(1, CommonBuilder.DefaultUkAddress2)
  }
}
