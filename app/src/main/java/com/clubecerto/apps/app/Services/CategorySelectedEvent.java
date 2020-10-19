package com.clubecerto.apps.app.Services;


import com.clubecerto.apps.app.classes.Category;

public class CategorySelectedEvent {
    public Category cats;

    public CategorySelectedEvent(Category cats) {
        this.cats = cats;
    }

}
