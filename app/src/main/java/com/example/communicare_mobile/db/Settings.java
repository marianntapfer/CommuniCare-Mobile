package com.example.communicare_mobile.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Settings {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "gender")
    public boolean gender;

    @ColumnInfo(name = "text_language")
    public String textLanguage;

    @ColumnInfo(name = "speech_language")
    public String speechLanguage;
}
