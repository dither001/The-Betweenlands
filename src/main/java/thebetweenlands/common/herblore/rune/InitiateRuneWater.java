package thebetweenlands.common.herblore.rune;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import thebetweenlands.api.rune.INodeComposition;
import thebetweenlands.api.rune.INodeConfiguration;
import thebetweenlands.api.rune.IRuneChainUser;
import thebetweenlands.api.rune.impl.AbstractRune;
import thebetweenlands.api.rune.impl.RuneChainComposition.RuneExecutionContext;
import thebetweenlands.api.rune.impl.RuneConfiguration;
import thebetweenlands.api.rune.impl.RuneEffectModifier;
import thebetweenlands.api.rune.impl.RuneStats;
import thebetweenlands.common.registries.AspectRegistry;

public final class InitiateRuneWater extends AbstractRune<InitiateRuneWater> {
	public static final class Blueprint extends AbstractRune.Blueprint<InitiateRuneWater> {
		public Blueprint() {
			super(RuneStats.builder()
					.aspect(AspectRegistry.ORDANIIS, 1)
					.duration(0.0f)
					.build());
		}

		public static final RuneConfiguration CONFIGURATION_1;

		static {
			RuneConfiguration.Builder builder = RuneConfiguration.builder();

			CONFIGURATION_1 = builder.build();
		}

		@Override
		public List<RuneConfiguration> getConfigurations(IConfigurationLinkAccess linkAccess, boolean provisional) {
			return ImmutableList.of(CONFIGURATION_1);
		}

		@Override
		public InitiateRuneWater create(int index, INodeComposition<RuneExecutionContext> composition, INodeConfiguration configuration) {
			return new InitiateRuneWater(this, index, composition, (RuneConfiguration) configuration);
		}

		@Override
		protected RuneEffectModifier.Subject activate(InitiateRuneWater state, RuneExecutionContext context, INodeIO io) {
			return null;
		}

		private static class WaterInitiationState extends InitiationState<InitiateRuneWater> {
			public boolean wasInWater = true;

			public void setSuccess() {
				this.success = true;
			}
		}

		@Override
		public InitiationState<InitiateRuneWater> checkInitiation(IRuneChainUser user, InitiationPhase phase, InitiationState<InitiateRuneWater> state) {
			if(state instanceof WaterInitiationState == false) {
				return new WaterInitiationState();
			} else {
				Entity entity = user.getEntity();
				if(entity != null) {
					WaterInitiationState waterState = (WaterInitiationState) state;
					if(!waterState.wasInWater && entity.isInWater()) {
						return InitiationState.success();
					}
					waterState.wasInWater = entity.isInWater();
				}
			}
			return state;
		}
	}

	private InitiateRuneWater(Blueprint blueprint, int index, INodeComposition<RuneExecutionContext> composition, RuneConfiguration configuration) {
		super(blueprint, index, composition, configuration);
	}
}
