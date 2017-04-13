package com.example.john.simapplication.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.example.john.simapplication.Adapter.CustomAdapter;
import com.example.john.simapplication.Helper.HttpDataHandler;
import com.example.john.simapplication.Models.ChatModel;
import com.example.john.simapplication.Models.SimsimiModel;
import com.example.john.simapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.list_of_message) ListView mListView;
    @Bind(R.id.user_message) EditText mEditText;
    @Bind(R.id.fab) FloatingActionButton btn_send_message;
    List<ChatModel> list_chat = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        btn_send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEditText.getText().toString();
                ChatModel model  = new ChatModel(text, true); //user send message
                list_chat.add(model);

                new SimsimiAPI().execute(list_chat);

                //remove user message
                mEditText.setText("");
            }
        });

        //displaying saved user names
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override

            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user !=null) {
                    getSupportActionBar().setTitle("Welcome, " + user.getDisplayName() + "!");
                }else {}
            }
        };

    }

    private class SimsimiAPI extends AsyncTask<List<ChatModel>, Void, String> {
        String stream = null;
        List<ChatModel> models;
        String text = mEditText.getText().toString();

        @Override
        protected String doInBackground(List<ChatModel>... params) {
            String url = String.format("http://sandbox.api.simsimi.com/request.p?key=%s&lc=een&ft=1.0&text=&s", getString(R.string.simsimi_api), text);
            models = params[0];

            //get result from api
            HttpDataHandler httpDataHandler = new HttpDataHandler();

            stream = httpDataHandler.GetHTTPData(url);

            return stream;
        }

        @Override
        protected void onPostExecute(String s) {
            Gson gson = new Gson();
            SimsimiModel response = gson.fromJson(s, SimsimiModel.class);
            ChatModel chatModel = new ChatModel(response.getResponse(), false); //get response from simsimi
            models.add(chatModel);

            CustomAdapter adapter = new CustomAdapter(models, getApplicationContext());
            mListView.setAdapter(adapter);
        }
    }

    //overflow menu logout method
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        if (id == R.id.about){
            aboutSimsim();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //logout method
    private void logout(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //About Activity method
    private void aboutSimsim(){

        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        if (mAuthStateListener !=null){
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
}
