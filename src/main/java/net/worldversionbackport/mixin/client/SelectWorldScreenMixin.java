package net.worldversionbackport.mixin.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.worldversionbackport.WorldVersionBackport;
import net.worldversionbackport.screen.BackportManagerScreen;
import net.worldversionbackport.widget.WvbIconButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {

    private static final Identifier MOD_ICON = Identifier.of(WorldVersionBackport.MOD_ID, "icon.png");

    protected SelectWorldScreenMixin() {
        super(Text.empty());
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addModButton(CallbackInfo ci) {
        int buttonY = this.height - 52;
        int x = this.width / 2 + 106;

        this.addDrawableChild(new WvbIconButton(
            x, buttonY, 20,
            MOD_ICON,
            Text.literal("WorldVersionBackport"),
            btn -> this.client.setScreen(new BackportManagerScreen((SelectWorldScreen)(Object) this))
        ));
    }
}
