package com.ted.voicerecognition.views

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.ted.voicerecognition.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        private const val AUDIO_RECORDING_PERMISSIONS = 0
    }

    private lateinit var mainActivityBinding: ActivityMainBinding
    private var mSpeechRecognizer: SpeechRecognizer? = null
    private var mIsListening = false
    private var commandList: MutableList<String>? = null
    private var message = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)

        checkForAudioPermissions()
        initCommands()
        createSpeechRecognizer()
        setListeners()

    }

    private fun initCommands() {
        commandList = ArrayList()
        commandList!!.add("hey")
        commandList!!.add("close application")
        commandList!!.add("open application")
    }

    private fun setListeners() {
        mainActivityBinding.imgAudioRecord.setOnClickListener { startListening() }
    }

    private fun startListening() {
        mainActivityBinding.lottieAudioRecord.visibility = View.VISIBLE
        mainActivityBinding.imgAudioRecord.visibility = View.GONE
        mainActivityBinding.lottieAudioRecord.playAnimation()
        if (mIsListening) handleSpeechEnd("") else handleSpeechStart()
    }

    private fun handleSpeechStart() {
        mainActivityBinding.txtInput.text = "Detecting speech..."
        mainActivityBinding.txtInput.visibility = View.VISIBLE
        mIsListening = true;
        mSpeechRecognizer!!.startListening(createSpeechIntent())
    }

    private fun handleSpeechEnd(message: String) {
        mainActivityBinding.txtInput.text = message
        if (!mainActivityBinding.txtInput.isVisible) mainActivityBinding.txtInput.visibility = View.VISIBLE
        mIsListening = false;
        mSpeechRecognizer!!.cancel()
    }

    private fun checkForAudioPermissions() {
        if (checkCallingPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                AUDIO_RECORDING_PERMISSIONS
            )
        }
    }

    private fun createSpeechRecognizer() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        mSpeechRecognizer?.setRecognitionListener(object : RecognitionListener{
            override fun onReadyForSpeech(params: Bundle?) {
            }

            override fun onBeginningOfSpeech() {
            }

            override fun onRmsChanged(rmsdB: Float) {
            }

            override fun onBufferReceived(buffer: ByteArray?) {
            }

            override fun onEndOfSpeech() {
                mainActivityBinding.lottieAudioRecord.cancelAnimation()
                mainActivityBinding.lottieAudioRecord.visibility = View.INVISIBLE
                mainActivityBinding.imgAudioRecord.visibility = View.VISIBLE
                Log.e("TAG", "onEndOfSpeech: here", )
                message = "End of speech!!"
                handleSpeechEnd(message)
            }

            override fun onError(error: Int) {
                mainActivityBinding.lottieAudioRecord.cancelAnimation()
                mainActivityBinding.lottieAudioRecord.visibility = View.INVISIBLE
                mainActivityBinding.imgAudioRecord.visibility = View.VISIBLE
                Log.e("TAG", "onError: here ${error}", )
                if (error != 5) {
                    message = "Error detecting speech!!"
                    handleSpeechEnd(message)
                }
            }

            override fun onResults(results: Bundle?) {
                mainActivityBinding.lottieAudioRecord.cancelAnimation()
                mainActivityBinding.lottieAudioRecord.visibility = View.INVISIBLE
                mainActivityBinding.imgAudioRecord.visibility = View.VISIBLE
                Log.e("TAG", "onResults: here", )
                if (results != null) {
                    val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val command  = matches[0]
                        Log.e("Command", "onResults: $command", )
                        mainActivityBinding.txtInput.text = "Speech detected!!"
                        mainActivityBinding.txtOutput.text = command
                        mainActivityBinding.txtOutput.visibility = View.VISIBLE
                        handleCommand(command)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "error recognizing speech", Toast.LENGTH_SHORT).show()
                }

            }

            override fun onPartialResults(partialResults: Bundle?) {
                mainActivityBinding.lottieAudioRecord.cancelAnimation()
                mainActivityBinding.lottieAudioRecord.visibility = View.INVISIBLE
                mainActivityBinding.imgAudioRecord.visibility = View.VISIBLE
                Log.e("TAG", "onPartialResults: here", )
                if (partialResults != null) {
                    val matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val command  = matches[0]
                        Log.e("Command", "onPartialResults: $command", )
                        mainActivityBinding.txtInput.text = "Partial speech detected!!"
                        mainActivityBinding.txtOutput.text = command
                        mainActivityBinding.txtOutput.visibility = View.VISIBLE
                        handleCommand(command)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "error recognizing speech", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {

            }
        })
    }

    private fun handleCommand(command: String?) {
        if (commandList!!.contains(command)) {
            Toast.makeText(this, "command ($command) valid", Toast.LENGTH_SHORT).show()
            processCommand(command)
        }
    }

    private fun processCommand(command: String?) {
        if (command.equals("close application", true)) {
            finish()
        } else if (command.equals("hey", true)) {
            mainActivityBinding.txtInput.text = "Hey. How are you doing?"
        } else if (command.equals("open application", true)) {
            mainActivityBinding.txtInput.text = "I am yet to learn this!"
        }
        mIsListening = false
        startListening()
    }

    private fun createSpeechIntent() : Intent {
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
        return speechIntent
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AUDIO_RECORDING_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "you can use voice commands", Toast.LENGTH_SHORT).show()
                //createSpeechIntent()
            } else {
                Toast.makeText(this, "please provide the required permissions", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}