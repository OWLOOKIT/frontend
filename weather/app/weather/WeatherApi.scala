package weather

import common.ExecutionContexts
import conf.Configuration
import models.{LocationResponse, CityId, City}
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.WS
import play.api.Play.current
import java.net.URLEncoder

import scala.concurrent.Future

object WeatherApi extends ExecutionContexts {
  lazy val weatherApiKey: String = Configuration.weather.apiKey.getOrElse(
    throw new RuntimeException("Weather API Key not set")
  )

  val weatherCityUrl = "http://api.accuweather.com/currentconditions/v1/"
  val weatherAutoCompleteUrl = "http://api.accuweather.com/locations/v1/cities/autocomplete"

  private def autocompleteUrl(query: String): String =
    s"$weatherAutoCompleteUrl?apikey=$weatherApiKey&q=${URLEncoder.encode(query, "utf-8")}"

  private def cityLookUp(cityId: CityId): String =
    s"$weatherCityUrl${cityId.id}.json?apikey=$weatherApiKey"

  def searchForLocations(query: String) =
    WS.url(autocompleteUrl(query)).get().map({ r =>
      Json.fromJson[Seq[LocationResponse]](r.json).get
    })

  def getWeatherForCityId(cityId: CityId): Future[JsValue] =
    WS.url(cityLookUp(cityId)).get().filter(_.status == 200).map(_.json)
}
