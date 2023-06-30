package com.ubergis.postgis;

import java.util.Date;

public class Vector {
    private String vectorid;
    private String vectorTableName;
    private String zipname;
    private String orginfilename;
    private String userid;
    private int orgintype;
    private Date uploadtime;
    private VectorField vectorField;

    public Vector() {

    }
    public Vector(String vectorid, String vectorTableName) {

        this.vectorid = vectorid;
        this.vectorTableName = vectorTableName;
    }

    public int getOrgintype() {
        return orgintype;
    }

    public void setOrgintype(int orgintype) {
        this.orgintype = orgintype;
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

    public String getZipname() {
        return zipname;
    }

    public void setZipname(String zipname) {
        this.zipname = zipname;
    }

    public String getOrginfilename() {
        return orginfilename;
    }

    public void setOrginfilename(String orginfilename) {
        this.orginfilename = orginfilename;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public Date getUploadtime() {
        return uploadtime;
    }

    public void setUploadtime(Date uploadtime) {
        this.uploadtime = uploadtime;
    }

    public VectorField getVectorField() {
        return vectorField;
    }

    public void setVectorField(VectorField vectorField) {
        this.vectorField = vectorField;
    }
}
