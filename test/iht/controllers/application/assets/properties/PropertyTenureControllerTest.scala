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

package iht.controllers.application.assets.properties

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.{CommonBuilder, TestHelper}
import play.api.i18n.Messages
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

/**
 * Created by Vineet on 22/06/16.
 */
class PropertyTenureControllerTest extends ApplicationControllerTest {
  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def propertyTenureController = new PropertyTenureController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
  }

  def propertyTenureControllerNotAuthorised = new PropertyTenureController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
  }

  implicit val headerCarrier = FakeHeaders()
  implicit val hc = new HeaderCarrier

  "PropertyTenure controller" must {

    "redirect to ida login page on PageLoad if the user is not logged in" in {
      val result = propertyTenureControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to ida login page on Submit if the user is not logged in" in {
      val result = propertyTenureControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond ok on page load" in {
      val result = propertyTenureController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
    }

    "display the correct title on page" in {
      val result = propertyTenureController.onPageLoad()(createFakeRequest())
      status(result) should be (OK)
      contentAsString(result) should include (Messages("iht.estateReport.assets.properties.freeholdOrLeasehold"))
    }

    "display the correct title on page in edit mode" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.property))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = propertyTenureController.onEditPageLoad("1")(createFakeRequest())
      status(result) should be (OK)
      contentAsString(result) should include (Messages("iht.estateReport.assets.properties.freeholdOrLeasehold"))
    }

    "redirect to PropertyDetails overview page on submit" in {
      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List())

      val formFill = propertyTenureForm.fill(CommonBuilder.buildProperty.copy(tenure = TestHelper.TenureFreehold))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = propertyTenureController.onSubmit()(request)

      status(result) should be (SEE_OTHER)
      redirectLocation(result) should be (Some(routes.PropertyDetailsOverviewController.onEditPageLoad("1").url))
    }

    "redirect to PropertyDetails overview page on submit in edit mode" in {
      val propertyId = "1"

      val applicationDetails = iht.testhelpers.CommonBuilder.buildApplicationDetails.
        copy(propertyList = List(CommonBuilder.buildProperty.copy(id = Some(propertyId),
          tenure = TestHelper.TenureFreehold,
          value = Some(1234))))

      val formFill = propertyTenureForm.fill(CommonBuilder.buildProperty.copy(tenure = TestHelper.TenureFreehold))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(formFill.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = propertyTenureController.onEditSubmit(propertyId)(request)

      status(result) should be (SEE_OTHER)
      redirectLocation(result) should be (Some(routes.PropertyDetailsOverviewController.onEditPageLoad(propertyId).url))
    }
  }
}
