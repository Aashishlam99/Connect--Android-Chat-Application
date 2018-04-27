package com.example.aashish.connect;

/* */

public class Friend_req {


    private String request_type;

    public Friend_req() {

    }

    public Friend_req(String request_type) {
        this.request_type = request_type;
    }


    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

}
