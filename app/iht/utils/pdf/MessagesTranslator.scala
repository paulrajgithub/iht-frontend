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

package iht.utils.pdf

import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages, MessagesApi}

/**
  * Created by grant on 02/12/16.
  */
class MessagesTranslator @Inject()(val messagesApi: MessagesApi) extends I18nSupport {
  def getMessagesText(key: String): String = Messages(key)

  def getMessagesTextWithParameter(key: String, parameter:String ): String = Messages(key, parameter)

  def getMessagesTextWithParameters(key: String, parameter1: String, parameter2:String): String =
      Messages(key, parameter1, parameter2)

  def getMessagesTextWithParameters(key: String, parameter1: String, parameter2:String , parameter3: String): String =
      Messages(key, parameter1, parameter2, parameter3)
}
