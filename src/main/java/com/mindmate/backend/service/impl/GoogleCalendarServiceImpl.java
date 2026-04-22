package com.mindmate.backend.service.impl;

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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleCalendarServiceImpl {

    private static final String APPLICATION_NAME = "MindMate";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final NetHttpTransport HTTP_TRANSPORT ;


    static {
        NetHttpTransport transport;
        try {
            transport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            transport = null;
        }
        HTTP_TRANSPORT = transport;
    }
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws Exception {

        InputStream in = GoogleCalendarServiceImpl.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT,
                        JSON_FACTORY,
                        clientSecrets,
                        SCOPES
                )
                        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                        .setAccessType("offline")
                        .build();

        LocalServerReceiver receiver =
                new LocalServerReceiver.Builder().setPort(8888).build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public String createEvent(String title, String date, String time) throws Exception {


        com.google.api.services.calendar.Calendar service =
                new com.google.api.services.calendar.Calendar.Builder(
                        HTTP_TRANSPORT,
                        JSON_FACTORY,
                        getCredentials(HTTP_TRANSPORT)
                )
                        .setApplicationName(APPLICATION_NAME)
                        .build();

        String cleanDate = date.trim();

        String rawTime = time.trim().toUpperCase();

        LocalTime parsedTime;
        try {
            DateTimeFormatter format12h = DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH);
            parsedTime = LocalTime.parse(rawTime, format12h);
        } catch (Exception e) {
            parsedTime = LocalTime.parse(rawTime, DateTimeFormatter.ofPattern("HH:mm"));
        }

        String cleanTime = parsedTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        String dateTimeString = cleanDate + "T" + cleanTime + ":00";

        DateTime startDateTime = new DateTime(dateTimeString);

        Event event = new Event()
                .setSummary(title)
                .setDescription("Saved automatically by MindMate AI");

        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Colombo");

        event.setStart(start);

        DateTime endDateTime =
                new DateTime(startDateTime.getValue() + 3600000);

        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Colombo");

        event.setEnd(end);

        event = service.events()
                .insert("primary", event)
                .execute();

        String link = event.getHtmlLink();

        System.out.println("EVENT CREATED: " + link);

        return link;
    }
}