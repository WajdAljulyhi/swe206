import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 Test Suite for the Battle Royale Player State Machine.
 */
public class PlayerTest {

    private Player player;

    @Before
    public void setUp() {
        player = new Player();
    }

    // ---------------------------------------------------------------
    // TC1: Initial State
    // ---------------------------------------------------------------
    @Test
    public void testInitialState_ShouldBeAlive() {
        assertEquals("A newly created player should start in the Alive state.", 
            Player.Status.Alive, player.getStatus());
    }

    // ---------------------------------------------------------------
    // TC2: Alive --> [damage] --> Downed
    // ---------------------------------------------------------------
    @Test
    public void testTransition_Alive_damage_Downed() {
        assertEquals(Player.Status.Alive, player.getStatus());

        boolean result = player.damage();

        assertTrue("damage() should return true when called from Alive state.", result);
        assertEquals("After damage in Alive state, player should be Downed.", 
            Player.Status.Downed, player.getStatus());
    }

    // ---------------------------------------------------------------
    // TC3: Downed --> [bleedOut] --> Dead
    // ---------------------------------------------------------------
    @Test
    public void testTransition_Downed_bleedOut_Dead() {
        player.damage(); // Alive -> Downed
        assertEquals(Player.Status.Downed, player.getStatus());

        boolean result = player.bleedOut();

        assertTrue("bleedOut() should return true when called from Downed state.", result);
        assertEquals("After bleedOut in Downed state, player should be Dead.", 
            Player.Status.Dead, player.getStatus());
    }

    // ---------------------------------------------------------------
    // TC4: Downed --> [revive] --> Revive
    // ---------------------------------------------------------------
    @Test
    public void testTransition_Downed_revive_Revive() {
        player.damage(); // Alive -> Downed
        assertEquals(Player.Status.Downed, player.getStatus());

        boolean result = player.revive();

        assertTrue("revive() should return true when called from Downed state.", result);
        assertEquals("After revive in Downed state, player should be in Revive state.", 
            Player.Status.Revive, player.getStatus());
    }

    // ---------------------------------------------------------------
    // TC5: Dead --> [respawn] --> Revive
    // ---------------------------------------------------------------
    @Test
    public void testTransition_Dead_respawn_Revive() {
        player.damage();    // Alive -> Downed
        player.bleedOut();  // Downed -> Dead
        assertEquals(Player.Status.Dead, player.getStatus());

        boolean result = player.respawn();

        assertTrue("respawn() should return true when called from Dead state.", result);
        assertEquals("After respawn in Dead state, player should be in Revive state.", 
            Player.Status.Revive, player.getStatus());
    }

    // ---------------------------------------------------------------
    // TC6: Revive --> [complete] --> Alive
    // ---------------------------------------------------------------
    @Test
    public void testTransition_Revive_complete_Alive() {
        player.damage();    // Alive -> Downed
        player.revive();    // Downed -> Revive
        assertEquals(Player.Status.Revive, player.getStatus());

        boolean result = player.complete();

        assertTrue("complete() should return true when called from Revive state.", result);
        assertEquals("After complete in Revive state, player should return to Alive.", 
            Player.Status.Alive, player.getStatus());
    }

    // ---------------------------------------------------------------
    // TC7: Revive --> [damage] --> Interrupted
    // ---------------------------------------------------------------
    @Test
    public void testTransition_Revive_damage_Interrupted() {
        player.damage();    // Alive -> Downed
        player.revive();    // Downed -> Revive
        assertEquals(Player.Status.Revive, player.getStatus());

        boolean result = player.damage();

        assertTrue("damage() should return true when called from Revive state.", result);
        assertEquals("After damage in Revive state, player should be Interrupted.", 
            Player.Status.Interrupted, player.getStatus());
    }

    // ---------------------------------------------------------------
    // TC8: Interrupted --> [assist] --> Revive
    // ---------------------------------------------------------------
    @Test
    public void testTransition_Interrupted_assist_Revive() {
        player.damage();    // Alive -> Downed
        player.revive();    // Downed -> Revive
        player.damage();    // Revive -> Interrupted
        assertEquals(Player.Status.Interrupted, player.getStatus());

        boolean result = player.assist();

        assertTrue("assist() should return true when called from Interrupted state.", result);
        assertEquals("After assist in Interrupted state, player should return to Revive.", 
            Player.Status.Revive, player.getStatus());
    }

    // ---------------------------------------------------------------
    // TC9: Full flow via Dead path (Alive->Downed->Dead->Revive->Alive)
    // ---------------------------------------------------------------
    @Test
    public void testFullFlow_DeathAndRespawn() {
        assertEquals(Player.Status.Alive, player.getStatus());

        player.damage();    // Alive -> Downed
        assertEquals(Player.Status.Downed, player.getStatus());

        player.bleedOut();  // Downed -> Dead
        assertEquals(Player.Status.Dead, player.getStatus());

        player.respawn();   // Dead -> Revive
        assertEquals(Player.Status.Revive, player.getStatus());

        player.complete();  // Revive -> Alive
        assertEquals("Full death/respawn cycle should return player to Alive.", 
            Player.Status.Alive, player.getStatus());
    }

    // ---------------------------------------------------------------
    // TC10: Full flow via Interrupted path
    // ---------------------------------------------------------------
    @Test
    public void testFullFlow_InterruptedRevive() {
        player.damage();    // Alive -> Downed
        player.revive();    // Downed -> Revive
        player.damage();    // Revive -> Interrupted
        player.assist();    // Interrupted -> Revive
        player.complete();  // Revive -> Alive

        assertEquals("After interrupted revive flow, player should be back to Alive.", 
            Player.Status.Alive, player.getStatus());
    }

    // ---------------------------------------------------------------
    // TC11: Invalid transitions should return false
    // ---------------------------------------------------------------
    @Test
    public void testInvalidTransition_bleedOut_FromAlive() {
        assertEquals(Player.Status.Alive, player.getStatus());
        boolean result = player.bleedOut();
        assertFalse("bleedOut() from Alive state should not be processed.", result);
        assertEquals("State should not change after invalid transition.", 
            Player.Status.Alive, player.getStatus());
    }

    @Test
    public void testInvalidTransition_complete_FromDead() {
        player.damage();
        player.bleedOut(); // Now Dead
        boolean result = player.complete();
        assertFalse("complete() from Dead state should not be processed.", result);
        assertEquals(Player.Status.Dead, player.getStatus());
    }

    @Test
    public void testInvalidTransition_assist_FromAlive() {
        boolean result = player.assist();
        assertFalse("assist() from Alive state should not be processed.", result);
        assertEquals(Player.Status.Alive, player.getStatus());
    }
}
