# 🧹 Schoonmaker Implementatie - Code Uitleg

## **Schoonmaker Constructor - Stap voor Stap**

```java
public Schoonmaker(String naam, int startX, int startY) {
    super(naam, "Schoonmaker");
    this.x = startX + 0.5;
    this.y = startY + 0.5;
    this.destX = x;
    this.schoonmaakTijd = 0;
    this.kleur = new Color(128, 128, 128);  // Grijs
    this.schoonmakerID = schoonmaakCounter++;  // Krijg unieke ID
    System.out.println("[Schoonmaker] " + naam + " krijgt ID: " + schoonmakerID);
}
```

---

### **Regel 1: Signature**
```java
public Schoonmaker(String naam, int startX, int startY)
```
| Onderdeel | Betekenis |
|-----------|-----------|
| `public` | Iedereen kan dit aanroepen |
| `Schoonmaker` | Constructor-naam = klassennaam |
| `String naam` | Wat heet deze schoonmaker? |
| `int startX, startY` | Startpositie op het grid |

**Voorbeeld:**
```java
new Schoonmaker("Schoonmaker1", 3, 1);
// Maakt een schoonmaker aan op positie (3,1)
```

---

### **Regel 2: INHERITANCE - Super Call ⭐**
```java
super(naam, "Schoonmaker");
```

**Wat gebeurt hier?**
- `super()` = roep de **parent klasse** (Persoon) aan
- Schoonmaker **extends Persoon** → is een soort Persoon
- Dit is **inheritance in actie!**

**Klassenstructuur:**
```
┌─────────────┐
│   Persoon   │ (abstract)
├─────────────┤
│ - naam      │
│ - type      │
│ - id        │
│ onTick()    │
└──────┬──────┘
       │ extends
       │
┌──────┴────────┐
│  Schoonmaker  │
├───────────────┤
│ - x, y        │
│ - destX       │
│ - kleur       │
│ - state       │
└───────────────┘
```

**In praktijk:**
```java
Persoon p = new Schoonmaker("Schoonmaker1", 3, 1);
// Kan gebruikt worden als Persoon!
```

---

### **Regel 3-4: Positie Initialiseren**
```java
this.x = startX + 0.5;
this.y = startY + 0.5;
```

**Waarom +0.5?**
- Grid werkt in vakjes (0-12 bijvoorbeeld)
- Center van vakje = integer + 0.5
- Voorbeeld: Vakje 3 → centrum = 3.5

```
Vakje 0      Vakje 1      Vakje 2
├─────┤      ├─────┤      ├─────┤
  0.5          1.5          2.5
```

**Code:**
```java
new Schoonmaker("S1", 3, 2);
// x = 3 + 0.5 = 3.5 ✓
// y = 2 + 0.5 = 2.5 ✓
```

---

### **Regel 5: Doel Positie**
```java
this.destX = x;
```
- Start met hetzelfde als huidige positie
- Geen beweging aan het begin
- Zal later veranderen als schoonmaker gaat wandelen

---

### **Regel 6: Schoonmaak Timer**
```java
this.schoonmaakTijd = 0;
```
- Counter voor hoe lang het duurt om een kamer schoon te maken
- Start op 0 (doet nog niets)
- Zal 50 ticks zijn als schoonmaken begint

---

### **Regel 7: Kleur - Object Creatie ⭐**
```java
this.kleur = new Color(128, 128, 128);  // Grijs
```

**`new Color()` = Object aanmaken!**
- `new` keyword = creëert nieuw object in geheugen
- `Color(R, G, B)` → RGB waarden (0-255)
- 128, 128, 128 = grijs (even veel rood, groen, blauw)

**Voorbeelden:**
```java
new Color(255, 0, 0);    // ROOD
new Color(0, 255, 0);    // GROEN
new Color(0, 0, 255);    // BLAUW
new Color(128, 128, 128); // GRIJS ← Schoonmakers
new Color(255, 255, 255); // WIT
```

---

### **Regel 8: Unieke ID - Static Variable ⭐⭐⭐**
```java
this.schoonmakerID = schoonmaakCounter++;
```

**Dit is SLIM! Static variabele:**
```java
private static int schoonmaakCounter = 0;  // GEDEELD door alle schoonmakers!
```

**Wat gebeurt:**
```java
Schoonmaker1: schoonmaakID = schoonmaakCounter++ → ID = 0, counter wordt 1
Schoonmaker2: schoonmaakID = schoonmaakCounter++ → ID = 1, counter wordt 2
Schoonmaker3: schoonmaakID = schoonmaakCounter++ → ID = 2, counter wordt 3
```

