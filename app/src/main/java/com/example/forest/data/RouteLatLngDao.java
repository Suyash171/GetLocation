package com.example.forest.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by naresh on 17-Jul-2019.
 */
@Dao
public interface RouteLatLngDao {
     /*  @Query("SELECT * FROM route_lat_lng WHERE routeId = :routeId")
    List<TRouteLatLng> getRouteLatLngOfRoute(long routeId);
*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(TRouteLatLng... routeLatLngs);

    @Insert()
    long insertLatLong(TRouteLatLng tRouteLatLng);

    @Query("SELECT * FROM tRouteLatLong")
    List<TRouteLatLng> getRouteLatLngOfRoute();

}
