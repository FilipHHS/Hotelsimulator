package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Simpele JUnit tests voor LayoutLoader.
 * Deze tests controleren alleen de basis: geldig bestand werkt, verkeerd bestandstype faalt.
 */
class LayoutLoaderTest {

    @Test
    void geldigJsonBestandWordtIngeladen() throws Exception {
        // Arrange: dit bestand bestaat in de layouts-map.
        String pad = "layouts/hotel_multistory.json";

        // Act: laad de layout in als Hotel-object.
        Hotel hotel = LayoutLoader.laadLayout(pad);

        // Assert: als het laden gelukt is, krijgen we een Hotel terug.
        assertNotNull(hotel);
    }

    @Test
    void verkeerdBestandstypeGooitException() {
        // Arrange: LayoutLoader accepteert alleen .json bestanden.
        String pad = "layouts/hotel_multistory.txt";

        // Act + Assert: bij een verkeerd bestandstype verwachten we een Exception.
        assertThrows(Exception.class, () -> LayoutLoader.laadLayout(pad));
    }
}
