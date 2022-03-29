package xueluoanping.fluiddrawerslegacy.block.tileentity;

import com.jaquadro.minecraft.storagedrawers.api.event.DrawerPopulatedEvent;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributes;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributesModifiable;
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import com.jaquadro.minecraft.storagedrawers.api.storage.attribute.LockAttribute;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawersStandard;
import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.StandardDrawerGroup;
import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.UpgradeData;
import com.jaquadro.minecraft.storagedrawers.capabilities.BasicDrawerAttributes;
import com.jaquadro.minecraft.storagedrawers.config.CommonConfig;
import com.jaquadro.minecraft.storagedrawers.core.ModItems;
import com.jaquadro.minecraft.storagedrawers.item.ItemUpgrade;
import com.jaquadro.minecraft.storagedrawers.item.ItemUpgradeStorage;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import xueluoanping.fluiddrawerslegacy.FluidDrawersLegacyMod;
import xueluoanping.fluiddrawerslegacy.ModContents;
import xueluoanping.fluiddrawerslegacy.config.General;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

public class TileEntityFluidDrawer extends TileEntityDrawersStandard {
    @CapabilityInject(IDrawerAttributes.class)
    static Capability<IDrawerAttributes> DRAWER_ATTRIBUTES_CAPABILITY = null;
    private IDrawerAttributesModifiable drawerAttributes = new DrawerAttributes();

    private GroupData groupData = new GroupData(1);
//    public static int Capacity = 32000;

    public TileEntityFluidDrawer() {
        super(ModContents.tankTileEntityType);
        groupData.setCapabilityProvider(this);
        injectPortableData(groupData);
    }

    private static int getCapacityStandard() {
        return General.volume.get();
    }

    @Override
    public IDrawerGroup getGroup() {
        return groupData;
    }

    @Override
    protected void onAttributeChanged() {
        super.onAttributeChanged();
        groupData.syncAttributes();
    }

    public boolean hasNoFluid() {
        return upgrades().write(new CompoundNBT()).toString().contains("storagedrawers:void_upgrade") || groupData.tank.getFluidInTank(0).getAmount() == 0;
    }

    public FluidStack getTankFLuid() {
        return groupData.tank.getFluidInTank(0);
    }

    public void inventoryChanged() {
        super.setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }


    @Override
    public IDrawerAttributes getDrawerAttributes() {
        return this.drawerAttributes;
    }

    public IFluidHandler getTank() {
        return this.groupData.tank;
    }

    public int getEffectiveCapacity() {
//FluidDrawersLegacyMod.LOGGER.info(""+getLevel()+upgrades().write(new CompoundNBT()).toString().contains("storagedrawers:creative_vending_upgrade"));
        if (upgrades().write(new CompoundNBT()).toString().contains("storagedrawers:creative_vending_upgrade")
                || upgrades().hasUnlimitedUpgrade())
            return Integer.MAX_VALUE;
        if (upgrades().hasOneStackUpgrade())
            return getCapacityStandard() / 32;
        return getCapacityStandard() * upgrades().getStorageMultiplier();
    }

    public static int calcultaeCapacitybyStack(ItemStack stack) {
        if (!stack.getOrCreateTag().contains("Upgrades"))
            return getCapacityStandard();
        else {
            int i = 0;
//            use 8 instead of 7, maybe 1 =null?
            UpgradeData upgradeData0 = new UpgradeData(8);

            ListNBT list = stack.getOrCreateTag().getList("Upgrades", Constants.NBT.TAG_COMPOUND);
            if (list.size() > 0)
                for (int j = 0; j < list.size(); j++) {
                    CompoundNBT id = (CompoundNBT) list.get(i);
                    String ids = id.get("id").getAsString();
                    String[] idGroup = ids.split(":");
                    ResourceLocation res = new ResourceLocation(idGroup[0], idGroup[1]);
                    if (ForgeRegistries.ITEMS.containsKey(res)) {
                        upgradeData0.addUpgrade(ForgeRegistries.ITEMS.getValue(res).getItem().getDefaultInstance());
                        if (ForgeRegistries.ITEMS.getValue(res).getItem() instanceof ItemUpgradeStorage) {
                            int level = ((ItemUpgradeStorage) ForgeRegistries.ITEMS.getValue(res).getItem()).level.getLevel();
                            i += CommonConfig.UPGRADES.getLevelMult(level);
                        }
//                    if(ForgeRegistries.ITEMS.getValue(res).getItem() ==ModItems.ONE_STACK_UPGRADE) return getCapacityStandard() / 32;
                    }
                }

//            i += upgradeData0.getStorageMultiplier();
            i = i == 0 ? 1 : i;
            if (upgradeData0.write(new CompoundNBT()).toString().contains("storagedrawers:creative_vending_upgrade")
                    || upgradeData0.write(new CompoundNBT()).toString().contains("storagedrawers:creative_storage_upgrade"))
                return Integer.MAX_VALUE;
            if (upgradeData0.write(new CompoundNBT()).toString().contains("storagedrawers:one_stack_upgrade"))
                return getCapacityStandard() / 32;
//            FluidDrawersLegacyMod.logger(""+upgradeData0.getStorageMultiplier());
            return upgradeData0.hasVendingUpgrade() ? Integer.MAX_VALUE : getCapacityStandard() * i;
        }
    }

