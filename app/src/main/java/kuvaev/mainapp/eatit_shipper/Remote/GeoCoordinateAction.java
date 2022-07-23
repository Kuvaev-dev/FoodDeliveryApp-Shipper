package kuvaev.mainapp.eatit_shipper.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeoCoordinateAction {
    @GET("maps/api/geocode/json?key=yourkey&sensor=true&language=en")
    Call<String> getGeoCode(@Query("address") String address);

    @GET("maps/api/directions/json?key=yourkey&sensor=true&language=en&mode=driving")
    Call<String> getDirections(@Query("origin") String origin, @Query("destination") String destination);
}
