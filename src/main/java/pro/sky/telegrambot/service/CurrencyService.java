package pro.sky.telegrambot.service;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Service
public class CurrencyService {

  private final RestTemplate restTemplate;
  private final DocumentBuilderFactory factory;

  public CurrencyService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
    this.factory = DocumentBuilderFactory.newInstance();

    // Безопасная конфигурация против XXE-атак
    try {
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);
    } catch (Exception e) {
      throw new RuntimeException("Failed to configure XML parser security", e);
    }
  }

  public String getUsdRate() {
    try {
      String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
      String url = "https://www.cbr.ru/scripts/XML_daily.asp?date_req=" + date;

      String xmlResponse = restTemplate.getForObject(url, String.class);

      if (xmlResponse == null || xmlResponse.trim().isEmpty()) {
        return "❌ Пустой ответ от сервера ЦБ РФ";
      }

      // Удаляем DTD ссылку чтобы избежать ошибок парсинга
      String cleanedXml = xmlResponse.replaceAll("<!DOCTYPE[^>]*>", "");

      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(new InputSource(new StringReader(cleanedXml)));

      NodeList valutes = doc.getElementsByTagName("Valute");
      for (int i = 0; i < valutes.getLength(); i++) {
        Element valute = (Element) valutes.item(i);

        Node charCodeNode = valute.getElementsByTagName("CharCode").item(0);
        if (charCodeNode == null) continue;

        String charCode = charCodeNode.getTextContent();

        if ("USD".equals(charCode)) {
          Node valueNode = valute.getElementsByTagName("Value").item(0);
          Node nameNode = valute.getElementsByTagName("Name").item(0);

          if (valueNode == null || nameNode == null) {
            return "❌ Неверный формат ответа от ЦБ РФ";
          }

          String value = valueNode.getTextContent();
          String name = nameNode.getTextContent();

          return "💵 Курс доллара ЦБ РФ на сегодня:\n\n" +
              "🇺🇸 " + name + "\n" +
              "💰 " + value + " руб.\n" +
              "📅 " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }
      }

      return "❌ Не удалось найти курс доллара в ответе ЦБ РФ";

    } catch (Exception e) {
      return "❌ Ошибка при получении курса: " + e.getClass().getSimpleName();
    }
  }
}