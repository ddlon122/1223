package com.example.trigger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class TriggerMod implements ClientModInitializer {
    private static boolean enabled = false;
    // максимальная дистанция (ванильная досягаемость ~3 блока, выше смысла нет)
    private static double maxDistance = 3.0;

    private static KeyBinding toggleKey, plusKey, minusKey;

    @Override
    public void onInitializeClient() {
        toggleKey = reg("key.triggermod.toggle", GLFW.GLFW_KEY_R);
        plusKey   = reg("key.triggermod.plus",   GLFW.GLFW_KEY_UP);
        minusKey  = reg("key.triggermod.minus",  GLFW.GLFW_KEY_DOWN);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) enabled = !enabled;
            while (plusKey.wasPressed())  maxDistance = Math.min(3.0, maxDistance + 0.25);
            while (minusKey.wasPressed()) maxDistance = Math.max(1.0, maxDistance - 0.25);
            if (enabled) tick(client);
        });
    }

    private static KeyBinding reg(String name, int key) {
        return KeyBindingHelper.registerKeyBinding(
            new KeyBinding(name, InputUtil.Type.KEYSYM, key, KeyBinding.Category.MISC));
    }

    private static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult hit)) return;
        if (hit.getType() != HitResult.Type.ENTITY) return;
        if (!(hit.getEntity() instanceof LivingEntity target) || !target.isAlive()) return;
        if (mc.player.distanceTo(target) > maxDistance) return;
        if (mc.player.getAttackCooldownProgress(0.0f) < 0.95f) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
