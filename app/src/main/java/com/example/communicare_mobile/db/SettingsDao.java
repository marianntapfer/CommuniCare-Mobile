package com.example.communicare_mobile.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SettingsDao {
    @Query("SELECT * FROM settings")
    List<Settings> getAll();

    @Query("SELECT * FROM settings WHERE id IN (:userIds)")
    List<Settings> loadAllByIds(int[] userIds);

    @Update
    public void updateSettings(Settings... settings);

    @Query("SELECT * FROM settings WHERE id = :id LIMIT 1")
    Settings findById(int id);

    @Insert
    void insertAll(Settings... settings);

    @Delete
    void delete(Settings settings);
}
