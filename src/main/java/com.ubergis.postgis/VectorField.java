package com.ubergis.postgis;

import java.util.Map;

public class VectorField {
    private String vectorid;
    private String vectorTableName;

    private Map<String, Object> properties;

    public VectorField(String vectorid, String vectorTableName){
        this.vectorid = vectorid;
        this.vectorTableName = vectorTableName;
    }

    public String getVectorid() {
        return vectorid;
    }

    public void setVectorid(String vectorid) {
        this.vectorid = vectorid;
    }

    public String getVectorTableName() {
        return vectorTableName;
    }

    public void setVectorTableName(String vectorTableName) {
        this.vectorTableName = vectorTableName;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
