package com.atlassian.plugin.connect.core.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PathBuilderTest
{
    @Test
    public void testStandardPath()
    {
        String path = new PathBuilder().withPathFragment("/test").build();
        assertThat(path, is("/test"));
    }

    @Test
    public void testNoPrefixPath()
    {
        String path = new PathBuilder().withPathFragment("test").build();
        assertThat(path, is("/test"));
    }

    @Test
    public void testVariation1()
    {
        String path = new PathBuilder().withPathFragment("test").withPathFragment("other").build();
        assertThat(path, is("/test/other"));
    }

    @Test
    public void testVariation2()
    {
        String path = new PathBuilder().withPathFragment("/test").withPathFragment("other").build();
        assertThat(path, is("/test/other"));
    }

    @Test
    public void testVariation3()
    {
        String path = new PathBuilder().withPathFragment("/test/").withPathFragment("other").build();
        assertThat(path, is("/test/other"));
    }

    @Test
    public void testVariation4()
    {
        String path = new PathBuilder().withPathFragment("test").withPathFragment("/other").build();
        assertThat(path, is("/test/other"));
    }

    @Test
    public void testVariation5()
    {
        String path = new PathBuilder().withPathFragment("/test").withPathFragment("/other").build();
        assertThat(path, is("/test/other"));
    }

    @Test
    public void testVariation6()
    {
        String path = new PathBuilder().withPathFragment("/test/").withPathFragment("/other").build();
        assertThat(path, is("/test/other"));
    }

    @Test
    public void testVariation7()
    {
        String path = new PathBuilder().withPathFragment("test").withPathFragment("/other/").build();
        assertThat(path, is("/test/other/"));
    }

    @Test
    public void testVariation8()
    {
        String path = new PathBuilder().withPathFragment("/test").withPathFragment("/other/").build();
        assertThat(path, is("/test/other/"));
    }

    @Test
    public void testVariation9()
    {
        String path = new PathBuilder().withPathFragment("/test/").withPathFragment("/other/").build();
        assertThat(path, is("/test/other/"));
    }


    @Test
    public void testBaseUrl1()
    {
        String path = new PathBuilder("http://example.com/").withPathFragment("/other/").build();
        assertThat(path, is("http://example.com/other/"));
    }

    @Test
    public void testBaseUrl2()
    {
        String path = new PathBuilder("http://example.com").withPathFragment("/other/").build();
        assertThat(path, is("http://example.com/other/"));
    }

    @Test
    public void testBaseUrl3()
    {
        String path = new PathBuilder("http://example.com/").withPathFragment("other/").build();
        assertThat(path, is("http://example.com/other/"));
    }

    @Test
    public void testBaseUrl4()
    {
        String path = new PathBuilder("http://example.com").withPathFragment("other/").build();
        assertThat(path, is("http://example.com/other/"));
    }

    @Test
    public void testEmptyPathSegment()
    {
        String path = new PathBuilder("http://example.com").withPathFragment("").build();
        assertThat(path, is("http://example.com/"));
    }

    @Test
    public void testEmptyPathSegment2()
    {
        String path = new PathBuilder("http://example.com/").withPathFragment("").build();
        assertThat(path, is("http://example.com//"));
    }

    @Test
    public void testEmptyPathSegment3()
    {
        String path = new PathBuilder("http://example.com/").withPathFragment("").withPathFragment("").build();
        assertThat(path, is("http://example.com///"));
    }

    @Test
    public void testSeparator()
    {
        String path = new PathBuilder("http://example.com").withPathFragment("/").withPathFragment("/").build();
        assertThat(path, is("http://example.com//"));
    }

    @Test
    public void testNullSegment()
    {
        String path = new PathBuilder("http://example.com").withPathFragment(null).withPathFragment(null).build();
        assertThat(path, is("http://example.com"));
    }

    @Test
    public void testMultiple()
    {
        String path = new PathBuilder("http://example.com")
                .withPathFragment("zero")
                .withPathFragment("one/")
                .withPathFragment("/two/")
                .withPathFragment("/three").build();
        assertThat(path, is("http://example.com/zero/one/two/three"));
    }

    @Test
    public void testNoEncodingIsDone()
    {
        String path = new PathBuilder("http://example.com")
                .withPathFragment(" <>\"#%{}|\\^~[]`")
                .withPathFragment(";/?:@=&/")
                .withPathFragment("/宮崎 駿/")
                .withPathFragment("/$-_.+!*'(),").build();
        assertThat(path, is("http://example.com/ <>\"#%{}|\\^~[]`/;/?:@=&/宮崎 駿/$-_.+!*'(),"));
    }

}
