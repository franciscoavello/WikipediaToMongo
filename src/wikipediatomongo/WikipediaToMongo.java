
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
            Mongo mongo = new Mongo("localhost",27017);
            DB db = mongo.getDB("wikipediaIndex");
            DBCollection tablaDatos = db.getCollection("datosWiki");
            boolean btitle = false;
            boolean bid = false;
            boolean btext = false;
            List<String> articulo = new ArrayList<>();
            int nivel=0;
            
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
                nivel++;
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
                    articulo.add(contenido);
                    btext = false;
                }
                if(articulo.size()==3){
                    BasicDBObject documento = new BasicDBObject();
                    documento.put("direccion", articulo.get(0));
                    documento.put("idDoc", articulo.get(1));
                    documento.put("contenido", articulo.get(2));
                    tablaDatos.insert(documento);
                    articulo.clear();                    
                }
            }
        };
        
        try {
            saxParser.parse("wikipediaTest.xml", handler);
            System.out.println("Hola");
        } catch (SAXException ex) {
            Logger.getLogger(WikipediaToMongo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WikipediaToMongo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

   
}
