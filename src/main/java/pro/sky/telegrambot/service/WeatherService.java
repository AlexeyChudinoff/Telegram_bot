package pro.sky.telegrambot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
  }

  public String getTomskWeather() {
    try {

      String jsonResponse = restTemplate.getForObject(url, String.class);
      JsonNode json = objectMapper.readTree(jsonResponse);

      JsonNode main = json.get("main");
      JsonNode wind = json.get("wind");
      JsonNode weather = json.get("weather").get(0);

      double temp = main.get("temp").asDouble();
      double feelsLike = main.get("feels_like").asDouble();
      int humidity = main.get("humidity").asInt();
      double windSpeed = wind.get("speed").asDouble();
      int windDeg = wind.has("deg") ? wind.get("deg").asInt() : 0;
      String description = weather.get("description").asText();

      return "🌤️ Погода в Томске:\n\n" +
          "🌡️ Температура: " + temp + "°C\n" +
          "💨 Ощущается как: " + feelsLike + "°C\n" +
          "💧 Влажность: " + humidity + "%\n" +
          "📝 " + capitalizeFirstLetter(description);

    } catch (Exception e) {
    }
  }

  public String getWindDirection(int degrees) {
    String[] directions = {"⬆️ С", "↗️ СВ", "➡️ В", "↘️ ЮВ", "⬇️ Ю", "↙️ ЮЗ", "⬅️ З", "↖️ СЗ"};
    int index = (int) ((degrees + 22.5) % 360 / 45);
    return directions[index];
  }

  public String capitalizeFirstLetter(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }
    return text.substring(0, 1).toUpperCase() + text.substring(1);
  }
}