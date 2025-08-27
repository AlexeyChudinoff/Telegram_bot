package pro.sky.telegrambot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ApiService {

  @Value("${openweather.api.key}")
  private String apiKey;

  public String getApiKey() {
    if (apiKey == null || apiKey.isEmpty()) {
      throw new IllegalStateException("OpenWeather API key not configured");
    }
    return apiKey;
  }

}