package pro.sky.telegrambot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ApiService {

  @Value("${weather.api.key}")
  private String apiKey;

  public String getApiKey() {
    return apiKey;
  }
}