# 🔧 Fix: Schoonmaker Blijft in Opslag

## Problem
Schoonmaker moet in opslag (8.5, 6.5) BLIJVEN totdat een gast werkelijk uit de kamer uitcheckt.

## Solution

### 1. onTick() - Beter idle management
```java
if (viezeKamer == null) {
    // If we were going to a room, cancel and abort mission
    if (state == State.NAAR_DOEL) {
        state = State.VRIJ;
        huidigKamer = null;
    }
    
    // Return to storage and stay there
    if (Math.abs(x - 8.5) > 0.1) {
        x += (x < 8.5) ? SPEED : -SPEED;
    } else if (Math.abs(y - 6.5) > 0.1) {
        y += (y < 6.5) ? SPEED : -SPEED;
    } else {
        state = State.VRIJ;
    }
    return;  // Exit early - don't do anything else!
}
```

**Result:** 
- Als geen vuile kamers: CANCEL huidige missie
- Teruggaan naar opslag
- Blijven wachten totdat er werk is

---

### 2. beweeg() - Check kamer is nog steeds vuil
```java
private void beweeg(double tx, double ty, Kamer doelKamer) {
    // Check: Is the target room still dirty? If not, abort!
    if (doelKamer.getStatus() != Kamer.KamerStatus.SCHOONMAKEN) {
        state = State.VRIJ;
        huidigKamer = null;
        return;  // Abort mission
    }
    
    // ... rest of movement logic ...
    
    // Also check again at arrival
    if (doelKamer != null && doelKamer.getStatus() == Kamer.KamerStatus.SCHOONMAKEN) {
        // Start cleaning only if still dirty
        this.huidigKamer = doelKamer;
        this.state = State.SCHOONMAKEN;
    }
}
```

**Result:**
- Bij elke stap: controleer of kamer nog vuil is
- Als niet: abort en ga terug naar opslag
- Voorkom dat Schoonmaker schone kamers gaat "schoonmaken"

---

### 3. handleLiftMovement() - Check na lift verlaten
```java
private void handleLiftMovement() {
    this.x = lift.getX();
    this.y = lift.getY();

    if (Math.abs(lift.getY() - (doelVerdieping + 0.5)) < 0.2 && lift.isIdle()) {
        // Double-check: Is there still a dirty room?
        Kamer viezeKamer = zoekViezeKamer();
        if (viezeKamer == null) {
            // No more work! Abort and go back to storage
            lift.verwijderGast(this);
            this.inLift = false;
            this.y = doelVerdieping + 0.5;
            this.state = State.VRIJ;
            return;
        }
        
        // Continue if work still exists
        lift.verwijderGast(this);
        this.inLift = false;
        this.state = State.NAAR_DOEL;
    }
}
```

**Result:**
- Zelfs NADAT de lift arriveert: check opnieuw
- Gast kan inmiddels zijn uitgecheckt, kamer is schoon
- Ga direct terug naar opslag zonder nutteloos werk te doen

---

## Lifecycle: Schoonmaker Nu

```
┌─ IN STORAGE (8.5, 6.5) ◄────┐
│ Waiting (State.VRIJ)         │
│ Check: Any dirty rooms?      │
│                              │
├─ YES ──────────────┐         │
│                    ▼         │
├─ Walk to room     ├─ NO ─────┘
│ (NAAR_DOEL)       │
│                   │ ABORT
├─ Lift (if needed) │ (return to storage)
│ Check again!   ◄──┘
│ (if no work → return)
│
├─ Arrive at room
│ Final check: still dirty?
│ (if not → return to storage)
│
├─ Clean (SCHOONMAKEN)
│ 100 ticks
│
├─ Mark room FREE
│ Set state to VRIJ
│
└─ Go back to storage ──┐
                        └──► LOOP
```

---

## Key Guarantees

✅ Schoonmaker blijft in opslag totdat kamer WERKELIJK dirty is
✅ Mid-journey abort als gast ineens uitcheckt
✅ Na lift: dubbelcheck voordat werk start
✅ Geen verspilde bewegingen naar schone kamers
✅ Altijd teruggaan naar storage (8.5, 6.5) als geen werk

---

## Files Modified
- ✅ `src/model/Schoonmaker.java`
  - onTick(): Added state check and abort logic
  - beweeg(): Added room status validation
  - handleLiftMovement(): Added final check before working

---

## Compilation
✅ SUCCESS - No errors

