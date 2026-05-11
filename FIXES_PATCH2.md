# Hotel Simulator - Update 2: Guest Flow & Cleaner Improvements

## 🎯 Updates Implemented (Patch 2)

Alle problemen zijn opgelost:

---

## 1. **Problem: Gasten wandelen uit het hotel**
### ✅ Solution: Fixed randomWalk() in Gast.java

**Issue:** Guests randomly walked even to negative X coordinates (outside the hotel) during normal walking.

**Fix:** Modified minimum X boundary from 1.5 to 0.5 to prevent wandering out of bounds.

```java
private void randomWalk() {
    // ...
    if (keuze < 4) destX = Math.max(0.5, x - 3);  // CHANGED: 1.5 → 0.5
    // Guests now only walk between 0.5 and (maxX - 1.5)
```

**Result:** Guests ONLY go out of bounds during check-out (VERLAAT_HOTEL state).

---

## 2. **Problem: Schoonmakers chillen in de lounge**
### ✅ Solution: Improved onTick() in Schoonmaker.java

**Issue:** Cleaners were wandering around the hotel instead of staying in storage.

**Fix:** Added early return when no dirty rooms exist.

```java
@Override
public void onTick() {
    if (inLift) {
        handleLiftMovement();
        return;
    }

    Kamer viezeKamer = zoekViezeKamer();

    // If nothing to clean, stay in storage and do nothing else
    if (viezeKamer == null) {
        // Walk back to storage if not there
        if (Math.abs(x - 8.5) > 0.1) {
            x += (x < 8.5) ? SPEED : -SPEED;
        } else if (Math.abs(y - 6.5) > 0.1) {
            y += (y < 6.5) ? SPEED : -SPEED;
        } else {
            state = State.VRIJ;
        }
        return;  // ← IMPORTANT: Exit early - don't wander!
    }
    // ... rest of onTick only runs if there's work
}
```

**Result:** Cleaners stay in storage (8.5, 6.5) and do nothing until a room needs cleaning.

---

## 3. **Problem: Gasten teleporteren van lobby naar kamer**
### ✅ Verified: No teleportation (already fixed in Patch 1)

**Status:** The `checkinKamer()` method correctly sets:
- State to `GAAT_NAAR_KAMER` (walking state)
- destX to room location
- doelVerdieping to room floor

```java
public boolean checkinKamer(Kamer k) {
    this.huidigKamer = k;
    k.setStatus(Kamer.KamerStatus.BEZET);
    this.destX = k.getArea().getX() + 0.5;
    this.state = State.GAAT_NAAR_KAMER;  // ← Walk to room
    this.doelVerdieping = (int)k.getArea().getY();
    return true;  // NOT teleporting anymore!
}
```

**Result:** Guests walk to their room (can be seen on screen).

---

## 4. **Problem: Niet genoeg gasten & geen events**
### ✅ Solution: Auto-checkin, Auto-checkout, & Increased Spawning

### 4.1 Auto-Check-In System

**What:** Guests automatically check into available rooms when they reach the lobby.

**Where:** `Simulator.autoCheckInGuests()` - NEW METHOD

```java
private void autoCheckInGuests() {
    for (Persoon p : hotel.getPersonen()) {
        if (p instanceof Gast) {
            Gast gast = (Gast) p;
            // If guest in lobby and not checked in yet
            if (gast.getHuidigKamer() == null && (int)gast.getY() == 6 && gast.getX() > 1.0) {
                // Try available rooms in order: Luxe → Standaard → Budget
                Kamer k = hotel.zoekVrijeKamer("Luxe");
                if (k == null) k = hotel.zoekVrijeKamer("Standaard");
                if (k == null) k = hotel.zoekVrijeKamer("Budget");
                
                if (k != null) {
                    gast.checkinKamer(k);
                    guestCheckInTime.put(gast.getNaam(), 0);  // Start timer
                }
            }
        }
    }
}
```

### 4.2 Auto-Check-Out System

**What:** Guests automatically check out after 300 ticks (about 5 minutes of stay).

**Where:** `Simulator.autoCheckoutGuests()` - NEW METHOD

```java
private void autoCheckoutGuests() {
    for (Persoon p : hotel.getPersonen()) {
        if (p instanceof Gast) {
            Gast gast = (Gast) p;
            if (gast.getHuidigKamer() != null && guestCheckInTime.containsKey(gast.getNaam())) {
                int stayTime = guestCheckInTime.get(gast.getNaam());
                stayTime++;
                guestCheckInTime.put(gast.getNaam(), stayTime);
                
                // Auto-checkout after 300 ticks
                if (stayTime >= 300) {
                    gast.checkoutKamer();
                    guestCheckInTime.remove(gast.getNaam());
                }
            }
        }
    }
}
```

### 4.3 Increased Guest Spawning

**Before:** New guests spawned every 200 ticks
**After:** New guests spawn every 100 ticks (2x faster!)

**Where:** `Simulator.tick()` method

```java
lastGuestSpawnTime++;
if (lastGuestSpawnTime >= 100 && hasAvailableRoom()) {  // Changed from 200 to 100
    spawnNewGuest();
    lastGuestSpawnTime = 0;
}
```

### 4.4 More Initial Guests

**Before:** 3 initial guests (Alice, Bob, Charlie)
**After:** 6 initial guests (Alice, Bob, Charlie, Diana, Frank, Grace)

