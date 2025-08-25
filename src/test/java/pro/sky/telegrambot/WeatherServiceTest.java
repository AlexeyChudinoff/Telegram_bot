package pro.sky.telegrambot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import pro.sky.telegrambot.service.ApiService;
import pro.sky.telegrambot.service.WeatherService;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private ApiService apiService;

  @InjectMocks
  private WeatherService weatherService;

  @Test
  void testGetTomskWeatherSuccess() throws Exception {
    // Arrange
    String mockJsonResponse = """
        {
            "main": {
                "temp": 20.5,
                "feels_like": 19.8,
                "humidity": 65
            },
            "wind": {
                "speed": 3.2,
                "deg": 90
            },
            "weather": [{
                "description": "ясно"
            }]
        }
        """;
    // настройка мока для objectMapper.readTree()
    JsonNode mockJsonNode = new ObjectMapper().readTree(mockJsonResponse);

    // Мокаем ApiService
    when(apiService.getApiKey()).thenReturn("test-api-key");

    // Мокаем REST вызов
    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenReturn(mockJsonResponse);

    // Мокаем ObjectMapper
    when(objectMapper.readTree(mockJsonResponse)).thenReturn(mockJsonNode);

    // Act
    String result = weatherService.getTomskWeather();

    // Debug
    System.out.println("Result: " + result);

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("Томске"));
    assertTrue(result.contains("21")); // Math.round(20.5) = 21
    assertTrue(result.contains("Ясно")); // capitalizeFirstLetter("ясно") = "Ясно"
    assertTrue(result.contains("Ветер"));
  }

  @Test
  void testGetTomskWeatherWhenException() {
    // Arrange
    when(apiService.getApiKey()).thenReturn("test-api-key");
    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenThrow(new RuntimeException("API error"));

    // Act
    String result = weatherService.getTomskWeather();

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("Ошибка при получении погоды"));
  }

  @Test
  void testGetTomskWeatherWhenNullResponse() {
    // Arrange
    when(apiService.getApiKey()).thenReturn("test-api-key");
    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenReturn(null);

    // Act
    String result = weatherService.getTomskWeather();

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("Не удалось получить данные о погоде"));
  }

  @Test
  void testCapitalizeFirstLetter() {
    // Act & Assert
    assertEquals("Ясно", weatherService.capitalizeFirstLetter("ясно"));
    assertEquals("Test", weatherService.capitalizeFirstLetter("test"));
    assertEquals("", weatherService.capitalizeFirstLetter(""));
    assertNull(weatherService.capitalizeFirstLetter(null));
  }

  @Test
  void testGetWindDirection() {
    // Act & Assert
    assertEquals("⬆️ С", weatherService.getWindDirection(0));
    assertEquals("↗️ СВ", weatherService.getWindDirection(45));
    assertEquals("➡️ В", weatherService.getWindDirection(90));
    assertEquals("↘️ ЮВ", weatherService.getWindDirection(135));
    assertEquals("⬇️ Ю", weatherService.getWindDirection(180));
    assertEquals("↙️ ЮЗ", weatherService.getWindDirection(225));
    assertEquals("⬅️ З", weatherService.getWindDirection(270));
    assertEquals("↖️ СЗ", weatherService.getWindDirection(315));
    assertEquals("⬆️ С", weatherService.getWindDirection(360));
  }
}