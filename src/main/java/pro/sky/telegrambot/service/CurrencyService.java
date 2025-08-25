package pro.sky.telegrambot.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.xml.sax.InputSource;

@Service
public class CurrencyService {

  private final RestTemplate restTemplate;

  public CurrencyService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public String getUsdRate() {
    try {
      String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
      String url = "https://www.cbr.ru/scripts/XML_daily.asp?date_req=" + date;

      String xmlResponse = restTemplate.getForObject(url, String.class);

      if (xmlResponse == null || xmlResponse.trim().isEmpty()) {
        return "❌ Пустой ответ от сервера ЦБ РФ";
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setNamespaceAware(true);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

      DocumentBuilder builder = factory.newDocumentBuilder();
      String cleanedXml = xmlResponse.trim().replace("\uFEFF", "");
      Document doc = builder.parse(new InputSource(new StringReader(cleanedXml)));

      NodeList valutes = doc.getElementsByTagName("Valute");
      for (int i = 0; i < valutes.getLength(); i++) {
        Element valute = (Element) valutes.item(i);
        String charCode = valute.getElementsByTagName("CharCode").item(0).getTextContent();

        if ("USD".equals(charCode)) {
          String value = valute.getElementsByTagName("Value").item(0).getTextContent();
          String name = valute.getElementsByTagName("Name").item(0).getTextContent();

          return "💵 Курс доллара ЦБ РФ на сегодня:\n\n" +
              "🇺🇸 " + name + "\n" +
              "💰 " + value + " руб.\n" +
              "📅 " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }
      }

      return "❌ Не удалось найти курс доллара";

    } catch (Exception e) {
      return "❌ Ошибка при получении курса: " + e.getClass().getSimpleName();
    }
  }
}