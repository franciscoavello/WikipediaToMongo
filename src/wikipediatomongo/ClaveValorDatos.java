package wikipediatomongo;

import java.io.Serializable;
import com.mongodb.*;

public class ClaveValorDatos implements Serializable{
    
    String idDocumento;
    Integer frecuencia;

    public ClaveValorDatos(String idDocumento, Integer frecuencia) {
        this.idDocumento = idDocumento;
        this.frecuencia = frecuencia;
    }
        
    public String getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(String idDocumento) {
        this.idDocumento = idDocumento;
    }

    public Integer getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(Integer frecuencia) {
        this.frecuencia = frecuencia;
    }
    
    public ClaveValorDatos(BasicDBObject dBObjectClaveVal) {
	this.idDocumento = dBObjectClaveVal.getString("documento");
	this.frecuencia = dBObjectClaveVal.getInt("frecuencia");
    }    
    
    public BasicDBObject toDBObjectKeyValues() {
        
        // Creamos una instancia BasicDBObject
        BasicDBObject keyValues = new BasicDBObject();

        keyValues.append("documento", this.getIdDocumento());
        keyValues.append("frecuencia", this.getFrecuencia());
        
        return keyValues;
    }  
    

}
