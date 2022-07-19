package kuvaev.mainapp.eatit_shipper.Model;

import com.google.android.gms.tasks.Task;
import com.google.firebase.installations.InstallationTokenResult;

public class Token {
    private String token;
    private boolean isServerToken;

    public Token() { }

    public Token(String token, boolean isServerToken) {
        this.token = token;
        this.isServerToken = isServerToken;
    }

    public Token(Task<InstallationTokenResult> token, boolean isServerToken) { }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isServerToken() {
        return isServerToken;
    }

    public void setServerToken(boolean serverToken) {
        isServerToken = serverToken;
    }
}
