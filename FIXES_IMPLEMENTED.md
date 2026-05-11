# Hotel Simulator - Bug Fixes & Improvements

## Summary
Implemented comprehensive fixes to address liftbug freezing issues, improved guest flow system, and fixed cleaner behavior. All changes ensure realistic simulation dynamics with proper lift handling and guest state management.

---

## 1. **Gast.java (Guest) - Major Fixes**

### 1.1 Added New State: `GAAT_NAAR_KAMER`
- **What:** New state to handle guests walking to their assigned room
- **Why:** Prevents teleportation; guests now walk to rooms
- **Lines:** 26-36 (State enum)
- **Code:**
```java
private enum State {
    WANDELEN,
    NAAR_LIFT_WACHTEN,
    WACHTEN_OP_VERVOER,
    IN_LIFT,
    GAAT_NAAR_FACILITEIT,
    IN_FACILITEIT,
    GAAT_NAAR_KAMER,  // NEW
    GAAT_NAAR_LOBBY,
    VERLAAT_HOTEL
}
```

### 1.2 Added `beweegNaarKamer()` Method
- **What:** Guest walks to assigned room instead of teleporting
- **Why:** Realistic movement simulation
- **Lines:** 154-169
- **Code:**
```java
private void beweegNaarKamer() {
    // Check if we need to change floors
    if ((int)y != doelVerdieping) {
        wiltVerdiepingWisselen();
    } else {
        // On the correct floor, move to room X
        double dx = destX - x;
        if (Math.abs(dx) < SPEED) {
            x = destX;
            this.y = doelVerdieping + 0.5;
            this.state = State.WANDELEN;
        } else {
            x += (dx > 0) ? SPEED : -SPEED;
        }
    }
}
```

### 1.3 Fixed `checkinKamer()` - NO MORE TELEPORTATION
- **What:** Modified to not teleport directly to room
- **Why:** Guests should walk to their room, not appear there instantly
- **Lines:** 206-214
- **Change:**
```java
// BEFORE: 
// this.x = k.getArea().getX() + 0.5;
// this.y = k.getArea().getY() + 0.5;
// this.destX = this.x;

// AFTER:
this.destX = k.getArea().getX() + 0.5;
this.state = State.GAAT_NAAR_KAMER;  // NEW
this.doelVerdieping = (int)k.getArea().getY();
```

### 1.4 Added `doelVerdieping` Tracking
- **Already Existed:** Line 20
- **Usage:** Properly set during check-in and lift operations
- **Benefit:** Ensures proper floor tracking for lift exit logic

---

## 2. **Schoonmaker.java (Cleaner) - Bug Fixes**

### 2.1 Added `doelVerdieping` Variable
- **What:** Tracks which floor the cleaner needs to reach
- **Why:** Was missing, causing lift bugs when cleaner gets stuck
- **Lines:** 17
- **Code:**
```java
private int doelVerdieping;
```

### 2.2 Improved `onTick()` - Idle Behavior
- **What:** Cleaners now properly stay in storage when there's no work
- **Why:** Prevents aimless wandering; realistic idle state
- **Lines:** 33-63
- **Code:**
```java
@Override
public void onTick() {
    if (inLift) {
        handleLiftMovement();
        return;
    }

    // 1. Zoek werk
    Kamer viezeKamer = zoekViezeKamer();

    // 2. Check if nothing to do - stay in storage
    if (viezeKamer == null) {
        if (Math.abs(x - 8.5) > 0.1) {
            x += (x < 8.5) ? SPEED : -SPEED;
        } else if (Math.abs(y - 6.5) > 0.1) {
            y += (y < 6.5) ? SPEED : -SPEED;
        } else {
            state = State.VRIJ;
        }
        return;  // Important: exit early, don't move further
    }
    // ... rest of method
}
```

### 2.3 Fixed `stapInLift()` - Set doelVerdieping
- **What:** Now properly sets the floor destination
- **Why:** Previous bug: doelVerdieping was never set, causing lift freeze
- **Lines:** 102-110
- **Code:**
```java
private void stapInLift(int doelVerdieping) {
    if (lift != null && Math.abs(lift.getY() - y) < 0.2 && lift.isIdle()) {
        if (lift.voegGastToe(this)) {
            this.inLift = true;
            this.doelVerdieping = doelVerdieping;  // KEY FIX
            this.state = State.IN_LIFT;
            lift.roepNaar(doelVerdieping);
        }
    }
}
```

### 2.4 Fixed `handleLiftMovement()` - Use doelVerdieping
- **What:** Now uses stored `doelVerdieping` to properly exit lift
- **Why:** Was checking the wrong target floor before
- **Lines:** 113-124
- **Code:**
```java
private void handleLiftMovement() {
    this.x = lift.getX();
    this.y = lift.getY();

    // Exit lift when at the correct floor and lift is idle
    if (Math.abs(lift.getY() - (doelVerdieping + 0.5)) < 0.2 && lift.isIdle()) {
        lift.verwijderGast(this);
        this.inLift = false;
        this.y = doelVerdieping + 0.5;  // Set exact position
        this.state = State.NAAR_DOEL;
    }
}
```

