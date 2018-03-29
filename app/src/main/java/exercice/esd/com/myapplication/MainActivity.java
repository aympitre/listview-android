package exercice.esd.com.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity
{
    public Socket mSocket;
    public Activity me;
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
        }
    }

}
