package com.gorillagang.buspoint.data;


import java.util.List;

public class OverpassApiResponse {
    public float version;
    public String generator;
    public Osm3s osm3s;

    public List<QueryElement> elements;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (QueryElement e : elements) {
            builder.append(e.tags.name);
        }
        return builder.toString();
    }

    public class Osm3s {
        public String timestamp_osm_base;
        public String copyright;
    }

    public class QueryElement {
        public String type;
        public Long id;
        public double lat;
        public double lon;
        public Tags tags;
    }

    public class Tags {
        public String bus;
        public String name;
    }
}