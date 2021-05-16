package com.tugraz.quizlet.backend

import androidx.annotation.VisibleForTesting
import com.google.common.collect.ImmutableList
import com.tugraz.quizlet.backend.database.DBInterface
import com.tugraz.quizlet.backend.database.model.Question
import com.tugraz.quizlet.backend.database.model.Question_category
import io.realm.RealmList
import io.realm.mongodb.AppException
import org.bson.types.ObjectId
import java.util.logging.Logger
import kotlin.jvm.Throws

class RequestHandler(private val dBInterface: DBInterface) {
    companion object {
        val LOG: Logger = Logger.getLogger(RequestHandler::class.java.name)
        const val POINTS_FOR_RIGHT_ANSWER = 5
    }

    private var remainingQuestionForCurrentGame: ArrayList<Question> = ArrayList()
    private var highscoreForCurrentGame = 0
    private var firstQuestion = true

    // TODO: add boolean for feedback?
    fun addUser(email: String, password: String) {
        LOG.fine("Processing adding user with email=$email")
        dBInterface.addUser(email, password)
    }

    @Throws(AppException::class)
    fun loginUser(email: String, password: String): Boolean {
        LOG.fine("Processing getting user with email=$email")
        return dBInterface.loginUser(email, password)
    }

    // TODO: add boolean for feedback?
    fun addQuestion(category: Question_category, question: String, rightAnswer: String, wrongAnswers: ImmutableList<String>) {
        val newQuestion = Question(
            ObjectId(),
            category,
            question,
            rightAnswer,
            null,
            getRealmListFromImmutableList(wrongAnswers)
        )
        LOG.fine("Processing getting user with email=$newQuestion")
        dBInterface.addQuestion(newQuestion)
    }

    private fun getRealmListFromImmutableList(wrongAnswers: ImmutableList<String>): RealmList<String> {
        val realmList = RealmList<String>()
        wrongAnswers.forEach {
            realmList.add(it)
        }
        return realmList
    }

    fun startNewGame() {
        this.highscoreForCurrentGame = 0
        firstQuestion = true
        remainingQuestionForCurrentGame.addAll(dBInterface.getAllQuestions())
    }

    private fun getNextQuestionAndUpdateRemaining(): Question? {
        return remainingQuestionForCurrentGame.removeFirstOrNull()
    }

    fun getNextQuestionAndUpdateRemainingAndUpdateHighscore(): Question? {
        if(!firstQuestion) {
            this.highscoreForCurrentGame += POINTS_FOR_RIGHT_ANSWER
        }
        firstQuestion = false
        return getNextQuestionAndUpdateRemaining()
    }

    fun endCurrentGameAndReturnCurrentHighscoreAndUpdateDatabase(): Int {
        remainingQuestionForCurrentGame.clear()
        if(getBestHighscoreOfCurrentUser() < highscoreForCurrentGame) {
            dBInterface.updateUserHighscore(this.highscoreForCurrentGame)
        }
        return highscoreForCurrentGame
    }

    fun getBestHighscoreOfCurrentUser(): Int {
        return dBInterface.getHighscoreOfCurrentUser()
    }

    fun getAllQuestion(): ImmutableList<Question> {
        LOG.fine("Processing getting all questions")
        return dBInterface.getAllQuestions()
    }

    fun getAllQuestionForCategory(categoryName: String): ImmutableList<Question> {
        LOG.fine("Processing getting all questions for category=$categoryName")
        return dBInterface.getAllQuestionsForCategory(categoryName)
    }

    fun getCurrentHighscoreOfGame(): Int {
        return this.highscoreForCurrentGame
    }

    @VisibleForTesting
    fun setRemainingQuestionForCurrentGame(questions: ArrayList<Question>) {
        this.remainingQuestionForCurrentGame = questions
    }

    @VisibleForTesting
    fun getRemainingQuestionForCurrentGame(): ArrayList<Question> {
        return this.remainingQuestionForCurrentGame
    }

    @VisibleForTesting
    fun setHighscoreCurrentGame(highscore: Int) {
        this.highscoreForCurrentGame = highscore
    }
}
