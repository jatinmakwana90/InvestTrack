# InvestTrack — Android Investment Portfolio Tracker

A full-featured Android app for tracking investments across multiple accounts with support for Mutual Funds, Equity, Fixed Deposits, Bonds, ETFs, and more.

---

## Features

- **Multiple Accounts** — Create Individual, Joint, Demat, NRI, Corporate accounts
- **Security Master** — Manage Mutual Funds, Equity, FD, Bonds, ETF, Gold, PPF, NPS, Crypto
- **Dashboard** — Consolidated portfolio value, P&L, and per-account summaries
- **Folio / Holdings View** — Per-account holdings with avg cost, CMP, units, P&L
- **Transaction View** — Full transaction history per account or per folio
- **Folio Detail** — Drill into a single security holding with all its transactions
- **Transaction Types** — BUY, SELL, SIP, SWP, SWITCH IN/OUT, DIVIDEND, BONUS, SPLIT, INTEREST, MATURITY

---

## Tech Stack

| Layer | Library |
|---|---|
| Language | Kotlin |
| UI | Material Components 3, ViewBinding |
| Architecture | MVVM + StateFlow |
| Database | Room |
| DI | Hilt |
| Navigation | Jetpack Navigation Component |
| Charts | MPAndroidChart |

---

## Project Structure

```
app/src/main/java/com/investtrack/
├── data/
│   ├── dao/           # Room DAOs (Account, Security, Transaction)
│   ├── database/      # InvestTrackDatabase
│   ├── models/        # Account, Security, Transaction, Folio, PortfolioSummary
│   └── repository/    # InvestmentRepository (single source of truth)
├── di/                # Hilt DatabaseModule
├── ui/
│   ├── dashboard/     # DashboardFragment + ViewModel + AccountSummaryAdapter
│   ├── accounts/      # AccountDetail, AddAccount, FolioDetail Fragments + ViewModels
│   ├── securities/    # Securities list + AddSecurity Fragments
│   └── transactions/  # AddTransaction Fragment
└── utils/             # CurrencyFormatter
```

---

## Setup & Build

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Gradle 8.4

### Steps

#### 1. Extract the ZIP

```bash
# On macOS/Linux
unzip InvestTrack.zip -d InvestTrack
cd InvestTrack

# On Windows (PowerShell)
Expand-Archive -Path InvestTrack.zip -DestinationPath InvestTrack
cd InvestTrack
```

#### 2. Open in Android Studio

- Launch Android Studio
- Choose **File → Open** and select the `InvestTrack` folder
- Wait for Gradle sync to complete (first sync downloads dependencies, ~2–3 min)

#### 3. Build & Run

**From Android Studio:**
- Select a device/emulator (API 26+)
- Click ▶ Run

**From command line:**
```bash
# macOS/Linux
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

The APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

#### 4. Install APK directly

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## GitHub Upload

1. Create a new repository on github.com (empty, no README)
2. From the extracted project folder:

```bash
git init
git add .
git commit -m "Initial commit: InvestTrack Android app"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/InvestTrack.git
git push -u origin main
```

---

## How to Use

1. **Add Securities** — Go to Securities tab → tap `+` → fill in name, type, current price
2. **Add Account** — Dashboard → tap `New Account` → fill name and type
3. **Add Transactions** — Tap an account → tap `+` FAB → select security, type, qty, price
4. **View Holdings** — Tap an account → Holdings tab shows folio-level view
5. **View Transactions** — Tap an account → Transactions tab for full history
6. **Drill into a Folio** — Tap any holding to see its dedicated detail screen

---

## Supported Security Types

| Type | Key Fields |
|---|---|
| Mutual Fund | AMC, Category, NAV |
| Equity | Exchange (NSE/BSE), Sector |
| Fixed Deposit | Interest Rate, Maturity Date |
| Bond | Interest Rate, Maturity Date, ISIN |
| ETF | Exchange |
| Gold, PPF, NPS, Crypto | Price |

---

## Extending the App

- **Price Updates**: Hook `SecurityDao.updatePrice()` to a live market data API
- **Charts**: `MPAndroidChart` is already a dependency — add pie/line charts to Dashboard
- **Export**: Add CSV export from `TransactionDao.getAllTransactions()`
- **Notifications**: Use WorkManager for SIP reminders or price alerts
