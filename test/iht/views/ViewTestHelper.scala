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

import iht.models.UkAddress
import iht.testhelpers.ContentChecker
import iht.utils.CommonHelper
import iht.{FakeIhtApp, TestUtils}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.safety.Whitelist
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.test.UnitSpec

import scala.collection.JavaConversions._

trait ViewTestHelper extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with HtmlSpec with BeforeAndAfter {
  val messageKeysRegex = """([A-Za-z]+\.){1,}[\w]+""".r
  /**
    * Items which look like message keys but actually are real content, so instances of them
    * found in generated HTML should not cause the message key detection unit test to fail :-
    *   GOV.UK: Displayed in browser titles
    *   U.S: The United States
    */
  val messageKeyExclusions = Set("GOV.UK", "U.S")
  val messageKeysDelimiter = ", "

  implicit override val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  def titleShouldBeCorrect(pageContent: String, expectedTitle: String) = {
    val doc = asDocument(pageContent)
    val headers = doc.getElementsByTag("h1")
    headers.size shouldBe 1
    headers.first.text() shouldBe expectedTitle
  }

  def browserTitleShouldBeCorrect(pageContent: String, expectedTitle: String) = {
    val doc = asDocument(pageContent)
    assertEqualsValue(doc, "title", buildApplicationTitle(expectedTitle))
  }

  def radioButtonShouldBeCorrect(doc: Document, labelTextMessagesKey: String, radioID: String,
                                 labelID: Option[String] = None) = {
    val labelText = messagesApi(labelTextMessagesKey)
    val label = doc.getElementById(labelID.fold(s"$radioID-label")(identity))
    label.text shouldBe labelText
    val radio = label.children.first
    radio.id shouldBe radioID
  }

  def noMessageKeysShouldBePresent(content:String) = {
    val cleanedContent = Jsoup.clean(content, Whitelist.basic) // Remove javascript
    val doc = asDocument(cleanedContent)
    doc.select("a").unwrap // Remove anchor tags but keep the content within them
    val candidatesIterator = messageKeysRegex.findAllIn(doc.toString) // Find all possible message key candidates
    val messageKeysFound = candidatesIterator.foldRight[String](""){ (currentCandidate, previousCandidates) =>
      if (messageKeyExclusions.exists( exclusion => currentCandidate.contains(exclusion) )) {
        previousCandidates
      } else {
        if (previousCandidates.nonEmpty) {
          previousCandidates + messageKeysDelimiter + currentCandidate
        } else {
          previousCandidates + currentCandidate
        }
      }
    }
    messageKeysFound shouldBe ""
  }

  def messagesShouldBePresent(content: String, expectedSentences: String*) = {
    for (sentence <- expectedSentences) ContentChecker.stripLineBreaks(content) should include(ContentChecker.stripLineBreaks(sentence))
  }

  def messagesShouldNotBePresent(content: String, unexpectedSentences: String*) = {
    for (sentence <- unexpectedSentences) ContentChecker.stripLineBreaks(content) should not include ContentChecker.stripLineBreaks(sentence)
  }

  def buildApplicationTitle(title: String) = {

    title + " " + messagesApi("site.title.govuk")
  }

  def labelShouldBe(doc: Document, labelId: String, messageKey: String) = {

    val label = doc.getElementById(labelId)
    val mainLabel = label.getElementsByTag("span").first
    mainLabel.text shouldBe messagesApi(messageKey)
  }

  def labelHelpTextShouldBe(doc: Document, labelId: String, messageKey: String) = {

    val label = doc.getElementById(labelId)
    val helpText = label.getElementsByTag("span").get(1)
    helpText.text shouldBe messagesApi(messageKey)
  }

  def elementShouldHaveText(doc: Document, id: String, expectedValueMessageKey: String) = {
    val element = doc.getElementById(id)
    element.text shouldBe messagesApi(expectedValueMessageKey)
  }

  /**
    * Gets the value of the specified element unless it contains an element of the
    * type specified which has the attribute aria-hidden set to true, in which case
    * the value of the latter is returned.
    */
  def getVisibleText(element: Element, containingElementType:String = "span", includeTextOfChildElements: Boolean = true) = {
    val containingElements: Set[Element] = element.getElementsByTag(containingElementType).toSet
    containingElements.find(_.attr("aria-hidden") == "true") match {
      case None =>
        if (includeTextOfChildElements) {
          element.text
        } else {
          element.ownText
        }
      case Some(ariaHiddenElement) => ariaHiddenElement.text
    }
  }

  def formatAddressForDisplay(address: UkAddress) =
    CommonHelper.withValue(address) { addr =>
      s"${addr.ukAddressLine1} ${addr.ukAddressLine2} ${addr.ukAddressLine3.getOrElse("")} ${addr.ukAddressLine4.getOrElse("")} ${addr.postCode}"
    }

  def tableCell(doc:Document, tableId:String, colNo: Int, rowNo: Int) = {
    val propertiesUl = doc.getElementById(tableId)
    val listItems = propertiesUl.getElementsByTag("li")
    listItems.get(rowNo).getElementsByTag("div").get(colNo)
  }

  def link(doc: => Document, anchorId: => String, href: => String, text: => String): Unit = {
    def anchor = doc.getElementById(anchorId)
    s"have a link with id $anchorId and correct target" in {
      anchor.attr("href") shouldBe href
    }
    s"have a link with id $anchorId and correct text" in {
      getVisibleText(anchor) shouldBe text
    }
  }
}
