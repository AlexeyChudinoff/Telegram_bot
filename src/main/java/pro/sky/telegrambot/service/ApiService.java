package pro.sky.telegrambot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ApiService {

  private static final Logger logger = LoggerFactory.getLogger(ApiService.class);

  @Value("${openweather.api.key}")
  private String apiKey;

  public String getApiKey() {
    if (apiKey == null || apiKey.trim().isEmpty()) {
      logger.error("OpenWeather API key is not configured");
      throw new IllegalStateException("OpenWeather API key not configured");
    }
    return apiKey;
  }
}