package manea.bogdan.wifitest;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import manea.bogdan.wifitest.R;

public class MainActivity extends Activity {

    Button clearButton, saveButton;
    TextView mainText;
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    0x12345);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }else {
            //Do something, permission was previously granted; or legacy device
            getConnectionList();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == 0x12345
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Do something with granted permission
            getConnectionList();
        }
    }

    public void getConnectionList(){
        setContentView(R.layout.activity_main);
        mainText = (TextView) findViewById(R.id.mainText);
        mainText.setMovementMethod(new ScrollingMovementMethod());

        clearButton = (Button) findViewById(R.id.clearButton);
        saveButton = (Button) findViewById(R.id.saveButton);

        // Initiate wifi service manager
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // Check if wifi is disabled
        if (mainWifi.isWifiEnabled() == false)
        {
            // If wifi is disabled then enable it
            Toast.makeText(getApplicationContext(), "Starting WiFi...",
                    Toast.LENGTH_LONG).show();

            mainWifi.setWifiEnabled(true);
        }
        // WiFi broadcast receiver
        receiverWifi = new WifiReceiver();

        // Register broadcast receiver
        // Broadcast receiver will automatically call when the number of wifi connections changes
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();
        mainText.setText("Starting Scan...");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Refresh");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        mainWifi.startScan();
        mainText.setText("Starting Scan");
        return super.onMenuItemSelected(featureId, item);
    }

    protected void onPause() {
        unregisterReceiver(receiverWifi);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    // Broadcast receiver class calls its receive method when the number of wifi connections changes

    class WifiReceiver extends BroadcastReceiver {

        // This method is called when the number of wifi connections changes
        public void onReceive(Context c, Intent intent) {

            sb = new StringBuilder();
            wifiList = mainWifi.getScanResults();
            sb.append("\nNumber of WiFi connections :"+wifiList.size()+"\n\n");

            for(int i = 0; i < wifiList.size(); i++){

                sb.append("Network #" + Integer.valueOf(i+1).toString());
                sb.append("\nSSID: " + (wifiList.get(i)).SSID.toString());
                sb.append("\nBSSID: " + (wifiList.get(i)).BSSID.toString());
                sb.append("\nFrequency: " + (wifiList.get(i)).frequency);
                sb.append("\nLevel of intensity: " + mainWifi.calculateSignalLevel((wifiList.get(i)).level,10));
                sb.append("\nCapabilities: " + (wifiList.get(i)).capabilities + "\n");
                sb.append("\n\n");
            }

            mainText.setText(sb);

            clearButton.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    mainText.setText("\nNumber of WiFi connections :"+wifiList.size()+"\n\n");
                }
            });

            saveButton.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){

                }
            });

        }

    }
}