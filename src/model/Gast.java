package model;

import java.awt.Color;
import java.util.Random;

public class Gast extends Persoon {
    private static final Random RANDOM = new Random();
    private static final double SPEED = 0.5;
    private static final double LIFT_WAIT_X = 1.5;
    private static final double TRAP_X = 8.5;

    private int maxX, maxY;
    private Hotel hotel;
    private Lift lift;
    private boolean inLift = false;
    private boolean usesTrap = false;
    private Kamer huidigKamer;
    private Color kleur;

    private int doelVerdieping;
    private int roomStayTimer = 0;  // Timer for how long guest stays in room

    private double faciliteitX, faciliteitY;
    private int faciliteitsBezoekDuur = 0; // Let op de 'l'
    private String huidigerFaciliteitType = null;

    private enum State {
        WANDELEN,
        NAAR_LIFT_WACHTEN,
        WACHTEN_OP_VERVOER,
        IN_LIFT,
        GAAT_NAAR_FACILITEIT,
        IN_FACILITEIT,
        GAAT_NAAR_LOBBY,
        GAAT_NAAR_KAMER,
        VERLAAT_HOTEL,
        EVACUATIE
    }

    private State state = State.WANDELEN;
    private int stapsInRichting = 0;
    private int maxStapsRichting = 5;

    public Gast(String naam, int ignoreX, int ignoreY) {
        super(naam, "Gast");
        this.kleur = new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
    }


    @Override
    public void onTick() {
        // Update activiteit label
        updateActiviteitLabel();
        
        // Handle fire alarm evacuation
        if (fireAlarmActive && !evacuatieBegonnen) {
            startEvacuatie();
        }
        
        // Handle when fire alarm is cleared while in evacuation
        if (!fireAlarmActive && evacuatieBegonnen) {
            if (state == State.EVACUATIE || state == State.VERLAAT_HOTEL) {
                // Go back to lobby and resume normal
                evacuatieBegonnen = false;
                state = State.WANDELEN;
                setHuidigeActiviteit("🚶 Wandel");
                huidigKamer = null;
                // Reset position to lobby
                x = 1.5;
                y = maxY - 1;
                destX = x;
            }
        }
        
        if (!inLift) {
            switch (state) {
                case WANDELEN: randomWalk(); break;
                case NAAR_LIFT_WACHTEN: beweegNaarLiftTrap(); break;
                case WACHTEN_OP_VERVOER: wachtOpVervoer(); break;
                case GAAT_NAAR_FACILITEIT: beweegNaarFaciliteit(); break;
                case IN_FACILITEIT: zitInFaciliteit(); break;
                case GAAT_NAAR_KAMER: beweegNaarKamer(); break;
                case GAAT_NAAR_LOBBY: beweegNaarLobby(); break;
                case VERLAAT_HOTEL: beweegNaarExit(); break;
                case EVACUATIE: beweegNaarLobbyDirect(); break;
            }
        } else {
            handleLiftLogic();
        }
    }
    
    private void updateActiviteitLabel() {
        if (inLift) {
            setHuidigeActiviteit("🛗 In Lift");
        } else {
            switch (state) {
                case WANDELEN:
                    if (huidigKamer != null) {
                        setHuidigeActiviteit("🛏️ Chill");
                    } else {
                        setHuidigeActiviteit("🚶 Wandel");
                    }
                    break;
                case NAAR_LIFT_WACHTEN:
                    setHuidigeActiviteit("⏳ Wacht Lift");
                    break;
                case WACHTEN_OP_VERVOER:
                    setHuidigeActiviteit("⏳ Vervoer");
                    break;
                case IN_FACILITEIT:
                    if (huidigerFaciliteitType != null) {
                        if ("Restaurant".equals(huidigerFaciliteitType)) {
                            setHuidigeActiviteit("🍽️ Eet");
                        } else if ("Fitness".equals(huidigerFaciliteitType)) {
                            setHuidigeActiviteit("💪 Sport");
                        } else {
                            setHuidigeActiviteit("📍 " + huidigerFaciliteitType);
                        }
                    }
                    break;
                case GAAT_NAAR_FACILITEIT:
                    setHuidigeActiviteit("🚶 > Faciliteit");
                    break;
                case GAAT_NAAR_KAMER:
                    setHuidigeActiviteit("✓ Check-in");
                    break;
                case GAAT_NAAR_LOBBY:
                    setHuidigeActiviteit("✗ Check-out");
                    break;
                case VERLAAT_HOTEL:
                    setHuidigeActiviteit("👋 Vertrekt");
                    break;
                case EVACUATIE:
                    setHuidigeActiviteit("🔥 EVACUATIE!");
                    break;
                default:
                    setHuidigeActiviteit("");
            }
        }
    }

