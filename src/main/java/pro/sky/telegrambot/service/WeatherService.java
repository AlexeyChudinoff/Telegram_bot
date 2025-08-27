package pro.sky.telegrambot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final ApiService apiService;

  public WeatherService(
      RestTemplate restTemplate,
      ObjectMapper objectMapper,
      ApiService apiService) {
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
    this.apiService = apiService;
  }

  public String getTomskWeather() {
    try {
      String apiKey = apiService.getApiKey();
      String url =
          "https://api.openweathermap.org/data/2.5/weather?q=Tomsk&units=metric&lang=ru&appid="
              + apiKey;

      String jsonResponse = restTemplate.getForObject(url, String.class);

      if (jsonResponse == null) {
        return "❌ Не удалось получить данные о погоде";
      }

      JsonNode json = objectMapper.readTree(jsonResponse);

      // Проверяем наличие необходимых полей в ответе
      if (!json.has("main") || !json.has("wind") || !json.has("weather")) {
        return "❌ Неверный формат ответа от сервера погоды";
      }

      JsonNode main = json.get("main");
      JsonNode wind = json.get("wind");
      JsonNode weather = json.get("weather").get(0);

      double temp = main.get("temp").asDouble();
      double feelsLike = main.get("feels_like").asDouble();
      int humidity = main.get("humidity").asInt();
      double windSpeed = wind.get("speed").asDouble();
      int windDeg = wind.has("deg") ? wind.get("deg").asInt() : 0;
      String description = weather.get("description").asText();

      // Формируем направление ветра
      String windDirection = getWindDirection(windDeg);

      return "🌤️ Погода в Томске:\n\n" +
          "🌡️ Температура: " + Math.round(temp) + "°C\n" +
          "💨 Ощущается как: " + Math.round(feelsLike) + "°C\n" +
          "💧 Влажность: " + humidity + "%\n" +
          "🌬️ Ветер: " + windSpeed + " м/с, " + windDirection + "\n" +
          "📝 " + capitalizeFirstLetter(description);

    } catch (Exception e) {
      return "❌ Ошибка при получении погоды: " + e.getClass().getSimpleName();
    }
  }

  // Вспомогательный метод для определения направления ветра
  public String getWindDirection(int degrees) {
    String[] directions = {"⬆️ С", "↗️ СВ", "➡️ В", "↘️ ЮВ", "⬇️ Ю", "↙️ ЮЗ", "⬅️ З", "↖️ СЗ"};
    int index = (int) ((degrees + 22.5) % 360 / 45);
    return directions[index % directions.length];
  }

  // Вспомогательный метод для капитализации первой буквы
  public String capitalizeFirstLetter(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }
    return text.substring(0, 1).toUpperCase() + text.substring(1);
  }
}