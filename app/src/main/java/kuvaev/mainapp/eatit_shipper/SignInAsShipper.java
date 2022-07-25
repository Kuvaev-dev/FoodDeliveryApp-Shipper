package kuvaev.mainapp.eatit_shipper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Objects;

import info.hoang8f.widget.FButton;
import kuvaev.mainapp.eatit_shipper.Common.Common;
import kuvaev.mainapp.eatit_shipper.Model.Shipper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignInAsShipper extends AppCompatActivity {
    FButton btn_signIn;
    MaterialEditText edtPhone, edtPassword;

    FirebaseDatabase database;
    DatabaseReference shippers;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // add calligraphy
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_signin);

        btn_signIn = findViewById(R.id.btnSignIn);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        edtPassword.setTransformationMethod(new PasswordTransformationMethod());

        // Init firebase
        database = FirebaseDatabase.getInstance();
        shippers = database.getReference(Common.SHIPPER_TABLE);

        btn_signIn.setOnClickListener(v -> login(Objects.requireNonNull(edtPhone.getText()).toString(),
                Objects.requireNonNull(edtPassword.getText()).toString()));
    }

    private void login(String phone, final String password) {
        final ProgressDialog mDialog = new ProgressDialog(SignInAsShipper.this);
        mDialog.setMessage("Please waiting...");
        mDialog.show();

        shippers.child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mDialog.dismiss();
                    Shipper shipper = dataSnapshot.getValue(Shipper.class);
                    assert shipper != null;
                    if (shipper.getPassword().equals(password)) {
                        // Login success
                        startActivity(new Intent(SignInAsShipper.this, HomeActivity.class));
                        Common.currentShipper = shipper;
                        finish();
                    } else if (shipper.getPhone() == null) {
                        Toast.makeText(SignInAsShipper.this, "Your Phone Number is Empty!", Toast.LENGTH_SHORT).show();
                    } else if (shipper.getPassword() == null) {
                        Toast.makeText(SignInAsShipper.this, "Your Password is Empty!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SignInAsShipper.this, "Wrong Password!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignInAsShipper.this, "User not exists in Database!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
