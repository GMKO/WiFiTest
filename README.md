# WiFiTest
WiFi Analyzer app for Android

This application will scan for access points over wifi and will display the following results:
SSID: The access point's name.
BSSID: The MAC address.
Frequency: The frequency on which the access point runs.
Level of intensity: A value between 0 and 10 representing the intensity of the signal.
Capabilities: The security capabilities of an access point.

The app will ask for location and storage permissions in order to be able to scan for networks and save the results in a local log file.
The app will check if the wifi is enabled at startup, if not, it will enable it in order to perform the scan. 
The list with the results gets automatcally updated if a change is detected (e.g. if a new network appears or if an existing network changes intensity)
There are 2 buttons with which the user can interact: 
  - A clear button which will clear the list of all the values. 
    New values will show up when a change is detected during a scan.
  - A save button which will save the current list to a specified path in the system.
    The file is saved at "/storage/emulated/0/Documents" under the name "log.txt".
