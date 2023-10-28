package tk.estecka.allaybehave;

import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;
import net.minecraft.world.GameRules.IntRule;
import net.minecraft.world.GameRules.Key;

import static net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory.createBooleanRule;
import static net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory.createIntRule;
import static net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory.createDoubleRule;

public class AllayGamerules
{
	static public final CustomGameRuleCategory CATEGORY = new CustomGameRuleCategory(new Identifier("allaybehave", "gamerules"), Text.translatable("gamerule.category.allaybehave").formatted(Formatting.AQUA, Formatting.BOLD));

	static public final Key<BooleanRule> STARE_CALL    = Register("allayCall.bySight", createBooleanRule(true));
	static public final Key<BooleanRule> NAME_CALL     = Register("allayCall.byName",  createBooleanRule(true));
	static public final Key<DoubleRule>  CALL_DURATION = Register("allayCall.duration", createDoubleRule(5.0, 0.0));
	static public final Key<IntRule>     CALL_RANGE    = Register("allayCall.range", createIntRule(32, 0));
	static public final Key<DoubleRule>  CALL_FOV      = Register("allayCall.refreshFov", createDoubleRule(90, 0, 360, (server,rule)->AllayUtil.SetStareFov(rule.get()) ));

	static public final Key<BooleanRule> TELEPORT  = Register("teleport.allowed",  createBooleanRule(true));
	static public final Key<IntRule> TELEPORT_DIST = Register("teleport.distance", createIntRule(32, 16, 64));

	static public void	Register(){
	}

	static private <T extends GameRules.Rule<T>> GameRules.Key<T>	Register(String name, GameRules.Type<T> type){
		return GameRuleRegistry.register("allaybehave."+name, CATEGORY, type);
	}
}
