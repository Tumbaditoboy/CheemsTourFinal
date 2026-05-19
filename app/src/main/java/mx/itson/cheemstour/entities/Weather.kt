package mx.itson.cheemstour.entities

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Weather : Serializable {

    @SerializedName("name")
    var city: String? = null

    @SerializedName("dt")
    var dt: Long = 0

    @SerializedName("timezone")
    var timezone: Int = 0

    @SerializedName("main")
    var main: MainData? = null

    @SerializedName("weather")
    var details: List<WeatherDetails>? = null

    @SerializedName("wind")
    var wind: WindData? = null

    @SerializedName("sys")
    var sys: SysData? = null

    // --- Subclases para la jerarquía del JSON ---

    class MainData : Serializable {
        @SerializedName("temp")
        var temperature: Double = 0.0

        @SerializedName("temp_min")
        var tempMin: Double = 0.0

        @SerializedName("temp_max")
        var tempMax: Double = 0.0

        @SerializedName("humidity")
        var humidity: Int = 0

    }

    class WeatherDetails : Serializable {
        @SerializedName("description")
        var description: String? = null

        @SerializedName("icon")
        var icon: String? = null
    }

    class WindData : Serializable {
        @SerializedName("speed")
        var speed: Double = 0.0

        @SerializedName("deg")
        var degree: Int = 0
    }

    class SysData : Serializable {
        @SerializedName("sunrise")
        var sunrise: Long = 0

        @SerializedName("sunset")
        var sunset: Long = 0
    }
}