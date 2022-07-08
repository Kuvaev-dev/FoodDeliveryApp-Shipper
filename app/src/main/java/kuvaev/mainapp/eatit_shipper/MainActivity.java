package kuvaev.mainapp.eatit_shipper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            Intent intentSignIn = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intentSignIn);
        });
    }
}