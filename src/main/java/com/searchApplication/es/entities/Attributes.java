package com.searchApplication.es.entities;

public class Attributes {

    private String attribute_name;
    private String attribute_value;
    private Long attribute_level;
    private String attribute_parent;

    public String getAttribute_name()
    {
        return attribute_name;
    }

    public void setAttribute_name( String attribute_name )
    {
        this.attribute_name = attribute_name;
    }

    public String getAttribute_value()
    {
        return attribute_value;
    }

    public void setAttribute_value( String attribute_value )
    {
        this.attribute_value = attribute_value;
    }

    public String getAttribute_parent()
    {
        return attribute_parent;
    }

    public void setAttribute_parent( String attribute_parent )
    {
        this.attribute_parent = attribute_parent;
    }

    public Long getAttribute_level()
    {
        return attribute_level;
    }

    public void setAttribute_level( Long attribute_level )
    {
        this.attribute_level = attribute_level;
    }
}
