package pro.sky.telegrambot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import pro.sky.telegrambot.service.ApiService;
import pro.sky.telegrambot.service.WeatherService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


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

    JsonNode mockJsonNode = new ObjectMapper().readTree(mockJsonResponse);

    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenReturn(mockJsonResponse);
    when(objectMapper.readTree(mockJsonResponse))
        .thenReturn(mockJsonNode);

    // Act
    String result = weatherService.getTomskWeather();

    // Assert - проверяем ключевые части ответа, а не точное совпадение
    assertNotNull(result);
    assertTrue(result.contains("Погода в Томске"));
    assertTrue(result.contains("20.5") || result.contains("20,5")); // учитываем разный формат чисел
    assertTrue(result.contains("ясно") || result.contains("Ясно")); // учитываем capitalize
  }

  @Test
  void testGetTomskWeatherWhenException() {
    // Arrange
    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenThrow(new RuntimeException("API error"));

    // Act
    String result = weatherService.getTomskWeather();

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("Не удалось получить погоду") || result.contains("Ошибка"));
  }

  @Test
  void testCapitalizeFirstLetter() {
    // Arrange
    WeatherService service = new WeatherService(restTemplate, new ObjectMapper(),apiService);

    // Act & Assert
    assertEquals("Ясно", service.capitalizeFirstLetter("ясно"));
    assertEquals("Test", service.capitalizeFirstLetter("test"));
    assertEquals("", service.capitalizeFirstLetter(""));
    assertEquals(null, service.capitalizeFirstLetter(null));
  }

  @Test
  void testGetWindDirection() {
    // Arrange
    WeatherService service = new WeatherService(restTemplate, new ObjectMapper(),apiService);

    // Act & Assert
    assertEquals("⬆️ С", service.getWindDirection(0));
    assertEquals("↗️ СВ", service.getWindDirection(45));
    assertEquals("➡️ В", service.getWindDirection(90));
    assertEquals("↘️ ЮВ", service.getWindDirection(135));
    assertEquals("⬇️ Ю", service.getWindDirection(180));
    assertEquals("↙️ ЮЗ", service.getWindDirection(225));
    assertEquals("⬅️ З", service.getWindDirection(270));
    assertEquals("↖️ СЗ", service.getWindDirection(315));

    // Проверяем граничные значения
    assertEquals("⬆️ С", service.getWindDirection(360));
    assertEquals("↗️ СВ", service.getWindDirection(405));
  }
}