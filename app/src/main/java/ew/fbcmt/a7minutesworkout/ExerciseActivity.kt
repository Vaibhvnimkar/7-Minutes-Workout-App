package ew.fbcmt.a7minutesworkout

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import ew.fbcmt.a7minutesworkout.databinding.ActivityExerciseBinding
import ew.fbcmt.a7minutesworkout.databinding.CustomdialogBackConfirmationBinding
import java.util.*
import kotlin.collections.ArrayList

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var binding: ActivityExerciseBinding? = null

    private var restTimer : CountDownTimer? = null
    private var restProgress = 0
    private var restTimerDuration:Long = 1 // This is 1 sec for demo it should be 10 for 7 min workout

    private var TimerExercise : CountDownTimer? = null
    private var ProgressExcercise = 0
    private var exerciseTimerDuration :Long = 1 // This is 1 sec for demo it should be 30 for 7 min workout

    private var exerciseList:ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    private var tts:TextToSpeech? = null
    private var player :MediaPlayer? = null

    private var exerciseAdapter :ExerciseStatusAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarExcercise)
        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        binding?.toolbarExcercise?.setNavigationOnClickListener {
            customDialogForBackButton()
        }


        exerciseList = Constants.defaultExerciseList()

        tts = TextToSpeech(this,this)


       setupRestView()
        setUpExerciseStatusRecyclerView()
    }

    override fun onBackPressed() {
        customDialogForBackButton()
     //   super.onBackPressed()
    }



    private fun customDialogForBackButton(){
        val customDialog = Dialog(this)
       val dialogConfirmationBinding = CustomdialogBackConfirmationBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogConfirmationBinding.root)
        customDialog.setCanceledOnTouchOutside(false)
        dialogConfirmationBinding.btnYes.setOnClickListener {
            this@ExerciseActivity.finish()
            customDialog.dismiss()
        }
        dialogConfirmationBinding.btnNo.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }
    private fun setUpExerciseStatusRecyclerView(){
        binding?.rvExerciseStatus?.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)

        exerciseAdapter = ExerciseStatusAdapter(exerciseList!!)
        binding?.rvExerciseStatus?.adapter = exerciseAdapter
    }

    private fun setupRestView(){

        try{
            val soundRI = Uri.parse("android.resource://ew.fbcmt.a7minutesworkout/"+R.raw.press_start)
            player= MediaPlayer.create(applicationContext, soundRI)
            player?.isLooping = false
            player?.start()
        }catch (e:Exception){
            e.printStackTrace()
        }
        binding?.flRestView?.visibility = View.VISIBLE
        binding?.tvTitle?.visibility =View.VISIBLE
        binding?.tvExerciseName?.visibility =View.INVISIBLE
        binding?.flExcerciseView?.visibility = View.INVISIBLE
        binding?.ivImage?.visibility =View.INVISIBLE
        binding?.NextExercise?.visibility =View.VISIBLE
        binding?.TextNextEx?.visibility =View.VISIBLE


        if(restTimer != null){
            restTimer?.cancel()
            restProgress = 0
    }
        binding?.NextExercise?.text = exerciseList!![currentExercisePosition+1].getName()
    setRestProgressBar()
    }

    private fun setUpExcerciseView(){
        binding?.flRestView?.visibility = View.INVISIBLE
        binding?.tvTitle?.visibility =View.INVISIBLE
        binding?.tvExerciseName?.visibility =View.VISIBLE
        binding?.flExcerciseView?.visibility = View.VISIBLE
        binding?.ivImage?.visibility =View.VISIBLE
        binding?.NextExercise?.visibility =View.INVISIBLE
        binding?.TextNextEx?.visibility =View.INVISIBLE

        if(TimerExercise != null){
            TimerExercise?.cancel()
            ProgressExcercise = 0
        }

        speakOut(exerciseList!![currentExercisePosition].getName())
        binding?.ivImage?.setImageResource(exerciseList!![currentExercisePosition].getImage())
        binding?.tvExerciseName?.text = exerciseList!![currentExercisePosition].getName()

        setExerciseProgressBar()
    }
    private fun setRestProgressBar(){
        binding?.progressBar?.progress = restProgress

        restTimer = object : CountDownTimer(restTimerDuration*1000,1000){
            override fun onTick(millisUntilFinished: Long) {
                restProgress++
                binding?.progressBar?.progress = 10 - restProgress
                binding?.tvTimer?.text = (10 - restProgress).toString()
            }

            override fun onFinish() {

                currentExercisePosition++

                exerciseList!![currentExercisePosition].setIsSelected(true)

                exerciseAdapter!!.notifyDataSetChanged()
                setUpExcerciseView()

            }
        }.start()
    }

    private fun setExerciseProgressBar(){
        binding?.progressBarExcercise?.progress = ProgressExcercise

        TimerExercise= object : CountDownTimer(exerciseTimerDuration*1000,1000){
            override fun onTick(millisUntilFinished: Long) {
                ProgressExcercise++
                binding?.progressBarExcercise?.progress = 30 - ProgressExcercise
                binding?.tvTimerExcercise?.text = (30 - ProgressExcercise).toString()
            }

            override fun onFinish() {





               if(currentExercisePosition<exerciseList?.size!! -1){
                   exerciseList!![currentExercisePosition].setIsSelected(false)
                   exerciseList!![currentExercisePosition].setIsCompleted(true)
                   exerciseAdapter!!.notifyDataSetChanged()
                   setupRestView()
               }else{
                   finish()
                   val intent = Intent(this@ExerciseActivity ,FinishActivity::class.java)
                   startActivity(intent)
               }


            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(restTimer != null){
            restTimer?.cancel()
            restProgress = 0
        }
        if(TimerExercise!=null){
            TimerExercise?.cancel()
            ProgressExcercise = 0
        }

        if(tts!=null){
            tts!!.stop()
            tts!!.shutdown()
        }

        if(player!=null){
            player!!.stop()
        }
        binding = null
    }

    override fun onInit(status: Int) {
        if(status==TextToSpeech.SUCCESS){
            val result = tts!!.setLanguage(Locale.US)

            if(result ==TextToSpeech.LANG_MISSING_DATA || result ==TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS","The Language is not supported !")
            }else{
                Log.e("TTS","Initialization Failed ")
            }
        }
    }

    private fun speakOut(text:String){
        tts!!.speak(text,TextToSpeech.QUEUE_FLUSH,null,"")
    }

}