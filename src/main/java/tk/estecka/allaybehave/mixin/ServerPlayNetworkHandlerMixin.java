package tk.estecka.allaybehave.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import tk.estecka.allaybehave.AllayGamerules;
import tk.estecka.allaybehave.AllayUtil;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin 
{
	private final ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler)(Object)this;

	@Inject( method="handleDecoratedMessage", at=@At("HEAD"))
	void	allaybehave$NameCall(SignedMessage message, CallbackInfo info) {
		if (handler.player.world.getGameRules().getBoolean(AllayGamerules.NAME_CALL))
			AllayUtil.CallNamedAllay(handler.player, message.getSignedContent());
	}
}
