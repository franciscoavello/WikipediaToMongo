package wikipediatomongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.util.ArrayList;
import java.util.List;

public class indexInvertido {
    
    String palabra;
    List<List<String>> docFrec;

    public String getPalabra() {
        return palabra;
    }

    public void setPalabra(String palabra) {
        this.palabra = palabra;
    }

    public List<List<String>> getDocFrec() {
        return docFrec;
    }

    public void setDocFrec(List<List<String>> docFrec) {
        this.docFrec = docFrec;
    }
    
    public static void realizarIndex(Mongo mongo, DB db, DBCollection indiceInvertido,String palabra, String documento){
        DBCursor cursor =indiceInvertido.find();
        if(!cursor.hasNext()){
            BasicDBObject palIndex = new BasicDBObject();
            palIndex.put("palabra", palabra);
            BasicDBObject datosIndex = new BasicDBObject();
            datosIndex.put("documento", documento);
            datosIndex.put("frecuencia", 0);
            palIndex.put("datos", datosIndex);
            indiceInvertido.insert(palIndex);
        }
        while(cursor.hasNext()){
            if(cursor.next().get("palabra").equals(palabra)){
                // Palabra ya está en la base de datos        
                return;
            }                        
        }
        BasicDBObject palIndex = new BasicDBObject();
        palIndex.put("palabra", palabra);
        BasicDBObject datosIndex = new BasicDBObject();
        datosIndex.put("documento", documento);
        datosIndex.put("frecuencia", 0);
        palIndex.put("datos", datosIndex);
        indiceInvertido.insert(palIndex);
    }

    
    
}
