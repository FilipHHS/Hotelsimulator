# 🎬 EVENT & DEBUGGING GUIDE

## WAT JE NU ZAL ZIEN IN DE CONSOLE:

### 1️⃣ SIMULATOR START
```
[Simulator] Lift geïnitialiseerd op positie (5.5, 0.5)
[Simulator] Gast 'Alice' heeft lift-referentie gekregen
[Simulator] Gast 'Bob' heeft lift-referentie gekregen
[Simulator] Gast 'Charlie' heeft lift-referentie gekregen
[Simulator] Simulatie gestart
```

### 2️⃣ ELKE TICK - GAST POSITIE & STATE
```
[Alice] Pos(2.5, 6.3) State:WANDELEN InLift:false MaxBounds(10,8)
[Bob] Pos(3.2, 6.5) State:WANDELEN InLift:false MaxBounds(10,8)
[Charlie] Pos(1.4, 7.1) State:WANDELEN InLift:false MaxBounds(10,8)
```

### 3️⃣ RANDOM WALK KEUZES
```
  → Alice kiest LINKS, nieuwe destX: 1.5
  → Bob kiest RECHTS, nieuwe destX: 6.2
  → Charlie blijft STAAN
```

### 4️⃣ BOTSINGEN (Collision Detection)
```
  ⚠️  BOTSING! Alice kan niet naar (5, 6) - vakje bezet!
  ⚠️  OUT OF BOUNDS! Bob probeerde naar X=10.5 (max=10), geclipped!
```

### 5️⃣ VERDIEPING WISSELEN
```
  → Alice wilt verdieping wisselen!
[Gast] Alice gaat trap naar verdieping 5
```

### 6️⃣ EVENTS TRIGGEREN (TimeTriggers)
```
🎬 EVENT 1 TRIGGERED! Alice gaat naar Restaurant!
[Simulator] Gast 'Alice' gestuurd naar Restaurant

🎬 EVENT 2 TRIGGERED! Bob gaat naar Fitness!
[Simulator] Gast 'Bob' gestuurd naar Fitness

🎬 EVENT 3 TRIGGERED! Charlie gaat naar Lounge!
[Simulator] Gast 'Charlie' gestuurd naar Lounge
```

### 7️⃣ IN FACILITEIT
```
[Alice] Pos(3.5, 6.5) State:IN_FACILITEIT InLift:false
```

---

## 🔍 HOE JE PROBLEMEN OPSPOORT:

### PROBLEEM: Gast loopt "verkeerd" (niet naar bestemming)
**OPLOSSING:** Check console voor:
```
  → Alice wilt verdieping wisselen!        ← Juist?
  ⚠️  BOTSING! Alice kan niet naar...      ← Andere gast in weg?
  ⚠️  OUT OF BOUNDS! Alice...              ← Te dicht bij rand?
```

### PROBLEEM: Event triggeert niet
**OPLOSSING:** Check of je ziet:
```
🎬 EVENT 1 TRIGGERED! Alice gaat naar Restaurant!
```
Zo nee → event-timing is wrong (check regel 151 in main.java)

### PROBLEEM: Gast is "out of bounds"
**OPLOSSING:** Check de MaxBounds in console:
```
MaxBounds(10, 8)  ← Hotel is 10 breed, 8 hoog
```
Als gast X > 10 of Y > 8 → probleem in setGridBounds()

---

## 🚀 HEDEN TESTEN:

1. Start programma
2. Klik "Start"
3. Kijk naar CONSOLE (niet het venster!)
4. Je ziet ALLES wat gasten doen
5. Bij timestep 50 → EVENT 1 triggeert
6. Bij timestep 150 → EVENT 2 triggeert
7. Bij timestep 250 → EVENT 3 triggeert

---

## 📊 TIMELINE (met snelheid 100ms per tick):

| Timestep | Seconden | Event | Wat gebeurd |
|----------|----------|-------|-----------|
| 0 | 0s | START | Simulatie start |
| 1-49 | 0-2.45s | - | Alice/Bob/Charlie wandelen |
| **50** | **2.5s** | **EVENT 1** | **Alice → Restaurant** |
| 51-149 | 2.55-7.45s | - | Alice in Restaurant, anderen wandelen |
| **150** | **7.5s** | **EVENT 2** | **Bob → Fitness** |
| 151-249 | 7.55-12.45s | - | Bob in Fitness, anderen wandelen |
| **250** | **12.5s** | **EVENT 3** | **Charlie → Lounge** |
| 251+ | 12.55s+ | - | Iedereen in hun faciliteit |

---

## 🛠️ VOLGENDE STAPPEN:

1. ✅ Draai programma met deze wijzigingen
2. ✅ Controleer console output
3. ✅ Zeg wat je ziet (copy-paste 10 regels uit console)
4. ✅ Zeg welke problemen je nog ziet
5. ✅ Dan fixen we ze stap-voor-stap

---

**DOEL:** Je kunt nu PRECIES ZIEN wat er gebeurd en WAAR het fout gaat! 🎯

