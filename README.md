# 🎒 InPlan

**A group expense tracker for trips, built for the "who paid for what" chaos.**

[▶ Watch Demo](https://youtube.com/shorts/V0y3oy4Tcww?feature=share)

---

## Core Features

| Feature | Description |
|---|---|
| Trip Groups | Create a trip, add friends to it (via invite link/QR/phone number) |
| Expense Logging | Log an expense: amount, payer, category (cab/food/stay/misc), and who's splitting it |
| Flexible Splitting | Equal split, custom amounts, percentage split, or split among a chosen subset of the group |
| Live Balances | Real-time "who owes whom" view per trip, updated as soon as an expense is added |
| Debt Simplification | Minimizes total number of payments needed to settle everyone up (Splitwise-style netting) |
| Payment Reminders | Automatic, low-friction nudges for pending dues — no need to awkwardly ask in person |
| UPI Settle-Up | One-tap "Pay now" via UPI deep link for the person who owes |
| Trip History | Past trips, total spend, and per-person spend history |
| In-app Trip Chat | Optional light chat/comments per trip, for quick "who's in for cab #2"-type coordination |

## Installation

### Prerequisites
- Android Studio (latest stable)
- A [Supabase](https://supabase.com) project (free tier is enough to start)
- Kotlin 1.9+ / Jetpack Compose BOM (set via Android Studio project template)

### Supabase Setup
1. Create a new project at [supabase.com](https://supabase.com).
2. In the SQL editor, create tables for `trips`, `trip_members`, `expenses`, and `expense_splits`.
3. Enable **Row Level Security (RLS)** on all tables so users only see trips they're a member of.
4. Grab your **Project URL** and **anon public key** from Project Settings → API.
5. (Optional, for live balances) Enable **Realtime** on the `expenses` and `expense_splits` tables.

### Android App Setup
1. Clone the repo and open it in Android Studio.
2. Add your Supabase credentials to `local.properties` or a `secrets.properties` file (don't commit this):
   ```
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_ANON_KEY=your-anon-key
   ```
3. Sync Gradle and run on an emulator or physical device.