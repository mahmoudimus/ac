package it.util;

public class TestProject
{
    private final String id;
    
    private final String key;
    
    public TestProject(String key, String id)
    {
        this.key = key;
        this.id = id;        
    }
    
    public String getKey()
    {
        return key;        
    }
    
    public String getId()
    {
        return id;
    }
}
