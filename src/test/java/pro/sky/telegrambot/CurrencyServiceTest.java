package pro.sky.telegrambot;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
  void testGetUsdRateDebug() {
    // Arrange
    String mockXmlResponse = """
        <ValCurs Date="21.08.2025" name="Foreign Currency Market">
            <Valute ID="R01235">
                <NumCode>840</NumCode>
                <CharCode>USD</CharCode>
                <Nominal>1</Nominal>
                <Name>Доллар США</Name>
                <Value>75,50</Value>
            </Valute>
        </ValCurs>
        """;

    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenReturn(mockXmlResponse);

    // Act
    String result = currencyService.getUsdRate();

    // Debug output
    System.out.println("=== DEBUG OUTPUT ===");
    System.out.println("Result: " + result);
    System.out.println("Contains 'Доллар США': " + result.contains("Доллар США"));
    System.out.println("Contains '75,50': " + result.contains("75,50"));
    System.out.println("Contains 'Курс доллара': " + result.contains("Курс доллара"));
    System.out.println("====================");

    // Assert
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  void testGetUsdRateSuccess() {
    // Arrange
    String mockXmlResponse = """
        <ValCurs Date="21.08.2025" name="Foreign Currency Market">
            <Valute ID="R01235">
                <NumCode>840</NumCode>
                <CharCode>USD</CharCode>
                <Nominal>1</Nominal>
                <Name>Доллар США</Name>
                <Value>75,50</Value>
            </Valute>
        </ValCurs>
        """;

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
    // Arrange - валидный XML без USD
    String validXmlWithoutUsd = "<?xml version=\"1.0\" encoding=\"windows-1251\"?>" +
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
        .thenReturn(validXmlWithoutUsd);

    // Act
    String result = currencyService.getUsdRate();

    // Отладочный вывод
    System.out.println("=== DEBUG ===");
    System.out.println("Expected to contain: 'Не удалось найти курс доллара'");
    System.out.println("Actual result: '" + result + "'");
    System.out.println("Contains expected: " + result.contains("Не удалось найти курс доллара"));
    System.out.println("=============");

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("Не удалось найти курс доллара"));
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

  @Test
  void testGetUsdRateWhenValidXmlButNoUsd() {
    // Arrange - валидный XML без USD
    String validXmlWithoutUsd = "<?xml version=\"1.0\" encoding=\"windows-1251\"?>" +
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
        .thenReturn(validXmlWithoutUsd);

    // Act
    String result = currencyService.getUsdRate();

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("Не удалось найти курс доллара"));
  }

}