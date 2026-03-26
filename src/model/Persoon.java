package model;

/**
 * Persoon - De basisklasse (Parent class) voor alle individuen in de hotel-simulatie.
 * Deze klasse zorgt ervoor dat elke persoon (of het nu een gast of personeelslid is)
 * een uniek ID krijgt en een naam heeft.
 */
public class Persoon {
    // 'static' betekent dat dit veld wordt gedeeld door ALLE objecten van de klasse Persoon.
    // Dit fungeert als een centrale teller die bijhoudt wat het volgende beschikbare ID is.
    private static int volgendeId = 1;

    // Instance variabelen: uniek voor elk specifiek Persoon-object.
    private int id;
    private String naam;
    private String type; // Bijv. "Gast", "Schoonmaker", "Receptionist"

    /**
     * Constructor voor een nieuw Persoon-object.
     * @param naam De volledige naam van de persoon.
     * @param type De rol die de persoon vervult in het hotel.
     */
    public Persoon(String naam, String type) {
        // We wijzen het huidige vrije ID toe en verhogen daarna de centrale teller direct met 1.
        // Zo heeft gegarandeerd iedere persoon een uniek nummer.
        this.id = volgendeId++;
        this.naam = naam;
        this.type = type;
    }

    // --- Getters om de eigenschappen op te vragen, maar niet zomaar te wijzigen (immutability) ---

    public int getId() {
        return id;
    }

    public String getNaam() {
        return naam;
    }

    public String getType() {
        return type;
    }

    /**
     * De toString methode is handig voor debugging; hiermee kun je een persoon
     * direct printen naar de console om te zien welke data erin zit.
     */
    @Override
    public String toString() {
        return "Persoon{" +
                "id=" + id +
                ", naam='" + naam + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}