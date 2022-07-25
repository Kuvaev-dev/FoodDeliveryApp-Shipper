package kuvaev.mainapp.eatit_shipper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

import kuvaev.mainapp.eatit_shipper.Common.Common;
import kuvaev.mainapp.eatit_shipper.Model.Request;
import kuvaev.mainapp.eatit_shipper.Model.Token;
import kuvaev.mainapp.eatit_shipper.ViewHolder.OrderViewHolder;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomeActivity extends AppCompatActivity {
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    Location mLastLocation;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference shipperOrders;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // add calligraphy
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_home);

        // check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CALL_PHONE
            }, Common.REQUEST_CODE);
        } else {
            buildLocationRequest();
            buildLocationCallBack();

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }

        // Init Firebase
        database = FirebaseDatabase.getInstance();
        shipperOrders = database.getReference(Common.ORDER_NEED_SHIP_TABLE);

        // Init View
        recyclerView = findViewById(R.id.recycler_orders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        updateTokenShipper(FirebaseMessaging.getInstance().getToken().toString());
        loadAllOrderNeedShip(Common.currentShipper.getPhone());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadAllOrderNeedShip(String phone) {
        DatabaseReference orderInChildofShipper = shipperOrders.child(phone);
        FirebaseRecyclerOptions<Request> listOrders = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(orderInChildofShipper, Request.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(listOrders) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, final int position, @NonNull final Request model) {
                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderPhone.setText(model.getPhone());
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderDate.setText(Common.getDate(Long.parseLong(Objects.requireNonNull(adapter.getRef(position).getKey()))));
                viewHolder.txtOrderName.setText(model.getName());
                viewHolder.txtOrderPrice.setText(model.getTotal());

                viewHolder.btnShipping.setOnClickListener(v -> {
                    Common.createShippingOrder(adapter.getRef(position).getKey(),
                            Common.currentShipper.getPhone(),
                            mLastLocation);
                    Common.currentRequest = model;
                    Common.currentKey = adapter.getRef(position).getKey();

                    startActivity(new Intent(HomeActivity.this, TrackingOrder.class));
                });
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_view_layout, parent, false);
                return new OrderViewHolder(itemView);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void updateTokenShipper(String token) {
        DatabaseReference tokens = database.getReference("Tokens");
        Token data = new Token(token, false);
        tokens.child(Common.currentShipper.getPhone()).setValue(data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllOrderNeedShip(Common.currentShipper.getPhone());
    }

    @Override
    protected void onStop() {
        if (adapter !=null)
            adapter.stopListening();
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Common.REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buildLocationRequest();
                    buildLocationCallBack();

                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                } else {
                    Toast.makeText(this, "You Should Assign Permission!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@androidx.annotation.NonNull LocationResult locationResult) {
                mLastLocation = locationResult.getLastLocation();
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
    }
}
