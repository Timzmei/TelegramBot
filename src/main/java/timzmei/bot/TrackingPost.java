package timzmei.bot;

import jakarta.xml.soap.*;
import org.jetbrains.annotations.NotNull;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

public class TrackingPost {
    /*Данный код создает запрос для получения информации о
    конкретном отправлении по Идентификатору отправления (barcode).
    Ответ на запрос выводится на экран в формате xml.
    Пример запроса:
        <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
                       xmlns:oper="http://russianpost.org/operationhistory"
                       xmlns:data="http://russianpost.org/operationhistory/data"
                       xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
           <soap:Header/>
           <soap:Body>
              <oper:getOperationHistory>
                 <data:OperationHistoryRequest>
                    <data:Barcode>RA644000001RU</data:Barcode>
                    <data:MessageType>0</data:MessageType>
                    <data:Language>RUS</data:Language>
                 </data:OperationHistoryRequest>
                 <data:AuthorizationHeader soapenv:mustUnderstand="1">
                    <data:login>myLogin</data:login>
                    <data:password>myPassword</data:password>
                 </data:AuthorizationHeader>
              </oper:getOperationHistory>
           </soap:Body>
        </soap:Envelope>
    */
    private final String url = "https://tracking.russianpost.ru/rtm34";
    private String barCodeMessage;
    
    public TrackingPost(String barCodeMessage) {
        this.barCodeMessage = barCodeMessage;
    }

    public String start() throws SOAPException, TransformerException, IOException {
        //Cоздаем соединение
        SOAPConnectionFactory soapConnFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection connection = soapConnFactory.createConnection();

        SOAPMessage message = createMessage();

        String string = getTracking(connection, message);

        //Закрываем соединение
        return string;
    }

    private String getTracking(SOAPConnection connection, SOAPMessage message) throws SOAPException, TransformerException, IOException {
        //Отправляем запрос и выводим ответ на экран
        SOAPMessage soapResponse = connection.call(message,url);
        Source sourceContent = soapResponse.getSOAPPart().getContent();

//        System.out.println(sourceContent);
        Transformer t= TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.METHOD, "xml");
        t.setOutputProperty(OutputKeys.INDENT, "yes");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);

        t.transform(sourceContent, result);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());


//        StringWriter stringWriter = new StringWriter();
//        StreamResult result2 = new StreamResult(stringWriter);
//        t.transform(sourceContent, result2);
//        String string = stringWriter.toString();
//        System.out.println(string);

        //Закрываем соединение
        connection.close();
        return new XMLParser().parse(bais);
    }

    @NotNull
    private SOAPMessage createMessage() throws SOAPException {
        //Cоздаем сообщение
        MessageFactory messageFactory = MessageFactory.newInstance("SOAP 1.2 Protocol");
        SOAPMessage message = messageFactory.createMessage();

        //Создаем объекты, представляющие различные компоненты сообщения
        SOAPPart soapPart = message.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        SOAPBody body = envelope.getBody();
        envelope.addNamespaceDeclaration("soap","http://www.w3.org/2003/05/soap-envelope");
        envelope.addNamespaceDeclaration("oper","http://russianpost.org/operationhistory");
        envelope.addNamespaceDeclaration("data","http://russianpost.org/operationhistory/data");
        envelope.addNamespaceDeclaration("soapenv","http://schemas.xmlsoap.org/soap/envelope/");
        SOAPElement operElement = body.addChildElement("getOperationHistory", "oper");
        SOAPElement dataElement = operElement.addChildElement("OperationHistoryRequest","data");
        SOAPElement barcode = dataElement.addChildElement("Barcode","data");
        SOAPElement messageType = dataElement.addChildElement("MessageType","data");
        SOAPElement language = dataElement.addChildElement("Language","data");
        SOAPElement dataAuth = operElement.addChildElement("AuthorizationHeader","data");
        SOAPFactory sf = SOAPFactory.newInstance();
        Name must = sf.createName("mustUnderstand","soapenv","http://schemas.xmlsoap.org/soap/envelope/");
        dataAuth.addAttribute(must,"1");
        SOAPElement login = dataAuth.addChildElement("login", "data");
        SOAPElement password = dataAuth.addChildElement("password","data");

        //Заполняем значения
        barcode.addTextNode(barCodeMessage);
        messageType.addTextNode("0");
        language.addTextNode("RUS");
        login.addTextNode("EtfqoAPWjvZrYX");
        password.addTextNode("OwG92vPfIlrr");

        //Сохранение сообщения
        message.saveChanges();
        return message;
    }


}