    private void handleLiftLogic() {
        if (lift != null) {
            this.x = lift.getX();
            this.y = lift.getY();

            // Controleer of we op onze doelverdieping zijn en lift stillaat
            if ((int)lift.getY() == doelVerdieping && lift.isIdle()) {
                lift.verwijderGast(this);
                this.inLift = false;
                this.y = doelVerdieping + 0.5;
                this.destX = this.x;
                this.state = State.WANDELEN;
            }
        }
    }

    private void randomWalk() {
        // If guest just arrived in room, don't move - stay there
        if (roomStayTimer > 0) {
            roomStayTimer--;
            destX = x;  // Stay in place
            if (roomStayTimer == 0) {
                // Timer done - maybe visit a facility
                if (RANDOM.nextDouble() < 0.5) {
                    // 50% chance to visit restaurant or fitness
                    String facility = RANDOM.nextBoolean() ? "Restaurant" : "Fitness";
                    // For now, just reset and start wandering
                    // TODO: Add facility visit logic here
                }
            }
            return;
        }

        if (stapsInRichting >= maxStapsRichting) {
            int keuze = RANDOM.nextInt(10);
            // FIX: Guests should NOT wander out of bounds (x < 0). Only stay inside hotel.
            if (keuze < 4) destX = Math.max(0.5, x - 3);
            else if (keuze < 8) destX = Math.min(maxX - 1.5, x + 3);
            else destX = x;
            stapsInRichting = 0;
            maxStapsRichting = RANDOM.nextInt(3) + 2;
        }

        double dx = destX - x;
        if (Math.abs(dx) < SPEED) x = destX;
        else x += (dx > 0 ? SPEED : -SPEED);

        stapsInRichting++;
        if (RANDOM.nextDouble() < 0.02) wiltVerdiepingWisselen();
    }

    private void beweegNaarLiftTrap() {
        double dx = destX - x;
        if (Math.abs(dx) < SPEED) {
            x = destX;
            this.state = State.WACHTEN_OP_VERVOER;
        } else {
            x += (dx > 0) ? SPEED : -SPEED;
        }
    }

    private void wachtOpVervoer() {
        if (usesTrap) {
            int direction = (y >= maxY - 1) ? -1 : (y <= 1 ? 1 : (RANDOM.nextBoolean() ? 1 : -1));
            int newFloor = (int)y + direction;
            this.y = newFloor + 0.5;
            this.destX = this.x;
            this.state = State.WANDELEN;
        } else if (lift != null) {
            if (Math.abs(lift.getY() - this.y) < 1.0 && lift.isIdle()) {
                // Kies willekeurig doeketage (behalve huidge)
                this.doelVerdieping = RANDOM.nextInt(maxY);
                if (this.doelVerdieping == (int)this.y) {
                    this.doelVerdieping = (this.doelVerdieping + 1) % maxY;
                }
                
                if (lift.voegGastToe(this, doelVerdieping)) {
                    this.inLift = true;
                    this.state = State.IN_LIFT;
                }
            }
        }
    }

    private void wiltVerdiepingWisselen() {
        this.usesTrap = RANDOM.nextBoolean();
        this.destX = usesTrap ? TRAP_X : LIFT_WAIT_X;
        this.state = State.NAAR_LIFT_WACHTEN;
    }

    // === FACILITEIT LOGICA (Gebruikt nu de juiste variabelenaam) ===

    public void gaatNaarFaciliteit(String type, double fX, double fY) {
        this.huidigerFaciliteitType = type;
        this.faciliteitX = fX;
        this.faciliteitY = fY;
        if ((int)fY != (int)y) wiltVerdiepingWisselen();
        else this.state = State.GAAT_NAAR_FACILITEIT;
    }

