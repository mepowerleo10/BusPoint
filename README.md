# BusPoint
An Android app to find the nearest bus stops for public transportation services in Tanzania (Daladala)

## Architecture
The application has several components interacting to provide reliable services:
- MapBox API provides Tiling, Rendering, Matrix and Navigation Services
- Google Places API provides location search services (I hope to build a custom search service in the future)
- OSM Overpass API to search for nearest nodes on the map
- Firebase Authentication handles user accounts and emailing services

## Development Environment
- Run and Tested on Android 8
- Android Studio on Linux

## Limitations
The application was made with the city of Dodoma as the case study. Searching for areas out of bound might yield unresonable and unexpected results.
