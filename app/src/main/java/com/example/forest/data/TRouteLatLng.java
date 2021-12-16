package com.example.forest.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Created by naresh on 17-Jul-2019.
 */
@Entity(tableName = "tRouteLatLong")
public class TRouteLatLng {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "lat")
    public double lat;

    @ColumnInfo(name = "lng")
    public double lng;

    @ColumnInfo(name = "locName")
    public String locName;

    @ColumnInfo(name = "dtStamp")
    public String dtStamp;
}
