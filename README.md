# Smart Keyboard — Setup Guide

Ye poora Android Studio project hai. Bas 3 cheezein karni hain:

## STEP 1 — Project kholo
1. Android Studio kholo → **Open** → is folder (`SmartKeyboard`) ko select karo
2. Gradle sync hone do (thoda time lagega, neeche progress bar dikhega)

## STEP 2 — Firebase Connect karo (zaroori!)
Is project mein `google-services.json` file **missing hai** — bina iske app build nahi hogi.

1. [Firebase Console](https://console.firebase.google.com) par jao
2. **Add Project** → naam do → create karo
3. **Add app → Android** icon par click karo
4. Package name daalo: `com.smartkeyboard.app` (ye exact match hona chahiye)
5. `google-services.json` file download hogi
6. Us file ko copy karke `SmartKeyboard/app/` folder mein paste karo (root build.gradle ke bagal wale app folder mein, jahan build.gradle hai)
7. Firebase Console mein **Firestore Database** banao → **Test mode** mein start karo

## STEP 3 — Run karo
1. Apna Android phone USB se connect karo (USB debugging ON hona chahiye), ya ek Emulator chalao
2. Android Studio mein green **▶ Run** button dabao
3. App phone mein install ho jayegi

## STEP 4 — Keyboard Enable karo
1. App kholo → "1. Keyboard ko Enable karo" button dabao → Settings khulegi
2. Wahan **Smart Keyboard** ko ON/Enable karo
3. Wapas app mein aake "2. Smart Keyboard select karo" dabao → keyboard select karo
4. Ab WhatsApp/kahin bhi type karo — globe icon (🌐) dabake "Smart Keyboard" choose karo

## Kya kaam karta hai abhi
- Har letter type hoga normal keyboard ki tarah
- Jab **Enter/Send** dabaoge, ab tak jo type kiya wo Firebase **Firestore** mein `typed_entries` collection mein save ho jayega
- Password fields automatically **skip** hote hain (security ke liye) — wahan kuch save nahi hota
- Kaunsi app se text aaya (WhatsApp/Facebook/etc.) wo bhi record hota hai

## Abhi jo missing hai (aage add karna hoga)
- [ ] Symbols/numbers ka doosra keyboard layout (abhi "?123" button sirf placeholder hai)
- [ ] Emoji panel
- [ ] Clipboard history panel
- [ ] Firebase Authentication (taaki data sirf aapka account access kare)
- [ ] App icon (abhi default Android icon use ho raha hai)

Koi bhi error aaye Android Studio mein, uska exact message bhejo — us hisaab se fix bata dunga.
