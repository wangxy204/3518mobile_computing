package com.xsh.backend;

import com.google.firebase.database.*;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class MessagePurger extends Thread {
    private static Logger logger = Logger.getLogger(MessagePurger.class.getName());

    private DatabaseReference firebase;
    private int purgeInterval;
    private int purgeLogs;
    private ConcurrentLinkedQueue<String> branches;

    public MessagePurger(DatabaseReference firebase, int purgeInterval, int purgeLogs) {
        this.setDaemon(true);
        this.firebase = firebase;
        this.purgeInterval = purgeInterval;
        this.purgeLogs = purgeLogs;
        branches = new ConcurrentLinkedQueue<String>();
    }

    public void registerBranch(String branchKey) {
        branches.add(branchKey);
    }

    public void run() {
        while(true) {
            try {
                Thread.sleep(purgeInterval);
                Iterator<String> iter = branches.iterator();
                while(iter.hasNext()) {
                    final String branchKey = (String)iter.next();
                    // Query to check whether entries exceed "maxLogs".
                    Query query = firebase.child(branchKey).orderByKey().limitToFirst(purgeLogs);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            // If entries are less than "maxLogs", do nothing.
                            if (snapshot.getChildrenCount() == purgeLogs) {
                                for (DataSnapshot child: snapshot.getChildren()) {
                                    firebase.child(branchKey + "/" + child.getKey()).removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            logger.warning(error.getDetails());
                        }
                    });
                }
            } catch(InterruptedException ie) {
                logger.warning(ie.getMessage());
                break;
            }
        }
    }
}