---

## 3. **Simulator.java - Dynamic Guest System & Cleanup**

### 3.1 Added Guest Spawning & Removal
- **What:** Guests now spawn dynamically and are removed when they leave
- **Why:** Realistic continuous flow; prevent memory issues
- **Lines:** 7-9 (Fields), 101-102 (Removal logic)
- **Code:**
```java
private Area lobbyArea;
private int lastGuestSpawnTime = 0;

// In tick():
hotel.getPersonen().removeIf(p -> p instanceof Gast && p.getX() < -1.0);
```

### 3.2 Dynamic Guest Spawning
- **What:** New guests arrive automatically every 200 ticks if rooms available
- **Why:** Makes simulation more dynamic; realistic hotel flow
- **Lines:** 104-109
- **Code:**
```java
// Periodically spawn new guests
lastGuestSpawnTime++;
if (lastGuestSpawnTime >= 200 && hasAvailableRoom()) {
    spawnNewGuest();
    lastGuestSpawnTime = 0;
}
```

### 3.3 Added `spawnNewGuest()` Method
- **What:** Creates new random guests with realistic names
- **Why:** Enables continuous simulation stream
- **Lines:** 121-139
- **Includes:** Random name selection, guest initialization, proper listener registration

### 3.4 Added `hasAvailableRoom()` Helper
- **What:** Checks if any rooms are available
- **Why:** Prevents spawning when hotel is full
- **Lines:** 114-119

### 3.5 Fixed Guest Spawn Position
- **What:** Guests now spawn at x = -1.0 (outside hotel)
- **Why:** Realistic entrance; guests walk in naturally
- **Lines:** 63-65 in `initialiseerPersonen()`
- **Code:**
```java
if (lobbyArea != null) {
    // Spawn at x = -1.0 outside the hotel
    double startX = -1.0;
    double startY = (lobbyArea.getY() - 1) + 0.5;
    g.setStartPositie(startX, startY);
}
```

---

## 4. **main.java - Initial Guest Spawning**

### 4.1 Fixed `addTestGuests()` Method
- **What:** Initial test guests now spawn at x = -1.0
- **Why:** Consistency with dynamic spawning system
- **Lines:** 155-164
- **Code:**
```java
private static void addTestGuests(Hotel hotel) {
    String[] namen = {"Alice", "Bob", "Charlie"};
    // Spawn at x = -1.0 (out of bounds)
    for (int i = 0; i < namen.length; i++) {
        Gast g = new Gast(namen[i], -1, 0);
        g.setHotel(hotel);
        g.setStartPositie(-1.0, 6.5); // Spawn outside at lobby level
        hotel.addPersoon(g);
    }
}
```

---

## 5. **Persoon.java (Already Fixed) - Confirmed Good**

- ✅ Has `setStartPositie()` method
- ✅ Uses protected variables: x, y, destX
- ✅ Can be overridden by subclasses correctly

---

## Key Issues Fixed

| Issue | Root Cause | Solution | Result |
|-------|-----------|----------|--------|
| **Lift Freeze** | Schoonmaker missing `doelVerdieping` | Added variable, set in `stapInLift()` | Lift now works for cleaners |
| **Teleportation** | `checkinKamer()` directly set x,y | Now sets state to `GAAT_NAAR_KAMER` | Realistic room walk-in |
| **Cleaner Idle** | No proper idle in storage | Improved `onTick()` logic | Cleaners stay put when no work |
| **Guest Removal** | Ghosts still in simulation after checkout | Added removal in `tick()` when x < -1 | Memory cleanup |
| **Spawn Position** | Guests appeared inside lobby | Changed to x = -1.0 (outside) | Natural entry flow |

---

## Testing Recommendations

1. **Lift Test:**
   - Spawn cleaner, mark room dirty → should move to room
   - Cross floors with cleaner → should use lift smoothly

2. **Guest Flow Test:**
   - Guests start at x = -1.0, walk into lobby
   - Check-in triggered → guests walk to rooms (not teleport)
   - Check-out → guests walk to lobby, then exit

3. **Dynamic Spawn Test:**
   - Run simulation > 200 ticks with free rooms
   - New guests should appear at x = -1.0

4. **Memory Test:**
   - Run long simulation
   - Check that guests are properly removed when they leave (x < -1)

---

## Compilation
✅ Successfully compiles without errors:
```bash
javac -cp lib/gson-2.8.9.jar:lib/HotelEventsObs.jar -d bin src/model/*.java src/ui/*.java src/main.java
```

---

## Files Modified
- ✅ `/src/model/Gast.java` - State machine & room walk logic
- ✅ `/src/model/Schoonmaker.java` - Lift bug fixes & idle behavior  
- ✅ `/src/model/Simulator.java` - Dynamic spawning & removal
- ✅ `/src/main.java` - Initial spawn position

