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

package iht.views.application.assets.stocksAndShares

import iht.controllers.application.assets.stocksAndShares.routes._
import iht.models.application.assets.StockAndShare
import iht.testhelpers.CommonBuilder
import iht.views.ViewTestHelper
import iht.views.application.StocksAndSharesOverviewViewBehaviour
import iht.views.html.application.asset.stocksAndShares.stocks_and_shares_overview
import org.jsoup.nodes.Document

class StocksAndSharesOverviewViewTest extends ViewTestHelper with StocksAndSharesOverviewViewBehaviour {

  override def pageTitle = "iht.estateReport.assets.stocksAndShares"
  override def browserTitle = "iht.estateReport.assets.stocksAndShares"
  override def guidanceParagraphs = Set("page.iht.application.assets.stocksAndShares.overview.guidance",
                                        "page.iht.application.assets.stocksAndShares.overview.guidance2")
  override def stocksAndSharesListedHeadingElementId = "stocks-and-shares-listed-heading"
  override def stocksAndSharesNotListedHeadingElementId = "stocks-and-shares-notListed-heading"
  override def urlToStocksAnsSharesListedPage = StocksAndSharesListedController.onPageLoad().url
  override def urlToStocksAnsSharesNotListedPage = StocksAndSharesNotListedController.onPageLoad().url
  override def stocksAndSharesListedHeaderText = "iht.estateReport.assets.stocksAndSharesListed"
  override def stocksAndSharesNotListedHeaderText = "iht.estateReport.assets.stocksAndSharesNotListed"
  override def stocksAndSharesListedRowId = "stocks-and-shares-listed-question"
  override def stocksAndSharesListedQuestionText = "iht.estateReport.assets.stocksAndShares.listed.question"
  override def stocksAndSharesListedValueRowId = "stocks-and-shares-listed-value"
  override def stocksAndSharesListedValueText = "iht.estateReport.assets.stocksAndShares.valueOfListed"
  override def stocksAndSharesNotListedQuestionRowId = "stocks-and-shares-notListed-question"
  override def stocksAndSharesNotListedQuestionText = "iht.estateReport.assets.stocksAndShares.notListed.question"
  override def stocksAndSharesNotListedValueRowId = "stocks-and-shares-notListed-value"
  override def stocksAndSharesNotListedValueText = "iht.estateReport.assets.stocksAndShares.valueOfNotListed"

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  override def deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  "StocksAnsShares overview view" must {

    behave like overviewView()
  }

  "StocksAnsShares overview view" when {
    "no questions have been answered" must {

      behave like overviewViewWithQuestionsUnanswered()
    }

    "the questions have been answered as No" must {

      behave like overviewViewWithQuestionsAnsweredNo()
    }

    "the questions have been answered as Yes with no value supplied" must {

      behave like overviewViewWithQuestionsAnsweredYes()
    }

    "the questions have been answered and values given" must {

      behave like overviewViewWithValues()
    }
  }

  override def fixture(data: Option[StockAndShare]) = new {
    implicit val request = createFakeRequest()
    val view = stocks_and_shares_overview(data, regDetails).toString
    val doc: Document = asDocument(view)
  }
}