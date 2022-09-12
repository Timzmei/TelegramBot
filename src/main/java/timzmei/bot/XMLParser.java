package timzmei.bot;

import org.jdom2.*;

import org.jdom2.filter.ElementFilter;
import org.jdom2.input.*;
import org.jdom2.util.IteratorIterable;

import java.io.IOException;
import java.io.InputStream;


/**
 * Created by sha on 04.09.2016.
 */
public class XMLParser {

    String parse(InputStream x) throws  IOException {

        SAXBuilder parser = new SAXBuilder();
        Document xmlDoc;
        try {
            xmlDoc = parser.build(x);
        } catch (JDOMException e) {
            System.out.println("Ошибка парсера");
            return "Ошибка при разборе данных";
        }
        IteratorIterable<Element> elements = xmlDoc.getDescendants(new ElementFilter("historyRecord"));

//        System.out.println(elements.iterator().next().getChild("UserParameters", elements.iterator().next().getNamespace()).getName());

        StringBuilder sb = new StringBuilder("");


        String Sndr = new String();
        String Rcpn = new String();
        String DestinationAddress = new String();
        String Barcode = new String();
        String ComplexItemName = new String();


        int c = 1;
        while (elements.hasNext()) {
            Element history = (Element) elements.next();
            String country;

            String SndrTemp = history.getChild("UserParameters",history.getNamespace()).getChildText("Sndr",history.getNamespace());
            String RcpnTemp = history.getChild("UserParameters",history.getNamespace()).getChildText("Rcpn",history.getNamespace());
            if (history.getChild("AddressParameters",history.getNamespace()).getChild("DestinationAddress",history.getNamespace()) != null) {
                String DestinationAddressTemp = history.getChild("AddressParameters",history.getNamespace()).getChild("DestinationAddress",history.getNamespace()).getChildText("Description",history.getNamespace());
                if (DestinationAddressTemp.length() > 0) {
                    DestinationAddress = DestinationAddressTemp;
                }
            }
            String BarcodeTemp = history.getChild("ItemParameters",history.getNamespace()).getChildText("Barcode",history.getNamespace());
            if (history.getChild("ItemParameters",history.getNamespace()).getChild("ComplexItemName",history.getNamespace()) != null) {
                String ComplexItemNameTemp = history.getChild("ItemParameters",history.getNamespace()).getChildText("ComplexItemName",history.getNamespace());
                if (ComplexItemNameTemp.length() > 0) {
                    ComplexItemName = ComplexItemNameTemp;
                }
            }

            if (SndrTemp.length() > 0) {
                Sndr = SndrTemp;
            }

            if (RcpnTemp.length() > 0) {
                Rcpn = RcpnTemp;
            }

            if (BarcodeTemp.length() > 0) {
                Barcode = BarcodeTemp;
            }

            String datetime = history.getChild("OperationParameters",history.getNamespace()).getChildText("OperDate",history.getNamespace());
            String nameOperType = history.getChild("OperationParameters",history.getNamespace()).getChild("OperType",history.getNamespace()).getChildText("Name",history.getNamespace());
            String nameOperAttr = history.getChild("OperationParameters",history.getNamespace()).getChild("OperAttr",history.getNamespace()).getChildText("Name",history.getNamespace());

            String countryOperID = history.getChild("AddressParameters",history.getNamespace()).getChild("CountryOper",history.getNamespace()).getChildText("Id",history.getNamespace());
            if (countryOperID.equals("643"))
                country = history.getChild("AddressParameters",history.getNamespace()).getChild("OperationAddress",history.getNamespace()).getChildText("Description",history.getNamespace());
            else country = history.getChild("AddressParameters",history.getNamespace()).getChild("CountryOper",history.getNamespace()).getChildText("NameRU",history.getNamespace());
            sb.append(c + ". " + nameOperType + " " + nameOperAttr + " - " + country+" - " + datetime.substring(0,19).replaceAll("T","-") + "\n");
            c++;
        }

        sb.append("\n\nКод отправления: " + Barcode + "\n");
        sb.append("Вид отправления: " + ComplexItemName + "\n");
        sb.append("Отправитель: " + Sndr + "\n");
        sb.append("Получатель: " + Rcpn + "\n");
        sb.append("Адрес получателя: " + DestinationAddress + "\n");


        if (Barcode.length() == 0) {
            return "Отправление не найдено";
        }

        return sb.toString();
    }
}