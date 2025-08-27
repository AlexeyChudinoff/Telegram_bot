package pro.sky.telegrambot;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import pro.sky.telegrambot.service.CurrencyService;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

  @Mock
  private RestTemplate restTemplate;

  @InjectMocks
  private CurrencyService currencyService;

  @Test
  void testGetUsdRateSuccess() {
    // Arrange
    String mockXmlResponse = "<?xml version=\"1.0\" encoding=\"windows-1251\"?>" +
        "<ValCurs Date=\"21.08.2025\" name=\"Foreign Currency Market\">" +
        "<Valute ID=\"R01235\">" +
        "<NumCode>840</NumCode>" +
        "<CharCode>USD</CharCode>" +
        "<Nominal>1</Nominal>" +
        "<Name>Доллар США</Name>" +
        "<Value>75,50</Value>" +
        "</Valute>" +
        "</ValCurs>";

    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenReturn(mockXmlResponse);

    // Act
    String result = currencyService.getUsdRate();

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("Доллар США"));
    assertTrue(result.contains("75,50"));
    assertTrue(result.contains("Курс доллара ЦБ РФ"));
  }

  @Test
  void testGetUsdRateWhenUsdNotFound() {
    // Arrange
    String xmlWithoutUsd = "<?xml version=\"1.0\" encoding=\"windows-1251\"?>" +
        "<ValCurs Date=\"21.08.2025\" name=\"Foreign Currency Market\">" +
        "<Valute ID=\"R01239\">" +
        "<NumCode>978</NumCode>" +
        "<CharCode>EUR</CharCode>" +
        "<Nominal>1</Nominal>" +
        "<Name>Евро</Name>" +
        "<Value>85,00</Value>" +
        "</Valute>" +
        "</ValCurs>";

    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenReturn(xmlWithoutUsd);

    // Act
    String result = currencyService.getUsdRate();

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("Не удалось найти курс доллара"));
  }

  @Test
  void testGetUsdRateWhenEmptyResponse() {
    // Arrange
    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenReturn("");

    // Act
    String result = currencyService.getUsdRate();

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("Пустой ответ от сервера ЦБ РФ"));
  }

  @Test
  void testGetUsdRateWhenException() {
    // Arrange
    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenThrow(new RuntimeException("Connection error"));

    // Act
    String result = currencyService.getUsdRate();

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("Ошибка при получении курса"));
  }
}