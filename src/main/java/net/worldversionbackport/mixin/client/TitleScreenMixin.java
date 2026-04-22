package net.worldversionbackport.mixin.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.worldversionbackport.WorldVersionBackport;
import net.worldversionbackport.screen.BackportManagerScreen;
import net.worldversionbackport.widget.WvbIconButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    private static final Identifier MOD_ICON = Identifier.of(WorldVersionBackport.MOD_ID, "icon.png");

    protected TitleScreenMixin() {
        super(Text.empty());
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addModButton(CallbackInfo ci) {
        int rowY = this.height / 4 + 156;

        int maxRight = this.width / 2 + 104;
        for (var child : this.children()) {
            if (child instanceof ClickableWidget cw) {
                if (Math.abs(cw.getY() - rowY) <= 2) {
                    int right = cw.getX() + cw.getWidth();
                    if (right > maxRight) maxRight = right;
                }
            }
        }

        this.addDrawableChild(new WvbIconButton(
            maxRight + 4, rowY, 20,
            MOD_ICON,
            Text.literal("WorldVersionBackport"),
            btn -> this.client.setScreen(new BackportManagerScreen((TitleScreen)(Object) this))
        ));
    }
}
