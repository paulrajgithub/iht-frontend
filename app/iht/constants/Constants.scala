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

package iht.constants

import java.net.URL

import play.api.Play
import play.api.Play.current

import scala.collection.immutable.ListMap
import iht.constants.IhtProperties._

/**
  * Created by dbeer on 04/08/15.
  */
object Constants {

  val insurancePolicyFormFieldsWithExtraContentLineInErrorSummary = Set(
    "policyInDeceasedName", "isJointlyOwned", "isInsurancePremiumsPayedForSomeoneElse", "isAnnuitiesBought", "isInTrust"
  )

  val AppSectionProperties = "properties"
  val AppSectionMoney = "money"
  val AppSectionHousehold = "household"
  val AppSectionVehicles = "vehicles"
  val AppSectionPrivatePension = "privatePension"
  val AppSectionStockAndShare = "stockAndShare"
  val AppSectionInsurancePolicy = "insurancePolicy"
  val AppSectionBusinessInterest = "businessInterest"
  val AppSectionNominated = "nominated"
  val AppSectionHeldInTrust = "heldInTrust"
  val AppSectionForeign = "foreign"
  val AppSectionMoneyOwed = "moneyOwed"
  val AppSectionOther = "otherAsset"


  val AppSectionMortgages = "mortgages"
  val AppSectionFuneralExpenses = "funeralExpenses"
  val AppSectionDebtsOwedFromTrust = "debtsFromTrust"
  val AppSectionDebtsOwedToAnyoneOutsideUK = "debtsOutsideUk"
  val AppSectionDebtsOwedOnJointAssets = "jointlyOwned"
  val AppSectionDebtsOther = "otherDebt"

  val AppSectionExemptionsPartnerIsAssetForDeceasedPartner = "exemptionsPartnerIsAssetForDeceasedPartner"
  val AppSectionExemptionsPartnerIsPartnerHomeInUK = "exemptionsPartnerIsPartnerHomeInUK"
  val AppSectionExemptionsPartnerName = "exemptionsPartnerName"
  val AppSectionExemptionsPartnerDateOfBirth = "exemptionsPartnerDateOfBirth"
  val AppSectionExemptionsPartnerNino = "exemptionsPartnerNino"
  val AppSectionExemptionsPartnerTotalAssets = "exemptionsPartnerTotalAssets"

  val AppSectionExemptionsCharityValue = "exemptionsCharityValue"
  val AppSectionExemptionsQualifyingBodyValue = "exemptionsQualifyingBodyValue"
  val AppSectionEstateAssets = "estateAssets"
  val AppSectionEstateDebts = "estateDebts"
  val AppSectionEstateGifts = "estateGifts"

  // Fragment Identifiers - used to return user to position in list
  val AppSectionPropertiesID = "properties-buildings-and-land"
  val AppSectionMoneyID = "money"
  val AppSectionHouseholdID = "household-and-personal-items"
  val AppSectionVehiclesID = "motor-vehicles"
  val AppSectionPrivatePensionID = "private-pensions"
  val AppSectionStockAndShareID = "stocks-and-shares"
  val AppSectionInsurancePolicyID = "insurance-policies"
  val AppSectionBusinessInterestID = "business-interests"
  val AppSectionNominatedID = "nominated-assets"
  val AppSectionHeldInTrustID = "assets-held-in-a-trust"
  val AppSectionForeignID = "foreign-assets"
  val AppSectionMoneyOwedID = "money-owed"
  val AppSectionOtherID = "other-assets"
  val GiftsGivenAwayQuestionID = "gave-away-seven-years"
  val GiftsReservationBenefitSectionID = "with-reservation-of-benefit-section"
  val GiftsReservationBenefitQuestionID = "with-reservation-of-benefit"
  val GiftsSevenYearsSectionID = "seven-years-section"
  val GiftsSevenYearsQuestionID = "seven-years"
  val GiftsSevenYearsQuestionID2 = "seven-years2"
  val GiftsValueOfGiftsSectionID = "value-of-gifts"
  val GiftsValueOfGiftsQuestionID = "value-of-gifts"
  val GiftsValueDetailID = "value-of-gifts-for-period-"

  val MaxIterationValueForGiftYears = 8

  val PDFHMRCGuidance: URL = Play.classloader.getResource("pdf/151001 Notes to help you fill in IHT online - final - for DDCN.pdf")

  val yesAnswer = "Yes"
  val noAnswer = "No"

  val AssetTypeSingle = "single"
  val AssetTypeJoint = "joint"

  //KEYSTORE KEYS
  val ExemptionsGuidanceContinueUrlKey = "ExemptionsGuidanceContinueUrl"
  val PDFIHTReference = "PDFIHTReference"

  //HTTP METHODS
  val GET = "GET"
  val POST = "POST"

  //Filter UI related constants
  lazy val filterChoices = "filter-choices"
  lazy val continueEstateReport = "continue"
  lazy val register = "register"
  lazy val alreadyStarted = "already-started"
  lazy val agent = "agent"

  lazy val domicile = "domicile"
  lazy val englandOrWales = "england-or-wales"
  lazy val scotland = "scotland"
  lazy val northernIreland = "northern-ireland"
  lazy val otherCountry = "other"

  lazy val estimate = "estimate"
  lazy val under325000 = "under-325000"
  lazy val between325000and1million = "between-325000-and-1million"
  lazy val moreThan1million = "more-than-1million"

  lazy val ETMPAssetCodesToIHTMessageKeys = ListMap(
    ETMPAssetCodeMoney -> "iht.estateReport.assets.money.upperCaseInitial",
    ETMPAssetCodeHouseHold -> "iht.estateReport.assets.householdAndPersonalItems.title",
    ETMPAssetCodePrivatePension -> "iht.estateReport.assets.privatePensions",
    ETMPAssetCodeStockShareNotListed -> "iht.estateReport.assets.stocksAndSharesNotListed",
    ETMPAssetCodeStockShareListed -> "iht.estateReport.assets.stocksAndSharesListed",
    ETMPAssetCodeInsurancePolicy -> "iht.estateReport.assets.insurancePolicies",
    ETMPAssetCodeBusinessInterest -> "iht.estateReport.assets.businessInterests.title",
    ETMPAssetCodeNominatedAsset -> "iht.estateReport.assets.nominated",
    ETMPAssetCodeForeignAsset -> "iht.estateReport.assets.foreign.title",
    ETMPAssetCodeMoneyOwed -> "iht.estateReport.assets.moneyOwed",
    ETMPAssetCodeOtherAsset -> "page.iht.application.assets.main-section.other.title",
    ETMPAssetCodeTrust -> "iht.estateReport.assets.heldInATrust.title"
  )

  lazy val ETMPExemptionTypesToIHTMessageKeys = ListMap(
    ETMPExemptionTypeGNCP -> "pdf.exemption.otherQualifyingBodies"
  )

  lazy val  NINO= "customerNino"
}
