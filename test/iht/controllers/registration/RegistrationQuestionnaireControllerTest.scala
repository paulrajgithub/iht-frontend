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

import iht.connector.{CachingConnector, ExplicitAuditConnector, IhtConnector}
import iht.models.QuestionnaireModel
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier


/**
 * Created by yasar on 6/18/15.
 */
class RegistrationQuestionnaireControllerTest extends RegistrationControllerTest {

  override implicit val hc = new HeaderCarrier()
  val mockIhtConnector = mock[IhtConnector]
  val mockAuditConnector = mock[ExplicitAuditConnector]

  before {
    mockCachingConnector = mock[CachingConnector]
  }

  // Create controller object and pass in mock.
  def questionnaireController = new RegistrationQuestionnaireController {
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val isWhiteListEnabled = false
    override def explicitAuditConnector = mockAuditConnector
    def cachingConnector = mockCachingConnector
    def ihtConnector = mockIhtConnector
  }

  def questionnaireControllerNotAuthorised = new RegistrationQuestionnaireController {
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val isWhiteListEnabled = false
    override def explicitAuditConnector = mockAuditConnector
    def cachingConnector = mockCachingConnector
    def ihtConnector = mockIhtConnector
  }

  "onApplicationPageLoad method" must {

    "redirect to GG login page on onPageLoad if the user is not logged in" in {
      val result = questionnaireControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to GG login page on Submit if the user is not logged in" in {
      val result = questionnaireControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK and correct header title on page load" in {
      val result = questionnaireController.onPageLoad()(createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include(Messages("site.registration.title"))
    }

    "respond with redirect on page submit" in {
      val result = questionnaireController.onSubmit()(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(iht.controllers.routes.IhtMainController.signOut().url))
    }

    "set up instance for explicit audit connector" in {
      RegistrationQuestionnaireController.explicitAuditConnector shouldBe ExplicitAuditConnector
    }

    "log helper bad request validation" in {
      val questionnaireModel = QuestionnaireModel(Some(7),None,None,None)
      val questionnaireForm1 = iht.forms.QuestionnaireForms.questionnaireForm.fill(questionnaireModel)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(questionnaireForm1.data.toSeq: _*)

      val result = questionnaireController.onSubmit()(request)
      status(result) should be(BAD_REQUEST)
    }
  }
}
