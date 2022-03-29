package xueluoanping.fluiddrawerslegacy;


import com.jaquadro.minecraft.storagedrawers.api.event.DrawerPopulatedEvent;
import com.jaquadro.minecraft.storagedrawers.core.ModBlocks;
import com.jaquadro.minecraft.storagedrawers.core.ModContainers;
import com.jaquadro.minecraft.storagedrawers.inventory.ContainerDrawers1;
import com.mojang.datafixers.types.Type;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import xueluoanping.fluiddrawerslegacy.block.BlockFluidDrawer;
import xueluoanping.fluiddrawerslegacy.block.ItemFluidDrawer;
import xueluoanping.fluiddrawerslegacy.block.tileentity.TileEntityFluidDrawer;
import xueluoanping.fluiddrawerslegacy.client.gui.ContainerFluiDrawer;
import xueluoanping.fluiddrawerslegacy.client.gui.Screen;
import xueluoanping.fluiddrawerslegacy.client.model.FluidDrawerBakedModel;
import xueluoanping.fluiddrawerslegacy.client.render.TESRFluidDrawer;

import java.util.Map;
import java.util.function.Supplier;

import static xueluoanping.fluiddrawerslegacy.FluidDrawersLegacyMod.CREATIVE_TAB;

// You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
// Event bus for receiving Registry Events)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContents {
    public static Block fluiddrawer = null;
    public static TileEntityType<TileEntityFluidDrawer> tankTileEntityType = null;
    public static BlockItem itemBlock = null;
    public static ContainerType<ContainerFluiDrawer> containerType = null;

    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
        // register a new block here
        FluidDrawersLegacyMod.logger("注册方块");
        fluiddrawer = new BlockFluidDrawer(AbstractBlock.Properties.of(Material.GLASS)
                .sound(SoundType.GLASS).strength(5.0F)
                .noOcclusion().isSuffocating(ModContents::predFalse).isRedstoneConductor(ModContents::predFalse)

        );
        event.getRegistry().register(fluiddrawer.setRegistryName("fluiddrawer"));
    }
    private static boolean predFalse(BlockState p_235436_0_, IBlockReader p_235436_1_, BlockPos p_235436_2_) {
        return false;
    }
    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        FluidDrawersLegacyMod.logger("注册方块实体");
        tankTileEntityType = (TileEntityType<TileEntityFluidDrawer>) TileEntityType.Builder.of(TileEntityFluidDrawer::new, fluiddrawer).build((Type) null).setRegistryName(new ResourceLocation(FluidDrawersLegacyMod.MOD_ID, "fluiddrawer"));
        event.getRegistry().register(tankTileEntityType);

    }

    @SubscribeEvent
    public static void registerItems(final RegistryEvent.Register<Item> event) {
        itemBlock = new ItemFluidDrawer(fluiddrawer, new Item.Properties().tab(CREATIVE_TAB));
        event.getRegistry().register(itemBlock.setRegistryName(fluiddrawer.getRegistryName()));
        FluidDrawersLegacyMod.logger("注册物品");
    }

    @SubscribeEvent
    public static void onRenderTypeSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            RenderTypeLookup.setRenderLayer(ModContents.fluiddrawer, ModContents::isGlassLanternValidLayer);
        });
    }

    // does the Glass Lantern render in the given layer (RenderType) - used as Predicate<RenderType> lambda for setRenderLayer
    public static boolean isGlassLanternValidLayer(RenderType layerToCheck) {
        return layerToCheck == RenderType.cutoutMipped() || layerToCheck == RenderType.translucent();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerModels(ModelBakeEvent event) {
//            ClientRegistry.bindTileEntityRenderer(ModBlocks.Tile.STANDARD_DRAWERS_1, TileEntityDrawersRenderer::new);
    }

    @SubscribeEvent
    public static void onClientEvent(FMLClientSetupEvent event) {
        FluidDrawersLegacyMod.logger("注册渲染");
        event.enqueueWork(() -> {
            ClientRegistry.bindTileEntityRenderer(ModContents.tankTileEntityType, TESRFluidDrawer::new);
            ScreenManager.register(ModContents.containerType, Screen.Slot1::new);

        });
    }

    @SubscribeEvent
    public static void onContainerRegistry(final RegistryEvent.Register<ContainerType<?>> event) {
        containerType= (ContainerType<ContainerFluiDrawer>) IForgeContainerType.create(ContainerFluiDrawer::new).setRegistryName("container_1");
        event.getRegistry().register(containerType);
    }

    @SubscribeEvent
    public static void onModelBaked(ModelBakeEvent event) {
        Map<ResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        ModelResourceLocation location = new ModelResourceLocation(ModContents.itemBlock.getRegistryName(), "inventory");
        IBakedModel existingModel = modelRegistry.get(location);
        if (existingModel == null) {
            throw new RuntimeException("Did not find in registry");
        } else if (existingModel instanceof FluidDrawerBakedModel) {
            throw new RuntimeException("Tried to replace twice");
        } else {
            FluidDrawerBakedModel model = new FluidDrawerBakedModel(existingModel);
            event.getModelRegistry().put(location, model);
        }
    }


}
