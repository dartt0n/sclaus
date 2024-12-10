package com.dartt0n.sclaus.dialogs

import com.dartt0n.sclaus.domain.User

object EnglishDialogs extends Dialogs {

  def greeting(user: User): String = {
    s"""🎅 Hello there${user.firstName.map(", " + _).getOrElse("")}, ho ho ho! It's Santa Claus here, spreading joy and cheer! ☃️
       |
       |🧝‍♂️ My clever little elves have been working hard to help me bring some holiday magic to the wonderful folks at Innopolis University.
       |
       |🎁 They've created this special bot just for you, so you can join in the fun and share the warmth of the season with nice gifts!
       |
       |Are you ready for a ✨ miracle ✨?
       |""".stripMargin
  }

  def greetingReplyButton(): String =
    s"""Of course, let's go!"""

  def rules(): String = {
    s"""🔥 That's the spirit, keep it up!
       |
       |🤫 Meanwhile, let me explain you how Secret Santa works:
       |
       |🤞 First, each participant shares a bit about themselves - their likes, dislikes, something they would really appreciate, and something they are allergic to, so that the Secret Santa can choose the perfect gift!
       |
       |🎲 Next, each participant becomes a secret Santa for a random other participant, someone becomes a Secret Santa for them
       |
       |🎊 And on the appointed day, all the participants gather at the party and receive their gifts! Yay!
       |
       |📜 However, there are several important rules and dates for each participant. Are you listening carefully?
       |""".stripMargin
  }

  def rulesReplyButton(): String =
    s"""I'm all ears"""

  def timeline(): String = {
    s"""👍 Good to hear!
       |
       |So, please, pay attention to the following points:
       |1️⃣ You need to specify your preferences in the bot before **December 12, 20:00**, otherwise you will not participate
       |
       |2️⃣ On **December 13**, you will receive the name of the person and his preferences to prepare a gift
       |
       |3️⃣ You need to prepare present:
       | • The maximum value of the gift is 300-500 rubles
       | • You need to make and **bring it to the office 319** before **December 19 17:00**
       | • Add the note or little postcard, which **includes the name of the person** for whom the present is
       |
       |4️⃣ Pick up your gift on **December 20** at the office 319
       |
       |❗ If you leave early, you can pick up your gift from office 319 from 9:00 to 18:00 from Monday to Thursday, on Friday until 16:45, **please notify the keepers of gifts (office 319 employees) in advance**
       |
       |
       |Hope everything is clear, so let's begin the fun? Are you ready to specify preferences about your present?`
       |""".stripMargin
  }

  def timelineReplyButton(): String =
    s"""Yes, I'm ready""".stripMargin

  def askPreferences(): String = {
    s"""🤩 Ho ho ho!
       |
       |🥺 Probably, the person who would receive your name will not know about you anything...
       |
       |🤗 So, You need to help him/her to prepare a good gift for you!
       |
       |Please tell me about yourself: what kind of gift would you like to receive, what do you dream about, what on the contrary is not worth giving, and I will try to make a short list of this information. You've got my attention!
       |""".stripMargin
  }

  def showPreferences(user: User): String =
    // todo: implement
    s""""""

  def onlyPrivateChatsAllowed(): String = {
    s"""👋 Hi!
       |
       |⚠️ Please note, I only communicate in private messages
       |
       |🤫 It's not worth sharing your secrets here
       |""".stripMargin
  }

}
