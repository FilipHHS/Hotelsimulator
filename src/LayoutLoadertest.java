import model.LayoutLoader;
import model.Hotel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Deze testklasse controleert of de LayoutLoader correct werkt.
 * Er wordt getest of bestanden goed worden ingeladen en of de validatie
 * van de hotelindeling (bijv. de aanwezigheid van verplichte faciliteiten) klopt.
 * * Elke test is opgebouwd volgens het Arrange-Act-Assert (AAA) patroon:
 * - Arrange: Zet de testgegevens (variabelen en states) klaar.
 * - Act: Voer de daadwerkelijke methode uit die je wilt testen.
 * - Assert: Controleer of het resultaat is zoals je had verwacht.
 */
public class LayoutLoadertest {

    /**
     * Test 1: Controleert of een geldig JSON-bestand succesvol wordt ingeladen.
     * Verwachting: De LayoutLoader geeft een geldig Hotel-object terug (niet null).
     */
    @Test
    void testGeldigeLayoutLaden() throws Exception {
        // Arrange: Definieer het pad naar een geldig (test)bestand
        String pad = "layouts/hotel1.json";

        // Act: Probeer het bestand in te laden via de LayoutLoader
        Hotel hotel = LayoutLoader.laadLayout(pad);

        // Assert: Controleer of het resulterende hotel-object daadwerkelijk is aangemaakt
        // Het tweede argument is een optionele boodschap die toont als de test faalt.
        assertNotNull(hotel, "Het ingeladen hotel mag niet null zijn bij een geldig JSON-bestand.");
    }

    /**
     * Test 2: Controleert of het systeem correct reageert op een verkeerd bestandstype.
     * Verwachting: Er wordt een Exception gegooid omdat het systeem waarschijnlijk alleen JSON aankan.
     */
    @Test
    void testVerkeerdeExtensie() {
        // Arrange: Definieer een pad naar een onondersteund bestand (.txt in plaats van .json)
        String pad = "layouts/hotel1.txt";

        // Act + Assert: Gebruik assertThrows om te verifiëren dat de code 'crasht' (een Exception gooit) zoals verwacht
        assertThrows(Exception.class, () -> {
            LayoutLoader.laadLayout(pad);
        }, "Er moet een Exception gegooid worden als de bestandsextensie onjuist is.");
    }

    /**
     * Test 3: Controleert de validatieregels van de hotelindeling.
     * Een geldig hotel moet een Lobby ('L') hebben.
     */
    @Test
    void testGeenLobby() {
        // Arrange: Maak een test-grid (plattegrond) aan waarin de 'L' (Lobby) ontbreekt
        String[][] grid = {
                {"G", "K", "K"}, // G = Gang, K = Kamer
                {"G", "F", "K"}, // F = Lift
                {"T", "G", "G"}  // T = Trap
        };

        // Act + Assert: Verifieer dat valideerLayout() faalt en een Exception gooit
        assertThrows(Exception.class, () -> {
            LayoutLoader.valideerLayout(grid);
        }, "Een layout zonder lobby ('L') moet als ongeldig worden gemarkeerd.");
    }

    /**
     * Test 4: Controleert de validatieregels voor de aanwezigheid van een lift ('F').
     */
    @Test
    void testGeenLift() {
        // Arrange: Maak een test-grid aan waarin de 'F' (Lift) bewust is weggelaten
        String[][] grid = {
                {"L", "K", "K"}, // L = Lobby
                {"G", "G", "K"}, // Let op: geen 'F' hier
                {"T", "G", "G"}  // T = Trap
        };

        // Act + Assert: Verifieer dat de layout wordt afgekeurd
        assertThrows(Exception.class, () -> {
            LayoutLoader.valideerLayout(grid);
        }, "Een layout zonder lift ('F') moet als ongeldig worden gemarkeerd.");
    }

    /**
     * Test 5: Controleert de validatieregels voor de aanwezigheid van een trap ('T').
     */
    @Test
    void testGeenTrap() {
        // Arrange: Maak een test-grid aan waarin de 'T' (Trap) ontbreekt
        String[][] grid = {
                {"L", "K", "K"},
                {"G", "F", "K"},
                {"G", "G", "G"}  // Let op: geen 'T' in deze rij
        };

        // Act + Assert: Verifieer dat de layout wordt afgekeurd
        assertThrows(Exception.class, () -> {
            LayoutLoader.valideerLayout(grid);
        }, "Een layout zonder trap ('T') moet als ongeldig worden gemarkeerd.");
    }

    /**
     * Test 6: Controleert de "Happy Flow" van de layout-validatie.
     * Verwachting: Als alle verplichte elementen aanwezig zijn, loopt de methode foutloos door.
     */
    @Test
    void testAllesAanwezig() {
        // Arrange: Maak een test-grid dat voldoet aan alle eisen (bevat L, F en T)
        String[][] grid = {
                {"L", "K", "K"}, // Lobby aanwezig
                {"G", "F", "K"}, // Lift aanwezig
                {"T", "G", "G"}  // Trap aanwezig
        };

        // Act + Assert: assertDoesNotThrow bevestigt dat de code succesvol en zonder Exceptions draait
        assertDoesNotThrow(() -> {
            LayoutLoader.valideerLayout(grid);
        }, "Een correcte layout met alle verplichte onderdelen (L, F, T) mag geen Exception gooien.");
    }
}