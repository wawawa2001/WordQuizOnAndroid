package com.example.wordquizapp

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.navigation.ui.AppBarConfiguration
import com.example.wordquizapp.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    val contentPath = "/storage/emulated/0/Android/data/com.example.wordquizapp/data/"
    var engList = mutableListOf<String>() // 英語の単語を格納するリスト
    var jpnList = mutableListOf<String>()
    var sortQuiz = mutableListOf<String>()
    var quizNum = 0
    var startNum = 0
    var endNum = 1
    var quiz = mutableListOf<String>()
    var baseFirstList = mutableListOf<String>()
    var baseSecondList = mutableListOf<String>()
    var Qlist: MutableList<List<String>> = mutableListOf()
    private var correctSound: Int = 0
    private var incorrectSound: Int = 0
    var wrongList = mutableListOf<String>()

    private fun swapList() {
        var tmp = baseFirstList.toMutableList()
        baseFirstList = baseSecondList.toMutableList()
        baseSecondList = tmp.toMutableList()

        var q1 = Qlist[0].toMutableList()
        var q2 = Qlist[1].toMutableList()

        tmp = q1.toMutableList()
        q1 = q2.toMutableList()
        q2 = tmp.toMutableList()

        Qlist = mutableListOf()

        Qlist.add(q1)
        Qlist.add(q2)

    }

    private fun loadCsvFile(fName: String) {
        val file = File(contentPath + fName)

        if (file.exists()) {
            BufferedReader(FileReader(file)).use { br ->
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    val parts = line!!.split(",")
                    if (parts.size >= 2) {
                        engList.add(parts[0]) // 英語の単語を追加
                        jpnList.add(parts[1]) // 日本語の訳を追加
                    }
                }
            }
        }

        baseFirstList = engList.toMutableList()
        baseSecondList = jpnList.toMutableList()

        startNum = 0
        endNum = engList.size - 1
    }

    private fun printQuiz(Qlist: MutableList<List<String>>) {
        findViewById<TextView>(R.id.quizText).text = (quizNum + 1).toString() + ". " + Qlist[0][0]

        quiz = Qlist[1].toMutableList()
        val rndNums = (0..2).toList().shuffled().take(3)
        sortQuiz = mutableListOf(
            Qlist[1][rndNums[0]],
            Qlist[1][rndNums[1]],
            Qlist[1][rndNums[2]]
        )

        findViewById<Button>(R.id.ansBtn1).text = Qlist[1][rndNums[0]]
        findViewById<Button>(R.id.ansBtn2).text = Qlist[1][rndNums[1]]
        findViewById<Button>(R.id.ansBtn3).text = Qlist[1][rndNums[2]]
    }

    private fun pickTrueWord(index: Int): MutableList<List<String>> {
        var tmpFst = baseFirstList.toMutableList()
        tmpFst.removeAt(index)
        var tmpScd = baseSecondList.toMutableList()
        tmpScd.removeAt(index)

        val rndNums = (0..tmpFst.size - 1).toList().shuffled().take(2)
        var f_idx1 = rndNums[0]
        var f_idx2 = rndNums[1]

        println("-----------------------")
        println(index)
        println(baseFirstList.size)
        println(baseSecondList.size)
        println(startNum)
        println(endNum)
        println(listOf(baseFirstList.slice(startNum..endNum)))
        println(listOf(baseSecondList.slice(startNum..endNum)))

        Qlist = mutableListOf(
            listOf(baseFirstList.slice(startNum..endNum)[index], tmpFst[f_idx1], tmpFst[f_idx2]),
            listOf(baseSecondList.slice(startNum..endNum)[index], tmpScd[f_idx1], tmpScd[f_idx2])
        )

        return Qlist
    }

    private fun nextQuiz(rndNums: List<Int>) {
        if (quizNum + 1 < (endNum - startNum)+1) {
            quizNum++
        } else {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Mistake Word...")
            builder.setMessage(wrongList.joinToString("\n"))

            builder.setPositiveButton("OK") { dialog, which ->
                wrongList.clear()
            }

            // ネガティブボタン（キャンセルボタン）の追加
            builder.setNegativeButton("Cancel") { dialog, which ->
                wrongList.clear()
            }

            builder.show()

            quizNum = 0
        }
        println("================")
        println(rndNums)
        printQuiz(pickTrueWord(rndNums[quizNum]))
    }

    private fun judge(num: Int): Boolean {
        if (quiz[0] == sortQuiz[num]) {
            return true
        } else {
            wrongList.add(Qlist[0][0] + " --- " + quiz[0])
            return false
        }
    }

    private fun skip(){
        wrongList.add(Qlist[0][0] + " --- " + quiz[0])
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var soundPool = SoundPool.Builder().setMaxStreams(10).build()
        correctSound = soundPool.load(this, R.raw.correct, 1)
        incorrectSound = soundPool.load(this, R.raw.incorrect, 1)

        val switch = findViewById<Switch>(R.id.chooseEnSwitch)

        loadCsvFile("sample.csv")

        val idxNums = (0..engList.size-1).toList()
        printQuiz(pickTrueWord(idxNums[quizNum]))

        findViewById<Button>(R.id.skipbtn).setOnClickListener{
            skip()
            nextQuiz(idxNums)
        }

        findViewById<Button>(R.id.ansBtn1).setOnClickListener{
            if(judge(0)){
                soundPool.play(correctSound, 1f, 1f, 1, 0, 1f)
            }else{
                soundPool.play(incorrectSound, 1f, 1f, 1, 0, 1f)
            }
            nextQuiz(idxNums)
        }

        findViewById<Button>(R.id.ansBtn2).setOnClickListener{
            if(judge(1)){
                soundPool.play(correctSound, 1f, 1f, 1, 0, 1f)
            }else{
                soundPool.play(incorrectSound, 1f, 1f, 1, 0, 1f)
            }
            nextQuiz(idxNums)
        }

        findViewById<Button>(R.id.ansBtn3).setOnClickListener{
            if(judge(2)){
                soundPool.play(correctSound, 1f, 1f, 1, 0, 1f)
            }else{
                soundPool.play(incorrectSound, 1f, 1f, 1, 0, 1f)
            }
            nextQuiz(idxNums)
        }

        findViewById<ImageButton>(R.id.filBtn).setOnClickListener {
            var sNum = findViewById<TextView>(R.id.startNumText).text
            var eNum = findViewById<TextView>(R.id.endNumText).text

            if (!sNum.isNullOrEmpty() && !eNum.isNullOrEmpty()) {
                if (sNum.toString().toInt() < eNum.toString().toInt()) {
                    startNum = sNum.toString().toInt() -1
                    endNum = eNum.toString().toInt() -1
                }else{
                    startNum = 0
                    endNum = startNum + 1
                }
            } else {
                startNum = 0
                if (engList.size - 1 < 1) {
                    endNum = 1
                } else {
                    endNum = engList.size - 1
                }
            }

            engList = baseFirstList.slice(startNum..endNum).toMutableList()
            jpnList = baseSecondList.slice(startNum..endNum).toMutableList()

            quizNum = 0

            val rndNums = (0..engList.size-1).toList().shuffled().take(endNum)
            printQuiz(pickTrueWord(idxNums[quizNum]))
        }

        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switch.trackTintList = ColorStateList.valueOf(Color.parseColor("#00FF99"))
                swapList()
                printQuiz(pickTrueWord(idxNums[quizNum]))
            } else {
                switch.trackTintList = ColorStateList.valueOf(Color.parseColor("#9f9f9f"))
                swapList()
                printQuiz(pickTrueWord(idxNums[quizNum]))
            }

        }
    }


}