/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xsh.backend;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
@SuppressWarnings("serial")
public class MessageProcessServlet extends HttpServlet {

    // Firebase keys shared with client applications
    private static final String IBX = "inbox";
    private static final String CH = "channels";
    private static final String REQLOG = "requestLogger";

    private static Logger localLog = Logger.getLogger(MessageProcessServlet.class.getName());
    private DatabaseReference firebase;

    private String channels;
    private String inbox;

    // If the number of messages or user events in each channel exceeds
    // "purgeLogs", it will be purged.
    private int purgeLogs;
    // Purger is invoked with every "purgeInterval".
    private int purgeInterval;
    private MessagePurger purger;

    private ConcurrentLinkedQueue<LogEntry> logs;


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String credential = config.getInitParameter("credential");
        String databaseUrl = config.getInitParameter("databaseUrl");
        channels = config.getInitParameter("channels");
        purgeLogs = Integer.parseInt(config.getInitParameter("purgeLogs"));
        purgeInterval = Integer.parseInt(config.getInitParameter("purgeInterval"));

        logs = new ConcurrentLinkedQueue<LogEntry>();
        generateUniqueId();
        localLog.info("Credential file : " + credential);
        InputStream credentialStream = config.getServletContext().getResourceAsStream(credential);


        FirebaseOptions options = new FirebaseOptions.Builder()
                .setServiceAccount(credentialStream).setDatabaseUrl(databaseUrl)
                .build();

        FirebaseApp.initializeApp(options);
        firebase = FirebaseDatabase.getInstance().getReference();

        // [START replyToRequest]
    /*
     * Receive a request from an Android client and reply back its inbox ID.
     * Using a transaction ensures that only a single Servlet instance replies
     * to the client. This lets the client knows to which Servlet instance
     * to send consecutive user event logs.
     */
        firebase.child(REQLOG).addChildEventListener(new ChildEventListener() {
            public void onChildAdded(DataSnapshot snapshot, String prevKey) {
                firebase.child(IBX + "/" + snapshot.getValue()).runTransaction(new Transaction.Handler() {
                    public Transaction.Result doTransaction(MutableData currentData) {
                        // The only first Servlet instance will write
                        // its ID to the client inbox.
                        if (currentData.getValue() == null) {
                            currentData.setValue(inbox);
                        }
                        return Transaction.success(currentData);
                    }

                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                    }
                });
                firebase.child(REQLOG).removeValue();
            }
            // [END replyToRequest]

            public void onCancelled(DatabaseError error) {
                localLog.warning(error.getDetails());
            }

            public void onChildChanged(DataSnapshot snapshot, String prevKey) {
            }

            public void onChildMoved(DataSnapshot snapshot, String prevKey) {
            }

            public void onChildRemoved(DataSnapshot snapshot) {
            }
        });

        purger = new MessagePurger(firebase, purgeInterval, purgeLogs);
        String[] channelArray = channels.split(",");
        for (int i = 0; i < channelArray.length; i++) {
            purger.registerBranch(CH + "/" + channelArray[i]);
        }
        initLogger();
        purger.setPriority(Thread.MIN_PRIORITY);
        purger.start();


    }

    private void generateUniqueId() {
        Random rand = new Random();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 16; i++) {
            buf.append(Integer.toString(rand.nextInt(10)));
        }
        inbox = buf.toString();
    }

    @Override
    public void destroy() {
        purger.interrupt();
        firebase.child(IBX + "/" + inbox).removeValue();
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println("Inbox : " + inbox);

        for (Iterator<LogEntry> iter = logs.iterator(); iter.hasNext();) {
            LogEntry entry = (LogEntry) iter.next();
            resp.getWriter()
                    .println(new Date(entry.getTimeLong()).toString() + "(id=" + entry.getTag() + ")" + " : " + entry.getLog());
        }

    }

    private void initLogger() {
        String loggerKey = IBX + "/" + inbox + "/logs";
        purger.registerBranch(loggerKey);
        firebase.child(loggerKey).addChildEventListener(new ChildEventListener() {
            public void onChildAdded(DataSnapshot snapshot, String prevKey) {
                if (snapshot.exists()) {
                    LogEntry entry = snapshot.getValue(LogEntry.class);
                    // not only save in-memory
                    logs.add(entry);


                }
            }

            public void onCancelled(DatabaseError error) {
                localLog.warning(error.getDetails());
            }

            public void onChildChanged(DataSnapshot arg0, String arg1) {
            }

            public void onChildMoved(DataSnapshot arg0, String arg1) {
            }

            public void onChildRemoved(DataSnapshot arg0) {
            }
        });
    }
}
// [END example]
