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

package iht.controllers.application.assets.stocksAndShares

/**
  * Created by vineet on 04/07/16.  */

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._
import play.api.test.Helpers.{contentAsString, _}
import iht.utils.CommonHelper
import iht.testhelpers.TestHelper._

class StocksAndSharesListedControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]
  lazy val submitUrl = CommonHelper.addFragmentIdentifierToUrl(routes.StocksAndSharesOverviewController.onPageLoad().url, AssetsStocksListedID)


  def setUpTests(applicationDetails: ApplicationDetails) = {
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetails),
      getAppDetails = true,
      saveAppDetails = true,
      storeAppDetailsInCache = true)
  }

  def stocksAndSharesListedController = new StocksAndSharesListedController {
    val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def stocksAndSharesListedControllerNotAuthorised = new StocksAndSharesListedController {
    val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  before {
    mockIhtConnector = mock[IhtConnector]
  }

  "StocksAndSharesListedController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = stocksAndSharesListedControllerNotAuthorised.onPageLoad(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = stocksAndSharesListedControllerNotAuthorised.onSubmit(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      setUpTests(applicationDetails)
      val result = stocksAndSharesListedController.onPageLoad(createFakeRequest())
      status(result) should be(OK)
    }

    "save application and go to stocksAndShares overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      val formFill = stockAndShareListedForm.fill(CommonBuilder.buildStockAndShare.copy(isListed = Some(true),
        valueListed = Some(200)))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)
      setUpTests(applicationDetails)

      val result = stocksAndSharesListedController.onSubmit()(request)
      status(result) should be (SEE_OTHER)
      redirectLocation(result) should be (Some(submitUrl))
    }

    "wipe out the sharesListed value if user selects No, save application and " +
      "go to stocksAndShares overview page on submit" in {

      val sharesListed = CommonBuilder.buildStockAndShare.copy(isListed = Some(false), valueListed = Some(200))

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
                                  allAssets = Some(CommonBuilder.buildAllAssets.copy(
                                 stockAndShare = Some(sharesListed))))

      val formFill = stockAndShareListedForm.fill(sharesListed)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)
      setUpTests(applicationDetails)

      val result = stocksAndSharesListedController.onSubmit()(request)
      status(result) should be (SEE_OTHER)
      redirectLocation(result) should be (Some(submitUrl))

      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      val expectedAppDetails = applicationDetails.copy(allAssets = applicationDetails.allAssets.map(_.copy(
        stockAndShare = Some(CommonBuilder.buildStockAndShare.copy(valueListed = None, isListed = Some(false))))))

      capturedValue shouldBe expectedAppDetails
    }

    "display validation message when form is submitted with no values entered" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      implicit val request = createFakeRequest()

      setUpTests(applicationDetails)

      val result = stocksAndSharesListedController.onSubmit()(request)
      status(result) should be (BAD_REQUEST)
      contentAsString(result) should include (messagesApi("error.problem"))
    }

    "redirect to overview when form is submitted with answer yes and a value entered" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails
      implicit val request = createFakeRequest().withFormUrlEncodedBody(("isListed", "true"), ("valueListed", "233"))

      setUpTests(applicationDetails)

      val result = stocksAndSharesListedController.onSubmit()(request)
      status(result) should be (SEE_OTHER)
      redirectLocation(result) should be (Some(submitUrl))
    }

    "respond with bad request when incorrect value are entered on the page" in {
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = stocksAndSharesListedController.onSubmit (fakePostRequest)
      status(result) shouldBe (BAD_REQUEST)
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      stocksAndSharesListedController.onPageLoad(createFakeRequest()))
  }

}
