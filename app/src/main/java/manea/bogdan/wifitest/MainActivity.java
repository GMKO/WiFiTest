package manea.bogdan.wifitest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    Button clearButton, saveButton;
    TextView mainText;
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            //Ask for location permission (to scan the WiFi)
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0x12345);

            //Ask for storage permission (to write to file)
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            getConnectionList();
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

        if (requestCode == 1
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
        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

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

    //Method used to save the results in a log file at the specified path
    public void saveResults(String result) throws IOException {
        try {
            String filename = "log.txt";
            String path = "/storage/emulated/0/Documents";

            File filePath = new File(path, filename);
            filePath.createNewFile();

            FileOutputStream fileOutputStream = new FileOutputStream(filePath);

            //Each time a new list needs to be written, the previous one is deleted
            fileOutputStream.flush();
            fileOutputStream.write(result.getBytes());
            fileOutputStream.close();

            String showText = String.format("File saved at %s/%s", path, filename);
            Toast.makeText(getApplicationContext(), showText, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.d("SAVE","EXCEPTION",e);
        }
    }

    // Broadcast receiver class calls its receive method when the number of wifi connections changes
    class WifiReceiver extends BroadcastReceiver {

        // This method is called when the number of wifi connections changes
        public void onReceive(Context c, Intent intent) {

            sb = new StringBuilder();
            wifiList = mainWifi.getScanResults();
            sb.append("\nNumber of WiFi connections :"+wifiList.size()+"\n\n");

            //Append results to the list that is going to be displayed to the user
            for(int i = 0; i < wifiList.size(); i++){
                sb.append("Network #" + Integer.valueOf(i+1).toString());
                sb.append("\nSSID: " + (wifiList.get(i)).SSID.toString());
                sb.append("\nBSSID: " + (wifiList.get(i)).BSSID.toString());
                sb.append("\nFrequency: " + (wifiList.get(i)).frequency);
                sb.append("\nLevel of intensity: " + mainWifi.calculateSignalLevel((wifiList.get(i)).level,10));
                sb.append("\nCapabilities: " + (wifiList.get(i)).capabilities + "\n");
                sb.append("\n\n");
            }

            //Display the list
            mainText.setText(sb);

            //Clear the list
            clearButton.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    mainText.setText("\nNumber of WiFi connections :"+wifiList.size()+"\n\n");
                }
            });

            //Save the list locally
            saveButton.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    try {
                        saveResults(sb.toString());
                    } catch (Exception e) {
                        Log.d("EXCEPTION","Something broke: " + e.toString());
                    }
                }
            });
        }
    }
}
