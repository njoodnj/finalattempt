package com.example.nkssa.finalattempt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {
    private ListView listView;
    private ArrayList<String> mDeviceList = new ArrayList<String>();
    private BluetoothAdapter mBluetoothAdapter;
    Context ctx = this;
    String ID=null, NAME=null, PASSWORD=null, EMAIL=null, ADDRESS=null, ROLE=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device.getAddress());
                Log.i("BT",device.getAddress());

                listView.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mDeviceList));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                            long id) {

                        String address = ((TextView) view).getText().toString();

                        BackGround b = new BackGround();
                        b.execute(address);
                    }
     ////////////////////////////////////////////////

                    class BackGround extends AsyncTask<String, String, String> {

                        @Override
                        protected String doInBackground(String... params) {
                            String address = params[0];

                            String data="";
                            int tmp;

                            try {
                                URL url = new URL("http://192.168.8.103/ES/bluetooth.php");
                                String urlParams = "mac="+address;

                                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                                httpURLConnection.setDoOutput(true);
                                OutputStream os = httpURLConnection.getOutputStream();
                                os.write(urlParams.getBytes());
                                os.flush();
                                os.close();

                                InputStream is = httpURLConnection.getInputStream();
                                while((tmp=is.read())!=-1){
                                    data+= (char)tmp;
                                }

                                is.close();
                                httpURLConnection.disconnect();

                                return data;
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                                return "Exception: "+e.getMessage();
                            } catch (IOException e) {
                                e.printStackTrace();
                                return "Exception: "+e.getMessage();
                            }
                        }

                        @Override
                        protected void onPostExecute(String s) {

                            try {
                                JSONObject root = new JSONObject(s);
                                JSONObject user_data = root.getJSONObject("user_data");
                                ID = user_data.getString("r_id");
                                NAME = user_data.getString("r_name");
                                PASSWORD = user_data.getString("r_password");
                                EMAIL = user_data.getString("r_email");
                                ADDRESS = user_data.getString("r_address");
                                ROLE= user_data.getString("r_role");
                            } catch (JSONException e) {
                                e.printStackTrace();

                                    Intent i = new Intent(ctx, Auth.class);
                                    i.putExtra("r_id", ID);
                                    i.putExtra("r_name", NAME);
                                    i.putExtra("r_password", PASSWORD);
                                    i.putExtra("r_email", EMAIL);
                                    i.putExtra("r_address", ADDRESS);
                                    startActivity(i);}}
                        }

                });
            }
        }

    };




}