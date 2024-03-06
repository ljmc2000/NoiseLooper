package ie.delilahsthings.soothingloop;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.util.Scanner;

public class CreditsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        loadCredits();
    }

    private void loadCredits() {
        TextView creditsView = this.findViewById(R.id.sound_credits);
        InputStream creditsFile = getResources().openRawResource(R.raw.credits);
        Scanner creditsScanner = new Scanner(creditsFile).useDelimiter("\\A");
        String credits = creditsScanner.hasNext() ? creditsScanner.next() : "";
        creditsView.setText(credits);
    }
}