package io.github.ultreon.controllerx.impl;

import dev.architectury.platform.Platform;
import io.github.ultreon.controllerx.Hooks;
import io.github.ultreon.controllerx.api.ControllerAction;
import io.github.ultreon.controllerx.api.ControllerActions;
import io.github.ultreon.controllerx.api.ControllerContext;
import io.github.ultreon.controllerx.api.ControllerMapping;
import io.github.ultreon.controllerx.gui.widget.ItemSlot;
import io.github.ultreon.controllerx.injection.CreativeModeInventoryScreenInjection;
import io.github.ultreon.controllerx.input.ControllerAxis;
import io.github.ultreon.controllerx.input.ControllerButton;
import io.github.ultreon.controllerx.input.ControllerInput;
import io.github.ultreon.controllerx.input.ControllerJoystick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class MenuControllerContext extends ControllerContext {
    public static final MenuControllerContext INSTANCE = new MenuControllerContext();
    public final ControllerMapping<?> joystickMove;
    public final ControllerMapping<?> dpadMove;
    public final ControllerMapping<?> activate;
    public final ControllerMapping<?> scrollY;

    public final ControllerMapping<?> close;
    public final ControllerMapping<?> back;
    public final ControllerMapping<?> closeInventory;

    public final ControllerMapping<?> pickup;
    public final ControllerMapping<?> place;
    public final ControllerMapping<?> split;
    public final ControllerMapping<?> putSingle;
    public final ControllerMapping<?> drop;
    public final ControllerMapping<?> prevPage;
    public final ControllerMapping<?> nextPage;

    protected MenuControllerContext() {
        super();

        this.joystickMove = mappings.register(new ControllerMapping<>(new ControllerAction.Joystick(ControllerJoystick.Left), ControllerMapping.Side.LEFT, Component.translatable("controllerx.action.menu.joystick_move")));
        this.dpadMove = mappings.register(new ControllerMapping<>(new ControllerAction.Joystick(ControllerJoystick.Dpad), ControllerMapping.Side.LEFT, Component.translatable("controllerx.action.menu.dpad_move")));
        this.activate = mappings.register(new ControllerMapping<>(new ControllerAction.Button(ControllerButton.A), ControllerMapping.Side.RIGHT, Component.translatable("controllerx.action.menu.activate"), this::canActivate));
        this.scrollY = mappings.register(new ControllerMapping<>(new ControllerAction.Axis(ControllerAxis.RightStickY), ControllerMapping.Side.RIGHT, Component.translatable("controllerx.action.menu.scroll_y")));

        this.closeInventory = mappings.register(new ControllerMapping<>(ControllerActions.Y, ControllerMapping.Side.RIGHT, Component.translatable("controllerx.action.inventory.closeInventory"), MenuControllerContext::isInventory));
        this.back = mappings.register(new ControllerMapping<>(new ControllerAction.Button(ControllerButton.B), ControllerMapping.Side.RIGHT, Component.translatable("controllerx.action.menu.back"), this::isCloseableInGame));
        this.close = mappings.register(new ControllerMapping<>(ControllerActions.START, ControllerMapping.Side.RIGHT, Component.translatable("controllerx.action.inGameMenu.close"), this::isCloseableInMenu));

        this.pickup = mappings.register(new ControllerMapping<>(new ControllerAction.Button(ControllerButton.A), ControllerMapping.Side.LEFT, Component.translatable("controllerx.action.menuOnSlot.pickup"), this::canPickup));
        this.place = mappings.register(new ControllerMapping<>(new ControllerAction.Button(ControllerButton.A), ControllerMapping.Side.LEFT, Component.translatable("controllerx.action.menuOnSlot.place"), this::canPlace));
        this.split = mappings.register(new ControllerMapping<>(new ControllerAction.Button(ControllerButton.X), ControllerMapping.Side.RIGHT, Component.translatable("controllerx.action.menuOnSlot.split"), this::canSplit));
        this.putSingle = mappings.register(new ControllerMapping<>(new ControllerAction.Button(ControllerButton.X), ControllerMapping.Side.RIGHT, Component.translatable("controllerx.action.menuOnSlot.putSingle"), this::canPutSingle));
        this.drop = mappings.register(new ControllerMapping<>(new ControllerAction.Button(ControllerButton.RightStickClick), ControllerMapping.Side.RIGHT, Component.translatable("controllerx.action.menuOnSlot.drop"), this::canDrop));

        this.prevPage = mappings.register(new ControllerMapping<>(new ControllerAction.Button(ControllerButton.LeftShoulder), ControllerMapping.Side.LEFT, Component.translatable("controllerx.action.creativeMenu.prevPage"), MenuControllerContext::hasPrevPage));
        this.nextPage = mappings.register(new ControllerMapping<>(new ControllerAction.Button(ControllerButton.RightShoulder), ControllerMapping.Side.RIGHT, Component.translatable("controllerx.action.creativeMenu.nextPage"), MenuControllerContext::hasNextPage));
    }

    private boolean canActivate(Minecraft minecraft) {
        Screen screen = minecraft.screen;
        if (screen instanceof AbstractContainerScreen<?> containerScreen
                && Hooks.isOnSlot(containerScreen)) return false;

        if (screen == null) return false;
        GuiEventListener focused = screen.getFocused();
        if (focused == null || !focused.isFocused()) {
            return false;
        }

        if (focused instanceof AbstractWidget widget) {
            if (!widget.visible) return false;
            return widget.active;
        }

        return true;
    }

    private static boolean isInventory(Minecraft mc) {
        Screen screen = Minecraft.getInstance().screen;
        return screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen;
    }

    private static boolean hasNextPage(Minecraft mc) {
        return mc.screen instanceof CreativeModeInventoryScreen screen && ((CreativeModeInventoryScreenInjection) screen).controllerX$getNextPage() != null;
    }

    private static boolean hasPrevPage(Minecraft mc) {
        return mc.screen instanceof CreativeModeInventoryScreen screen && ((CreativeModeInventoryScreenInjection) screen).controllerX$getPrevPage() != null;
    }

    private boolean canPickup(Minecraft minecraft) {
        if (minecraft.player == null) return false;
        if (!(minecraft.screen instanceof AbstractContainerScreen<?> containerScreen)) return false;
        if (!(containerScreen.getFocused() instanceof ItemSlot slot)) return false;

        ItemStack carried = minecraft.player.containerMenu.getCarried();
        if (!carried.isEmpty()) return false;
        return slot.getSlot().mayPickup(minecraft.player);
    }

    private boolean canPlace(Minecraft minecraft) {
        if (minecraft.player == null) return false;
        if (!(minecraft.screen instanceof AbstractContainerScreen<?> containerScreen)) return false;
        if (!(containerScreen.getFocused() instanceof ItemSlot slot)) return false;

        ItemStack carried = minecraft.player.containerMenu.getCarried();
        if (carried.isEmpty()) return false;
        return slot.getSlot().mayPlace(carried);
    }

    private boolean canSplit(Minecraft minecraft) {
        if (minecraft.player == null) return false;
        if (!(minecraft.screen instanceof AbstractContainerScreen<?> containerScreen)) return false;
        if (!(containerScreen.getFocused() instanceof ItemSlot slot)) return false;

        ItemStack carried = minecraft.player.containerMenu.getCarried();
        if (carried.isEmpty()) return false;
        return slot.getSlot().mayPickup(minecraft.player);
    }

    private boolean canPutSingle(Minecraft minecraft) {
        if (minecraft.player == null) return false;
        if (!(minecraft.screen instanceof AbstractContainerScreen<?> containerScreen)) return false;
        if (!(containerScreen.getFocused() instanceof ItemSlot slot)) return false;

        ItemStack carried = minecraft.player.containerMenu.getCarried();
        if (!carried.isEmpty()) return false;
        return slot.getSlot().mayPlace(carried);
    }

    private boolean canDrop(Minecraft minecraft) {
        if (minecraft.player == null) return false;
        if (!(minecraft.screen instanceof AbstractContainerScreen<?> containerScreen)) return false;
        if (!(containerScreen.getFocused() instanceof ItemSlot slot)) return false;
        return slot.getSlot().mayPickup(minecraft.player);
    }

    private boolean isCloseableInMenu(Minecraft mc) {
        return mc.player == null && mc.level == null && mc.screen != null && mc.screen.shouldCloseOnEsc() && !isInventory(mc);
    }

    private boolean isCloseableInGame(Minecraft mc) {
        return mc.player != null && mc.level != null && mc.screen != null && mc.screen.shouldCloseOnEsc() && !isInventory(mc);
    }

    @Override
    public int getYOffset() {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof ChatScreen) {
            return 32;
        }

        if (screen instanceof TitleScreen) {
            if (Platform.isForge()) return 36;
            return 12;
        }

        return super.getYOffset();
    }
}
