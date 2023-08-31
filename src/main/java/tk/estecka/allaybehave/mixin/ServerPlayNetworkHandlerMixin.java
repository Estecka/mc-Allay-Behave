package tk.estecka.allaybehave.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.Box;
import tk.estecka.allaybehave.AllayUtil;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
	private final ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler)(Object)this;

	Box	GetSearchBox(PlayerEntity player){
		return new Box(
			player.getX() - AllayUtil.STARE_DISTANCE,
			player.getY() - AllayUtil.STARE_DISTANCE,
			player.getZ() - AllayUtil.STARE_DISTANCE,
			player.getX() + AllayUtil.STARE_DISTANCE,
			player.getY() + AllayUtil.STARE_DISTANCE,
			player.getZ() + AllayUtil.STARE_DISTANCE
		);
	}

	boolean SeachPredicate(AllayEntity allay, String rawMessage){
		return AllayUtil.IsLikedOrBeholder(allay, handler.player)
		    && allay.hasCustomName()
		    && allay.getCustomName().getString().equals(rawMessage)
		    ;
	}

	@Inject( method="handleDecoratedMessage", at=@At("HEAD"))
	void	NameCallAllays(SignedMessage message, CallbackInfo info) {
		final PlayerEntity player = handler.player;
		String rawMessage = message.getSignedContent();

		var allays = player.getWorld().getEntitiesByClass(AllayEntity.class, GetSearchBox(player), (allay)->this.SeachPredicate(allay, rawMessage));
		for (AllayEntity a : allays)
			AllayUtil.SetBeheld(a, player);
	}
}