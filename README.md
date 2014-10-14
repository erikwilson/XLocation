XLocation
=========

Android XPosed plugin for advanced location management. XLocation is a heavily modified version of XPrivacy, found at https://github.com/M66B/XPrivacy/.

### Fix for Android bug (as of 4.4.4):
 * GPS stuck on in battery usage reports after toggling GPS off with an active listener. Changes to location provider through Secure Settings are intercepted and any active location listeners are unregistered before provider change and then re-registered afterwards. Battery stats should accurately report that the GPS is off and no programs should be using GPS when off, as reported through dumpsys. This bug is automatically patched after XLocation is installed.

### Fix for XPrivacy bug (as of 3.4):
 * Proxied location listeners leak and were never able to unregister. Implement 'equals' and 'hashCode' methods for proper unregistration in the Android system.

### New features:
 * Centralized proxy system for all types of location requesting
 * XPrivacy would prevent any location requests which occured using a PendingIntent, functionality added to allow PendingIntents to be proxied via a system service BroadcastReceiver. 
 * An overlay 'OnTouch' system has been created to allow touch gestures to be interpreted as physical GPS movement. Also features touch toggle, compass rotation, and altitude adjustment features, bearing and speed reporting.

### ToDo:
 * Move Strings to XML and add translations
 * Configurability and settings persistence
 * Cleanup logs, documentation
 * Add to Market
 * Port desired modifications back to XPrivacy
