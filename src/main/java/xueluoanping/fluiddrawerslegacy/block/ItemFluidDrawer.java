package xueluoanping.fluiddrawerslegacy.block;


import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.LockAttribute;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.fluids.FluidStack;
import xueluoanping.fluiddrawerslegacy.FluidDrawersLegacyMod;
import xueluoanping.fluiddrawerslegacy.client.render.FluidDrawerItemStackTileEntityRenderer;
import xueluoanping.fluiddrawerslegacy.util.SafeClientAccess;
import xueluoanping.fluiddrawerslegacy.util.TooltipKey;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

public class ItemFluidDrawer extends BlockItem {


    public ItemFluidDrawer(Block block, Properties properties) {
        super(block, properties);

    }

    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.IItemRenderProperties> consumer)
    {
        super.initializeClient(consumer);

        consumer.accept(new IItemRenderProperties()
        {
            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer()
            {
                return new FluidDrawerItemStackTileEntityRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
            }
        });
    }


    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack);
    }


    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level, List<Component> componentList, TooltipFlag flag) {
        super.appendHoverText(stack, level, componentList, flag);
        if (level instanceof ClientLevel) {
            TooltipKey key = SafeClientAccess.getTooltipKey();
            if (key == TooltipKey.SHIFT || key == TooltipKey.UNKNOWN) {
                if (stack.getOrCreateTag().contains("tank")) {
                    FluidStack fluidStack = FluidStack.loadFluidStackFromNBT((CompoundTag) stack.getOrCreateTag().get("tank"));

                    if (fluidStack.getAmount() > 0) {
                        if (stack.getOrCreateTag().toString().contains("storagedrawers:creative_vending_upgrade"))
                            fluidStack.setAmount(Integer.MAX_VALUE);
                        componentList.add(new TranslatableComponent("statement.fluiddrawerslegacy.fluiddrawer1")
                                .append(String.valueOf(fluidStack.getAmount()))
//                                .append("/" + TileEntityFluidDrawer.calcultaeCapacitybyStack(stack) + "mB")
                                .append(new TranslatableComponent("statement.fluiddrawerslegacy.fluiddrawer2"))
                                .append(new TranslatableComponent(fluidStack.getTranslationKey())));
                    }
                }
                if (stack.getOrCreateTag().contains("Lock")) {
                    Byte b = stack.getOrCreateTag().getByte("Lock");
                    EnumSet<LockAttribute> attrs = LockAttribute.getEnumSet(b);
                    if (attrs.contains(LockAttribute.LOCK_EMPTY))
                        componentList.add(new TextComponent(" §7(" + I18n.get("tooltip.storagedrawers.waila.locked") + ") "));
                }
            }
        }
    }
}
