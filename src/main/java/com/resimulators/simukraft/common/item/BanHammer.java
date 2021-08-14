package com.resimulators.simukraft.common.item;

import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.tileentity.TileMarker;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;


public class BanHammer extends Item {



    public BanHammer(Properties properties) {
        super(properties);
    }



    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if(!world.isClientSide()) {

            ItemStack item = player.getItemInHand(hand);

            //"debug" tool state where 0 - kill, 1 - fire, 2 - make factionless, 3 - reset sim's inv and 4 - kill al unloaded sims
            CompoundNBT nbt = item.getOrCreateTag();

            if (player.isShiftKeyDown()) {

                if (nbt.getInt("state") == 4) {

                    nbt.putInt("state", 0);
                    player.displayClientMessage(new StringTextComponent(String.format("Switched to Kill Sim Mode")), true);
                } else {

                    nbt.putInt("state", nbt.getInt("state") + 1);
                    switch (nbt.getInt("state")) {
                        case 1:
                            player.displayClientMessage(new StringTextComponent(String.format("Switched to Fire Sim Mode")), true);
                            break;
                        case 2:
                            player.displayClientMessage(new StringTextComponent(String.format("Switched to Remove Sim From Faction Mode")), true);
                            break;
                        case 3:
                            player.displayClientMessage(new StringTextComponent(String.format("Switched to Reset Targeted Sim's Inventory")), true);
                            break;
                        case 4:
                            player.displayClientMessage(new StringTextComponent(String.format("Switched to Purge Bugged Sims Mode")), true);
                            break;
                    }
                }
                item.setTag(nbt);
            } else {

                if (nbt.getInt("state") == 4) {

                    ArrayList<Integer> simList = new ArrayList<>();
                    Faction faction = SavedWorldData.get(world).getFactionWithPlayer(player.getUUID());
                    if (world.isClientSide()) {
                        simList = faction.getSimIds((ServerWorld) world);
                    }

                    for (int simUUID : simList) {

                        System.out.print(faction.getSimIds((ServerWorld) world));
                        if (world.getEntity(simUUID).isAddedToWorld()) {
                            System.out.print(faction.getSimIds((ServerWorld) world));
                            faction.removeSim(world.getEntity(simUUID).getUUID());
                            System.out.print(simList);

                        }
                        System.out.print(faction.getSimIds((ServerWorld) world));
                    }
                    System.out.print(faction.getSimIds((ServerWorld) world));
                }
                System.out.print("kyle");
            }
            System.out.print("kyle");
        }
        return super.use(world, player, hand);
    }


    @Override
    public ActionResultType interactLivingEntity(ItemStack item, PlayerEntity player, LivingEntity entity, Hand hand) {
        if(!player.level.isClientSide()) {

            player.displayClientMessage(new StringTextComponent(String.format("interaction worked")), true);

            if (entity instanceof SimEntity && !player.isShiftKeyDown()) {

                switch (item.getOrCreateTag().getInt("state")) {
                    //kills targeted sim
                    case 0:
                        entity.kill();
                        ((SimEntity) entity).fireSim(((SimEntity)entity),SavedWorldData.get(entity.level).getFactionWithSim(entity.getUUID()).getId(),true);
                        break;
                    //fires targeted sim
                    case 1:
                        ((SimEntity) entity).fireSim(((SimEntity)entity),SavedWorldData.get(entity.level).getFactionWithSim(entity.getUUID()).getId(),false);                        break;
                    //removes targeted sim from faction
                    case 2:
                        SavedWorldData.get(player.level).getFactionWithSim(entity.getUUID()).removeSim(entity.getUUID());
                        break;
                    //resets targeted sim's inventory
                    case 3:
                        ((SimEntity) entity).resetInventory();
                        break;
                }

            }

            //thanos snaps all non existing sims
            else if (entity instanceof PlayerEntity && item.getOrCreateTag().getInt("state") == 4 && !player.isShiftKeyDown()) {

                Faction faction = SavedWorldData.get(entity.level).getFactionWithPlayer(entity.getUUID());
                ArrayList<Integer> simList = faction.getSimIds(entity.getServer().overworld());

                for (int simUUID : simList) {
                    if (entity.level.getEntity(simUUID) == null) {

                        //I hate uuids and the fact that its not always used
                        faction.removeSim(UUID.fromString(String.valueOf(simUUID)));
                    }
                }
            }
        }

        return super.interactLivingEntity(item, player, entity, hand);
    }

}
