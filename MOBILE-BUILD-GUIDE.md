# Sirf Mobile se APK Banane ki Guide (GitHub Actions)

Ye guide bina kisi computer ke, sirf phone se app compile karne ke liye hai. Cloud mein (GitHub ke server par) build hoga, aap sirf files upload karoge aur final APK download karoge.

---

## STEP 1 — GitHub Account Banao
1. Chrome mein [github.com](https://github.com) kholo
2. **Sign up** karo (free hai) — email, username, password
3. Login kar lo

---

## STEP 2 — Naya Repository Banao
1. Top-right corner mein **+** icon → **New repository**
2. Naam do: `SmartKeyboard`
3. **Public** select karo (private bhi chalega, lekin public mein free build minutes zyada milte hain)
4. **Create repository** dabao

---

## STEP 3 — Project Files Upload Karo
Maine jo `SmartKeyboard-project.zip` diya tha, use pehle **unzip** karna hoga phone mein:
1. Phone ke Files app mein zip par tap karo → **Extract/Unzip** karo
2. Ab GitHub wali website par apne naye repository mein jao
3. **"Add file" → "Upload files"** par tap karo
4. Extract ki hui `SmartKeyboard` folder ke andar se **saari files aur folders** select karo (build.gradle, app folder, .github folder, sab kuch — root `SmartKeyboard` folder ke andar jo bhi hai)
5. Upload karke neeche **Commit changes** dabao

**Zaroori:** `.github/workflows/build.yml` file bhi upload honi chahiye — yahi file batati hai GitHub ko APK kaise banani hai. Kuch mobile file managers hidden `.github` folder show nahi karte — agar aisa ho, GitHub website par hi seedhe "Add file → Create new file" se ye file manually bana sakte ho (maine niche content diya hai).

---

## STEP 4 — Firebase Config Add Karo
1. [Firebase Console](https://console.firebase.google.com) par jao (mobile browser se hi ho jayega)
2. Add project → Android app add karo → package name: `com.smartkeyboard.app`
3. `google-services.json` download karo
4. GitHub repo mein jao → `app` folder ke andar → **Add file → Upload files** → wahi `google-services.json` upload karo

---

## STEP 5 — Build Chalao
1. GitHub repo ke top mein **"Actions"** tab par tap karo
2. Left side mein **"Build APK"** workflow dikhega, usko select karo
3. **"Run workflow"** button dabao → phir se **"Run workflow"** confirm karo
4. 2-5 minute wait karo (cloud mein build ho raha hoga — page refresh karte raho)
5. Jab green ✅ tick dikhe, uss build entry par tap karo

---

## STEP 6 — APK Download Karo
1. Us build page ke neeche **"Artifacts"** section mein `SmartKeyboard-debug-apk` dikhega
2. Usko tap karke download karo (zip file aayegi)
3. Us zip ko extract karo → andar `app-debug.apk` milegi

---

## STEP 7 — Phone Mein Install Karo
1. Us `.apk` file par tap karo
2. Phone poochega "Unknown apps install karne ki permission do" — allow karo
3. Install ho jayega
4. App kholo → keyboard enable karo (jaisa pehle bataya tha)

---

## Agar `.github/workflows/build.yml` file manually banani pade
GitHub website par apne repo mein **"Add file" → "Create new file"** karo, naam do:
```
.github/workflows/build.yml
```
(GitHub khud `.github` aur `workflows` folders bana lega jab aap ye naam type karoge)

Phir uska content wahi paste karo jo maine zip mein diya tha.

---

## Agar kahin fasso
Jo bhi error aaye (chahe upload mein, chahe build mein), uska **screenshot bhej do** — us hisaab se exact fix bata dunga. Ye process pehli baar thoda lamba lag sakta hai, lekin ek baar set ho gaya to future mein sirf code change karke dobara "Run workflow" dabana hoga.
