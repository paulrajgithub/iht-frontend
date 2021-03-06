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

package iht.views.application

import iht.models.application.ApplicationDetails
import iht.testhelpers.CommonBuilder
import iht.utils.{ApplicationKickOutHelper, CommonHelper, KickOutReason}
import iht.views.ViewTestHelper
import iht.views.html.application.iht_kickout_application
import play.api.i18n.Messages.Implicits._

/**
  * Created by vineet on 15/11/16.
  */

//TODO  - Rest of the various Kickout reason tests will be written as part of New Acceptance Test Framework
class IhtKickoutApplicationViewTest extends ViewTestHelper{

  lazy val appDetails = CommonBuilder.buildApplicationDetails

  def ihtKickOutApplicationView(kickOutReason: String,
                                applicationDetails: ApplicationDetails,
                                applicationLastSection: Option[String],
                                applicationLastID: Option[String] = None,
                                summaryParameter1:Option[String]=None)  = {
    implicit val request = createFakeRequest()
    val view = iht_kickout_application(kickOutReason,
                                       applicationDetails,
                                       applicationLastSection,
                                       applicationLastID,
                                       summaryParameter1).toString()
    asDocument(view)
  }

  "IhtKickoutApplication View" must {

    "have the correct title and browser title" in {
      val view = ihtKickOutApplicationView("",
                                          appDetails,
                                          Some(ApplicationKickOutHelper.ApplicationSectionAssetsMoneyOwed)).toString
      titleShouldBeCorrect(view, messagesApi("iht.notPossibleToUseService"))
      browserTitleShouldBeCorrect(view, messagesApi("iht.notPossibleToUseService"))

    }

    "have 'Next steps' heading" in {
      val view = ihtKickOutApplicationView("",
                                          appDetails,
                                          Some(ApplicationKickOutHelper.ApplicationSectionAssetsMoneyOwed))

      val headers = view.getElementsByTag("h2")
      headers.size shouldBe 2
      headers.first.text() shouldBe messagesApi("iht.nextSteps")
    }

    "have details are correct button " in {
      val view = ihtKickOutApplicationView("",
        appDetails,
        Some(ApplicationKickOutHelper.ApplicationSectionAssetsMoneyOwed))

      val detailsAreCorrectButton = view.getElementById("finish")
      detailsAreCorrectButton.attr("value") shouldBe messagesApi("site.button.details.correct")
    }
  }

  "Assets KickOuts " must {
    "have correct text and return link for Kickout Reason more than a Million" in {

      val view = ihtKickOutApplicationView(KickOutReason.AssetsTotalValueMoreThanMax,
        appDetails,
        Some(ApplicationKickOutHelper.ApplicationSectionAssetsMoneyOwed))

      messagesShouldBePresent(view.toString, messagesApi("page.iht.application.assets.kickout.assetsTotalValueMoreThanMax.nextSteps1"))
      messagesShouldBePresent(view.toString, messagesApi("iht.estateReport.assets.kickout.MoreThan1Million"))
      messagesShouldBePresent(view.toString, messagesApi("iht.estateReport.kickout.returnToEstateOverview"))

      val returnLink = view.getElementById("back-button")
      returnLink.text shouldBe messagesApi("iht.estateReport.kickout.returnToEstateOverview.linkText")
      returnLink.attr("href") shouldBe
        iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef("").url

    }

  "have correct text and return link for Kickout Reason PartnerDiedBeforeMinDate" in {
    val view = ihtKickOutApplicationView(KickOutReason.PartnerDiedBeforeMinDate,
      appDetails,
      Some(ApplicationKickOutHelper.ApplicationSectionGiftsWithReservation))

    messagesShouldBePresent(view.toString,messagesApi("page.iht.application.tnrb.kickout.estateMoreThanThreshold.summary"))
    messagesShouldBePresent(view.toString, messagesApi("iht.estateReport.kickout.nextSteps"))
    messagesShouldBePresent(view.toString, messagesApi("iht.estateReport.kickout.returnToEstateOverview"))
    messagesShouldBePresent(view.toString, messagesApi("site.threshold.value.display"))
    messagesShouldBePresent(view.toString, messagesApi("iht.estateReport.ihtThreshold"))
    messagesShouldBePresent(view.toString, CommonHelper.escapePound(messagesApi("page.iht.application.overview.value")))

    val returnLink = view.getElementById("back-button")
    returnLink.text shouldBe messagesApi("iht.estateReport.kickout.returnToEstateOverview.linkText")
    returnLink.attr("href") shouldBe
      iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef("").url

  }
}

}
