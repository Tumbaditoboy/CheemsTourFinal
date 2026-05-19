package mx.itson.cheemstour.utils
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Extensión global para hacer vibrar el dispositivo en momentos especificos para mejorar la experiencia de usuario.
 * @param duration Milisegundos que durará la vibración.
 */
fun Context.vibratePhone(duration: Long = 1500) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12 (API 31) o superior
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        val vibrator = vibratorManager?.defaultVibrator
        vibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        // Para versiones anteriores a Android 12
        @Suppress("DEPRECATION")
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(duration)
    }
}