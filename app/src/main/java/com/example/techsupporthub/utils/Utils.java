package com.example.techsupporthub.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Utils {

    /**
     * Returns a DatabaseReference for the specified child/table.
     *
     * @param child The child or table name.
     * @return A DatabaseReference for the specified child/table.
     */
    public static DatabaseReference getDatabaseReference(String child) {
        return FirebaseDatabase.getInstance().getReference(child);
    }
}