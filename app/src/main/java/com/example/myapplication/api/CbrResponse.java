package com.example.myapplication.api;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import java.util.List;

@Root(name = "ValCurs")
public class CbrResponse {
    
    @ElementList(inline = true, entry = "Valute")
    private List<Valute> valutes;
    
    public List<Valute> getValutes() {
        return valutes;
    }
    
    public void setValutes(List<Valute> valutes) {
        this.valutes = valutes;
    }
    
    // Метод для поиска валюты по ID
    public Valute findValuteById(String id) {
        if (valutes != null) {
            for (Valute valute : valutes) {
                if (id.equals(valute.getId())) {
                    return valute;
                }
            }
        }
        return null;
    }
    
    @Root(name = "Valute")
    public static class Valute {
        
        @Element(name = "ID")
        private String id;
        
        @Element(name = "NumCode")
        private String numCode;
        
        @Element(name = "CharCode")
        private String charCode;
        
        @Element(name = "Nominal")
        private String nominal;
        
        @Element(name = "Name")
        private String name;
        
        @Element(name = "Value")
        private String value;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getNumCode() {
            return numCode;
        }
        
        public void setNumCode(String numCode) {
            this.numCode = numCode;
        }
        
        public String getCharCode() {
            return charCode;
        }
        
        public void setCharCode(String charCode) {
            this.charCode = charCode;
        }
        
        public String getNominal() {
            return nominal;
        }
        
        public void setNominal(String nominal) {
            this.nominal = nominal;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            this.value = value;
        }
        
        public double getValueAsDouble() {
            try {
                return Double.parseDouble(value.replace(",", "."));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
    }
}