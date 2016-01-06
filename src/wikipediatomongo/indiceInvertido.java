package wikipediatomongo;

import java.util.ArrayList;
import java.util.List;

public class indiceInvertido {
    String palabra;
    List<String> docFrec = new ArrayList<>();

    public String getPalabra() {
        return palabra;
    }

    public void setPalabra(String palabra) {
        this.palabra = palabra;
    }

    public List<String> getDocFrec() {
        return docFrec;
    }

    public void setDocFrec(List<String> docFrec) {
        this.docFrec = docFrec;
    }
    
    
}
