package id.derysudrajat.blocking

import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import id.derysudrajat.blocking.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val _isTimeNotEmpty = MutableStateFlow(false)
    private val isTimeNotEmpty: StateFlow<Boolean> get() = _isTimeNotEmpty

    private val _resultText = MutableStateFlow("Let's Start Count")
    private val resultText: StateFlow<String> get() = _resultText

    private val _timeText = MutableStateFlow("")
    private val timeText: StateFlow<String> get() = _timeText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            edtTime.doAfterTextChanged { _isTimeNotEmpty.value = !it.isNullOrEmpty() }

            btnRunNonBlock.setOnClickListener {
                val time = edtTime.text.toString().toInt()
                _isTimeNotEmpty.value = false
                _resultText.value = buildString {
                    append("Run without blocking for ")
                    append(time)
                    append(" min")
                }
                countDown(time * 60000L, buildString { append("Run non blocking finished") })
                lifecycleScope.launch {
                    delay(time * 60000L)
                }
            }
            btnRunBlock.setOnClickListener {
                _isTimeNotEmpty.value = false
                val time = edtTime.text.toString().toInt()
                _resultText.value = buildString {
                    append("Run blocking for ")
                    append(time)
                    append(" min")
                }
                countDown(time * 60000L, buildString { append("Run blocking finished") })
                Thread.sleep(time * 60000L)
            }
        }

        lifecycleScope.launch {
            isTimeNotEmpty.collect {
                with(binding) {
                    btnRunBlock.isEnabled = it
                    btnRunNonBlock.isEnabled = it
                }
            }
        }

        lifecycleScope.launch { resultText.collect { binding.tvResult.text = it } }
        lifecycleScope.launch { timeText.collect { binding.tvTimeLeft.text = it } }


    }

    private fun countDown(time: Long, message: String) {
        val timer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeText.value = buildString {
                    append(millisUntilFinished / 1000).append(" Second Left")
                }
            }

            override fun onFinish() {
                _isTimeNotEmpty.value = true
                _timeText.value = ""
                _resultText.value = message
            }
        }
        timer.start()
    }
}