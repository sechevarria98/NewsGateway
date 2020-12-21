package com.example.gateway;

import java.io.Serializable;

public class News implements Serializable {

    private String id;
    private String name;
    private String category;

    News(String id, String name, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public String getCategory() { return category; }

}