    private void beweegNaarFaciliteit() {
        double dx = faciliteitX - x;
        if (Math.abs(dx) < SPEED) {
            x = faciliteitX;
            this.state = State.IN_FACILITEIT;
            this.faciliteitsBezoekDuur = 20 + RANDOM.nextInt(30); // Gefixt
        } else {
            x += (dx > 0) ? SPEED : -SPEED;
        }
    }

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
                // Guest arrived in room - set timer to stay there
                this.roomStayTimer = 100 + RANDOM.nextInt(200);  // Stay 100-300 ticks
            } else {
                x += (dx > 0) ? SPEED : -SPEED;
            }
        }
    }

    private void zitInFaciliteit() {
        faciliteitsBezoekDuur--; // Gefixt
        if (faciliteitsBezoekDuur <= 0) {
            this.state = State.WANDELEN;
            this.huidigerFaciliteitType = null;
        }
    }

    private void beweegNaarLobby() {
        double lobbyX = 1.5;
        int lobbyY = maxY - 1;
        if ((int)y != lobbyY) wiltVerdiepingWisselen();
        else {
            double dx = lobbyX - x;
            if (Math.abs(dx) < SPEED) {
                x = lobbyX;
                this.state = State.VERLAAT_HOTEL;
            } else {
                x += (dx > 0) ? SPEED : -SPEED;
            }
        }
    }

    private void beweegNaarExit() {
        // Move outside (-1.5 to -2.0 range)
        double exitX = -1.5;
        double dx = exitX - x;
        if (Math.abs(dx) < SPEED) {
            x = exitX;
            // Stay outside, don't move further
            setHuidigeActiviteit("👋 Buiten hotel");
        } else {
            x -= SPEED;  // Keep moving left
        }
    }

    // === SETTERS & GETTERS ===
    public void setGridBounds(int maxX, int maxY) { this.maxX = maxX; this.maxY = maxY; }
    public void setLift(Lift lift) { this.lift = lift; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }
    public Color getKleur() { return kleur; }
    public Kamer getHuidigKamer() { return huidigKamer; }

    public boolean checkinKamer(Kamer k) {
        this.huidigKamer = k;
        k.setStatus(Kamer.KamerStatus.BEZET);
        // Don't teleport! Set destination and change state to walk there
        this.destX = k.getArea().getX() + 0.5;
        this.state = State.GAAT_NAAR_KAMER;
        this.doelVerdieping = (int)k.getArea().getY();
        return true;
    }

    public void checkoutKamer() {
        if (huidigKamer != null) {
            huidigKamer.setStatus(Kamer.KamerStatus.SCHOONMAKEN);
            huidigKamer = null;
            this.state = State.GAAT_NAAR_LOBBY;
        }
    }
    
    /**
     * Wordt aangeroepen bij brandalarm - interrupt alle activiteiten
     */
    private void startEvacuatie() {
        evacuatieBegonnen = true;
        
        // Stop alle lopende activiteiten
        if (inLift && lift != null) {
            lift.verwijderGast(this);
            inLift = false;
            this.x = lift.getX();
            this.y = (int)lift.getY() + 0.5;
        }
        
        // Cancel alle andere activiteiten
        huidigKamer = null;
        huidigerFaciliteitType = null;
        faciliteitsBezoekDuur = 0;
        roomStayTimer = 0;
        
        // Start evacuation towards lobby via stairs
        this.state = State.EVACUATIE;
        this.usesTrap = true; // FORCE stairs, not elevator
        System.out.println("[FireAlarm] 🚨 " + getNaam() + " begint evacuatie naar lobby!");
    }
    
    /**
     * Beweeg direct naar lobby via trap (tijdens brandalam)
     */
    private void beweegNaarLobbyDirect() {
        double lobbyX = 1.5;      // Lobby position X
        double stairX = 8.5;       // Stairs position X
        int lobbyY = maxY - 1;     // Lobby floor
        
        // Step 1: Move to stairs if not on lobby floor
        if ((int)y != lobbyY) {
            // First move to stair position X
            if (Math.abs(x - stairX) > SPEED) {
                x += (x < stairX) ? SPEED : -SPEED;
            } else {
                // At stairs, move to lobby floor
                if (y < lobbyY) {
                    y += SPEED;
                } else if (y > lobbyY) {
                    y -= SPEED;
                }
            }
        } else {
            // Step 2: On lobby floor, move to lobby position
            double dx = lobbyX - x;
            if (Math.abs(dx) < SPEED) {
                x = lobbyX;
                // Reached lobby - now exit building (go outside)
                this.state = State.VERLAAT_HOTEL;
                setHuidigeActiviteit("👋 Verlaat Hotel");
            } else {
                x += (dx > 0) ? SPEED : -SPEED;
            }
        }
    }


}