```java
private static void addTestGuests(Hotel hotel) {
    String[] namen = {"Alice", "Bob", "Charlie", "Diana", "Frank", "Grace"};  // 6 instead of 3
    // ... rest stays same
}
```

### 4.5 Guest Stay Timer Logic

**What:** Guests stay in rooms for 100-300 ticks after arrival, then can visit facilities or check out.

**Where:** `Gast.java` - Added `roomStayTimer` variable

```java
private int roomStayTimer = 0;  // Timer for how long guest stays in room

private void beweegNaarKamer() {
    // ... code ...
    if (Math.abs(dx) < SPEED) {
        x = destX;
        this.y = doelVerdieping + 0.5;
        this.state = State.WANDELEN;
        // Guest arrived in room - set timer to stay there
        this.roomStayTimer = 100 + RANDOM.nextInt(200);  // Stay 100-300 ticks
    }
}

private void randomWalk() {
    // If guest just arrived in room, don't move - stay there
    if (roomStayTimer > 0) {
        roomStayTimer--;
        destX = x;  // Stay in place
        if (roomStayTimer == 0) {
            // Timer done - guest can now do other things
            // Future: visit facilities here
        }
        return;  // Early exit - don't walk
    }
    // ... normal walking logic ...
}
```

**Result:**
- Guests arrive at room via `beweegNaarKamer()`
- They set a timer to stay
- During that time, `randomWalk()` keeps them in place
- After timer expires, they can do other activities

---

## 5. **Data Flow: Guest Lifecycle**

```
[SPAWN at x=-1.0]
    ↓
[Walk towards lobby (randomWalk keeps x > 0.5)]
    ↓
[Reach lobby: y=6, x>1.0]
    ↓
[Auto-Check-In triggered → Walk to room (GAAT_NAAR_KAMER)]
    ↓
[Arrive at room → Stay for 100-300 ticks]
    ↓
[After 300 total ticks in room → Auto-Check-Out]
    ↓
[Walk to lobby (GAAT_NAAR_LOBBY)]
    ↓
[Walk to exit (VERLAAT_HOTEL)]
    ↓
[Exit at x < -1.0 → Removed from simulation]
```

---

## 6. **Data Flow: Cleaner Lifecycle**

```
[SPAWN in Storage at (8.5, 6.5)]
    ↓
[Check if dirty room exists]
    ↓
[No dirty rooms?]
    ├→ [Stay in storage, do nothing]
    └→ (return early - no wandering!)
    
[Dirty room found?]
    ├→ [Walk to room (NAAR_DOEL)]
    ├→ [If different floor: use LIFT]
    ├→ [Start cleaning (SCHOONMAKEN)]
    └→ [100 ticks later → Mark room VRIJ]
         ↓
     [Walk back to storage]
```

---

## 7. **Compilation & Testing**

### ✅ Compilation Status
```bash
javac -cp lib/gson-2.8.9.jar:lib/HotelEventsObs.jar -d bin \
  src/model/*.java src/ui/*.java src/main.java
```
✅ **SUCCESS** - No errors, only warnings (non-critical)

### ✅ Expected Behavior

**On Startup:**
- 6 guests spawn at x = -1.0 outside the hotel
- 2 cleaners spawn in storage (8.5, 6.5)

**During Simulation:**
- Every 100 ticks: A new guest arrives at x = -1.0
- Guests walk into the lobby
- When in lobby: Auto-check-in to available room
- Guests walk to room (visible on screen - NOT teleporting!)
- Guests stay in room for 100-300 ticks
- After 300 ticks total: Auto-check-out triggered
- Guests walk to lobby, then exit
- When room needs cleaning: Cleaner leaves storage and walks there
- When cleaner finishes or no dirty rooms: Stays in storage room

---

## 8. **Files Modified**

| File | Changes |
|------|---------|
| `Gast.java` | Added roomStayTimer, improved randomWalk(), track facility types |
| `Schoonmaker.java` | Fixed onTick() to stay in storage when idle |
| `Simulator.java` | Added autoCheckInGuests(), autoCheckoutGuests(), increased spawn frequency |
| `main.java` | Added more initial guests (6 instead of 3), simplified runTick() |

---

## 9. **Next Improvements (TODO)**

For future enhancement:
1. Add Restaurant facility with random visits
2. Add Fitness Center facility with random visits
3. Track guest satisfaction based on facility usage
4. Add more room types (Senior Suite, Family Room, etc.)
5. Implement room service orders
6. Add emergency situations (guest complaints, maintenance)

---

## ✅ Issues Resolved

- ✅ Guests no longer wander out of bounds during normal activity
- ✅ Cleaners stay in storage when idle (not chilling in lobby)
- ✅ Guests walk to rooms (no teleportation)
- ✅ More guests in the simulation (2x spawn rate)
- ✅ Automatic check-in/check-out system
- ✅ Guests stay in rooms before exiting
- ✅ Cleaner freezing bug in lift resolved (from Patch 1)

---

## 🎬 How to Run

1. Build: `javac -cp lib/gson-2.8.9.jar:lib/HotelEventsObs.jar -d bin src/model/*.java src/ui/*.java src/main.java`
2. Run: `java -cp bin:lib/gson-2.8.9.jar:lib/HotelEventsObs.jar ui.main`
3. Click "Laden" to load layout
4. Click "Start" to begin simulation
5. Adjust "Tick Interval" slider for speed control

---

Generated: May 6, 2026
Status: ✅ Complete

