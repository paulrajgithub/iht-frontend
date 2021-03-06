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

package iht.views

import iht.views.html.iht_main_template
import iht.{FakeIhtApp, TestUtils}
import org.scalatest.mock.MockitoSugar
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._

class IhtMainTemplateTest extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils {

  "RegistrationMainTemplate" must {

    "contain the correct text for the sign out link" in {
      val signOutUrl = "localhost"
      iht_main_template(title = "", signOutText = "", signOutUrl = Some(Call("GET", signOutUrl)), headerTitle = None)(HtmlFormat.empty)
        .toString should include (signOutUrl)
    }
  }
}
