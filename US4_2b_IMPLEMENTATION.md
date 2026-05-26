# US4.2.b - Validatie van onlogische events op basis van simulatiestatus

## 📋 Beschrijving

Implementatie van validatie van binnenkomende DLL-events zodat alleen toegestane events per simulatiestatus verwerkt worden.

## ✅ Acceptatiecriteria

| Gegeven | Wanneer | Dan |
|---------|---------|-----|
| Simulatie staat in status "Stopped" | DLL stuurt "ProcessData" event (alleen geldig in "Running") | Event wordt geweigerd als "Illogical" |
| | | Systeemstatus blijft "Stopped" |
| | | Waarschuwing wordt gelogd in systeemlogs |

---

## 🔧 Implementatie

### Wijziging 1: EventBusImpl.java - Voeg Simulator-referentie toe

**Locatie:** Regel 25-32

```java
// --- US4.2.b EVENT VALIDATION ---
// Referentie naar Simulator voor validatie van DLL-events op basis van running-status
private Simulator simulator;

/**
 * US4.2.b: Set de Simulator referentie zodat EventBus de running-status kan checken
 */
public void setSimulator(Simulator simulator) {
    this.simulator = simulator;
}
```

**Wat gebeurt hier:**
- `private Simulator simulator;` → Voeg een veld toe dat naar de Simulator wijst
- `setSimulator(Simulator simulator)` → Setter zodat de Simulator zichzelf kan koppelen

**Waarom:** EventBus moet kunnen controleren of de simulatie draait (`simulator.isRunning()`).

---

### Wijziging 2: EventBusImpl.java - Vervang handleExternalDLLEvent met validatie

**Locatie:** Regel 37-105 (complete vervanging)

**De kernlogica (stap-voor-stap):**

```java
// STAP 1: Bepaal of simulatie draait
boolean simIsRunning = false;
if (this.simulator != null) {
    try {
        simIsRunning = this.simulator.isRunning();
    } catch (Exception e) {
        System.err.println("⚠️ [EventBus] Fout bij status-check...");
        simIsRunning = false;
    }
}
```

**Uitleg:**
- `simIsRunning = false` → Start defensief (veilig)
- Check of simulator ingesteld is (`!= null`)
- Haal `isRunning()` op (true/false)
- Foutafhandeling: als iets fout gaat, behandel als "niet-running"

---

```java
// STAP 2: Valideer event
if ("ProcessData".equalsIgnoreCase(dllEventName)) {
    if (!simIsRunning) {
        // ❌ ILLOGICAL: Event geweigerd
        String warningMsg = "ILLOGICAL_DLL_EVENT: '" + dllEventName + "' geweigerd — simulatie niet in RUNNING status";
        System.out.println("  ❌ [VALIDATIE] " + warningMsg);
        logEvent(warningMsg);
        return;  // 🔑 KRITIEK: Stop hier!
    }
}
```

**Uitleg:**
- Check of het event "ProcessData" is
- Check of simulatie NIET draait (`!simIsRunning`)
- Zo ja: Log waarschuwing in console EN event-log
- **BELANGRIJK:** Early `return` → methodestop, `systemState` wordt NIET gewijzigd!

---

```java
// STAP 3: Normale event-verwerking (alleen bereikt als validatie passed)
switch (dllEventName) {
    case "ProcessData":
        System.out.println("  → [Interne Logica] Verwerking van externe data...");
        this.systemState = "PROCESSING_DATA";
        break;
    // ... overige events ...
}
```

**Uitleg:**
- Deze code wordt ALLEEN bereikt als:
  1. Event "ProcessData" is EN simulatie DRAAIT, OF
  2. Event is een ander type (geen validatie nodig)
- Pas `systemState` alleen aan voor geldige events

---

### Wijziging 3: Simulator.java - Koppel EventBus aan Simulator

**Locatie:** Regel 40 (direct na `new EventBusImpl()`)

```java
this.eventBus = new EventBusImpl();  // Initialize event system
this.eventBus.setSimulator(this);   // US4.2.b: Koppel simulator aan EventBus
```

**Wat gebeurt hier:**
- Direct na EventBus creatie: zeg tegen EventBus "ik (de Simulator) ben je referentie"
- Nu kan EventBus `simulator.isRunning()` aanroepen

---

## 🔄 Flow-diagram

```
User klikt "Pause"
    ↓
simulator.pause()
    ↓
simulator.running = false
    ↓
DLL stuurt event (bijv. ExternalEventProvider.start())
    ↓
eventBus.handleExternalDLLEvent("ProcessData")
    ↓
simIsRunning = simulator.isRunning() → FALSE
    ↓
if ("ProcessData" && !simIsRunning) → TRUE
    ↓
❌ LOG: "ILLOGICAL_DLL_EVENT: ProcessData geweigerd"
    ↓
RETURN (metodestop)
    ↓
systemState blijft ongewijzigd ✅
```

