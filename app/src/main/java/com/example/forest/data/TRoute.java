package com.example.forest.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by naresh on 17-Jul-2019.
 */
@Entity(tableName = "route")
public class TRoute {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "routeName")
    public String routeName;

    @ColumnInfo(name = "dtStamp")
    public String dtStamp;
}
