
/**
 * @author ES-SEBBAR Imane
 * @author EL MAROUNI Majda
 * */

package qengine_program;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.github.jsonldjava.shaded.com.google.common.collect.BiMap;
import com.github.jsonldjava.shaded.com.google.common.collect.HashBiMap;

public class Dictionnaire {
    
    private static    HashMap<Integer, Object> dictio = new HashMap<Integer, Object>();
    
    static BiMap<Object,Integer> resource_key = HashBiMap.create();
    
    private static Dictionnaire dictio_ = null ;
    
    private Dictionnaire() {
        
    }
    
    public static Dictionnaire getDictioInstance() {
        if(dictio_ == null ) {
            dictio_ = new Dictionnaire();
        }
        return dictio_;
    }
    
    /** retourne le dictionnaire construit **/
    public static HashMap<Integer, Object> getDictio() {
        return dictio;
    }
    
    /** La méthode appelée dans MainRDFHandler 
     ** appelle addOne pour chacun des elements d'un statement **/
    public static void addToDictio(Object subject, Object predicate, Object object) {
        addOne(subject);
        addOne(predicate);
        addOne(object);
    }
    
    /** appelée dans addToDictio() 
     **  ajoute chancun des elements d'un statement dans le dictionnaire s'ils n'y existent pas encore **/
    private static void addOne(Object value) {
    	if(resource_key.get(value)==null){
			resource_key.put(value,dictio.size()+1);
			dictio.put(dictio.size()+1, value);
		
        }
    }
     /** Retourne la valeur correspondante à une clé donnée **/
    public static Object getValue(Integer key) {
        return dictio.get(key);
    }
    
     /** Retourne la clé correspondante à une valeur donnée **/
    public static Integer getKey( Object value)
    {
        return resource_key.get(value);
    }
    
    /** Affichage **/
    @SuppressWarnings("rawtypes")
    public void print() {
        System.out.println("Dictionnaire : \n {");
         for (Map.Entry x : dictio.entrySet()) {
              System.out.println("\t "+x.getKey() + "  :\t " + x.getValue());
            }
        System.out.println(" }");

    }
    
    public String toString() {
        String s = "";
        s = s+ "Dictionnaire : \n {";
         for (Map.Entry x : dictio.entrySet()) {
              s=s+"\n\t "+x.getKey() + "  :\t " + x.getValue();
            }
        s = s+"\n }";
        return s;
    }
}