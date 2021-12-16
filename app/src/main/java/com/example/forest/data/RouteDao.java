package com.example.forest.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Created by naresh on 17-Jul-2019.
 */
@Dao
public interface RouteDao {

    @Query("SELECT * FROM route")
    List<TRoute> getRoutes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(TRoute... tRoutes);

    @Delete
    void delete(TRoute tRoute);
}
