package com.mindmate.backend.service;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;


import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;


import com.google.api.client.util.DateTime;


import com.google.api.client.util.store.FileDataStoreFactory;


import com.google.api.services.calendar.CalendarScopes;


import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleCalendarService {


    private static final String APPLICATION_NAME = "MindMate";


    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();


    private static final String TOKENS_DIRECTORY_PATH = "tokens";


    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);

    // Path to your credentials.json inside resources
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";


    /**
     * Handles OAuth authentication flow
     * - Loads credentials.json
     * - Opens browser for user login (first time only)
     * - Saves token locally for reuse
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws Exception {

        // Load credentials.json from resources folder
        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        // Parse client secrets (client_id, client_secret)
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build OAuth flow (authorization process)
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT,
                        JSON_FACTORY,
                        clientSecrets,
                        SCOPES
                )
                        // Store tokens locally so user doesn't log in every time
                        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))

                        // offline gives a refresh token
                        .setAccessType("offline")
                        .build();

        // Starts a local server to handle OAuth redirect (localhost:8888)
        LocalServerReceiver receiver =
                new LocalServerReceiver.Builder().setPort(8888).build();

        // Opens browser → user logs in → permission granted
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    /**
     * Creates a calendar event
     * @param title Event title
     * @param date  Format: YYYY-MM-DD
     * @param time  Format: HH:mm or HH:mm AM/PM
     */
    public void createEvent(String title, String date, String time) throws Exception {

        // Secure HTTP transport for API communication
        final NetHttpTransport HTTP_TRANSPORT =
                GoogleNetHttpTransport.newTrustedTransport();

        // Build Google Calendar service client
        com.google.api.services.calendar.Calendar service =
                new com.google.api.services.calendar.Calendar.Builder(
                        HTTP_TRANSPORT,
                        JSON_FACTORY,
                        getCredentials(HTTP_TRANSPORT)
                )
                        .setApplicationName(APPLICATION_NAME)
                        .build();

        // Create new event object
        Event event = new Event()
                .setSummary(title)
                .setDescription("Saved automatically by MindMate AI");


       // -Data cleaning section

        // Remove extra spaces from date
        String cleanDate = date.trim(); // Example: "2026-04-12"

        // Extract only time part (ignore AM/PM if present)
        String cleanTime = time.trim().split(" ")[0]; // "10:00 AM" → "10:00"


        //Date and time formatting
        String startString =
                cleanDate + "T" +
                        (cleanTime.contains(":") ? cleanTime : cleanTime + ":00") +
                        ":00Z";

        // Debug log (VERY useful when debugging date issues)
        System.out.println("Final Google Calendar DateTime String: " + startString);


        // Convert string → Google DateTime object
        DateTime startDateTime = new DateTime(startString);

        // Set start time
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("UTC");

        event.setStart(start);


     // Calculate the end time
        DateTime endDateTime =
                new DateTime(startDateTime.getValue() + 3600000);

        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("UTC");

        event.setEnd(end);



        //insert into google calendar

        event = service.events()
                .insert("primary", event)
                .execute();


        System.out.println(" Success! Event created: " + event.getHtmlLink());
    }
}