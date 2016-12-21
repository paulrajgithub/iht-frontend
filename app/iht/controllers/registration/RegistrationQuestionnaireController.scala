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

package iht.controllers.registration

import iht.controllers.{IhtConnectors, QuestionnaireController}
import iht.utils.IhtSection
import iht.views.html.registration.registration_questionnaire

object RegistrationQuestionnaireController extends RegistrationQuestionnaireController with IhtConnectors {}

trait RegistrationQuestionnaireController extends RegistrationController with QuestionnaireController {
  override def guardConditions: Set[Predicate] = Set.empty
  override lazy val ihtSection = IhtSection.Registration
  def questionnaireView = (form, request) => registration_questionnaire(form)(request)
}