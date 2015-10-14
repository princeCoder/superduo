package it.jaschke.alexandria.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Prinzly Ngotoum on 9/8/15.
 */
public class Book implements Serializable {

    private String id;

    private  String title ;

    private  String imageUrl;

    private  String subtitle;

    private  String description;

    private ArrayList<Author> authors;

    private ArrayList<Category> categories;

    public Book(){

    }

    public Book(String id, String title, String imageUrl, String subtitle, String description){
        setId(id);
        setTitle(title);
        setImageUrl(imageUrl);
        setSubtitle(subtitle);
        setDescription(description);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(ArrayList<Author> authors) {
        this.authors = authors;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }
}
