XPrivacy
========

The ultimate, yet easy to use, privacy manager for Android

<img src="http://www.xprivacy.eu/open-source-rookie-of-the-year-resized.png" width="315" height="333" alt="Open source Rookie of the year" />
([publication](http://www.blackducksoftware.com/news/releases/black-duck-announces-open-source-rookies-year-winners))

Index
-----

* [Description](#description)
* [Features](#features)
* [Screenshots](#screenshots)
* [Restrictions](#restrictions)
* [Limitations](#limitations)
* [Compatibility](#compatibility)
* [Installation](#installation)
* [Upgrading](#upgrading)
* [Usage](#usage)
* [Permissions](#permissions)
* [Frequently Asked Questions (FAQ)](#frequently-asked-questions-faq)
* [Support](#support)
* [Changelog](https://github.com/M66B/XPrivacy/blob/master/CHANGELOG.md)
* [Similar Solutions](#similar-solutions)
* [In The Media](#in-the-media)
* [Contributing](#contributing)
* [License](#license)

Description
-----------

XPrivacy can prevent applications from leaking privacy-sensitive data
by restricting the categories of data an application can access.
XPrivacy feeds applications fake data or no data at all.
It can restrict several data categories, such as *contacts* or *location*.
For example, if you restrict an application's access to contacts,
that application will receive an empty contacts list (don't try this with the contacts application itself without a backup).
Similarly, restricting an application's access to your location will send a fake location to that application.

XPrivacy doesn't revoke or block permissions from an application,
so most applications will continue to work as before and won't force close (crash).
There are two exceptions: access to the internet and to external storage (typically an SD-card)
are restricted by denying access (revoking permissions).
There is no other way to restrict such access
because Android delegates handling these permissions to the underlying Linux network/file system.
XPrivacy can fake an offline (internet) and unmounted (storage) state,
but some applications still try to access the internet and storage,
potentially resulting in crashes or error messages.
If restricting a category of data for an application causes functional limitations,
XPrivacy can once again allow access to the data category to solve the issue.
There is a convenient on/off toggle switch for all restrictions for each application.

By default, all newly installed applications cannot access any data category,
which prevents a new application from leaking sensitive data right after installing it.
Shortly after installing a new application,
XPrivacy will ask which data categories you want the new application to have access to.
XPrivacy comes with an application browser
that allows you to quickly enable or disable applications' access to any data category.
You can edit all of an application's data categories.

To help you identify potential data leaks,
XPrivacy monitors all applications' attempts to access sensitive data.
XPrivacy displays an orange warning triangle icon when an application has attempted to access data.
If an application has requested Android permissions to access data, XPrivacy displays a green key icon.
XPrivacy also displays an internet icon if an application has internet access,
which clarifies that the application poses a risk of sharing data with an external server.

XPrivacy is built using the [Xposed framework](http://forum.xda-developers.com/xposed/xposed-installer-versions-changelog-t2714053),
which it uses to tap into a vast number of carefully selected Android functions.
Depending on the function, XPrivacy skips execution of the original function
(for example, when an application tries to set a proximity alert)
or alters the result of the original function (for example, to return an empty message list).

XPrivacy has been tested with Android version 4.0.3 - 6.0.1 (Ice Cream Sandwich, Jelly Bean, KitKat, Lollipop, Marshmallow),
and is reported to work with most Android variants, including stock ROMs.
Root access is needed to install the Xposed framework.


**XPrivacy was a lot of work, so please support this project.**

If you want to donate, see [here](http://www.xprivacy.eu/) for all options.

**Use XPrivacy entirely at your own risk.**

Features
--------

* Simple to use
* No need to patch anything (no source, no [smali](https://github.com/JesusFreke/smali) or anything else)
* For any stock variant of Android version 4.0.3 - 6.0.1 (ICS, Jelly Bean, KitKat, Lollipop, Marshmallow)
* Newly installed applications are restricted by default
* Displays data actually used by an application
* Option to restrict on demand
* Free and open source
* Free from advertisements

Screenshots
-----------

<img src="screenshots/categories.png" width="232" height="371" hspace="4"/>
<img src="screenshots/applications.png" width="232" height="371" hspace="4"/>
<img src="screenshots/application.png" width="232" height="371" hspace="4"/>
<img src="screenshots/expert.png" width="232" height="371" hspace="4"/>
<img src="screenshots/expert-2.png" width="232" height="371" hspace="4"/>
<img src="screenshots/help.png" width="232" height="371" hspace="4"/>
<img src="screenshots/settings.png" width="232" height="371" hspace="4"/>
<img src="screenshots/settings-2.png" width="232" height="371" hspace="4"/>
<img src="screenshots/settings-3.png" width="232" height="371" hspace="4"/>
<img src="screenshots/usagedata.png" width="232" height="371" hspace="4"/>
<img src="screenshots/menu.png" width="232" height="371" hspace="4"/>
<img src="screenshots/sort.png" width="232" height="371" hspace="4"/>
<img src="screenshots/filter.png" width="232" height="371" hspace="4"/>
<img src="screenshots/template.png" width="232" height="371" hspace="4"/>
<img src="screenshots/toggle.png" width="232" height="371" hspace="4"/>
<img src="screenshots/on-demand-dialogue.png" width="232" height="371" hspace="4"/>
<img src="screenshots/whitelist.png" width="232" height="371" hspace="4"/>
<img src="screenshots/about.png" width="232" height="371" hspace="4"/>

Restrictions
------------

For easy usage, data is restricted by category:

<a name="accounts"></a>
* Accounts
	* return an empty account list
	* return an empty account type list
	* return fake account info
	* return empty authorization tokens
	* return an empty list of synchronizations
<a name="browser"></a>
* Browser
	* return an empty bookmark list
	* return an empty download list
	* return empty search history
<a name="calendar"></a>
* Calendar
	* return an empty calendar
<a name="calling"></a>
* Calling
	* prevent calls from being placed
	* prevent SIP calls from being placed
	* prevent SMS messages from being sent
	* prevent MMS messages from being sent
	* prevent data messages from being sent
	* return an empty call log
<a name="clipboard"></a>
* Clipboard
	* prevent paste from clipboard (both manual and from an application)
<a name="contacts"></a>
* Contacts
	* return an empty contact list
		* content://com.android.contacts
		* content://com.android.contacts/contacts
		* content://com.android.contacts/data
		* content://com.android.contacts/phone_lookup
		* content://com.android.contacts/profile
		* SIM card
<a name="dictionary"></a>
* Dictionary
	* return an empty user dictionary
<a name="email"></a>
* E-mail
	* return an empty list of accounts, e-mails, etc (standard)
	* return an empty list of accounts, e-mails, etc (Gmail)
<a name="identification"></a>
* Identification
	* return a fake Android ID
	* return a fake device serial number
	* return a fake host name
	* return a fake Google services framework ID
	* return file not found for folder [/proc](http://linux.die.net/man/5/proc)
	* return a fake Google advertising ID
	* return a fake system property CID (Card Identification Register = SD-card serial number)
	* return file not found for /sys/block/.../cid
	* return file not found for /sys/class/.../cid
	* return a fake input device descriptor
	* return a fake USB ID/name/number
	* return a fake Cast device ID / IP address
<a name="internet"></a>
* Internet
	* revoke permission to internet access
	* revoke permission to internet administration
	* revoke permission to internet bandwidth statistics/administration
	* revoke permission to [VPN](http://en.wikipedia.org/wiki/Vpn) services
	* revoke permission to [Mesh networking](http://en.wikipedia.org/wiki/Mesh_networking) services
	* return fake extra info
	* return fake disconnected state
	* return fake supplicant disconnected state
<a name="IPC"></a>
* IPC
	* Binder
	* Reflection
<a name="location"></a>
* Location
	* return a random or set location (also for Google Play services)
	* return empty cell location
	* return an empty list of (neighboring) cell info
	* prevents geofences from being set (also for Google Play services)
	* prevents proximity alerts from being set
	* prevents sending NMEA data to an application
	* prevent phone state from being sent to an application
		* Cell info changed
		* Cell location changed
	* prevent sending extra commands (aGPS data)
	* return an empty list of Wi-Fi scan results
	* prevent [activity recognition](http://developer.android.com/training/location/activity-recognition.html)
<a name="media"></a>
* Media
	* prevent recording audio
	* prevent taking pictures
	* prevent recording video
	* you will be notified if an application tries to perform any of these actions
<a name="messages"></a>
* Messages
	* return an empty SMS/MMS message list
	* return an empty list of SMS messages stored on the SIM (ICC SMS)
	* return an empty list of voicemail messages
<a name="network"></a>
* Network
	* return fake IP's
	* return fake MAC's (network, Wi-Fi, bluetooth)
	* return fake BSSID/SSID
	* return an empty list of Wi-Fi scan results
	* return an empty list of configured Wi-Fi networks
	* return an empty list of bluetooth adapters/devices
<a name="nfc"></a>
* NFC
	* prevent receiving NFC adapter state changes
	* prevent receiving NDEF discovered
	* prevent receiving TAG discovered
	* prevent receiving TECH discovered
<a name="notifications"></a>
* Notifications
	* prevent applications from receiving [statusbar notifications](https://developer.android.com/reference/android/service/notification/NotificationListenerService.html) (Android 4.3+)
	* prevent [C2DM](https://developers.google.com/android/c2dm/) messages
<a name="overlay"></a>
* Overlay
	* prevent draw over / on top
<a name="phone"></a>
* Phone
	* return a fake own/in/outgoing/voicemail number
	* return a fake subscriber ID (IMSI for a GSM phone)
	* return a fake phone device ID (IMEI): 000000000000000
	* return a fake phone type: GSM (matching IMEI)
	* return a fake network type: unknown
	* return an empty ISIM/ISIM domain
	* return an empty IMPI/IMPU
	* return a fake MSISDN
	* return fake mobile network info
		* Country: XX
		* Operator: 00101 (test network)
		* Operator name: fake
	* return fake SIM info
		* Country: XX
		* Operator: 00101
		* Operator name: fake
		* Serial number (ICCID): fake
	* return empty [APN](http://en.wikipedia.org/wiki/Access_Point_Name) list
	* return no currently used APN
	* prevent phone state from being sent to an application
		* Call forwarding indication
		* Call state changed (ringing, off-hook)
		* Mobile data connection state change / being used
		* Message waiting indication
		* Service state changed (service/no service)
		* Signal level changed
	* return an empty group identifier level 1
<a name="sensors"></a>
* Sensors
	* return an empty default sensor
	* return an empty list of sensors
	* restrict individual sensors:
		* acceleration
		* gravity
		* heartrate
		* humidity
		* light
		* magnetic
		* motion
		* orientation
		* pressure
		* proximity
		* rotation
		* step
		* temperature
<a name="shell"></a>
* Shell
	* return I/O exception for Linux shell
	* return I/O exception for Superuser shell
	* return unsatisfied link error for load/loadLibrary
<a name="storage"></a>
* Storage
	* revoke permission to the [media storage](http://www.doubleencore.com/2014/03/android-external-storage/)
	* revoke permission to the external storage (SD-card)
	* revoke permission to [MTP](http://en.wikipedia.org/wiki/Media_Transfer_Protocol)
	* return fake unmounted state
	* prevent access to provided assets (media, etc.)
<a name="system"></a>
* System
	* return an empty list of installed applications
	* return an empty list of recent tasks
	* return an empty list of running processes
	* return an empty list of running services
	* return an empty list of running tasks
	* return an empty list of widgets
	* return an empty list of applications (provider)
	* prevent package add, replace, restart, and remove notifications
<a name="view"></a>
* View
	* prevent links from opening in the browser
	* return fake browser user agent string
		* *Mozilla/5.0 (Linux; U; Android; en-us) AppleWebKit/999+ (KHTML, like Gecko) Safari/999.9*

Limitations
-----------

#### General

* [*/proc*](http://linux.die.net/man/5/proc), CID, system (build) properties, serial number, IMEI, and MAC addresses cannot be restricted for Android itself, because restricting these will result in bootloops
* */proc/self/cmdline* will not be restricted by */proc*, because it will result in instability
* The phone number cannot be restricted for the standard phone application
* The browser bookmarks and history cannot be restricted for the browser itself
* Internet and storage can only be restricted for applications, providers, and services started by the Android package manager
* There is no usage data for *inet*, *media* and *sdcard*, since this is not technically possible
* Because it is static, [*Build.SERIAL*](http://developer.android.com/reference/android/os/Build.html#SERIAL) can only be randomized when an application starts, and there is no usage data
* Due to a bug in Chromium, the user agent cannot be restricted in all cases ([issue](https://github.com/M66B/XPrivacy/issues/825))
* Due to a custom implementation, the clipboard cannot be restricted on some Samsung stock ROMs ([issue](https://github.com/M66B/XPrivacy/issues/857))
* It is not possible to restrict external hardware MAC addresses or the external IP address, see also [FAQ 33](#FAQ33)
* You cannot restrict *Configuration.MCC/MNC* on demand
* Allowing contacts for SIM-contacts isn't supported (who is using these anymore?)
* Calendars and contacts cannot be restricted for specific accounts; it is all or nothing; however, it is possible to allow individual contacts with a [pro license](http://www.xprivacy.eu/)
* It is possible to unhook methods in user space using native libraries, see for more details [FAQ 68](#FAQ68)
* In some situations, the on demand restricting dialog freezes, notably when using volume keys. This cannot be fixed due to Android limitations.
* In some situations, the on demand restricting dialog is overlayed by other windows, notably notifications. This cannot be fixed due to Android limitations.
* Restricting *Internet/connect* and/or *View/loadURL* for the stock browser doesn't prevent loading of pages ([issue](https://github.com/M66B/XPrivacy/issues/1685))
* Android System Webview cannot be restricted on Android Lollipop

#### XPrivacy

* You cannot restrict some functions in the *Identification* category, because it is used for submitting restrictions
	* The Android ID is salted with the serial number and MD5 hashed before communicating with the crowd sourced restrictions server
	* This means that the crowd sourced restrictions server never gets the serial number nor the Android ID
* You cannot restrict *IPC* because it is needed for internal checks
* You cannot restrict *Storage* because it is needed to read the pro license file
* You cannot restrict *System* because it is needed to get the application list
* You cannot restrict *View* because it is needed to open links to the [crowd sourced restrictions](http://crowd.xprivacy.eu/)

You can restrict the XPrivacy app's access to accounts, contacts, and other things.

Compatibility
-------------

* XPrivacy has been tested on Android versions 4.0.3 - 6.0.1 (ICS, Jelly Bean, KitKat, Lollipop, Marshmallow).
It is reported to work with most Android variants, including stock ROMs.

* **XPrivacy is incompatible with LBE Security Master** ([issue](https://github.com/M66B/XPrivacy/issues/1231)).

* **XPrivacy is incompatible with the security center of MIUI** ([issue](https://github.com/M66B/XPrivacy/issues/1940)).
See [here](http://forum.xda-developers.com/showpost.php?p=55810186&postcount=12178) for a solution.

* You need to use the quirk 'noresolve' when using [GoPro](https://play.google.com/store/apps/details?id=com.gopro.smarty)
and some other wireless cameras, like the Sony QX1/10/30/100 ([issue](https://github.com/M66B/XPrivacy/issues/1751)).

* Candy Crush is known to crash on some ROMs, see [here](http://forum.xda-developers.com/showpost.php?p=58722199&postcount=13666).

Installation
------------

Installation may seem lengthy, but you can do it quickly:

1. Requirements:
	* Android version 4.0.3 - 6.0.1 (ICS, Jelly Bean, KitKat, Lollipop, Marshmallow); verify via *System Settings* > *About phone* > *Android version*
	* Read about [compatibility](#compatibility) before installing
1. **Make a backup**
1. If you haven't already, root your device; the rooting procedure depends on your device's brand and model.
	* You can find a guide [here](http://www.androidcentral.com/root) for most devices
1. Enable *System settings* > *Security* > *Unknown sources*
1. Install the [Xposed framework](http://forum.xda-developers.com/xposed)
	* The Xposed fix is **not** needed anymore
	* For Android 4.0.3 through 4.4.4 see [this XDA thread](http://forum.xda-developers.com/xposed/xposed-installer-versions-changelog-t2714053). If this does not work for you (red error text in Xposed Installer -> Framework), see [this XDA thread](http://forum.xda-developers.com/xposed/xposed-android-4-4-4-t3249895)
	* For Android 5.x see [this XDA thread](http://forum.xda-developers.com/showthread.php?t=3034811)
	* For Android 5.0.x Touchwiz ROMs see [this XDA thread](http://forum.xda-developers.com/xposed/unofficial-xposed-samsung-lollipop-t3113463)
	* For Android 5.1 Touchwiz ROMs see [this XDA thread](http://forum.xda-developers.com/xposed/unofficial-xposed-samsung-lollipop-t3180960)
	* For Android 6.0.x see [this XDA thread](http://forum.xda-developers.com/showthread.php?t=3034811)
1. Download and install XPrivacy from [here](http://repo.xposed.info/module/org.thoughtland.xlocation)
	* Alternatively, download it from [here](https://github.com/M66B/XPrivacy/releases)
1. Enable XPrivacy in the Xposed installer
1. Start XPrivacy one time
1. Reboot

I do not recommend using XPrivacy in combination with any of the
[similar solutions](#similar-solutions),
because this could result in conflicts and potential data leaks.

There is an unofficial backported Gingerbread version available [here](http://forum.xda-developers.com/showpost.php?p=44034334).
Please note that only the official version is supported.

If you want to uninstall XPrivacy, you have two options:

1. Disable XPrivacy in the Xposed installer
1. Uninstall the XPrivacy application

In either case, don't forget to reboot.

To save space, you can delete the folder */data/system/xprivacy* after uninstalling.

Upgrading
---------

* **Make a backup**
* **Do not remove the previous version** (else you will lose your settings)
* Download the new version (with a [pro license](http://www.xprivacy.eu/) you can use the menu *Help ...* > *Check for updates*)
* Install the new version over the previous version
* Wait until the Xposed installer recognizes the update (else XPrivacy might not be enabled)
* Start the new version once (else Android will not send the *Boot Completed* event)
* Reboot your device
* Wait until the XPrivacy update service has been completed (showing 100%)

When following this procedure, your data will not leak because the Xposed part of XPrivacy keeps running.

Usage
-----

#### Brief tutorial

* Find the application to restrict in the main application list
* Tap on the application icon
* Tap the first check box of any category you want to restrict

Use common sense when restricting; don't expect internet access if you restricted the internet category, etc.

Get used to XPrivacy before using more advanced features, like function exceptions.

#### Detailed tutorial

XPrivacy starts in the main view, where a data category can be selected at the top.
By ticking one or more check boxes in the list below, you can restrict the selected data category for the chosen applications.
The default category is *All*, meaning that all data categories will be restricted.

Tapping on an application icon shows the detailed view where you can manage each of the data categories for the selected application.
This view will also appear when you tap on the notification that appears after installing or updating an application.
By default, all data categories will be restricted for newly installed applications to prevent leaking privacy-sensitive data.
You can change which data categories will be restricted by changing the *Template* available from the main menu.

Data categories make it easier to manage restrictions.
You can drill down the data categories in the detailed view to reveal individual functions.
If the category is restricted, you can un-restrict individual functions by clearing the function's check box.

To see restrictions in action, try restricting the category *Identification* for
[Android Id Info](https://play.google.com/store/apps/details?id=com.bzgames.androidid).

**Applying some restrictions requires restarting applications.**

You can turn on and off all restrictions for an application using the on/off toggle switch.

Since version 1.99, you can also restrict on demand.
This means you will be asked to allow or deny a category/function
when the category/function is used by an application.
Restricting on demand is the default for newly installed applications (when using the default template).
You can turn on and off restricting on demand in the application details view
using either the settings or the check box to the right of the on/off toggle switch.
You can turn on and off restricting on demand for individual categories and functions
using the second column of check boxes.

If an application has requested Android permissions for a data category,
the category will be marked with a green key icon.
If an application has used or tried to use data, the data category will be marked with an orange warning triangle icon.
If an application has internet permissions, a world globe icon will be shown to the left of the application name.
These icons are just guidelines because an application can still access some privacy-sensitive data without Android permissions,
such as your device's serial number, and because it is not possible to monitor data usage in each and every situation,
such as access to the internet or external storage.
Be aware that an application can still access the internet through other (sister) applications.

Restricting internet or storage means blocking access to the internet and to external storage (typically the SD-card), respectively.
Either of these may result in error messages and even cause applications to force close (crash).

Function restrictions considered 'dangerous' are marked with a reddish background color.
These 'dangerous' functions are more likely to cause applications to crash if you restrict them.
'Dangerous' functions can be turned into normal functions by long-clicking them in the default template. This is only recommended for experienced users who know how to identify the cause of a crash in the logcat!

Global settings and application specific settings are accessible from the application list's menu
and from the menu of the application's detailed view. The global settings,
such as randomized or set latitude/longitude, apply to all applications
unless you override them with specific application settings.
But saving an empty set of specific application settings (you can use the clear button)
will erase all application specific settings so that the global settings will again be in force.

The default restrictions template (in the main menu) is applied automatically to newly installed applications
and manually via the menu item 'Apply template' in the application's detailed view.

You can find a **very useful overview of all menu items** [here](https://github.com/M66B/XPrivacy/blob/master/MENU.md).

**Use XPrivacy entirely at your own risk.**

Permissions
-----------

XPrivacy asks for the following Android permissions:

* Accounts: to be able to restrict applications' access to accounts
* Contacts: to be able to restrict applications' access to contacts
* Boot: to be able to check if XPrivacy is enabled
* Internet: to be able to submit and fetch [crowd sourced restrictions](http://crowd.xprivacy.eu/)
* Storage: to be able to read the pro license file and to be able to export XPrivacy's settings to the SD-card (only with a [pro license](http://www.xprivacy.eu/))
* Wakelock: to keep the processor running during batch operations

If desired, you can even restrict XPrivacy from accessing any of the above,
but there are some [limitations](#limitations).

Please note that any Xposed module basically has root permissions and therefore can circumvent any Android permission.

<a name="frequently-asked-questions"></a>Frequently Asked Questions (FAQ)
--------------------------------

<a name="FAQ1"></a>
**(1) Will XPrivacy make my device slower?**

Maybe a little bit, but you probably won't notice.

If you run comparison benchmarks, please submit them.

But my device is slow with XPrivacy!
It appeared that in some cases this was caused by the TRIM bug.
See [here](http://forum.xda-developers.com/showthread.php?t=2104326) for more information and a solution.

<a name="FAQ2"></a>
**(2) Does XPrivacy use a lot of memory and battery?**

Almost nothing.

<a name="FAQ3"></a>
**(3) Can you help me with rooting my device?**

There are many [guides](http://www.androidcentral.com/root) to help you to root your device.
Use your favorite search engine to find one.

<a name="FAQ4"></a>
**(4) How can I reset an application's XPrivacy settings?**

While viewing an application's restrictions, select *Menu* > *Clear*, then reboot.

<a name="FAQ5"></a>
**(5) Can I backup XPrivacy's restrictions, settings, and usage data?**

Starting with version 1.11.13, you can no longer backup XPrivacy's data with standard backup tools, such as Titanium Backup.
This is because the database is no longer stored in the XPrivacy data folder, but in a system folder.
I have tried to store the database in the XPrivacy data folder, but this leads to all kinds of permission problems.

The best practice is to use XPrivacy's export function (*Main Menu* > *Export*) to backup XPrivacy data,
but please note that this requires a [pro license](http://www.xprivacy.eu/).

You can automate backups by sending an intent:

```
adb shell am start -a org.thoughtland.xlocation.action.EXPORT
```

If you want to specify a file name for the backup:

```
adb shell am start -a org.thoughtland.xlocation.action.EXPORT -e FileName /sdcard/test.xml
```

You can do this with [Tasker](https://play.google.com/store/apps/details?id=net.dinglisch.android.taskerm), for example:

* New task: Any name you like
* Action Category: Misc/Send Intent
* Action: org.thoughtland.xlocation.action.EXPORT
* Target: Activity
* Extra: FileName:/sdcard/test.xml (optional, to specify an export location and file name)

<a name="FAQ6"></a>
**(6) Precisely which functions can XPrivacy restrict?**

Many. See [here](https://github.com/M66B/XPrivacy/blob/master/org/thoughtland/xlocation/Meta.java) for details.

<a name="FAQ7"></a>
**(7) How safe is XPrivacy?**

Great care has been taken to develop XPrivacy. Nevertheless, on rare occasions, data can leak and applications can crash.

<a name="FAQ8"></a>
**(8) What does "An internal check failed..." error message mean?**

An internal check of XPrivacy failed, resulting in potential data leakage.
Please press *OK* to send me the support information so I can look into it.

<a name="FAQ9"></a>
**(9) What is the procedure to update a ROM?**

Assuming you don't wish to wipe data, and that Xposed and XPrivacy are already installed before updating the ROM, the best procedure to update a ROM is:

1. Export XPrivacy settings
1. Enable airplane/flight mode
1. Use the menu option in XPrivacy to clear all data
1. Reboot to recovery
1. Flash ROM
1. Flash Google apps (optional)
1. Re-activate Xposed using [Xposed toggle](http://forum.xda-developers.com/showpost.php?p=45188739)
1. Reboot to Android
1. Restore the android ID (when needed. For example, with [Titanium backup](https://play.google.com/store/apps/details?id=com.keramidas.TitaniumBackup))
1. Import XPrivacy settings
1. Disable airplane/flight mode
1. Fake the network type (Wi-Fi, mobile)

If you skip the export, clear, or import steps above, some system applications can end up with the wrong restrictions because the ROM update might have changed these applications' UID's.

To import and export XPrivacy's data, you need a [pro license](http://www.xprivacy.eu/).

<a name="FAQ10"></a>
**(10) Can I restrict root access?**

Yes, via *Shell* > *su*,
but be aware that applications can acquire root privileges through native libraries too.
An example is [Android Terminal Emulator](https://play.google.com/store/apps/details?id=jackpal.androidterm).

<a name="FAQ11"></a>
**(11) Will restrictions be applied immediately?**

Changes to restrictions may require up to 15 seconds to take effect because of caching. Changing internet and storage restrictions requires restarting the application. Please note that in many cases pressing *back* within target applications merely moves the application to the background.

<a name="FAQ12"></a>
**(12) Does XPrivacy include a firewall?**

You can restrict internet access for any application. But if you want to partly enable internet, for example for Wi-Fi only, you will have to use a firewall application, such as [AFWall+](http://forum.xda-developers.com/showthread.php?t=1957231). XPrivacy works within Android,
and detailed firewall rules can only be applied within the Linux kernel.

The latest versions of XPrivacy allow you to white and black list IP addresses and host names.

<a name="FAQ13"></a>
**(13) What does the "Unable to parse package." message mean?**

This means XPrivacy's apk file is corrupt. Try disabling your popup blocker or downloading using another device.

<a name="FAQ14"></a>
**(14) How do I make a logcat?**

The simplest way is to use an application like [Logcat Extreme](https://play.google.com/store/apps/details?id=scd.lcex) or  [Catlog](https://play.google.com/store/apps/details?id=com.nolanlawson.logcat),
but logcats captured this way are not always sufficient. The best way to capture a logcat is:

* Install the [Android SDK](http://developer.android.com/sdk/index.html) (Click *Download for other platforms* for a minimal download)
* Make sure you can connect to your device via USB (see [here](http://developer.android.com/sdk/win-usb.html) for drivers and instructions)
* **Enable XPrivacy debug logging in the main settings**
* Power off your device
* Start logging by entering this command in the command line: *adb logcat >log.txt*
* Power on your device
* Reproduce the problem

If you need a logcat from system start, you can run this command on your device (this will force an Android restart):

```
killall system_server; logcat | grep -i xprivacy
```

Upload the captured logcat somewhere, for example to Google Drive,
and link to it from the issue you created.
Don't forget to mention the *UID* of the application to look into, when relevant.

<a name="FAQ15"></a>
**(15) Where does XPrivacy store its settings?**

XPrivacy's restrictions, settings, and usage data are stored in an sqlite3 database in this folder:

```
/data/system/xprivacy
```

<a name="FAQ16"></a>
**(16) Why doesn't clearing the check box for a data category also clear the functions inside that category?**

In the application details view, it will. In the main list view, you are protected against losing the restriction settings inside a data category by accidentally unchecking that category's check box. The restriction settings inside a category only apply when that category is restricted.

<a name="FAQ17"></a>
**(17) How can I export/import my settings?**

You need a [pro license](http://www.xprivacy.eu/) to import your settings. Exported settings are stored in the folder *.xprivacy* in the file *XPrivacy.xml*. You can copy this file to the same place on any other device. When importing, settings are only applied to user and system applications that actually exist on both devices.

The export file will contain all restrictions and settings, but note that allowed accounts and contacts (not the accounts and contacts themselves) can only be imported when the Android ID is the same.

Also see the [above FAQ](#FAQ9) about what to do when updating your ROM.

<a name="FAQ18"></a>
**(18) Why does a GPS status icon still appear after I have restricted locations?**

This is by design. XPrivacy only replaces the real location with a fake location. It even uses the real location to randomize the fake location. The idea is that everything should appear as normal as possible to an application.

See [here](http://forum.xda-developers.com/showpost.php?p=57417209&postcount=13097) for some addtional details.

<a name="FAQ19"></a>
**(19) How about multi-user support?**

Secondary users can install and use XPrivacy the same way as the primary user.
The primary user cannot manage the restrictions of secondary users.
This is because Android totally separates the environments of the users
(which is a good thing, from a security perspective).
Each user has its own set of settings, so each user can define its own template and global fake values.

* Only the primary user can clear all data
* Only the primary user can define 'dangerous' functions
* Only the primary user can enable/disable debug logging
* The primary user can see all usage data
* Secondary users can only see their own usage data
* The pro license needs to be individually activated for each user

<a name="FAQ20"></a>
**(20) Why is the *Settings* > *Fake data* > *Search* button disabled?**

Because some Google components are not installed.

<a name="FAQ21"></a>
**(21) Do I still need root after installing Xposed?**

No, root is only required to install Xposed one time.

<a name="FAQ22"></a>
**(22) Why isn't XPrivacy available in the Google Play Store anymore?**

Because Google removed it. Read the explanation [here](http://forum.xda-developers.com/showpost.php?p=44427089&postcount=2433).

<a name="FAQ23"></a>
**(23) What is "Template" used for?**

XPrivacy uses the default template to apply restrictions to newly installed applications. Templates can also be used when you select "*Apply template*" from the menu inside the application detail view.

<a name="FAQ24"></a>
**(24) Will there be iOS or Windows Phone versions?**

No. Because they are not open source, it's too difficult to implement something like XPrivacy on these OS's.

<a name="FAQ25"></a>
**(25) Will you restrict...?**

* device brand/manufacturer
* device model/product name
* device (phone) type
* network type (mobile, Wi-Fi, etc.)
* synchronization state
* screen locking
* display settings (DPI, resolution, etc.)
* Wi-Fi settings
* Bluetooth settings
* shortcuts
* autostarting
* starting other applications
* Android version
* vibration
* checks for root
* lockscreen
* time(zone), alarm
* nag-screens, popups
* statusbar notifications
* installing shortcuts
* wake ups / wakelocks

No, because I don't consider this information to be privacy-sensitive data (i.e. able to identify you and collect data about you). I am happy to add new restrictions for data that is really privacy-sensitive.

* Calendars by account
* Contacts by account

For the few users who will be using this, it is too much work to implement.
The [calendar](http://developer.android.com/guide/topics/providers/calendar-provider.html)
and [contacts](http://developer.android.com/guide/topics/providers/contacts-provider.html) API are quite complicated.
There is also a better way to accomplish this.
You can use different users on your device with different accounts.
To enable multiple users for a phone you can follow [these instructions](http://www.pocketables.com/2013/03/how-to-enable-multiple-user-mode-on-cyanogenmod-10-1-and-some-other-android-4-2-2-roms.html)
or install [this Xposed mod](http://forum.xda-developers.com/xposed/modules/xposed-multiple-users-phone-t2676516).
Note that the user selector on the lockscreen works in landscape only.

<a name="FAQ26"></a>
**(26) Will you revoke permissions?**

Android permissions cover only a part of the functions that leak privacy-sensitive information.
Revoking permissions will make quite a few applications unusable/crash.
XPrivacy feeds applications with fake or no data, which keeps most applications working.
In other words, XPrivacy is not a permission manager, but a privacy manager.
If you need a permission manager, there are several Xposed modules that offer this functionality.

<a name="FAQ27"></a>
**(27) Does XPrivacy work with SELinux (Fort Knox)?**

Yes, I develop XPrivacy on a device with SELinux in restrictive mode.

<a name="FAQ28"></a>
**(28) How does the tri-state check box work?**

The tri-state check box works this way:

* unchecked = **no** items in the category are restricted
* solid square = **some** items in the category are restricted
* check mark = **all** items in the category are restricted

Note: By default, categories and functions are filtered by permission, so you may not see all of them. The check box state is independent of this.

<a name="FAQ30"></a>
**(30) What should I do if an application force closes (crashes) or something doesn't work?**

Inspect the application's usage view, via the main menu item *Usage data* to see which restrictions were enforced.
Restrict and unrestrict one by one until you have found which one causes the application to force close.
Wait 15 seconds after each change to let the XPrivacy cache time-out.
Help others by submitting your working set of restrictions.

See also [this FAQ item](#FAQ63).

<a name="FAQ31"></a>
**(31) Can XPrivacy handle non-Java applications?**

In general, due to Android's isolated virtual machine architecture, calls to native libraries and binaries are via Java and so XPrivacy can restrict them. XPrivacy can cover any route to a native library or binary.

XPrivacy cannot hook into native libraries, but can prevent native libraries from loading. This can break applications such as Facebook, but can prevent malware from doing its work.

XPrivacy can also restrict access to the Linux shell (including superuser) to prevent native binaries from running. You can find these restrictions in the *Shell* category.

Starting with version 2.0, XPrivacy can protect against direct interprocess communication (IPC).

<a name="FAQ32"></a>
**(32) Why do I see data usage when an application does not have the corresponding Android permission?**

Many functions do not require Android permissions, so this is quite normal. Sometimes an application tries to access a function for which it doesn't have an Android permission. Since XPrivacy usually runs prior to the function, such access will be noted.

If you filter on permissions, and an application tries to use a function without having permission, the application will still be shown.

If you think a function requires permissions while XPrivacy shows it doesn't, please report it.

<a name="FAQ33"></a>
**(33) How can I restrict the hardware, external MAC, IP, and IMEI number?**

You can restrict the (internal) IP and MAC addresses and IMEI number for any application.

The external IP is assigned by your provider and cannot be changed. You could use a [VPN](http://en.wikipedia.org/wiki/Virtual_private_network) or [TOR](http://en.wikipedia.org/wiki/Tor_\(anonymity_network\)) to hide your external IP to a certain extent.

The hardware MAC address can be changed on some devices, but this is device-dependent and can only be done at the driver or kernel level. XPrivacy only works on the Android level and is device-independent.

The same applies to the IMEI number, additionally complicated by legal issues in most countries.

<a name="FAQ34"></a>
**(34) What is the logic behind on demand restricting?**

* The on demand restricting dialog will appear if:
	* On demand restricting is enabled in the main settings
	* On demand restricting is enabled in the application settings
	* The category and the function are marked with question marks
	* However a few functions are exempted from prompting (only *Phone/Configuration.MCC/MNC*)
	* Prompts will not be shown for 'dangerous' functions
		* An exception are functions with white/black lists
	* Prompts will not be shown for System applications
* *Apply to entire category* will:
	* Set the entire category according to your choice (deny/allow)
	* Existing settings for individual functions are forgotten
* When applying to a function only (*Apply to entire category* not checked):
	* The function is set according to your choice
* The default after dialog timeout is taken from the current restriction settings
* There are four possibilities for the restriction / on demand checkboxes:
	1. &#9744; &#9744; You will not receive an on demand popup, the permission will always be allowed
	2. &#9744; [?] You will receive an on demand popup, if this times out or the screen is locked the permission will be allowed once
	3. &#9745; [?] You will receive an on demand popup, if this times out or the screen is locked the permission will be denied once
	4. &#9745; &#9744; You will not receive an on demand popup, the permission will always be denied
* Be aware that the on demand popups are global, which could be an issue if your device has multiple users. Unfortunately, this cannot be changed.

<a name="FAQ37"></a>
**(37) Do I need to have the Google Play Store/services installed for the pro version?**

Only for the [pro license fetcher](https://play.google.com/store/apps/details?id=org.thoughtland.xlocation.license), not for a pro license acquired through a PayPal donation.
The pro license fetcher needs the Google Play Store/services for fetching a pro license, but not for using a pro license.

<a name="FAQ38"></a>
**(38) What does the update service do?**

The update services runs after upgrading XPrivacy and after each boot.
It takes care of migrating settings, randomization of fake data, and upgrading settings for new versions.

<a name="FAQ46"></a>
**(46) Why do I need to register to submit restrictions?**

To prevent a malicious application author from automatically submitting many *allow* restrictions to outvote the legitimate users.

<a name="FAQ47"></a>
**(47) What is IPC?**

It is an acronym for **I**nter-**P**rocess **C**ommunication. See [here](http://forum.xda-developers.com/showpost.php?p=50274730&postcount=7006) and [here](http://forum.xda-developers.com/showpost.php?p=50352683&postcount=7206) for more information.

<a name="FAQ48"></a>
**(48) Can XPrivacy be detected by other application?**

Yes, but I don't see this as a problem,
since [security through obscurity](http://en.wikipedia.org/wiki/Security_through_obscurity) is not a good principle.

<a name="FAQ49"></a>
**(49) Why do I keep getting a 'Restart required' message?**

Things to do / check:

* Ensure the Xposed framework is (still) installed using the Xposed installer
* Ensure the XPrivacy application is stored on the internal storage and not moved to the SD-card or somewhere else
	* Fairphone users, see [here](https://fairphone.zendesk.com/hc/en-us/articles/201154666-How-can-I-see-all-the-different-places-where-information-is-stored-on-my-phone-SD-Card-Internal-Storage-and-Phone-Storage-)
* Ensure LBE Security Master is not installed (disabling is not enough)
* Ensure the security center of MIUI is disabled (see [here](http://forum.xda-developers.com/showpost.php?p=55810186&postcount=12178))
* In the Xposed installer, disable XPrivacy, wait a few seconds, and then enable it again. Then reboot.
* Clear the (Dalvik) cache using the device's recovery

The most common problem seems to be the storage location of the XPrivacy application.

If these suggestions don't help, please create an issue and provide a logcat (see [*Support*](#support)).

<a name="FAQ50"></a>
**(50) Do you have suggestions about additional privacy-related software?**

IMHO, in addition to XPrivacy, you should at least install an ad blocker and a firewall:
* [AdAway](https://free-software-for-android.github.io/AdAway//) ([source code](https://github.com/Free-Software-for-Android/AdAway))
* [AFWall+](https://play.google.com/store/apps/details?id=dev.ukanth.ufirewall) ([source code](https://github.com/ukanth/afwall))

Here is a list of additional privacy-related software:
* [CrappaLinks](http://forum.xda-developers.com/showthread.php?t=2603868) ([source code](https://github.com/GermainZ/CrappaLinks))
* [PlayPermissionsExposed](http://forum.xda-developers.com/xposed/modules/playpermissionsexposed-fix-play-store-t2783076) ([source code](https://github.com/GermainZ/PlayPermissionsExposed))
* [Xabber](https://play.google.com/store/apps/details?id=com.xabber.android) ([source code](https://github.com/redsolution/xabber-android))
* [Wi-Fi Privacy Police](https://play.google.com/store/apps/details?id=be.uhasselt.privacypolice)

Please note that none of these applications are written by me, so please be sure to contact their respective authors for support questions.

<a name="FAQ51"></a>
**(51) What does the on/off toggle switch do in the application details view?**

It turns all restrictions for the application on or off, but still allows you to change the restrictions.

<a name="FAQ52"></a>
**(52) Why was my issue closed?**

I have really spent a lot of time developing XPrivacy and I am happy to look into any issue,
but I am asking you to properly document your issue.
*It doesn't work* or *it crashes* is not sufficient!
So, please *help me help you* by describing the exact steps to reproduce the problem and/or provide a logcat.

See [*Support*](#support) for more details.

<a name="FAQ53"></a>
**(53) What happens if I make XPrivacy a device administrator?**

This will ensure that other applications cannot uninstall XPrivacy without your knowledge.

<a name="FAQ54"></a>
**(54) Why do exporting and importing take so long?**

There are more than 400 restrictions per application. 
Additionally, there can be quite a few application specific settings 
(for example, when you use white/black lists).
So, yes, exporting and importing restrictions and settings can take quite some time.
The default is to export restrictions for all applications, since the export is meant to be a full backup.
However, it is possible to filter the applications whose restrictions you want to export using the filter icon in the action bar 
(for example, only user applications with restrictions),
and then to select only these applications using the action bar *select all* (first icon).

<a name="FAQ56"></a>
**(56) How can I recover from a bootloop?**

For devices with a custom recovery (TWRP/CWM) you can flash the [Xposed-Disabler-Recovery.zip](http://forum.xda-developers.com/attachment.php?attachmentid=2568891&d=1391958634). Alternatively, (on most devices) press the volume down button 5 times during boot (there will be a vibration with each press when done correctly).

See [here](https://github.com/M66B/XPrivacy/blob/master/DATABASE.md#xprivacydb) on how to enable debug logging without XPrivacy activated in Xposed.

<a name="FAQ57"></a>
**(57) How does *Expert mode* work?**

Expert mode has the following sub-options which can be toggled individually:

* *Restrict system components (Android)*
	* Enabling this option allows you to restrict applications which have a UID less than 10000 (Android System, Bluetooth Share, Dialer, NFC, Phone, etc.). Restricting these core functions is quite dangerous, and can easily lead to bootloops. Always make a backup (export/nandroid) before changing these restrictions.
* *Use secure connections*
	* Enabling this option (default), forces communications with the crowd sourced restrictions server (submitting/fetching, device registration) to travel through a secure socket. This option can only be disabled by first enabling *Expert mode*.
* *Maximum fetch confidence interval*
	* Increasing this value will result in fetching less-reliable crowd sourced restrictions.
* *Quirks*
	* Used to fix some application compatibility issues or to enable special or expert features
		* *freeze*: show the on demand restriction dialog, even if there is the possibility it will freeze
		* *resolve*, *noresolve*: enable/disable resolving IP addresses to names for usage data / on demand restricting
		* *test*: allow the XPrivacy update checker to also check for test versions
		* *safemode*: hide unsafe function restrictions
* *Clear cache* will clear settings and restrictions caches for all applications and the privacy service
* *Clear all data* will erase **all** settings and restrictions. Use with care!

<a name="FAQ58"></a>
**(58) Can I write a thesis about XPrivacy?**

Yes, you can. I will even help you with it.
However, I will not write or review code or text for you. Nevertheless, I will try to answer any questions you have.
XPrivacy is open source (see the [license](#license)) and all code you write needs to be contributed back to the project.
To help you, I want to see an e-mail from your professor with a confirmation he or she has read and agrees to this README.

<a name="FAQ59"></a>
**(59) Will you implement multiple profiles?**

No, because privacy is not something that is optional.
It makes no sense to restrict something during the day and not during the night, or at work and not at home.

<a name="FAQ60"></a>
**(60) Why is the upgrade notification stuck at 100%?**

This is by design, so you can see the upgrade has completed successfully.
You can swipe away the notification after you have seen it.

<a name="FAQ61"></a>
**(61) Can the default on demand restricting time-out be increased?**

Unfortunately this is not possible.
The on demand restricting dialog is holding up system processes, 
and Android may reboot automatically if too much time has passed without a response.
Recent versions of XPrivacy have a reset button; use at your own risk.

<a name="FAQ62"></a>
**(62) How can I 'toggle' multiple applications?**

Multiple selection works as in any Android application.
Tap and hold down on an application in the application list to start selecting, 
and tap other applications to select more applications.
Toggle restrictions will work on the selected applications.

You can use this as a powerful way to apply a template to multiple applications.

You can also use the filters to show the applications you want to act on,
since toggle restrictions works on the visible applications by default.
There is one exception to this: exporting will be done for all applications by default,
since the export is intended to be a full backup.

<a name="FAQ63"></a>
**(63) How can I troubleshoot an issue believed to be caused by XPrivacy?**

* Always make sure you have a backup (XPrivacy export, or nandroid)
* Most issues are caused by a "bad" restriction, so try to reproduce the issue and check the main usage data
* Verify that the issue is actually caused by XPrivacy
	* Turn on airplane/flight mode
	* Disable XPrivacy in the Xposed installer (don't forget to reboot)
	* Check if the issue is still present; if yes, XPrivacy is not causing the issue
* Finding the culprit:
	* Clear all XPrivacy restrictions (don't forget to reboot)
	* Check if the issue is still present
	* Import half of your restrictions, check if the issue is still present
	* If yes, clear again and import only half of these restrictions
	* Continue this process until you have found the "bad" restriction
* If following these steps fails to find the issue, please follow the [*Support*](#support) instructions

<a name="FAQ64"></a>
**(64) Is the on demand dialog always shown?**

That depends on your ROM version.
On stock ROM 4.4.4 (Nexus 5), the on demand dialog is always shown.
On older or customized ROMs, the on demand dialog is almost always shown,
except sometimes for the functions *inet*, *sdcard* and *media*,
and sometimes for restrictions triggered by the hardware buttons (for example, the volume buttons).
This is to prevent the on demand dialog from freezing (locking up), caused by an internal Android lock.
This freeze cannot be fixed by XPrivacy.

If you want to have the on demand dialog always shown, then you can add the *quirk* "freeze".

<a name="FAQ65"></a>
**(65) Why is my data is still visible?**

The data might be cached by the application, so you might have to wait a while until the cache is updated.
It may even be necessary to restart the application or reboot your device to clear the cache.

The Google Maps view can look like a part of an application, but is in fact not.
Even if your current position is shown on the map, it doesn't mean the application knows your current position.

<a name="FAQ66"></a>
**(66) How can I directly start ... ?**

See [question #5](#FAQ5) for how to start an export and for details on Tasker.

Similarly you can start other activities:

* Settings

```
am start -a org.thoughtland.xlocation.action.SETTINGS
am start -a org.thoughtland.xlocation.action.SETTINGS --ei Uid 10123
```

* Application details view

```
am start -a org.thoughtland.xlocation.action.APPLICATION --ei Uid 10123
am start -a org.thoughtland.xlocation.action.APPLICATION --ei Uid 10123 --ei Action 1
am start -a org.thoughtland.xlocation.action.APPLICATION --ei Uid 10123 --ei Action 2
am start -a org.thoughtland.xlocation.action.APPLICATION --ei Uid 10123 -e RestrictionName location
am start -a org.thoughtland.xlocation.action.APPLICATION --ei Uid 10123 -e RestrictionName location -e MethodName GMS.addGeofences
```

Action 1 means clear; action 2 means settings.

* Usage data

```
am start -a org.thoughtland.xlocation.action.USAGE
am start -a org.thoughtland.xlocation.action.USAGE --ei Uid 10123
```

* Export, import, submit, fetch, toggle

```
am start -a org.thoughtland.xlocation.action.EXPORT
am start -a org.thoughtland.xlocation.action.EXPORT --eia UidList 10123,10124 --ez Interactive true
am start -a org.thoughtland.xlocation.action.IMPORT
am start -a org.thoughtland.xlocation.action.IMPORT --eia UidList 10123,10124 --ez Interactive true
am start -a org.thoughtland.xlocation.action.SUBMIT --eia UidList 10123,10124 --ez Interactive true
am start -a org.thoughtland.xlocation.action.FETCH --eia UidList 10123,10124 --ez Interactive true
am start -a org.thoughtland.xlocation.action.TOGGLE --eia UidList 10123,10124 --ez Interactive true
```

* Flush cache, check for updates

These actions require root or the permission *org.thoughtland.xlocation.MANAGE_XLOCATION*.

```
am startservice -a org.thoughtland.xlocation.action.FLUSH
am startservice -a org.thoughtland.xlocation.action.UPDATE
```

With Tasker, you can create shortcuts on your homescreen:

* Create a task and give it a name (Tasker)
* Create a shortcut on your homescreen (launcher)
* Choose shortcut: "Task Shortcut" (launcher)
* Task Selection: "your_usage_data_task" (Tasker)
* Task Shortcut Icon: tap the *Image Select* button (lower right) and choose an icon (for example the XPrivacy icon) (Tasker)
* Create icon: tap the back button to finish creating the shortcut (Tasker)

<a name="FAQ67"></a>
**(67) Why do I get 'refused', 'forbidden', or an error while submitting/fetching/navigating the crowd sourced restrictions?**

This probably means your IP address has been blacklisted.
Typically, this is because of spamming or other kind of abuses.
TOR exit nodes and VPS servers are often blacklisted.
It is possible your device is infected with malware, and is sending spam without your knowledge.

You can check if [your IP address](http://www.whatismyip.com/) is blacklisted by checking if it is mentioned on one of these lists:

* [Spamhaus DROP List](http://www.spamhaus.org/drop/drop.lasso)
* [Spamhaus EDROP List](http://www.spamhaus.org/drop/edrop.lasso)
* [Stop Forum Spam](http://www.stopforumspam.com/search) (30 days or less)

Another potential cause is documented [here](http://forum.xda-developers.com/showpost.php?p=56034808&postcount=12293).

<a name="FAQ68"></a>
**(68) What are unsafe restrictions?**

Unsafe restrictions can be disabled by removing the Xposed method hook using a native library.
See [here](https://github.com/cernekee/WinXP) for a proof of concept.
You can prevent this by not allowing native libaries to load, which will, unfortunately, often result in a crash.

XPrivacy 3 does partly address this problem for [vanilla Android KitKat](https://source.android.com/)
and some other frequently used ROMs (see below).
This is done by hooking into the Android counterparts of the user space functions.
Unfortunately, not all user space functions have an Android counterpart,
meaning that this cannot be done for all restrictions.
Nevertheless, the most sensitive data, like contacts and your location, can safely be protected by XPrivacy 3.

*XPrivacy 3 modes*

* AOSP mode = vanilla (Google) Android KitKat or:
	* [OmniROM](http://omnirom.org/)
	* [CyanogenMod](http://www.cyanogenmod.org/) (based)
	* [MIUI](http://en.miui.com/)
	* [SlimKat](http://www.slimroms.net/)
	* [Carbon ROM](https://carbonrom.org/)
	* [Dirty Unicorns](http://www.teamdirt.me/)
	* [Liquid Smooth](http://liquidsmooth.net/)
	* [Paranoid Android](http://paranoidandroid.co/)
	* [Android Revolution HD](http://forum.xda-developers.com/showthread.php?t=1925402)
	* [Mahdi ROM](https://plus.google.com/u/0/communities/116540622179206449806)
	* [Omega ROM](http://omegadroid.co/omega-roms/)
* AOSP mode = vanilla (Google) Android Lollipop
* Compatibility mode = all other Android versions/variants

*About* will show if XPrivacy 3 is running in compatibility mode, which means XPrivacy 3 is behaving as XPrivacy 2.
If there is no message about compatibility mode, XPrivacy 3 is running in AOSP mode.
XPrivacy 3 will always run in compatibility mode on Android versions before KitKat.

You can force XPrivacy 3 into AOSP mode using the main settings.
This will work on Lollipop when SELinux is disabled, or in permissive mode, or if you add this build property:

```
xprivacy.options=ignoreselinux
```

There is no need to force AOSP mode if XPrivacy recognizes your ROM as a compatible ROM.

If you force AOSP mode, privacy-sensitive data may leak.
XPrivacy has many internal checks, so if something is going wrong,
you will probably sooner or later get a debug info popup.

For the benefit of others, please report if XPrivacy 3 works for the ROM you are using
(also post a screenshot of the *About* of XPrivacy, so I can see how the ROM is recognized).

<a name="FAQ69"></a>
**(69) What do the state colors mean, and when do they change?**

* **Grey**: three cases:
     1. after clearing restrictions for an application
     1. after setting any restriction for an application
     1. after you perform an XPrivacy import
* **Orange**: four cases:
     1. application is new
     1. application is updated
     1. after you delete all regular (not on demand) restrictions for an application
     1. after you fetch restrictions from the crowd sourced XPrivacy server for an application
* **Green**: after you submit restriction changes to the crowd sourced XPrivacy server for an application

<a name="FAQ70"></a>
**(70) Why do I get a '429 Too Many Requests' message?**

This can happen when checking for updates using the main menu.
Everybody can download a new version of XPrivacy five times in 12 hours.
After this limit, you will get '*429 Too Many Requests*'.
This is to limit the bandwidth of the server to acceptable levels.
Simply wait 12 hours after the last download, and you can download again.
Please note that I will not make exceptions to this, because the limit is there for a reason.

<a name="FAQ71"></a>
**(71) Why don't I see the changelog**

Make sure you have an internet connection and that XPrivacy has internet access (check your firewall).
The changelog is an in-app display of [this page](https://github.com/M66B/XPrivacy/blob/master/CHANGELOG.md)

<a name="FAQ72"></a>
**(72) Can you make XPrivacy available on F-Droid, Amazon, or other stores/repositories?**

I don't want to publish XPrivacy in dozens of places, because it is extra work that doesn't add any value.
The current download locations,
the [Xposed repo](http://repo.xposed.info/module/org.thoughtland.xlocation)
and [GitHub](https://github.com/M66B/XPrivacy/releases),
should be accessible to almost everybody.

[F-Droid](https://f-droid.org/) also doesn't allow me to sign the APK with my own signature.

<a name="FAQ74"></a>
**(74) Why do I not see a specific application in the application list?**

XPrivacy allows you to restrict each and every application.  XPrivacy even allows you to restrict
itself as well as core Android system components, although there are a few [limitations](#limitations).
These limitations are for your own safety (for example, to prevent a bootloop or to keep XPrivacy usable in all circumstances).

The application you are searching for is most likely filtered, for example because it is a system application.
You can change the filters by using the main menu *Filter*.
By default, system applications and applications without permissions for the selected category are filtered.

Some applications (components) share data, which means they share the same UID.
These applications are shown only once in XPrivacy (this cannot be changed).
An example is the Dolphin browser and its plugins/addons.

<a name="FAQ76"></a>
**(76) What happened to the import/export enabler?**

Please read [here](http://forum.xda-developers.com/xposed/modules/xprivacy-ultimate-android-privacy-app-t2320783/page1281#post57091458).

<a name="FAQ77"></a>
**(77) Why does my pro license not work?**

* Make sure that the license file name and contents were not altered while downloading the file (for example, by a virus scanner or your e-mail client). The **most common problem** is that some e-mail clients rename the file.
* Make sure that you have put the license file into the root folder of the SD-card (this is the folder you will see when you connect your Android device to a PC).
* After starting XPrivacy, the license file will be imported, which means the license file will be removed from the root folder of the SD-card, and that the about dialog will show *Licensed*.

If your device doesn't have an SD-card, you will need to put the license file into the root folder of the external storage folder.
This is the folder you will see if you connect your Android device to a PC. When in doubt, you can use the menu *Help*, *About* to see the correct folder name.

If it still does not work, try to put the license file in the alternate location as shown in the *About* dialog.

<a name="FAQ78"></a>
**(78) Why do I get 'The Play store says not licensed' message when I try to fetch a license?**

This message basically means that the Google Play Store thinks you didn't pay for the [pro license fetcher](https://play.google.com/store/apps/details?id=org.thoughtland.xlocation.license).

Please make sure you are using the original Google Play Store application, and that the Google Play Store and Google Play services have internet access (mind your firewall).
Make sure you are not using *Lucky Patcher*, *Freedom*, or similar applications.
Also make sure you didn't restrict the Google Play Store, Google Play services, or Pro License Fetcher using XPrivacy.

Start the Google Play Store and wait about a minute so it can synchronize with the Google servers.

Try to fetch a license again. If you keep having this problem, please contact me.

<a name="FAQ79"></a>
**(79) How can I disable restrictions at boot (experts only)**

By creating this file:

*/data/system/xprivacy/disabled*

Each line should either contain a category name or a category name and function name separated by a slash (/).
See [here](https://github.com/M66B/XPrivacy/blob/master/org/thoughtland/xlocation/Meta.java#L47) for the correct category and function names.

If SELinux is restrictive, you need to add this build property:

```
xprivacy.options=ignoreselinux
```

<a name="FAQ80"></a>
**(80) Why do I get 'Privacy service version mismatch' error?**

When installed, XPrivacy has two parts: the Xposed module part and the application part. This error means the Xposed module part and the application part have different version numbers.
If a reboot doesn't solve this problem, try disabling and then enabling XPrivacy in the Xposed installer, and reboot again.

<a name="FAQ81"></a>
**(81) Why does restricting a category not restrict all of its functions?  What are 'dangerous' functions?**

Some functions are marked 'dangerous' (red background) because they frequently cause applications to crash when they are restricted. When a category is restricted, the 'dangerous' functions within this category will not be restricted. These functions can be restricted directly, if needed.
Experienced users can change a 'dangerous' function into a normal function by long-clicking it in the default template. Usually, the restriction that caused an application to crash can be identified in the logcat. Again, restricting 'dangerous' functions is only recommended for experienced users and will cause crashes more frequently!

<a name="FAQ82"></a>
**(82) Do whitelists override function restrictions?**

Yes, whitelists always override function restrictions.

<a name="FAQ83"></a>
**(83) Do whitelists override the 'Deny' button?**

Yes, whitelists will always override the 'Deny' button.

<a name="FAQ84"></a>
**(84) What does the 'Don't know' button do?**

Nobody knows. That is why it has that name.

Actually, pressing 'Don't know' will, for 15 seconds, deny non-dangerous restrictions and allow dangerous restrictions.

For a non-dangerous restriction with a whitelist, the whitelist will always override the complete denial.

Support
-------

*Please read everything below first!*

#### General

**Do not use my personal or XDA e-mail for bug reports, feature requests, or questions.**

It is okay to use my personal or XDA e-mail for things that cannot be shared in public, such as security reports.

**There is only support for official XPrivacy releases.**

**There is no support for versions prior to the latest stable release.**

It is already enough work to support the latest stable official version.

There is no support for XPrivacy on Ice Cream Sandwich (ICS) or Jelly Bean anymore. 
There is limited support for XPrivacy on KitKat.
Limited support means I will try to fix bugs, but only if it doesn't take much time.

**I will not look into issues of applications that cost money or have root access.**

**There is no support for anything other than privacy (no support for game cheating, root cloaking, etc.)**

Please check [the limitations](#limitations) before reporting a bug, requesting a feature, or asking a question.

#### Bugs

**First ask if other people have encountered the same bug!** (see questions below)

If you encounter a bug, please [create an issue](https://github.com/M66B/XPrivacy/issues).

Please describe the exact steps to reproduce the issue, including the wrong and expected result,
and include information about your device type, and Android and XPrivacy versions.

To increase the chance I can find and fix the bug, please read [here](http://www.chiark.greenend.org.uk/~sgtatham/bugs.html).

Include a [logcat](#FAQ14) when relevant (use [gist](https://gist.github.com/) or a similar service).
Try to keep the logcat as brief as possible, include just the crash/problem and a few dozen lines around it.
I have looked into many long logcats in the past, too often without any result.
Therefore I will not look into long logcats anymore.

**One bug report per issue please!**

**Do not forget to enable XPrivacy debug logging using the settings menu!**

**Before submitting any issue, please make sure you are running the latest version of XPrivacy.**

**Before submitting any issue, please make sure XPrivacy is causing the problem by temporarily disabling XPrivacy and seeing if that resolves the issue.**

#### Feature requests

If you have a feature request, please [create an issue](https://github.com/M66B/XPrivacy/issues).

New features are only considered for implementation when requested on GitHub with a detailed description of the feature and only if there are ten +1's within two weeks.
You can promote your feature request on XDA, but for a maximum of two times only. Feature requests promoted more than two times will be closed and not be considered for implementation anymore.
See [here](http://forum.xda-developers.com/showpost.php?p=51574315&postcount=8776) for some more information.

Please read [here](http://forum.xda-developers.com/showpost.php?p=52644313&postcount=9241) before voting.

Implementation of new features is dependent on contributions to the XPrivacy project.
Please read [here](http://forum.xda-developers.com/xposed/modules/xprivacy-ultimate-android-privacy-app-t2320783/post57469136#post57469136) for more information.

**One feature request per issue please!**

#### Questions

If you have any questions, please leave a message in the [XDA XPrivacy  thread](http://forum.xda-developers.com/showthread.php?p=42488236).
More people follow the support thread than the GitHub issue tracker, which increases your chance of receiving a helpful answer.
Moreover, the answers given might be beneficial to more people than you alone.

**Answering questions on XDA is left to the community.**
You can read [here](http://forum.xda-developers.com/showpost.php?p=54391559&postcount=10814) why.

For questions about Xposed, please use [the Xposed XDA thread](http://forum.xda-developers.com/xposed).

**Please do not ask questions on GitHub!**

GitHub issues are for bug reports and feature requests.

Changelog
---------

The changelog is [here](https://github.com/M66B/XPrivacy/blob/master/CHANGELOG.md).

Similar Solutions
-----------------

* [PDroid](http://forum.xda-developers.com/showthread.php?t=1357056)
* [PDroid 2.0](http://forum.xda-developers.com/showthread.php?t=1923576)
* [OpenPDroid](http://forum.xda-developers.com/showthread.php?t=2098156)
* [LBE Privacy Guard](https://play.google.com/store/apps/details?id=com.lbe.security.lite) (now LBE Security Master)
* [CyanogenMod Incognito Mode](https://plus.google.com/100275307499530023476/posts/6jzWcRR6hyu) (now Paranoid Android Privacy Guard)
* [Per App Settings Module](http://forum.xda-developers.com/showthread.php?t=2072081)
* [Android 4.3+ Permission Manager](http://www.androidpolice.com/2013/07/25/app-ops-android-4-3s-hidden-app-permission-manager-control-permissions-for-individual-apps/)
* [SRT AppGuard](http://www.srt-appguard.com/en/)
* [DonkeyGuard](http://forum.xda-developers.com/showthread.php?t=2831556)

The *PDroid* family provides fake or no data, more or less in the same way as XPrivacy does.
A difference is that you need to patch Android and that there is (therefore) only limited stock ROM support.
The PDroid family is open source. The *PDroid* family is not supported anymore.

*LBE Privacy Guard* revokes permissions, which will make some applications unusable.
LBE Privacy Guard also features malware protection and data traffic control.
Some consider the closed source code of Chinese origin to be a problem.

The members of the PDroid family and XPrivacy hardly use any memory, but LBE Privacy Guard does.

The *CyanogenMod Incognito Mode* seems not to be fine grained and provides only privacy for personal data,
like contacts, if the associated content provider chooses to do so.

The *Per App Settings Module* revokes permissions like LBE Privacy Guard does.
This modules offers many other interesting features.

The *Android 4.3+ Permission Manager* is like *CyanogenMod Incognito Mode*.

*SRT AppGuard* does not require root, and therefore revokes permissions by uninstalling the application to be monitored
and reinstalling a modified version. Without a backup, application data will be lost in this process.
Compared to XPrivacy, permission control is not as fine grained and comprehensive.
System applications cannot be restricted.

After over a year of silence, the author of PDroid 2.0 released *DonkeyGuard*.
*DonkeyGuard* is not open source.

XPrivacy can restrict more data than any of the above solutions,
even for closed source applications and libraries, like Google Play services.
Unlike any other solution, XPrivacy has [crowd sourced restrictions](http://crowd.xprivacy.eu/).

I do not recommend using XPrivacy in combination with any of the similar solutions; this could result in conflicts and potential data leaks.

I need all my time developing XPrivacy, so I will not test XPrivacy alongside any of the similar solutions.
If you test XPrivacy alongside any of the similar solutions, you can probably help others by reporting your test results.

<a name="news"></a>In The Media
------------

* [Manage Individual App Permissions with XPrivacy - XDA Developer](http://www.xda-developers.com/android/manage-individual-app-permissions-with-xprivacy/) (June 20, 2013)
* [XPrivacy Gives You Massive Control Over What Your Installed Apps Are Allowed To Do - Android Police](http://www.androidpolice.com/2013/06/23/xprivacy-gives-you-massive-control-over-what-your-installed-apps-are-allowed-to-do/) (June 23, 2013)
* [Protect Your Privacy with XPrivacy - XDA Developer TV](http://www.xda-developers.com/android/protect-your-privacy-with-xprivacy-xda-developer-tv/) (July 17, 2013)
* [Black Duck Announces Open Source Rookies of the Year Winners - Black Duck Software](http://www.blackducksoftware.com/news/releases/black-duck-announces-open-source-rookies-year-winners) (January 28, 2014)
* [The Open Source Rookies of the Year Awards - InfoWorld](http://www.infoworld.com/d/open-source-software/the-open-source-rookies-of-the-year-awards-235116) (January 28, 2014)
* [XPrivacy تطبيق](http://waleedhassan.wordpress.com/2014/01/31/xprivacy/) (January 31, 2014)
* [Out in the Open: How to Protect Your Secrets From Nosey Android Apps - Wired](http://www.wired.com/2014/03/x-privacy/) (March 31, 2014)
* [Android privacy tool feeds fake data to prying apps - Wired UK](http://www.wired.co.uk/news/archive/2014-04/01/x-privacy-android-app) (April 1, 2014)
* [Internet Vandaag](http://www.bnr.nl/radio/bnr-internet-vandaag/708487-1404/internet-vandaag-74) (April 7, 2014)
* [Protecting Your Privacy: App Ops, Privacy Guard, and XPrivacy - XDA Developers](http://www.xda-developers.com/android/protecting-your-privacy-app-ops-privacy-guard-and-xprivacy/) (June 11, 2014)
* [XPrivacy – Android ohne Google?! Teil6](http://www.kuketz-blog.de/xprivacy-android-ohne-google-teil6/) (September 23, 2014)

Contributing
------------

Translations:

* Translations to other languages are welcomed
* Check if the language [is supported by Android](http://stackoverflow.com/questions/7973023/what-is-the-list-of-supported-languages-locales-on-android) and find its locale
* Copy [this file](https://github.com/M66B/XPrivacy/blob/master/res/values/strings.xml) to the correct locale folder
* Translate the strings in the copied file and omit all lines with **translatable="false"**
* Create a [pull request](https://help.github.com/articles/using-pull-requests) for the new/updated translation
* If you don't know how to create a pull request, you can send the translated file [via XDA PM](http://forum.xda-developers.com/member.php?u=2799345)

Current translations:

1. Bulgarian (bg)
1. Catalan (ca)
1. Czech (cs)
1. Croatian (hr)
1. Danish (da)
1. Dutch/Flemish (nl)
1. English
1. Estonian (ee)
1. Farsi (Persian) (fa)
1. Finnish (fi)
1. French (fr)
1. German (de)
1. Greek (el)
1. Hebrew (he/iw)
1. Hindi (hi)
1. Hungarian (hu)
1. Indonesian (in)
1. Irish (ga)
1. Italian (it)
1. Japanese (ja)
1. Korean (ko)
1. Kurdish (ku-rIR, ku-rIQ)
1. Lithuanian (lt)
1. Malay (ms)
1. Norwegian (nb-rNO, nn-rNO, no-rNO)
1. Polish (pl)
1. Portuguese (pt)
1. Romanian (ro)
1. Russian (ru)
1. Serbian (sr)
1. Simplified Chinese (zh-rCN)
1. Slovak (sk)
1. Slovenian (sl)
1. Spanish (es)
1. Swedish (sv)
1. Tagalog (tl-PH)
1. Traditional Chinese (zh-rTW)
1. Turkish (tr)
1. Ukrainian (ua)
1. Vietnamese (vi)
1. Welsh (cy-rGB)

Restrict new data:

* Find the package/class/method that exposes the data (look into the Android documentation/sources)
* Create a class that extends [XHook](https://github.com/M66B/XPrivacy/blob/master/org/thoughtland/xlocation/XHook.java)
* Hook the methods in [XPrivacy](https://github.com/M66B/XPrivacy/blob/master/org/thoughtland/xlocation/XPrivacy.java)
* Write a before and/or after method to restrict the data
* Do a [pull request](https://help.github.com/articles/using-pull-requests) if you want to contribute

Using [Eclipse](http://www.eclipse.org):

* Download and install the [ADT Bundle](http://developer.android.com/sdk/index.html)
* Clone the GitHub project to a temporary location
* Import the GitHub project into Eclipse, copy the files
* Close Eclipse and copy the project from the temporary location over the imported project
	* Make sure you copy all hidden files and folders (especially the .git folders)
	* This step might not be necessary anymore for recent Eclipse releases

Testing:

* [XPrivacy Tester](https://github.com/M66B/XPrivacyTester) (developers only)
* [Elixir 2](https://play.google.com/store/apps/details?id=com.bartat.android.elixir)
* [Network Info II](https://play.google.com/store/apps/details?id=aws.apps.networkInfoIi)

The goal of the XPrivacy project is to provide a free and open source privacy solution for Android to as many as possible people.

To keep XPrivacy maintainable, hooking into private/internal classes and methods is undesirable,
since these vary considerably in different Android versions
and are often customized/modified by manufacturers and custom ROM builders.

To prevent applications from crashing, fake data should be returned whenever possible.
Empty values (*null*) should not be replaced by fake values to prevent misuse.
The same applies to creating new fake data (for example, an account).
There is no need to apply restrictions in situations where the user is presented a dialog first (for example, to pick an account).
Setting data (in contrast to getting data) should never be restricted; this is outside the goal of XPrivacy.

Application-specific code is undesirable, because it could result in maintenance and support problems.

XPrivacy is intended to restrict applications, but is not intended to restrict Android itself
(although this is often possible as a side effect).

See [here](https://www.openhub.net/p/xprivacy/) for XPrivacy code metrics.

Please note that you agree to the license below by contributing, including the copyright.

License
-------

[GNU General Public License version 3](http://www.gnu.org/licenses/gpl.txt)

Copyright (c) 2013-2015 Marcel Bokhorst ([M66B](http://forum.xda-developers.com/member.php?u=2799345))

All rights reserved

This file is part of XPrivacy.

XPrivacy is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your discretion) any later version.

XPrivacy is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with XPrivacy.  If not, see [http://www.gnu.org/licenses/](http://www.gnu.org/licenses/).
