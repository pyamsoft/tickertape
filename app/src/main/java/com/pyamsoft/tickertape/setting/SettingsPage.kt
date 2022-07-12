package com.pyamsoft.tickertape.setting

interface SettingsPage

sealed class TopLevelSettingsPage : SettingsPage {
  object AppSettings : TopLevelSettingsPage()
}
