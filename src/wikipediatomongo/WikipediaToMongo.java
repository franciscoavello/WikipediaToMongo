
package wikipediatomongo;

import com.mongodb.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
    
    // Se agregan las stopwords desde el archivo al programa
    public static ArrayList<String> ingresarStopwords() throws IOException{
        ArrayList<String> stopwords = new ArrayList<String>();
        File archivo = new File ("stopwords.txt");
        FileReader fr = new FileReader (archivo);
        BufferedReader br = new BufferedReader(fr);
        String linea;
        while((linea=br.readLine())!=null){
            linea=linea.replace('á', 'a');
            linea=linea.replace('é', 'e');
            linea=linea.replace('í', 'i');
            linea=linea.replace('ó', 'o');
            linea=linea.replace('ú', 'u');
            stopwords.add(linea);
        }
        fr.close();   
        return stopwords;
    }
    
    // Se eliminan las stopwords de un string
    public static String eliminarStopwords(String palabra, ArrayList<String> stopwords){
        for (int i = 0; i < stopwords.size(); i++) {
            palabra = palabra.replaceAll(" "+stopwords.get(i)+" ", " ");
        }
        return palabra;
    }
    
    // Se realiza el indice invertido
    public static void realizarIndex(Mongo mongo, DB db, DBCollection indiceInvertido,String palabra, String documento){
        DBCursor cursor =indiceInvertido.find();
        if(!cursor.hasNext()){ //Es el primer elemento en la base de datos.
            ArrayList<ClaveValorDatos> clavesValores = new ArrayList<ClaveValorDatos>();
            ClaveValorDatos keyValuePal = new ClaveValorDatos(documento,1);            
            clavesValores.add(keyValuePal);
            IndexInvertido palIndex = new IndexInvertido(palabra, clavesValores);
            indiceInvertido.insert(palIndex.toDBObjectIndexInvertido(clavesValores));
        }
        while(cursor.hasNext()){
            DBObject objetoPresente = cursor.next();
            if(objetoPresente.get("palabra").equals(palabra)){
                boolean estabaDocumento=false; // para tener contol sobre si existen los datos de esa palabra en ese documento
                DBObject finPal = new BasicDBObject("palabra", objetoPresente.get("palabra"));
                indiceInvertido.remove(finPal);
                IndexInvertido ind = new IndexInvertido((BasicDBObject) objetoPresente);
                ArrayList<ClaveValorDatos> clavesValores = new ArrayList<ClaveValorDatos>();
                for(int i=0;i<ind.docFrec.size();i++){                    
                    if(ind.docFrec.get(i).idDocumento.equals(documento)){
                        estabaDocumento=true;
                        ind.docFrec.get(i).frecuencia++;
                    }
                    ClaveValorDatos keyValuePal = new ClaveValorDatos(ind.docFrec.get(i).idDocumento,ind.docFrec.get(i).frecuencia);            
                    clavesValores.add(keyValuePal);
                }
                if(!estabaDocumento){
                    ClaveValorDatos keyValuePalnew = new ClaveValorDatos(documento,1);            
                    clavesValores.add(keyValuePalnew);
                }                
                Collections.sort(clavesValores, Collections.reverseOrder());                
                indiceInvertido.insert(ind.toDBObjectIndexInvertido(clavesValores));
                return;
            }                        
        }        
        ArrayList<ClaveValorDatos> clavesValores = new ArrayList<ClaveValorDatos>();
        ClaveValorDatos keyValuePal = new ClaveValorDatos(documento,1);            
        clavesValores.add(keyValuePal);
        IndexInvertido palIndex = new IndexInvertido(palabra, clavesValores);
        indiceInvertido.insert(palIndex.toDBObjectIndexInvertido(clavesValores));
    }
    
    public static void main(String[] args) throws UnknownHostException, IOException{  
        
        ArrayList<String> stopwordsList = ingresarStopwords();
        // Se agregan conexiones de MongoDB
        Mongo mongo = new Mongo("localhost",27017);
        DB db = mongo.getDB("wikipediaIndex");
        db.dropDatabase();
        DBCollection tablaDatos = db.getCollection("datosWiki");    
        DBCollection indiceInvertido = db.getCollection("indiceInvertido");  
        
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
                    titulo = titulo.replace(' ', '_');
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
                    contenido = eliminarStopwords(contenido, stopwordsList);
                    contenido = contenido.replaceAll("[^\\x00-\\x7F]", "");
                    contenido = contenido.replaceAll("\\\\/", "/");
                    // Se eliminan los saltos de linea
                    contenido = contenido.replaceAll("\\r\\n|\\r|\\n", "");
                    // Se eliminan las palabras con menos de 2 caracteres
                    contenido = contenido.replaceAll("\\b[\\w']{1,2}\\b", "");
                    // Se simplifican los espacios en blanco
                    contenido = contenido.replaceAll("\\s+"," ");
                    // Se elimina el espacio inicial en caso de existir
                    String contenidoTrimeado = contenido.trim().toLowerCase();
                    String[] palabras = contenidoTrimeado.split(" ");
                    if (contenidoTrimeado.length() > 0) {
                        for(int i=0;i < palabras.length; i++){
                            realizarIndex(mongo, db,indiceInvertido,palabras[i], articulo.get(1));
                        }
                        contenidoPrueba.append(contenidoTrimeado + " ");   
                    }
                } 
            }         
        };
        
        BasicDBObject index1 = new BasicDBObject();
        index1.put("idDoc", 1);
        tablaDatos.ensureIndex(index1);
        BasicDBObject index2 = new BasicDBObject();
        index2.put("palabra", 1);
        indiceInvertido.ensureIndex(index2);
        
        try {
            saxParser.parse("prueba.xml", handler);
        } catch (SAXException ex) {
            Logger.getLogger(WikipediaToMongo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WikipediaToMongo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

   
}
