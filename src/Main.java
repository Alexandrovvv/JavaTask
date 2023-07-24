import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class Main {
    public static void main(String[] args) {
        try {
            String result1 = "", result2 = ""; //Результаты 1 и 2 задания
            Scanner reader = new Scanner(System.in);
            DateFormat df = new SimpleDateFormat("yyyy-mm-dd");

            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document documentObj = documentBuilder.parse("AS_ADDR_OBJ.xml"); //парсим AS_ADDR_OBJ.XML
            Document documentHierarchy = documentBuilder.parse("AS_ADM_HIERARCHY.xml"); //парсим AS_ADM_HIERARCHY.XML
            NodeList rootObj = documentObj.getElementsByTagName("OBJECT"); //получаем массив элементов по тэгу OBJECT из AS_ADDR_OBJ.XML
            NodeList rootHierarchy = documentHierarchy.getElementsByTagName("ITEM"); //получаем массив элементов по тэгу ITEM из AS_ADM_HIERARCHY.XML

            System.out.println("Введите дату в формате yyyy-mm-dd");
            String dateString = reader.nextLine();
            if (!dateString.matches("\\d{4}-\\d{2}-\\d{2}"))  {
                System.out.println("Введенная дата не соответствует формату");
                return;
            }

            Date date = df.parse(dateString); //приводим строку к дате
            System.out.println("Введите идентификаторы через запятую");
            String stringIds = reader.nextLine();
            if (!stringIds.matches("^[0-9, ]+$")) {
                System.out.println("Неправильно введены данные");
                return;
            }
            stringIds = stringIds.replace(" ", ""); //убираем пробелы
            String[] ids = stringIds.split(","); //получаем массив идентификаторов из введенной строки

            for (int i = 0; i < rootObj.getLength(); i++) {
                NamedNodeMap node = rootObj.item(i).getAttributes(); //получаем атрибуты

                //Задача 1
                String objectId = node.getNamedItem("OBJECTID").getNodeValue();
                String typename = node.getNamedItem("TYPENAME").getNodeValue();

                if (Arrays.asList(ids).contains(objectId)) {
                    Date startDate = df.parse(node.getNamedItem("STARTDATE").getNodeValue());
                    Date endDate = df.parse(node.getNamedItem("ENDDATE").getNodeValue());

                    //сравниваем введенную дату с STARTDATE и ENDDATE
                    if (date.equals(startDate) || (date.after(startDate) && date.before(endDate))) {
                        String name = node.getNamedItem("NAME").getNodeValue();
                        result1 += objectId + ": " + typename + " " + name + "\n"; //формируем ответ на задание 1
                    }
                }

                //Задача 2
                if (typename.equals("проезд")) {
                    List<String> listParents = new ArrayList<String>();
                    listParents = getParents(objectId, rootHierarchy, listParents); //получаем список родительских OBJECTID
                    Collections.reverse(listParents); //перевернем список, чтобы в начале был самый старший предок

                    result2 += getPath(listParents, rootObj) + "\n"; //формируем ответ на задание 2
                }
            }

            System.out.println("Задача 1");
            System.out.println(result1);
            System.out.println("Задача 2");
            System.out.println(result2);

        } catch (ParserConfigurationException ex) {
            ex.printStackTrace(System.out);
        } catch (SAXException ex) {
            ex.printStackTrace(System.out);
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        } catch (ParseException ex) {
            ex.printStackTrace(System.out);
        }
    }

    //функция получения списка родителей по OBJECTID потомка
    static List<String> getParents(String objectId, NodeList root, List<String> idHierarchy) {
        for (int i = 0; i < root.getLength(); i++) {
            NamedNodeMap node = root.item(i).getAttributes(); //получаем атрибуты
            if (node.getNamedItem("OBJECTID").getNodeValue().equals(objectId) && node.getNamedItem("ISACTIVE").getNodeValue().equals("1")) {
                String parentObjId = node.getNamedItem("PARENTOBJID").getNodeValue();
                if (parentObjId.equals("0")) return idHierarchy;
                idHierarchy.add(parentObjId);
                idHierarchy = getParents(parentObjId, root, idHierarchy);
            }
        }
        return idHierarchy;
    }

    //функция получения полного адреса по списку из OBJECTID родителей
    static String getPath(List<String> parentsList, NodeList root) {
        String path = "";
        for (String parents: parentsList) {
            for (int j = 0; j < root.getLength(); j++) {
                NamedNodeMap node = root.item(j).getAttributes();
                if (parents.equals(node.getNamedItem("OBJECTID").getNodeValue()) && node.getNamedItem("ISACTIVE").getNodeValue().equals("1")) {
                    path += node.getNamedItem("TYPENAME").getNodeValue() + " " + node.getNamedItem("NAME").getNodeValue() + ",";
                    break;
                }
            }
        }
        return path.substring(0, path.length() - 1); //substring - чтоб обрезать последнюю запятую
    }
}