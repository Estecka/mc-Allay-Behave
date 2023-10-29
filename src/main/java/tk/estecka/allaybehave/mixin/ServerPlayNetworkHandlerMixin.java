package tk.estecka.allaybehave.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import tk.estecka.allaybehave.AllayRules;
import tk.estecka.allaybehave.AllayUtil;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin 
{
	@Shadow public ServerPlayerEntity player;

	@Inject( method="handleDecoratedMessage", at=@At("HEAD"))
	void	allaybehave$NameCall(SignedMessage message, CallbackInfo info) {
		if (((PlayerEntity)player).getWorld().getGameRules().getBoolean(AllayRules.NAME_CALL))
			AllayUtil.CallNamedAllays(player, message.getSignedContent());
	}
}
