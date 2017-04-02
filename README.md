# ShiftManagement

## Synopsis
This project is a simple Android employee shift application done in Android Studio. It uses a RESTFul service to create 
and list employee shifts performed over a period of time. A shift has a start and end date as well as a start and end pair
of coordinates that are shown in Google Map.

## Motivation
The goal is to showcase the interaction with a RESTFul API service as well as the use of basic device location capabilities. 

## The application
Shift Management features
1. A list of current shifts sorted by date with a thumbnail associated image. Note it's actually a call to a url that randomly generates
pictures. Since images are catche, it shows the same image for all the shifts
2. Pinch down list gesture to update list by calling API service 
3. Detail view of any shift showing the start and end date and the start and end points in a embedded Google Map. 
4. Button to start and stop shifts. Only one shift can be started at any time.
5. Shows two-pane view for tablet-size screens

## Built with
1. Retrofit HTTP client for RESTFul API calls
2. Picasso for image handling and caching. 



