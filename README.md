# VYRO — Connect Beyond Limits

**VYRO** is a production-grade, native Android social media and real-time messaging application developed with **Kotlin**, **Jetpack Compose**, and **Firebase (Cloud Firestore & Authentication)**. VYRO introduces a visual identity based on **3D Glassmorphism**, frosted translucent glass surfaces, ambient radial lighting, real-time encrypted messaging, 24-hour social stories, voice note recording/playback, and voice/video calling.

---

## 🚀 Key Modules & Capabilities

### 1. 3D Glassmorphism Design System
- **Frosted Glass Cards (`GlassCard`)**: Subtle border gradients, translucent backgrounds, rounded corners (18–26dp).
- **Ambient Lighting (`AmbientGlowBackground`)**: Canvas radial glow blending Electric Violet (`#8B7CFF`) and Cyber Cyan (`#55D6BE`).
- **Translucent Bottom Bar (`VyroGlassBottomBar`)**: 5 primary navigation tabs with animated active indicators.
- **Brand Colors**:
  - Primary Background: `#080B14`
  - Secondary Background: `#111827`
  - Primary Accent: `#8B7CFF` (Electric Violet)
  - Secondary Accent: `#55D6BE` (Cyber Cyan)
  - Tertiary Accent: `#FF6584` (Sunset Coral)
  - Text Primary: `#FFFFFF` | Text Secondary: `#A8B0C2`

### 2. Authentication System
- **Jetpack Credential Manager**: Google Sign-In with `GetSignInWithGoogleOption` and auto-generated `default_web_client_id`.
- **Zero-Trust Auth Gate**: Composable auth boundary in `MainActivity` with real-time `AuthStateListener`.
- **Profile Initialization**: Seamless onboarding capturing Display Name, Username, Bio, and Avatar.

### 3. Social Media Feed & Stories
- **Dynamic Post Cards**: Text, high-res visual media, location tags, and hashtags.
- **Engagement**: Real-time Likes, Comments bottom sheet, and Bookmarks.
- **24-Hour Stories**: Horizontal stories bar with animated iridescent gradient rings, story viewer with timed progress bar, and quick emoji reactions.
- **Post Composer**: Caption, photo presets, location, and hashtag tags.

### 4. Real-Time Chat & Group Messaging
- **Direct & Group Chats**: Real-time synchronization powered by Cloud Firestore snapshot listeners.
- **Rich Message Types**: Text messages, image attachments, and voice notes with duration counter.
- **Status & Receipts**: Online green indicators, delivered double checkmarks, and timestamps.
- **Filter Tabs**: Instant filtering by All, Direct, and Groups.

### 5. Voice & Video Calling
- **Voice & Video Modes**: Fullscreen interactive call screen.
- **Call Controls**: Toggle mic mute, toggle camera stream, toggle speakerphone, and live duration timer.
- **Incoming Call Overlay**: Animated pulsing caller badge with Accept (green) and Decline (red) actions.

### 6. Search, Explore & Discovery
- **Explore Feed**: Trending hashtags (`#VyroFuture`, `#CyberArt`, `#TechAI`), suggested accounts, and search.
- **User Search**: Prefix querying across username records.

### 7. Profile, Notifications & Settings
- **User Profile**: Posts grid/list, saved bookmarks tab, followers & following stats, and Edit Profile dialog.
- **Notifications**: Inbox tracking Likes, Comments, Missed Calls, and Follow events.
- **Settings**: Theme preferences, language toggle (English & বাংলা Bangla), privacy toggles, Help & Support, Terms, and Sign Out.

---

## 🛡️ Zero-Trust Security Rules & Testing

The application adheres to the **8 Pillars of Hardened Firestore Rules**:
- Default-deny catch-all rule `match /{document=**} { allow read, write: if false; }`.
- Standalone validation helpers: `isValidUserProfile()`, `isValidPost()`, `isValidComment()`, `isValidStory()`, `isValidConversation()`, `isValidMessage()`, `isValidCall()`, `isValidNotification()`.
- Immutability guards for `createdAt`, `userId`, and `senderId`.
- Upper-bounded timestamps with `data.createdAt <= request.time`.
- Two-tier security validation:
  1. **Tier 1 (JavaScript Rules Tests)**: `FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js` (5/5 tests passing).
  2. **Tier 2 (Kotlin JVM Tests)**: `gradle :app:testDebugUnitTest --tests '*Rule*'` (Robolectric repository integration tests passing).

---

## 📦 Build & Run Instructions

### 1. Build and Run in Development
Run the standard debug build:
```bash
gradle assembleDebug
```
The debug APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 2. Build Release APK
```bash
gradle assembleRelease
```
The release APK is generated at:
```
app/build/outputs/apk/release/app-release-unsigned.apk
```

### 3. Build Android App Bundle (AAB) for Google Play
```bash
gradle :app:bundleRelease
```
The bundle is generated at:
```
app/build/outputs/bundle/release/app-release.aab
```

---

## 📂 Project Architecture

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/
│   ├── MainActivity.kt                # Root Auth gate & navigation orchestrator
│   ├── data/
│   │   ├── models/VyroModels.kt       # Firestore typed models with @DocumentId & @ServerTimestamp
│   │   ├── repository/VyroRepository.kt# Firestore data access layer & Flows
│   │   └── util/FirestoreErrorHandler.kt# Structured JSON diagnostic error logger
│   └── ui/
│       ├── components/GlassComponents.kt# 3D Glassmorphism design primitives
│       ├── navigation/VyroScreen.kt   # 5-tab navigation definitions
│       ├── screens/
│       │   ├── auth/AuthScreen.kt     # Google Sign-In & feature onboarding
│       │   ├── feed/                  # Feed, PostCard, Comments, StoriesRow, CreatePost
│       │   ├── stories/               # StoryViewerDialog, CreateStoryDialog
│       │   ├── chat/                  # ChatListScreen, ConversationDetailScreen
│       │   ├── calls/                 # ActiveCallScreen, IncomingCallDialog
│       │   ├── explore/ExploreScreen.kt
│       │   ├── profile/               # ProfileScreen, EditProfileDialog
│       │   ├── notifications/NotificationsScreen.kt
│       │   └── settings/SettingsScreen.kt
│       └── theme/                     # Color.kt, Theme.kt, Type.kt
└── res/
    ├── drawable/ic_vyro_logo.jpg      # Custom 3D winged 'V' iridescent logo
    ├── drawable/ic_launcher_foreground.xml
    ├── drawable/ic_launcher_background.xml
    ├── mipmap-*/                      # Scaled PNG icons across mdpi-xxxhdpi
    └── values/
        ├── firebase_applet_config.xml # Custom database ID
        └── strings.xml                # app_name = "VYRO"
```
