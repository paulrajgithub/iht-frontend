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

package iht.controllers.registration.executor

import iht.connector.CachingConnector
import iht.constants.IhtProperties
import iht.controllers.registration.RegistrationControllerTest
import iht.forms.registration.CoExecutorForms._
import iht.models.{CoExecutor, ContactDetails, RegistrationDetails}
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import org.joda.time.LocalDate
import org.scalatest.BeforeAndAfter
import play.api.data.Form
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

class CoExecutorPersonalDetailsControllerTest extends RegistrationControllerTest with BeforeAndAfter {

  before {
    mockCachingConnector = mock[CachingConnector]
  }

  // Create controller object and pass in mock.
  def controller = new CoExecutorPersonalDetailsController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val isWhiteListEnabled = false
  }

  def controllerNotAuthorised = new CoExecutorPersonalDetailsController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val isWhiteListEnabled = false
  }

  "CoExecutorPersonalDetailsController" must {

    "redirect to GG login page on PageLoad if the user is not logged in" in {
      val result = controllerNotAuthorised.onPageLoad(None)(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to GG login page on Submit if the user is not logged in" in {
      val result = controllerNotAuthorised.onSubmit(None)(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to GG login page on PageLoad in edit mode if the user is not logged in" in {
      val result = controllerNotAuthorised.onEditPageLoad("1")(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to GG login page on Submit in edit mode if the user is not logged in" in {
      val result = controllerNotAuthorised.onEditSubmit("1")(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "load when no prior co-executors are saved" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val result = controller.onPageLoad(None)(createFakeRequest())

      status(result) should be(OK)
      contentAsString(result) should include(messagesApi("page.iht.registration.co-executor-personal-details.title"))
    }


    "contain Continue button when Page is loaded in normal mode" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val result = controller.onPageLoad(None)(createFakeRequest())
      status(result) shouldBe OK

      contentAsString(result) should include(messagesApi("iht.continue"))
      contentAsString(result) should not include messagesApi("site.link.cancel")
    }

    "contain Continue and Cancel buttons when Page is loaded in edit mode" in {
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onEditPageLoad("1")(createFakeRequest())
      status(result) shouldBe OK

      contentAsString(result) should include(messagesApi("iht.continue"))
      contentAsString(result) should include(messagesApi("site.link.cancel"))
    }

    "not contain the 'Do you live in the UK' question when loaded in edit mode" in {
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onEditPageLoad("1")(createFakeRequestWithReferrer(referrerURL=referrerURL,host=host))
      status(result) shouldBe OK

      contentAsString(result) should not include messagesApi("page.iht.registration.co-executor-personal-details.isAddressInUk")
    }


    "load an existing co-executor" in {
      val firstName = CommonBuilder.firstNameGenerator
      val surname = CommonBuilder.surnameGenerator
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=firstName, lastName=surname)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onPageLoad(Some("1"))(createFakeRequest())

      status(result) should be(OK)
      contentAsString(result) should include(messagesApi("page.iht.registration.co-executor-personal-details.title"))
      contentAsString(result) should include(firstName)
      contentAsString(result) should include(surname)
    }

    "load when creating a new co-executor and another already exists" in {
      val firstName = CommonBuilder.firstNameGenerator
      val surname = CommonBuilder.surnameGenerator
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=firstName, lastName=surname)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val result = controller.onPageLoad(None)(createFakeRequest())

      status(result) should be(OK)
      contentAsString(result) should include(messagesApi("page.iht.registration.co-executor-personal-details.title"))
      contentAsString(result) should not include firstName
      contentAsString(result) should not include surname
    }

    "raise an error when accessed for a non-existant co-executor" in {
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      intercept[java.lang.Exception] {
        val result = controller.onPageLoad(Some("2"))(createFakeRequest())
        status(result)
      }
    }

    "raise an error when trying to add more co-executors than the maximum allowed" in {
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val coExec2 = CommonBuilder.buildCoExecutor copy (id = Some("2"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val coExec3 = CommonBuilder.buildCoExecutor copy (id = Some("3"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1, coExec2, coExec3))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      intercept[Exception] {
        val result = controller.onPageLoad(None)(createFakeRequest())
        status(result)
      }
    }

    "raise an error when trying to add a co-executor but others applying for probate is unanswered" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))

      intercept[java.lang.RuntimeException] {
        val result = controller.onPageLoad(None)(createFakeRequest())
        status(result)
      }
    }

    "raise an error when trying to add a co-executor but others are not applying for probate" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails
        copy (areOthersApplyingForProbate = Some(false))))

      intercept[java.lang.RuntimeException] {
        val result = controller.onPageLoad(None)(createFakeRequest())
        status(result)
      }
    }

    "save a valid new co-executor located in the uk" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails),
        Some(deceasedDetails), areOthersApplyingForProbate = Some(true))

      val coExecutor = CommonBuilder.buildCoExecutorPersonalDetails(None)
      val form = coExecutorPersonalDetailsForm.fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val result = controller.onSubmit(None)(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(
        Some(iht.controllers.registration.executor.routes.OtherPersonsAddressController.onPageLoadUK("1").url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)

      capturedValue.coExecutors.length shouldBe 1
      capturedValue.coExecutors.head shouldBe CommonBuilder.buildCoExecutorPersonalDetails(Some("1"))
      capturedValue.deceasedDetails shouldBe Some(deceasedDetails)
      capturedValue.applicantDetails shouldBe Some(applicantDetails)
    }

    "save a valid new co-executor located outside of the uk" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails), Some(deceasedDetails),
        areOthersApplyingForProbate = Some(true))

      val coExecutor = CommonBuilder.buildCoExecutorPersonalDetails() copy (isAddressInUk = Some(false))
      val form = coExecutorPersonalDetailsForm.fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(
        CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val result = controller.onSubmit(None)(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(
        Some(iht.controllers.registration.executor.routes.OtherPersonsAddressController.onPageLoadAbroad("1").url))
    }

    "save a valid co-executor when another already exists" in {
      val coExec1 = CommonBuilder.buildCoExecutorPersonalDetails(Some("1")) copy (firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val coExecutor = CommonBuilder.buildCoExecutorPersonalDetails(None)
      val form = coExecutorPersonalDetailsForm.fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq)

      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val result = controller.onSubmit(None)(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(
        Some(iht.controllers.registration.executor.routes.OtherPersonsAddressController.onPageLoadUK("2").url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val expectedCoExecutor = CommonBuilder.buildCoExecutorPersonalDetails(Some("2"))

      capturedValue.coExecutors.length shouldBe 2
      capturedValue.coExecutors.head shouldBe coExec1
      capturedValue.coExecutors(1) shouldBe expectedCoExecutor
    }

    "update an existing co-executor with valid data" in {
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val firstName = CommonBuilder.firstNameGenerator
      val surname = CommonBuilder.surnameGenerator
      val coExecutor = CommonBuilder.buildCoExecutorWithId(Some("1")) copy (firstName = firstName,
        lastName = surname)

      val form = coExecutorPersonalDetailsForm.fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq)

      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val result = controller.onSubmit(Some("1"))(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(
        Some(iht.controllers.registration.executor.routes.OtherPersonsAddressController.onPageLoadUK("1").url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val expectedCoExecutor = coExec1 copy (firstName = firstName, lastName = surname)
      capturedValue.coExecutors.length shouldBe 1
      capturedValue.coExecutors.head shouldBe expectedCoExecutor
    }

    "update an existing co-executor in edit mode with valid data" in {
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator
        , isAddressInUk = Some(true))
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val firstName = CommonBuilder.firstNameGenerator
      val surname = CommonBuilder.surnameGenerator
      val coExecutor = CommonBuilder.buildCoExecutorWithId(Some("1")) copy (firstName = firstName,
        lastName = surname, isAddressInUk = Some(false))

      val form = coExecutorPersonalDetailsForm.fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq)

      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val result = controller.onEditSubmit("1")(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(
        Some(iht.controllers.registration.routes.RegistrationSummaryController.onPageLoad().url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val expectedCoExecutor = coExec1 copy (firstName = firstName, lastName = surname, isAddressInUk = Some(true))
      capturedValue.coExecutors.length shouldBe 1
      capturedValue.coExecutors.head shouldBe expectedCoExecutor
    }

    "raise an error when trying to save for a non-existant co-executor" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val coExecutor = CommonBuilder.buildCoExecutor
      val form = coExecutorPersonalDetailsForm.fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq)

      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      intercept[Exception] {
        val result = controller.onSubmit(Some("1"))(request)
        status(result)
      }
    }

    "raise an error when trying to save a new co-executor and the maximum number already exist" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(CommonBuilder.DefaultCoExecutor1, CommonBuilder.DefaultCoExecutor2, CommonBuilder.DefaultCoExecutor3))

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val coExecutor = CommonBuilder.buildCoExecutor
      val form = coExecutorPersonalDetailsForm.fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq)

      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      intercept[Exception] {
        val result = controller.onSubmit(None)(request)
        status(result)
      }
    }

    "raise an error when trying to submit a co-executor but others applying for probate is unanswered" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetails
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val coExecutor = CommonBuilder.buildCoExecutor
      val form = coExecutorPersonalDetailsForm.fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq)

      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      intercept[Exception] {
        val result = controller.onSubmit(None)(request)
        status(result)
      }
    }

    "raise an error when trying to submit a co-executor but others are not applying for probate" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
        areOthersApplyingForProbate = Some(false))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val coExecutor = CommonBuilder.buildCoExecutor
      val form = coExecutorPersonalDetailsForm.fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq)

      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(registrationDetails))

      intercept[Exception] {
        val result = controller.onSubmit(None)(request)
        status(result)
      }
    }

    "show an error when some data is invalid" in {
      val coExecutor = CommonBuilder.buildCoExecutor copy (firstName = "")
      checkForErrorOnSubmissionOfModel(coExecutor, "error.firstName.give")
    }

    "save a valid new co-executor located in the uk will return an internal server error of the storage fails" in {
      val applicantDetails = CommonBuilder.buildApplicantDetails
      val deceasedDetails = CommonBuilder.buildDeceasedDetails
      val registrationDetails = RegistrationDetails(None, Some(applicantDetails),
        Some(deceasedDetails), areOthersApplyingForProbate = Some(true))

      val coExecutor = CommonBuilder.buildCoExecutorPersonalDetails(None)
      val form = coExecutorPersonalDetailsForm.fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq)

      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))
      createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val result = controller.onSubmit(None)(request)
      status(result) should be(INTERNAL_SERVER_ERROR)
    }

    "when update an existing co-executor with valid data return an internal server error if the storage fails" in {
      val coExec1 = CommonBuilder.buildCoExecutor copy (id = Some("1"), firstName=CommonBuilder.firstNameGenerator,
        lastName=CommonBuilder.surnameGenerator)
      val registrationDetails = CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate copy(
        coExecutors = Seq(coExec1))
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(registrationDetails))

      val coExecutor = CommonBuilder.buildCoExecutorWithId(Some("1")) copy (firstName = CommonBuilder.firstNameGenerator,
        lastName = CommonBuilder.surnameGenerator)

      val form = coExecutorPersonalDetailsForm.fill(coExecutor)

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host,
        data = form.data.toSeq)

      createMockToStoreRegDetailsInCacheWithFailure(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate))

      val result = controller.onSubmit(Some("1"))(request)
      status(result) should be(INTERNAL_SERVER_ERROR)
    }

    def checkForErrorOnSubmission(detailsToSubmit: Seq[(String, String)], expectedError: String): Unit = {
      val result = submitCoExecutorPersonalDetails(
        CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate, detailsToSubmit, None)
      status(result) should be(BAD_REQUEST)
      contentAsString(result) should include(messagesApi(expectedError))
    }

    def checkForErrorOnSubmissionOfModel(coExecutor: CoExecutor,expectedError: String): Unit = {
      val result = submitCoExecutorPersonalDetailsModel(
        CommonBuilder.buildRegistrationDetailsWithOthersApplyingForProbate, coExecutor, None)
      status(result) should be(BAD_REQUEST)
      contentAsString(result) should include(messagesApi(expectedError))
    }

    def prepareForm(coExecutor: CoExecutor): Form[CoExecutor] = {
      coExecutorPersonalDetailsForm.fill(coExecutor)
    }

    def submitCoExecutorPersonalDetailsModel(rd: RegistrationDetails, detailsToSubmit: CoExecutor,
                                             submissionId: Option[String]): Future[Result] = {
      val form = prepareForm(detailsToSubmit)
      submitCoExecutorPersonalDetails(rd, form.data.toSeq, submissionId)
    }

    def submitCoExecutorPersonalDetails(rd: RegistrationDetails, detailsToSubmit: Seq[(String, String)],
                                        submissionId: Option[String]): Future[Result] = {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(rd))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(rd))

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = detailsToSubmit)

      controller.onSubmit(submissionId)(request)
    }
  }
}
