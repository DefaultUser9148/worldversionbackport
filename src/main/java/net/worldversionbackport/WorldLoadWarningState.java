package net.worldversionbackport;

import net.minecraft.client.gui.screen.world.WorldListWidget;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Shared mutable state for WorldLoadWarningMixin.
 * Lives outside the mixin class because Mixin rules forbid non-private static fields.
 */
public class WorldLoadWarningState {

    /** World names that may bypass the WVB intercept exactly once. */
    public static final Set<String> bypassSet =
        Collections.synchronizedSet(new HashSet<>());

    /** The WorldEntry that triggered the most recent intercept, for auto-join after downgrade. */
    public static WorldListWidget.WorldEntry pendingAutoPlayEntry = null;
}
