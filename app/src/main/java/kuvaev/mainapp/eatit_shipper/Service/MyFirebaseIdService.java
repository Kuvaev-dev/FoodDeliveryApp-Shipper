package kuvaev.mainapp.eatit_shipper.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.installations.FirebaseInstallations;

import kuvaev.mainapp.eatit_shipper.Common.Common;
import kuvaev.mainapp.eatit_shipper.Model.Token;

public class MyFirebaseIdService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        onTokenRefresh();
        return null;
    }

    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstallations.getInstance().getToken();
        updateToService(refreshedToken);
    }

    private void updateToService(String refreshedToken) {
        if (Common.currentShipper != null){
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference tokens = database.getReference("Tokens");
            Token token = new Token(refreshedToken , true);  //true becuz this token send from Server Side
            tokens.child(Common.currentShipper.getPhone()).setValue(token);
        }
    }
}