    @Override
    public int getDrawerCapacity() {
        return 0;
    }

    @Override
    public int getEffectiveDrawerCapacity() {
        return 0;
    }

    @Nonnull
    @Override
    public IDrawer getDrawer(int slot) {
        return super.getDrawer(slot);
    }

    @Override
    public int getRedstoneLevel() {
        FluidDrawersLegacyMod.logger(getLevel().toString()+this.isRedstone());
        return (int) (((float) getTankFLuid().getAmount() / (float) getEffectiveCapacity()) * 15);
    }

    @Override
    public boolean isRedstone() {
        return  upgrades().getRedstoneType()!=null;
    }

    public class GroupData extends StandardDrawerGroup {
        private final LazyOptional<?> attributesHandler = LazyOptional.of(TileEntityFluidDrawer.this::getDrawerAttributes);
        public final betterFluidHandler tank;
        private final LazyOptional<betterFluidHandler> tankHandler;
        public DrawerData drawerData;

        public GroupData(int slotCount) {
            super(slotCount);
            tank = createFuildHandler();
            tankHandler = LazyOptional.of(() -> tank);

        }


        private TileEntityFluidDrawer.betterFluidHandler createFuildHandler() {
            return new TileEntityFluidDrawer.betterFluidHandler(General.volume.get()) {
            };
        }


        @Nonnull
        @Override
        protected DrawerData createDrawer(int slot) {
            drawerData = new StandardDrawerData(this, slot);
            return drawerData;
        }

        @Override
        public boolean isGroupValid() {
            return TileEntityFluidDrawer.this.isGroupValid();
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
            if (capability == TileEntityFluidDrawer.DRAWER_ATTRIBUTES_CAPABILITY)
                return attributesHandler.cast();
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
//                inventoryChanged();
                if (facing == null) {
//                    FluidDrawersLegacyMod.LOGGER.info(getLevel().toString() + facing+tank.serializeNBT());
                }
                return tankHandler.cast();

            }
            return super.getCapability(capability, facing);
        }


        @Override
        public CompoundNBT write(CompoundNBT tag) {

            tag.put("tank", tank.serializeNBT());
//            FluidDrawersLegacyMod.LOGGER.info(tank.serializeNBT());
            CompoundNBT nbt = super.write(tag);

            EnumSet<LockAttribute> attrs = EnumSet.noneOf(LockAttribute.class);
            if (drawerAttributes.isItemLocked(LockAttribute.LOCK_EMPTY))
                attrs.add(LockAttribute.LOCK_EMPTY);
            if (drawerAttributes.isItemLocked(LockAttribute.LOCK_POPULATED))
                attrs.add(LockAttribute.LOCK_POPULATED);
            if (!attrs.isEmpty()) {
                tag.putByte("Lock", (byte) LockAttribute.getBitfield(attrs));
            }

            if (drawerAttributes.isConcealed())
                tag.putBoolean("Shr", true);

            if (drawerAttributes.isShowingQuantity())
                tag.putBoolean("Qua", true);
//            inventoryChanged();
            //            If want to camouflage, pay attention to setting the capacity first, but we don't need it.
            return nbt;
        }


