package com.westproject.boot3.pocketsprinter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Delug on 16/05/2017.
 */

public class menuActivity extends AppCompatActivity {
    TextView name,email;
    SharedPreferences preferences;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);

      //  email = (TextView) findViewById(R.id.textEmail);
       // name = (TextView) findViewById(R.id.textName) ;
        preferences = this.getSharedPreferences("MYPREFS", Context.MODE_PRIVATE);

        String mName = preferences.getString("name","ERROR getting name");
        String mEmail = preferences.getString("email","ERROR getting email");
        name.setText(mName);
        email.setText(mEmail);

     /*   Button btnGoToCounterActivity = (Button) findViewById(R.id.btnGoToCounter);

        btnGoToCounterActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, CounterActivity.class);
                startActivity(intent);

            }
        });

*/
    }
}
