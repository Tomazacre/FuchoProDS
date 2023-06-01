package com.example.fuchopro

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

private var auxAnc: Int? = null
private var auxAlt: Int? = null
private var contA: Int = 0
private var contB: Int = 0

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private var sensoA: Sensor? = null
    private var sensoB: Sensor? = null
    private lateinit var sensoMan: SensorManager
    lateinit var viewDibujo: ProcessClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN

        //Barra de título
        supportActionBar?.hide()

        //Pantalla completa
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        //Se empieza a construir el contenido
        val metricDsp = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metricDsp)
        auxAlt = metricDsp.heightPixels
        auxAnc = metricDsp.widthPixels

        viewDibujo = ProcessClass(this)
        setContentView(viewDibujo)

        sensoMan = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if (sensoMan.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            val gravSensors: List<Sensor> = sensoMan.getSensorList(Sensor.TYPE_GRAVITY)
            sensoB =
                gravSensors.firstOrNull { it.vendor.contains("Google LLC") && it.version == 3 }
        }
        if (sensoB == null) {
            //Se establece el Acelerometro.
            sensoB = if (sensoMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                sensoMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            } else {
                //En caso de que no se pueda detectar ningun acelerometro no se podra correr el juego
                null
            }
        }
        sensoA = sensoMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        sensoA?.also {
            sensoMan.registerListener(
                viewDibujo, it, SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensoMan.unregisterListener(viewDibujo)
    }


    override fun onDestroy() {
        super.onDestroy()
        sensoMan.unregisterListener(viewDibujo)
    }

}

class ProcessClass(ctx: Context) : View(ctx), SensorEventListener {
    var auxX = auxAnc!! / 2f
    var auxY = auxAlt!! / 2f
    var auxAcX: Float = 0f
    var auxAcY: Float = 0f
    var auxVelX: Float = 0.0f
    var auxVelY: Float = 0.0f
    var auxRad = 50f

    val map = BitmapFactory.decodeResource(resources, R.drawable.cancha)

    val cnvaRect = Rect(0, 0, auxAnc!!, auxAlt!!)
    val mapRect = RectF(0f, 0f, map.width.toFloat(), map.height.toFloat())

    var painA = Paint()
    var painB = Paint()
    private var auxGrav = FloatArray(3)
    private var auxLinAcc = FloatArray(3)

    init {
        painA.color = Color.WHITE
        painB.color = Color.GRAY
        painB.textSize = 80f
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mapRect.offsetTo(
            cnvaRect.centerX() - mapRect.width() / 2,
            cnvaRect.centerY() - mapRect.height() / 2
        )

        canvas!!.drawBitmap(map, null, cnvaRect, null)
        canvas.drawCircle(auxX, auxY, auxRad, painA)
        canvas.drawText("[ $contA : $contB ]", auxAnc!! / 2f, auxAlt!! / 2f, painB)

        invalidate()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        //TODO("Not yet implemented")
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.

        val auxAlp = 0.8f

        auxGrav[0] = auxAlp * auxGrav[0] + (1 - auxAlp) * event!!.values[0]
        auxGrav[1] = auxAlp * auxGrav[1] + (1 - auxAlp) * event.values[1]
        auxGrav[2] = auxAlp * auxGrav[2] + (1 - auxAlp) * event.values[2]

        auxLinAcc[0] = event.values[0] - auxGrav[0]   //x
        auxLinAcc[1] = event.values[1] - auxGrav[1]   //y
        auxLinAcc[2] = event.values[2] - auxGrav[2]   //z

        processPelota(auxLinAcc[0], auxLinAcc[1] * -1)
    }

    private fun processPelota(auxOrX: Float, auxOrY: Float) {
        //TODO("Not yet implemented")
        auxAcX = auxOrX
        auxAcY = auxOrY
        cambioX()
        cambioY()
        whenGooool()
    }

    fun cambioX() {
        if (auxX < auxAnc!! - auxRad && auxX > 0 + auxRad) {
            auxVelX -= auxAcX * 3f
            auxX += auxVelX
        } else if (auxX >= auxAnc!! - auxRad) {
            auxX = auxAnc!! - auxRad * 2 + 1
            auxVelX -= auxAcX * 3f
            auxX += auxVelX
        } else if (auxX <= 0 + auxRad) {
            auxX = auxRad * 2 + 1
            auxVelX -= auxAcX * 3f
            auxX += auxVelX
        }
    }

    fun cambioY() {
        if (auxY < auxAlt!! - auxRad && auxY > 0 + auxRad) {
            auxVelY -= auxAcY * 3f
            auxY += auxVelY
        } else if (auxY >= auxAlt!! - auxRad) {
            auxY = auxAlt!! - auxRad * 3 + 50f
            auxVelY -= auxAcY * 3f
            auxY += auxVelY
        } else if (auxY <= 0 + auxRad) {
            auxY = auxRad * 3 + 50f
            auxVelY -= auxAcY * 3f
            auxY += auxVelY
        }
    }

    fun whenGooool() {
        if (auxY >= auxAlt!! - auxRad * 2 && (auxX <= auxAnc!! / 2f + 50 && auxX >= auxAnc!! / 2f - 50)) {
            contA++
            auxX = auxAnc!! / 2f
            auxY = auxAlt!! / 2f
        }

        if (auxY <= 0 + auxRad * 2 && (auxX <= auxAnc!! / 2f + 50 && auxX >= auxAnc!! / 2f - 50)) {
            contB++
            auxX = auxAnc!! / 2f
            auxY = auxAlt!! / 2f
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //TODO("Not yet implemented")
    }
}

