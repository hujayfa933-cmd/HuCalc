package com.example.calculator.ui.localization

import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage {
    ENGLISH,
    BANGLA
}

class HuCalcStrings(val language: AppLanguage) {
    val isBangla: Boolean = language == AppLanguage.BANGLA

    // App Identity
    val appName: String = "HuCalc"
    val appSubtitle: String = if (isBangla) "সাধারণ ও বৈজ্ঞানিক ক্যালকুলেটর" else "General & Scientific Calculator"
    val developerCredit: String = "Made by Hujayfa"
    val versionLabel: String = if (isBangla) "সংস্করণ ১.০.৬" else "Version 1.0.6"

    // Top Bar Modes
    val modeGeneral: String = if (isBangla) "সাধারণ" else "GENERAL"
    val modeScientific: String = if (isBangla) "বৈজ্ঞানিক" else "SCIENTIFIC"
    val mathAiButton: String = if (isBangla) "গণিত AI" else "MATH AI"

    // 3-Dot Overflow Menu
    val moreOptions: String = if (isBangla) "আরও অপশন" else "More Options"
    val menuHistory: String = if (isBangla) "ইতিহাস" else "History"
    val menuThemeSilver: String = if (isBangla) "থিম: সিলভার (দিন)" else "Theme: Silver (Day)"
    val menuThemeDark: String = if (isBangla) "থিম: ডার্ক প্লাটিনাম (রাত)" else "Theme: Dark Platinum (Night)"
    val menuThemeAuto: String = if (isBangla) "থিম: সিস্টেম অটো" else "Theme: System Auto"
    val menuHapticsOn: String = if (isBangla) "ভাইব্রেশন: চালু" else "Haptics: ON"
    val menuHapticsOff: String = if (isBangla) "ভাইব্রেশন: বন্ধ" else "Haptics: OFF"

    // Language Menu Items
    val menuLanguageHeader: String = if (isBangla) "ভাষা নির্বাচন" else "SELECT LANGUAGE"
    val menuLangEnglish: String = if (isBangla) "English (ইংরেজি)" else "English"
    val menuLangBangla: String = if (isBangla) "বাংলা (Bangla)" else "বাংলা (Bangla)"

    val menuSettings: String = if (isBangla) "সেটিংস" else "Settings"
    val menuAbout: String = if (isBangla) "HuCalc সম্পর্কে" else "About HuCalc"
    val menuAboutSub: String = "v1.0.6 • Made by Hujayfa"

    // Quick Bar
    val quickAns: String = if (isBangla) "উত্তর" else "Ans"
    val quickSteps: String = if (isBangla) "💡 সমাধান" else "💡 Steps"
    val quickBrandLine: String = "HuCalc • Made by Hujayfa • v1.0.6"

    // Settings Dialog
    val settingsTitle: String = if (isBangla) "সেটিংস" else "Settings"
    val settingsSubtitle: String = if (isBangla) "পছন্দ এবং কনফিগারেশন" else "Preferences & Options"

    val sectionTheme: String = if (isBangla) "থিম এবং প্রদর্শন" else "Appearance & Theme"
    val themeSilverTitle: String = if (isBangla) "সিলভার অ্যালুমিনিয়াম (দিন)" else "Silver Aluminum (Day)"
    val themeSilverDesc: String = if (isBangla) "ক্লাসিক ক্যাসিও fx-991ES হার্ডওয়্যার সিলভার" else "Classic Casio fx-991ES hardware silver"
    val themeDarkTitle: String = if (isBangla) "ডার্ক প্লাটিনাম (রাত)" else "Dark Platinum (Night)"
    val themeDarkDesc: String = if (isBangla) "কালো টাইটানিয়াম ব্রাশড চেসিস" else "Tactical dark titanium brushed chassis"
    val themeSystemTitle: String = if (isBangla) "সিস্টেম ডিফল্ট" else "System Default"
    val themeSystemDesc: String = if (isBangla) "অ্যান্ড্রয়েড সিস্টেমের ডার্ক মোড অনুসরণ করে" else "Follows Android system dark mode"

    val sectionLanguage: String = if (isBangla) "অ্যাপের ভাষা (Language)" else "App Language (ভাষা)"
    val langEnglishTitle: String = "English"
    val langEnglishDesc: String = if (isBangla) "আন্তর্জাতিক ইংরেজি ইন্টারফেস" else "International English interface"
    val langBanglaTitle: String = "বাংলা (Bengali)"
    val langBanglaDesc: String = if (isBangla) "সম্পূর্ণ বাংলা ইন্টারফেস ও মেনু" else "Complete Bengali interface and menus"

    val sectionHaptics: String = if (isBangla) "স্পর্শ ও ভাইব্রেশন" else "Tactile & Vibrations"
    val hapticsTitle: String = if (isBangla) "বোতামের ভাইব্রেশন" else "Keypress Haptics"
    val hapticsDescOn: String = if (isBangla) "স্পর্শ ভাইব্রেশন সক্রিয়" else "Tactile vibration enabled"
    val hapticsDescOff: String = if (isBangla) "শব্দহীন / ভাইব্রেশন বন্ধ" else "Silent / vibration disabled"

