package com.mrmr.animmod;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnimModClient implements ClientModInitializer {
    public static final Identifier LAYER_ID = Identifier.of("animmod", "movement");
    private final Map<UUID, MoveState> current = new HashMap<>();

    @Override
    public void onInitializeClient() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                LAYER_ID, 42, player -> new ModifierLayer<>());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) { current.clear(); return; }
            for (AbstractClientPlayerEntity p : client.world.getPlayers()) {
                updateAnimation(p);
            }
        });
    }

    private MoveState detectState(AbstractClientPlayerEntity p) {
        Vec3d v = p.getVelocity();
        double horizontal = Math.sqrt(v.x * v.x + v.z * v.z);

        if (p.isFallFlying()) return MoveState.GLIDE;
        if (p.isSwimming() || (p.isTouchingWater() && !p.isOnGround())) return MoveState.SWIM;
        if (p.isClimbing()) return MoveState.CLIMB;
        if (!p.isOnGround()) return v.y > 0 ? MoveState.JUMP : MoveState.FALL;
        if (p.isSneaking()) return MoveState.SNEAK;
        if (p.isSprinting()) return MoveState.RUN;
        if (horizontal > 0.01) return MoveState.WALK;
        return MoveState.IDLE;
    }

    @SuppressWarnings("unchecked")
    private void updateAnimation(AbstractClientPlayerEntity p) {
        MoveState state = detectState(p);
        if (current.get(p.getUuid()) == state) return;

        var layer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                .getPlayerAssociatedData(p).get(LAYER_ID);
        if (layer == null) return;

        var anim = PlayerAnimationRegistry.getAnimation(
                Identifier.of("animmod", state.animName));

        if (anim == null) {
            layer.setAnimation(null); // sin archivo -> animación vanilla
        } else {
            layer.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(5, Ease.INOUTSINE),
             anim.playAnimation());
        }
        current.put(p.getUuid(), state);
    }
}
