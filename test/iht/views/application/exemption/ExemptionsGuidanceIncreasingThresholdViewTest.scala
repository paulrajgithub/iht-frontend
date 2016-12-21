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

package iht.views.application.exemption

import iht.views.HtmlSpec
import iht.views.html.application.exemption.exemptions_guidance_increasing_threshold
import iht.{FakeIhtApp, TestUtils}
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec

class ExemptionsGuidanceIncreasingThresholdViewTest extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with HtmlSpec with BeforeAndAfter {


  "exemptions guidance increasing threshold page" must {

    "show the correct title" in {
      implicit val request = createFakeRequest()
      val view = exemptions_guidance_increasing_threshold("ihtReference").toString
      val doc = asDocument(view)
      val headers: Elements = doc.getElementsByTag("h1")
      headers.size() shouldBe 1
      headers.first().text() shouldBe Messages("page.iht.application.exemptions.guidance.increasing.threshold.title")
    }

    "show the correct browser title" in {
      implicit val request = createFakeRequest()
      val view = exemptions_guidance_increasing_threshold("ihtReference").toString
      val doc = asDocument(view)
      assertEqualsValue(doc, "title",
        Messages("page.iht.application.exemptions.guidance.increasing.threshold.title") + " " + Messages("site.title.govuk"))
    }

    "show the correct paragraphs" in {
      implicit val request = createFakeRequest()
      val view = exemptions_guidance_increasing_threshold("ihtReference").toString
      view should include(Messages("page.iht.application.exemptions.guidance.increasing.threshold.p1"))
      view should include(Messages("page.iht.application.exemptions.guidance.increasing.threshold.p2"))
      view should include(Messages("iht.estateReport.exemptions.guidance.provideAssetsDetails"))
    }

    "show the correct indent paragraph" in {
      implicit val request = createFakeRequest()
      val view = exemptions_guidance_increasing_threshold("ihtReference").toString
      val doc = asDocument(view)
      assertContainsMessage(doc, ".panel-indent", "iht.estateReport.exemptions.guidance.debtsSubtracted")
    }

    "show a url linked to exemptions overview page" in {
      implicit val request = createFakeRequest()
      val view = exemptions_guidance_increasing_threshold("ihtReference").toString
      val doc = asDocument(view)

      assertRenderedById(doc, "exemptions-link")
      val link = doc.getElementById("exemptions-link")
      link.text shouldBe Messages("page.iht.application.exemptions.guidance.increasing.threshold.link.text")
      link.attr("href") shouldBe iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad().url
    }

    "show button with Continue as the title" in {
      implicit val request = createFakeRequest()
      val view = exemptions_guidance_increasing_threshold("ihtReference").toString
      val doc = asDocument(view)
      val button: Element = doc.getElementById("continue")
      button.text() shouldBe Messages("iht.continue")
    }
  }
}