    val sectionDefaults: String = if (isBangla) "গণনার ডিফল্ট মান" else "Calculation Defaults"
    val angleTitle: String = if (isBangla) "কোণের একক" else "Angle Unit"
    val angleDescDeg: String = if (isBangla) "ডিগ্রি (DEG) • ৩৬০°" else "Degrees (DEG) • 360°"
    val angleDescRad: String = if (isBangla) "রেডিয়ান (RAD) • ২π" else "Radians (RAD) • 2π"

    val sectionHistory: String = if (isBangla) "ইতিহাস সংরক্ষণ" else "History Storage"
    val clearHistoryTitle: String = if (isBangla) "গণনার ইতিহাস মুছুন" else "Clear History"
    val clearHistoryDesc: String = if (isBangla) "সংরক্ষিত সব অতীত গণনা মুছে ফেলে" else "Removes all saved past calculations"
    val historyEmptyCount: String = if (isBangla) "খালি" else "Empty"
    fun historyItemsCount(count: Int): String = if (isBangla) "$count টি হিসাব সংরক্ষিত" else "$count items saved"

    val confirmClearHistoryTitle: String = if (isBangla) "ইতিহাস মুছে ফেলবেন?" else "Clear All History?"
    val confirmClearHistoryMsg: String = if (isBangla)
        "এটি আপনার সমস্ত অতীত হিসাব স্থায়ীভাবে মুছে ফেলবে। এটি ফিরিয়ে আনা যাবে না।"
    else
        "This will permanently remove all past calculations. This action cannot be undone."

    val btnCancel: String = if (isBangla) "বাতিল" else "Cancel"
    val btnClear: String = if (isBangla) "মুছুন" else "Clear"
    val btnDone: String = if (isBangla) "সম্পন্ন" else "Done"
    val btnOk: String = if (isBangla) "ঠিক আছে" else "OK"

    // History Sheet
    val historySheetTitle: String = if (isBangla) "গণনার ইতিহাস" else "Calculation History"
    val historySearchPlaceholder: String = if (isBangla) "সমীকরণ বা ফলাফল খুঁজুন..." else "Search expressions or results..."
    val historyTabAll: String = if (isBangla) "সব" else "All"
    val historyTabScientific: String = if (isBangla) "বৈজ্ঞানিক" else "Scientific"
    val historyTabGeneral: String = if (isBangla) "সাধারণ" else "General"
    val historyNoResults: String = if (isBangla) "কোন ফলাফল পাওয়া যায়নি" else "No matching calculations found"
    val historyEmptyState: String = if (isBangla) "এখনও কোন হিসাবের ইতিহাস নেই" else "No calculation history yet"
    val historyEmptySub: String = if (isBangla) "আপনার সমাধান করা হিসাবগুলো এখানে সংরক্ষিত হবে" else "Your solved calculations will be stored here automatically"
    val historyUseResult: String = if (isBangla) "ফলাফল ব্যবহার" else "Use Result"
    val historyUseExpression: String = if (isBangla) "সমীকরণ ব্যবহার" else "Use Expression"
    val historyDeleteTooltip: String = if (isBangla) "মুছে ফেলুন" else "Delete"

    // About Dialog
    val aboutDesc: String = if (isBangla)
        "দৈনন্দিন গণিত ও উন্নত বৈজ্ঞানিক হিসাব-নিকাশের জন্য একটি দ্রুত, নির্ভুল এবং ১০০% অফলাইন ক্যালকুলেটর। সাথে রয়েছে অন-ডিভাইস ম্যাথ অ্যাসিস্ট্যান্ট যা কোনো ইন্টারনেট ছাড়াই ধাপ অনুসারে সমস্যার সমাধান প্রদান করে।"
    else
        "A fast, accurate, 100% offline calculator for everyday mathematics and advanced scientific calculations, powered with On-Device Math Assistant for step-by-step problem solving with zero internet needed."

    val whatsNewTitle: String = if (isBangla) "✨ নতুন যা যুক্ত হয়েছে:" else "✨ What's New in v1.0.6:"
    val whatsNewContent: String = if (isBangla)
        "• ৩ ডট মেনু ও সেটিংসে বাংলা এবং ইংরেজি উভয় ভাষার পূর্ণ সমর্থন\n• গণনার ইতিহাসে তাৎক্ষণিক অনুসন্ধান ও ফিল্টারিং\n• সক্রিয় [M] সূচক সহ মেমোরি ব্যাংক ইন্সপেক্টর\n• অতি-দ্রুত সাব-সেকেন্ড বুট ও ল্যাগহীন ইঞ্জিন"
    else
        "• Full Bangla and English dual language support in 3-dot menu and Settings\n• Instant search & filter in calculation history\n• Memory bank inspector with active [M] indicator\n• Ultra-fast sub-second boot and zero lag engine"

    val offlineBadge: String = if (isBangla)
        "১০০% অফলাইন • অন-ডিভাইস ম্যাথ সলভার • fx-991ES ইঞ্জিন"
    else
        "100% Offline • On-Device Math Solver • fx-991ES Engine"
}

val LocalHuCalcStrings = staticCompositionLocalOf { HuCalcStrings(AppLanguage.ENGLISH) }
