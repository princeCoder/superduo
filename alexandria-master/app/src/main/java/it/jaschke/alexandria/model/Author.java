package it.jaschke.alexandria.model;

import java.io.Serializable;

/**
 * Created by Prinzly Ngotoum on 9/8/15.
 */
public class Author implements Serializable{

    private String id;
    private String name;

    public Author(String id, String name){
        setId(id);
        setName(name);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
