package com.tugraz.quizlet.frontend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.tugraz.quizlet.R
import com.tugraz.quizlet.backend.database.model.Question
import java.util.Collections.shuffle


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private val ANSWER_BUTTONS = arrayOf(
    R.id.button_answer_1,
    R.id.button_answer_2,
    R.id.button_answer_3,
    R.id.button_answer_4
)
private var currentQuestion: Question? = null
private var correctAnswerIndex = 0

/**
 * A simple [Fragment] subclass.
 * Use the [PlayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlayFragment : Fragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_play, container, false)

        for (i: Int in 0 .. 3 ) {
            view.findViewById<Button>(ANSWER_BUTTONS[i]).setOnClickListener(this)
        }

        SplashActivity.requestHandler.startNewGame()
        displayNewQuestion(view)

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Play.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlayFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun displayNewQuestion(view: View) {

        //TODO Implement once database is ready
        val newQuestion  = SplashActivity.requestHandler.getNextQuestionAndUpdateRemainingAndUpdateHighscore()
        currentQuestion = newQuestion
        val currentscore = view.findViewById<TextView>(R.id.text_view_current_score)
        currentscore.text = SplashActivity.requestHandler.getCurrentHighscoreOfGame().toString()

        //TODO handle no new question
        if(newQuestion == null) {
            switchToScoreFragment(this)
            return
        }

        val answersIndices = arrayListOf(0, 1, 2, 3)
        shuffle(answersIndices)
        for (i: Int in 0 until answersIndices.size - 1)
        {
            view.findViewById<Button>(ANSWER_BUTTONS[answersIndices[i]]).text = newQuestion.wrongAnswers[i]
        }
        view.findViewById<Button>(ANSWER_BUTTONS[answersIndices[answersIndices.size - 1]]).text = newQuestion.rightAnswer
        correctAnswerIndex = answersIndices[answersIndices.size - 1]

        view.findViewById<TextView>(R.id.text_view_question_text).text = newQuestion.question
    }

    override fun onClick(view: View?) {
        if (view != null) {
            var answer = -1

            for (i: Int in 0 .. 3 ) {
                if(view.id == ANSWER_BUTTONS[i])
                    answer = i
            }

            if (answer in 0 .. 3) {
                if (answer == correctAnswerIndex) {
                    displayNewQuestion(view.rootView)
                } else {
                    val toastText = getString(R.string.toast_wrong_answer)
                    val toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT)
                    toast.show()

                    val button = view.findViewById<Button>(ANSWER_BUTTONS[answer])
                    val shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.shake_animation)

                    val currentFragment = this

                    shakeAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {}

                        override fun onAnimationEnd(animation: Animation) {
                            switchToScoreFragment(currentFragment)
                        }

                        override fun onAnimationRepeat(animation: Animation) {}
                    })
                    button.startAnimation(shakeAnimation)

                }
            }
        }
    }

    private fun switchToScoreFragment(currentFragment: PlayFragment) {
        val transaction = parentFragmentManager.beginTransaction()
        val scoreFragment = ScoreFragment()

        val arguments = Bundle()
        arguments.putInt("score", SplashActivity.requestHandler.endCurrentGameAndReturnCurrentHighscoreAndUpdateDatabase())

        scoreFragment.arguments = arguments
        transaction.addToBackStack("Play-Score")
        transaction.hide(currentFragment)
        transaction.add(R.id.main_fragment_view, scoreFragment)
        transaction.commit()
    }
}