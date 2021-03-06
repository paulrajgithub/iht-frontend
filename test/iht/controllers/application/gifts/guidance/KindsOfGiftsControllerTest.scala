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

package iht.controllers.application.gifts.guidance

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.{CommonBuilder, TestHelper}
import org.mockito.Matchers._
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._


/**
 * Created by james on 21/01/16.
 */
class KindsOfGiftsControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def kindsOfGiftsController = new KindsOfGiftsController {
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def kindsOfGiftsControllerNotAuthorised = new KindsOfGiftsController {
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  private def setUpMocks() {
    val applicationDetails = CommonBuilder.buildApplicationDetails

    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetails),
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)

    createMockToGetSingleValueFromCache(mockCachingConnector,
      singleValueFormKey = same(TestHelper.lastQuestionUrl),
      singleValueReturn = Some("true"))
  }

  "KindsOfGiftsController" must {

    "redirect to ida login page on page load when user is not logged in" in {
      setUpMocks()

      val result = kindsOfGiftsControllerNotAuthorised.onPageLoad()(createFakeRequest())

      status(result) should be (SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on Page Load" in {
      setUpMocks()

      val result = kindsOfGiftsController.onPageLoad()(createFakeRequest())

      status(result) should be (OK)
    }

    "display page description 1" in {
      setUpMocks()

      val result = kindsOfGiftsController.onPageLoad()(createFakeRequest())

      status(result) should be (OK)
      contentAsString(result) should include (messagesApi("page.iht.application.gifts.guidance.kindOfGifts.description1"))
    }



    "display link for Gifts Given Away and Estate Overview as part of side bar links on the page" in {
      setUpMocks()

      val result = kindsOfGiftsController.onPageLoad()(createFakeRequest())

      contentAsString(result) should include (messagesApi("site.link.go.to.giftsGivenAwaySection"))
      contentAsString(result) should include (messagesApi("site.link.go.to.estateOverview"))
    }
  }
}
