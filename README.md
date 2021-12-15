# tickertape

A new-age Stock tracker and charter.

[![Get it on Google Play](https://raw.githubusercontent.com/pyamsoft/tickertape/main/art/google-play-badge.png)][1]

# What

A hobby project frontend for Yahoo Finance data bundled up in a simple Android application.

## Privacy

TickerTape requires the following Android permissions to run:

**android.permission.ACCESS_NETWORK_STATE** - To check if there is available internet connection.  
**android.permission.INTERNET** - To talk to the Yahoo Finance "API", and to check for updates  
and show other related applications by pyamsoft.  
**com.android.vending.BILLING** - For In-App Billing related operation.  
**android.permission.RECEIVE_BOOT_COMPLETED** - For WorkManager, and to respond to device boot by  
running background operations
**android.permission.FOREGROUND_SERVICE** - To show the optional always-on ticker tape notification
which tracks a user-defined list of stock tickers.

## Development

TickerTape is developed in the Open on GitHub at:
```
https://github.com/pyamsoft/tickertape
```

**PLEASE NOTE:** TickerTape is **not a fully FOSS application.**
TickerTape is open source, and always will be. It is free as in beer, but not free as in speech.
All features of TickerTape (and any pyamsoft Android applications) will be zero-cost. You will
never be asked to purchase an in-app product to unlock a feature in the app. You have the option of
using an in-app purchase to send a "support token" to the developer, but this is neither an
expectation nor an obligation for the user.

TickerTape talks via HTTP based API calls to the Yahoo Finance "API" which is a not an open source
API. No personally identifying data is intentionally sent to Yahoo Finance, but pyamsoft does not
have control over or knowledge about what the Yahoo Finance "API" is doing in the background. The
Yahoo Finance "API" is a non-free network service.

TickerTape is not completely FOSS due to the fact that it relies on the Google Play In-App Billing
library for in-app purchases. The Google Play library is proprietary, and requires a device using
the proprietary Google Play Services to use it. Aside from this single Google Play In-App Billing
library, the entire application and all of it's libraries are (should be to the best of my
knowledge) fully open source. TickerTape will never try to track, analyze, or invade your
privacy intentionally. Any such discoveries of unintentional tracking from TickerTape should be
brought to the attention of the developer via a GitHub Issue to be fixed as quickly as possible.

# Issues or Questions

Please post any issues with the code in the Issues section on GitHub. Pull Requests
will be accepted on GitHub only after extensive reading and as long as the request
goes in line with the design of the application.

[1]: https://play.google.com/store/apps/details?id=com.pyamsoft.tickertape

## License

Apache 2

```
Copyright 2021 Peter Kenji Yamanaka

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
