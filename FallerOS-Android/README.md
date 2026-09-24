# Faller'OS — aplikacja na Androida

To jest **kompletny, gotowy do zbudowania projekt Android Studio** — natywna
powłoka (WebView) uruchamiająca cały system Faller'OS spakowany lokalnie
w aplikacji. Nie jest to gotowy plik `.apk` — środowisko, w którym pracuje
Claude, nie ma zainstalowanego Android SDK ani dostępu do internetu
potrzebnego do jego pobrania, więc **skompilowanie APK musisz wykonać u
siebie**, w Android Studio (za darmo, kilka kliknięć, patrz niżej).

## Co jest w środku

- `app/src/main/assets/www/` — cały Faller'OS (index.html, manifest.json,
  service-worker.js, ikony) dokładnie taki, jak wersja webowa.
- `MainActivity.kt` — WebView z `WebViewAssetLoader`, dzięki czemu
  localStorage, Service Worker i `fetch()` działają tak samo jak
  w przeglądarce, ale bez żadnego serwera.
- Ikona aplikacji wygenerowana z fioletowo-czarnego logo Faller'OS
  (mipmap-mdpi … mipmap-xxxhdpi).
- Obsługa przycisku „Wstecz” systemu Android, wgrywania plików (np. zdjęć
  do galerii) i pobierania plików z Menedżera plików na dysk urządzenia.

## Jak zbudować APK (najprościej)

1. Zainstaluj [Android Studio](https://developer.android.com/studio) (darmowe).
2. Otwórz folder `FallerOS-Android` jako projekt (File → Open).
3. Poczekaj, aż Android Studio samo pobierze Gradle i zależności
   (pasek postępu na dole okna).
4. Kliknij **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
5. Gotowy plik `app-debug.apk` znajdziesz w
   `app/build/outputs/apk/debug/` — prześlij go na telefon i zainstaluj
   (włącz „Instalowanie z nieznanych źródeł” dla pliku).

Żeby opublikować aplikację w Google Play, użyj **Build → Generate Signed
App Bundle / APK** i przejdź kreator podpisywania (wymaga własnego klucza
podpisu — Android Studio pomoże go utworzyć).

## Bez Android Studio — opcja A: w chmurze GitHub Actions (zero instalacji)

Ta opcja nie wymaga instalowania niczego na Twoim komputerze — kompilacja
dzieje się na serwerach GitHuba. W projekcie jest już gotowy workflow
`.github/workflows/build-apk.yml`.

1. Załóż darmowe konto na [github.com](https://github.com) (jeśli jeszcze
   nie masz) i utwórz nowe, puste repozytorium (Public lub Private).
2. Wgraj do niego całą zawartość folderu `FallerOS-Android` — najłatwiej
   przez przeglądarkę: **Add file → Upload files**, przeciągnij cały
   folder (Chrome/Edge zachowają strukturę podfolderów), a potem
   **Commit changes**. Jeśli wolisz `git`, standardowe `git init`,
   `git add .`, `git commit`, `git push` też zadziała.
3. Wejdź w zakładkę **Actions** w repozytorium — workflow „Zbuduj APK
   Faller'OS” uruchomi się automatycznie po wgraniu plików (albo kliknij
   **Run workflow**, jeśli chcesz go odpalić ręcznie).
4. Poczekaj, aż build się zakończy (parę minut), wejdź w ukończony run
   i pobierz gotowy plik z sekcji **Artifacts → FallerOS-debug-apk**
   (spakowany w .zip — w środku jest `app-debug.apk`).
5. Prześlij APK na telefon i zainstaluj.

## Bez Android Studio — opcja B: z terminala na własnym komputerze

Potrzebujesz tylko JDK 17 i Android SDK Command-line Tools (dużo
lżejsze niż całe Android Studio, bez żadnego IDE/GUI):

1. Zainstaluj JDK 17, np. `sudo apt install openjdk-17-jdk` (Linux) albo
   pobierz [Temurin 17](https://adoptium.net/) (Windows/Mac).
2. Pobierz [Android SDK Command-line Tools](https://developer.android.com/studio#command-tools)
   i rozpakuj je, np. do `~/android-sdk/cmdline-tools/latest/`.
3. Zainstaluj potrzebne komponenty:
   ```bash
   export ANDROID_HOME=~/android-sdk
   yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses
   $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
   ```
4. Zainstaluj [Gradle](https://gradle.org/install/) (projekt nie zawiera
   pliku wrappera `gradlew`, więc potrzebny jest systemowy `gradle`).
5. W folderze `FallerOS-Android` uruchom:
   ```bash
   gradle assembleDebug
   ```
6. Gotowy plik znajdziesz w `app/build/outputs/apk/debug/app-debug.apk`.

## Najprostsza alternatywa: bez kompilowania czegokolwiek

Wersja webowa Faller'OS ma już włączone wsparcie PWA. Jeśli nie chcesz
budować natywnej aplikacji, wystarczy na Androidzie:

1. Otworzyć `index.html` w Chrome (z lokalnego serwera lub hostingu —
   PWA wymaga http/https, nie działa z `file://`).
2. Menu Chrome → **Dodaj do ekranu głównego / Zainstaluj aplikację**.

Otrzymasz ikonę na pulpicie i okno bez paska adresu — bardzo zbliżone
do natywnej aplikacji, zero budowania.

## Znane ograniczenia tej wersji natywnej

- Pierwsze uruchomienie Pythona (Pyodide) i instalacja pakietów pip nadal
  wymaga internetu (jak w wersji webowej) — potem WebView je cache'uje.
- „Pobierz na dysk” zapisuje pliki do prywatnego folderu aplikacji
  (`Android/data/com.falleros.app/files/Download/`) — w pełni działające,
  ale bez integracji z systemowym „Pobrane”. Rozszerzenie o MediaStore to
  naturalny kolejny krok, jeśli chcesz to dopracować.
- Ikona aplikacji jest wygenerowana automatycznie z istniejącej grafiki
  Faller'OS — nie jest to jeszcze „adaptacyjna ikona” (adaptive icon) na
  Androidzie 8+; działa poprawnie, ale możesz ją podmienić na własną
  w `res/mipmap-*/ic_launcher.png`.
