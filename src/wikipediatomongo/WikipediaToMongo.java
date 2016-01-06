
package wikipediatomongo;

import com.mongodb.*;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class WikipediaToMongo {   
          
    public static void main(String[] args) throws UnknownHostException{      
        
        MongoClient mongo = new MongoClient("localhost",27017);
        DB db = mongo.getDB("wikipediaIndex");
        DBCollection tablaDatos = db.getCollection("datosWiki");    
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        
        SAXParser saxParser = null;
        try {
            saxParser = factory.newSAXParser();            
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(WikipediaToMongo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (org.xml.sax.SAXException ex) {
            Logger.getLogger(WikipediaToMongo.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        DefaultHandler handler = new DefaultHandler(){           
            boolean btitle = false;
            boolean bid = false;
            boolean btext = false;
            List<String> articulo = new ArrayList<>();
            int nivel=0;
            StringBuilder contenidoPrueba;
            
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
                nivel++;
                contenidoPrueba = new StringBuilder();
                if(nivel==3){
                    if(qName.equals("title")){                        
                        btitle = true;
                    }
                    if(qName.equals("id")){
                        bid = true;
                    }
                }             
                
                if(nivel==4){
                    if(qName.equals("text")){
                        btext = true;
                    }
                }                
            }
            
            public void endElement(String uri, String localName, String qName) throws SAXException{
                    nivel--;
                    if(qName.equals("text")){
                        btext = false;                        
                        articulo.add(contenidoPrueba.toString());
                    }                    
                    if(articulo.size()==3 && !btext){
                        BasicDBObject documento = new BasicDBObject();
                        documento.put("direccion", articulo.get(0));
                        documento.put("idDoc", articulo.get(1));
                        documento.put("contenido", articulo.get(2));
                        tablaDatos.insert(documento);
                        articulo.clear();
                    }
                    
            }   
            
            public void characters(char ch[], int start, int length) throws SAXException{
                if(btitle){
                    String titulo=new String(ch, start, length);                    
                    articulo.add("https://es.wikipedia.org/wiki/"+titulo);
                    btitle = false;                    
                }
                if(bid){
                    String id=new String(ch, start, length);
                    articulo.add(id);
                    bid = false;
                }
                if(btext){                                     
                    String contenido=new String(ch, start, length);
                    // Se remueven caracter innecesarios en la cadena
                    contenido = contenido.replace("{", " ");
                    contenido = contenido.replace("|", " ");
                    contenido = contenido.replace(":", " ");
                    contenido = contenido.replace("]", " ");
                    contenido = contenido.replace("}", " ");
                    contenido = contenido.replace("/", " ");
                    contenido = contenido.replace("=", " ");
                    contenido = contenido.replace("#", " ");                    
                    contenido = contenido.replace(".", " ");
                    contenido = contenido.replace("-", " ");
                    contenido = contenido.replace(",", " ");
                    contenido = contenido.replace("<", " ");
                    contenido = contenido.replace(">", " ");
                    contenido = contenido.replace("!", " ");
                    contenido = contenido.replace("¡", " ");
                    contenido = contenido.replace("?", " ");
                    contenido = contenido.replace("¿", " ");
                    contenido = contenido.replace("'", " ");
                    contenido = contenido.replace("[", " ");
                    contenido = contenido.replace("(", " ");
                    contenido = contenido.replace(")", " ");
                    contenido = contenido.replace("\"", " ");
                    contenido = contenido.replace("©", " ");
                    contenido = contenido.replace("*", " ");
                    contenido = contenido.replace("á", "a");
                    contenido = contenido.replace("é", "e");
                    contenido = contenido.replace("í", "i");
                    contenido = contenido.replace("ó", "o");
                    contenido = contenido.replace("ú", "u");
                    contenido = contenido.replace("Á", "A");
                    contenido = contenido.replace("É", "E");
                    contenido = contenido.replace("Í", "I");
                    contenido = contenido.replace("Ó", "O");
                    contenido = contenido.replace("Ú", "U");
                    contenido = contenido.replace("à", "a");
                    contenido = contenido.replace("è", "e");
                    contenido = contenido.replace("ì", "i");
                    contenido = contenido.replace("ò", "o");
                    contenido = contenido.replace("ù", "u");
                    contenido = contenido.replace("À", "A");
                    contenido = contenido.replace("È", "E");
                    contenido = contenido.replace("Ì", "I");
                    contenido = contenido.replace("Ò", "O");
                    contenido = contenido.replace("Ù", "U");
                    contenido = contenido.replace("ä", "a");
                    contenido = contenido.replace("ë", "e");
                    contenido = contenido.replace("ï", "i");
                    contenido = contenido.replace("ö", "o");
                    contenido = contenido.replace("ü", "u");
                    contenido = contenido.replace("Ä", "A");
                    contenido = contenido.replace("Ë", "E");
                    contenido = contenido.replace("Ï", "I");
                    contenido = contenido.replace("Ö", "O");
                    contenido = contenido.replace("Ü", "U");
                    contenido = contenido.replace("^", " ");
                    contenido = contenido.replace("_", " ");
                    contenido = contenido.replace("-", " ");
                    contenido = contenido.replace("%", " ");
                    contenido = contenido.replace(";", " ");
                    contenido = contenido.replace("·", " ");                    
                    contenido = contenido.replace("º", " ");                    
                    contenido = contenido.replace("@", " ");                    
                    contenido = contenido.replace("&", " ");
                    contenido = contenido.replace("ñ", "n");
                    contenido = contenido.replace("Ñ", "Ñ");
                    contenido = contenido.replace("–", " ");
                    contenido = contenido.replaceAll("[^\\x00-\\x7F]", "");
                    contenido = contenido.replaceAll("\\\\/", "/");
                    // Se eliminan los saltos de linea
                    contenido = contenido.replaceAll("\\r\\n|\\r|\\n", "");
                    // Se eliminan las palabras con menos de 2 caracteres
                    contenido = contenido.replaceAll("\\b[\\w']{1,2}\\b", "");
                    // Se simplifican los espacios en blanco
                    contenido = contenido.replaceAll("\\s+"," ");
                    // Se elimina el espacio inicial en caso de existir
                    if (contenido.trim().length() > 0) {
                        contenidoPrueba.append(contenido.trim().toLowerCase() + " ");   
                    }
                } 
            }         
        };
        
        try {
            saxParser.parse("C:/Users/PC/Desktop/prueba.xml", handler);
        } catch (SAXException ex) {
            Logger.getLogger(WikipediaToMongo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WikipediaToMongo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

   
}
