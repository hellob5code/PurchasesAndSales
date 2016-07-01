package com.PopCorp.Purchases.presentation.decorator;

import com.PopCorp.Purchases.data.model.ListItem;
import com.PopCorp.Purchases.data.model.ListItemCategory;

public class ListItemDecorator {

    private String name;
    private boolean header;
    private ListItem item;
    private boolean buyed;
    private ListItemCategory category;

    public ListItemDecorator(String name, boolean header, ListItem item, boolean buyed, ListItemCategory category) {
        this.name = name;
        this.header = header;
        this.item = item;
        this.buyed = buyed;
        this.category = category;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ListItemDecorator)) return false;
        ListItemDecorator decorator = (ListItemDecorator) object;
        boolean result = false;
        if (header && decorator.isHeader()){
            if (category != null && decorator.getCategory() != null) {
                result = category.equals(decorator.getCategory());
            } else if (category == null && decorator.getCategory() == null){
                result = name.equals(decorator.getName());
            }
        } else if (!header && !decorator.isHeader()){
            result = item.equals(decorator.getItem());
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public ListItem getItem() {
        return item;
    }

    public void setItem(ListItem item) {
        this.item = item;
    }

    public boolean isBuyed() {
        return buyed;
    }

    public void setBuyed(boolean buyed) {
        this.buyed = buyed;
    }

    public ListItemCategory getCategory() {
        return category;
    }

    public void setCategory(ListItemCategory category) {
        this.category = category;
    }
}