---

## 🧪 Test-scenario

### Scenario 1: Simulatie GESTOPT (Failed event)

```java
simulator.pause();  // running = false
eventBus.handleExternalDLLEvent("ProcessData");
```

**Verwacht:**
- Console: `❌ [VALIDATIE] ILLOGICAL_DLL_EVENT: 'ProcessData' geweigerd`
- Event-log bevat: `ILLOGICAL_DLL_EVENT`
- systemState ongewijzigd (blijft "IDLE")
- ✅ Acceptatiecriteria vervuld

---

### Scenario 2: Simulatie DRAAIT (Valid event)

```java
simulator.start();  // running = true
eventBus.handleExternalDLLEvent("ProcessData");
```

**Verwacht:**
- Console: `→ [Interne Logica] Verwerking van externe data...`
- Event-log bevat: `DLL_EVENT: ProcessData`
- systemState wijzigt naar "PROCESSING_DATA"
- Event wordt normaal verwerkt

---

## 📊 Acceptatiecriteria Check

| Criterium | Implementatie | Status |
|-----------|---------------|--------|
| Event geweigerd als "Illogical" | `logEvent("ILLOGICAL_DLL_EVENT: ...")` + console print | ✅ |
| Status blijft "Stopped" | Early `return` zonder `systemState` aan te passen | ✅ |
| Waarschuwing gelogd | `logEvent(warningMsg)` + `System.out.println()` | ✅ |

---

## 🏃 Uitvoeren van de test

```bash
cd c:\Users\momes\IdeaProjects\Hotelsimulator

# Compileer test
javac -d . src/model/US4_2b_ValidationTest.java

# Run test
java model.US4_2b_ValidationTest
```

**Verwachte output:**
```
═══════════════════════════════════════════════════════════════
🧪 US4.2.b TEST: Validatie van onlogische events
═══════════════════════════════════════════════════════════════

GIVEN: Simulatie staat in 'Stopped' status
✓ Simulator gestopt via .pause()
  → simulator.isRunning() = false

WHEN: DLL stuurt 'ProcessData' event
  → Roep eventBus.handleExternalDLLEvent("ProcessData") aan...

⚙️ [EventBus] DLL Event ontvangen: ProcessData
  ❌ [VALIDATIE] ILLOGICAL_DLL_EVENT: 'ProcessData' geweigerd — simulatie niet in RUNNING status

THEN: Event wordt geweigerd als 'Illogical'
✅ GEVONDEN: ILLOGICAL_DLL_EVENT in logs

✅ ACCEPTATIECRITERIUM 1 VERVULD: Event werd als 'Illogical' geweigerd!
✅ ACCEPTATIECRITERIUM 2 VERVULD: Status bleef ongewijzigd!
✅ ACCEPTATIECRITERIUM 3 VERVULD: Waarschuwing werd gelogd!
```

---

## 🔒 Edge-cases & Robuustheid

| Edge-case | Handling |
|-----------|----------|
| Simulator is null | Behandel als `simIsRunning = false` (veilig) |
| Exception bij isRunning() | Catch, log, behandel als `simIsRunning = false` |
| Andere DLL-events | Geen validatie, normale verwerking |
| Simulatie draait → ProcessData | Validatie passed, event wordt verwerkt |

---

## 📝 Code-lijn uitleg (complete flow)

### Lijn-voor-lijn handleExternalDLLEvent:

| Lijn | Code | Wat doet het |
|------|------|-------------|
| 1 | `eventCounter++;` | Tel DLL-events (altijd, ook illogical) |
| 2 | `logEvent("DLL_EVENT: " + dllEventName);` | Log event in event-log |
| 3 | `System.out.println("\n⚙️ [EventBus]...");` | Print naar console |
| 4-9 | `boolean simIsRunning = false; if (simulator != null)...` | Check of simulatie draait |
| 10-15 | `if ("ProcessData" && !simIsRunning) { ... return; }` | **Validatie: stop event hier als invalid** |
| 16+ | `switch (dllEventName) { ... }` | Normale verwerking (bereikt alleen voor valide events) |

---

## 🎯 Samenvatting

**Wat is ge-implementeerd:**
- ✅ Validatie van DLL-events op basis van simulatiestatus
- ✅ "ProcessData" events worden geweigerd als simulatie niet draait
- ✅ Illogical events worden gelogd met waarschuwing
- ✅ Systemstatus blijft ongewijzigd bij weigering
- ✅ Foutafhandeling en defensief programmeren

**Storypoints:** 5 ✅

---