        @Override
        public void read(CompoundNBT nbt) {
            upgrades().read(nbt);

            if (nbt.contains("tank", Constants.NBT.TAG_COMPOUND)) {
                tank.deserializeNBT((CompoundNBT) nbt.get("tank"));
            }
            if (nbt.contains("Lock")) {
                EnumSet<LockAttribute> attrs = LockAttribute.getEnumSet(nbt.getByte("Lock"));
                if (attrs != null) {
                    drawerAttributes.setItemLocked(LockAttribute.LOCK_EMPTY, attrs.contains(LockAttribute.LOCK_EMPTY));
                    drawerAttributes.setItemLocked(LockAttribute.LOCK_POPULATED, attrs.contains(LockAttribute.LOCK_POPULATED));
                }
            } else {
                drawerAttributes.setItemLocked(LockAttribute.LOCK_EMPTY, false);
                drawerAttributes.setItemLocked(LockAttribute.LOCK_POPULATED, false);
            }

            if (nbt.contains("Shr"))
                drawerAttributes.setIsConcealed(nbt.getBoolean("Shr"));
            else
                drawerAttributes.setIsConcealed(false);


            if (nbt.contains("Qua"))
                drawerAttributes.setIsShowingQuantity(nbt.getBoolean("Qua"));
            else
                drawerAttributes.setIsShowingQuantity(false);
//            inventoryChanged();

            super.read(nbt);
        }


    }

    public class StandardDrawerData extends StandardDrawerGroup.DrawerData {
        private int slot;
//        private FluidStack fluid = new FluidStack(Fluids.EMPTY, 0);

        public StandardDrawerData(StandardDrawerGroup group, int slot) {
            super(group);
            this.slot = slot;
        }

        public betterFluidHandler getTank() {
            return TileEntityFluidDrawer.this.groupData.tank;
        }

//        public int getCapacity() {
//            return getCapacityStandard() * upgrades().getStorageMultiplier();
//        }

        @Override
        protected int getStackCapacity() {
            return upgrades().getStorageMultiplier() * getEffectiveDrawerCapacity();
        }

        @Override
        protected void onItemChanged() {
            DrawerPopulatedEvent event = new DrawerPopulatedEvent(this);
            MinecraftForge.EVENT_BUS.post(event);

            if (getLevel() != null && !getLevel().isClientSide()) {
                setChanged();
                markBlockForUpdate();
            }
        }

        @Override
        protected void onAmountChanged() {
            if (getLevel() != null && !getLevel().isClientSide()) {
                syncClientCount(slot, getStoredItemCount());
                setChanged();

            }
        }


    }

    public class betterFluidHandler extends FluidTank {


        public betterFluidHandler(int capacity) {
            super(capacity);
        }

        public CompoundNBT serializeNBT() {
//            发送信息时调整容量大小
            if (this.getCapacity() != TileEntityFluidDrawer.this.getEffectiveCapacity())
                this.setCapacity(TileEntityFluidDrawer.this.getEffectiveCapacity());
            return writeToNBT(new CompoundNBT());
        }

        public void deserializeNBT(CompoundNBT tank) {
            readFromNBT(tank);
        }

        //        need to override ,or not sync
        @Override
        protected void onContentsChanged() {

            inventoryChanged();

            super.onContentsChanged();
        }

        public boolean isFull() {
            return this.getFluidAmount() == this.getCapacity();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (upgrades().hasVendingUpgrade())
                return 0;
            if ((this.getCapacity() - fluid.getAmount() - resource.getAmount()) < 0
                    && upgrades().write(new CompoundNBT()).toString().contains("storagedrawers:void_upgrade")) {
                if (resource.isEmpty() || !isFluidValid(resource)) {
                    return 0;
                }
                if (action.simulate()) {
                    return resource.getAmount();
                }
                if (fluid.isEmpty()) {
                    fluid = new FluidStack(resource, Math.min(capacity, resource.getAmount()));
                    onContentsChanged();
                    return fluid.getAmount();
                }
                if (!fluid.isFluidEqual(resource)) {
                    return 0;
                }
                fluid.setAmount(capacity);
                onContentsChanged();
                return resource.getAmount();
            }
            return super.fill(resource, action);
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (upgrades().hasVendingUpgrade())
                return resource.getFluid() == fluid.getFluid() ?
                        new FluidStack(fluid.getFluid(), resource.getAmount()) :
                        FluidStack.EMPTY;
            return super.drain(resource, action);
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (upgrades().hasVendingUpgrade())
                return new FluidStack(fluid.getFluid(), maxDrain);
            return super.drain(maxDrain, action);
        }
    }

    //    extra attributes, not very useful
    private class DrawerAttributes extends BasicDrawerAttributes {
        private DrawerAttributes() {
        }


        protected void onAttributeChanged() {

            TileEntityFluidDrawer.this.onAttributeChanged();
            if (TileEntityFluidDrawer.this.getLevel() != null && !TileEntityFluidDrawer.this.getLevel().isClientSide) {
                TileEntityFluidDrawer.this.setChanged();
                TileEntityFluidDrawer.this.markBlockForUpdate();
            }

        }
    }

}