**Waarom `++`?**
- `schoonmaakCounter++` = geef oude waarde, verhoog daarna
- First: ID = 0 (counter = 0), daarna counter = 1
- Next: ID = 1 (counter = 1), daarna counter = 2

**Diagram:**
```
        STATIC (gedeeld)
        ┌──────────┐
        │ counter= │
        │    0     │
        └────┬─────┘
             │
        Schoonmaker1     Schoonmaker2
        ┌──────────┐     ┌──────────┐
        │ ID = 0   │     │ ID = 1   │
        └──────────┘     └──────────┘
```

---

### **Regel 9: Debug Output**
```java
System.out.println("[Schoonmaker] " + naam + " krijgt ID: " + schoonmakerID);
```

**String Concatenation:**
```java
"[Schoonmaker] " + "Schoonmaker1" + " krijgt ID: " + 0
= "[Schoonmaker] Schoonmaker1 krijgt ID: 0"
```

**Output in console:**
```
[Schoonmaker] Schoonmaker1 krijgt ID: 0
[Schoonmaker] Schoonmaker2 krijgt ID: 1
```

---

## **Encapsulation: `this.` keyword**

Elke regel heeft `this.`:
```java
this.x = startX + 0.5;      // "Mijn x"
this.y = startY + 0.5;      // "Mijn y"
this.destX = x;              // "Mijn destX"
this.kleur = new Color(...); // "Mijn kleur"
```

**Waarom?**
- Duidelijkheid: dit zijn MIJN variabelen
- Encapsulation: private variabelen beschermd
- Avoids ambiguity met parameters

---

## **Samenvatting - De Concepten**

| Concept | Uw Code | Voorbeeld |
|---------|---------|-----------|
| **Constructor** | `public Schoonmaker(...)` | Initialiseren object |
| **Inheritance** | `super(naam, "Schoonmaker")` | Kent Persoon aan |
| **Encapsulation** | `private int x, y` | Data beschermd |
| **Object Creation** | `new Color(128, 128, 128)` | Maak nieuw object |
| **Static Variable** | `schoonmaakCounter++` | Gedeeld door allemaal |
| **this keyword** | `this.x = ...` | Reference naar instance |

---

## **Volledige Diagram: Constructor Flow**

```
JVM roept aan:
    new Schoonmaker("S1", 3, 1)
         │
         ▼
    1. Constructor wordt geladen
    2. super() → Persoon init (naam, type, id)
    3. Positie init: x=3.5, y=1.5
    4. Doel init: destX=3.5
    5. Timer init: schoonmaakTijd=0
    6. Kleur init: grijs Color object
    7. ID init: Krijg volgnummer van static counter
    8. Print naar console
         │
         ▼
    Schoonmaker object KLAAR
    ├─ naam: "S1"
    ├─ x: 3.5
    ├─ y: 1.5
    ├─ kleur: grijs
    ├─ state: WANDELEN
    └─ schoonmakerID: 0
```

---

## **Test jezelf: Vragen**

1. **Wat doet `super()`?**
   - A) Roept de parent klasse aan
   - B) Maakt object super snel
   - C) Verwijdert oude objecten

2. **Waarom `startX + 0.5`?**
   - A) Random getal toevoegen
   - B) Centrum van vakje berekenen
   - C) Altijd + 0.5 convention

3. **Wat is het verschil tussen deze twee?**
   ```java
   int id = schoonmaakCounter++;  // Geeft OUDE waarde
   int id = ++schoonmaakCounter;  // Geeft NIEUWE waarde
   ```

4. **Hoeveel Color objecten worden gemaakt voor 2 schoonmakers?**
   - A) 1 gedeeld
   - B) 2 afzonderlijke
   - C) Oneindig

---

## **Antwoorden**
1. **A** ✓ - `super()` roept Persoon constructor aan (inheritance!)
2. **B** ✓ - 3 + 0.5 = 3.5 = centrum van vakje 3
3. `i++` geeft 0 daarna 1; `++i` geeft 1 daarna 2
4. **B** ✓ - Elke schoonmaker krijgt eigen `new Color()` object

---

## **Bonus: Load Balancing (waarom static ID belangrijk is)**

```java
// zoekEerstViezeKamer():
int kamerIndex = schoonmakerID % viezeKamers.size();

Schoonmaker1: ID=0 → 0 % 3 = 0 → neemt kamer[0]
Schoonmaker2: ID=1 → 1 % 3 = 1 → neemt kamer[1]
Schoonmaker3: ID=2 → 2 % 3 = 2 → neemt kamer[2]
Schoonmaker4: ID=3 → 3 % 3 = 0 → neemt kamer[0]
```

Dit zorgt dat ze **load-balanced** over kamers verspreiden! 🎯


