package exercice.esd.com.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
{
    public Socket mSocket;
    public Activity me;
    public OkHttpClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        me = this;

        try {
            mSocket = IO.socket("https://esd-b1-messenger-project.glitch.me/");
            mSocket.connect();

            mSocket.on("ajout", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    me.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject data = (JSONObject) args[0];
                            String email;
                            String message;
                            try {
                                email = data.getString("email");
                                message = data.getString("message");
                            } catch (JSONException e) {
                                return;
                            }

                            EditText txtMessage = (EditText) findViewById(R.id.message);

                            Toast.makeText(getApplicationContext(),">>" + email + "/" + message, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

            mSocket.on("liste", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    me.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject data = (JSONObject) args[0];

                            Toast.makeText(getApplicationContext(),">>" + data.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

        } catch (URISyntaxException e) {
            Log.wtf("debug",e.toString());
        }


        Button btSend = (Button) findViewById(R.id.send);
        btSend.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                       sendMessage();
                    }
                }
        );

        ((TextView) findViewById(R.id.intro)).setMovementMethod(new ScrollingMovementMethod());


        client = new OkHttpClient();

        ((Button) findViewById(R.id.get)).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            JSONArray obj = new JSONArray(getMessage());

                            String strMessage = "";
                            for (int i=0; i<obj.length(); i++) {
                                JSONObject message = obj.getJSONObject(i);
                                strMessage += "-" + message.getString("email") + "/" + message.getString("message") + "\n";
                            }

                            ((TextView) findViewById(R.id.intro)).setText(strMessage);


                        } catch (JSONException e) {

                        }
                    }
                }
        );
    }

    public String getMessage()
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Request request = new Request.Builder()
                .url("https://esd-b1-messenger-project.glitch.me/getMessages")
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {

        }
        return "";
    }

    public void sendMessage()
    {
        EditText txtMessage = (EditText) findViewById(R.id.message);
        String message = txtMessage.getText().toString().trim();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message",message);
            jsonObject.put("email","test appli");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(message)) {
            txtMessage.setText("");
            mSocket.emit("new message", jsonObject);
        }else{
            mSocket.emit("connection", jsonObject);
        }
    }

}
