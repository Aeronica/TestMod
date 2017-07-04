/** 
 * The MIT License (MIT)
 *
 * Test Mod 3 - Copyright (c) 2015-2016 Choonster
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package tld.testmod.init;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.animation.AnimationTESR;
import net.minecraftforge.common.animation.Event;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import tld.testmod.common.IVariant;
import tld.testmod.common.animation.EdgarAllenTileEntity;
import tld.testmod.common.animation.ForgeAnimTileEntity;
import tld.testmod.common.animation.ForgeSpinTileEntity;
import tld.testmod.common.animation.ModAnimation;
import tld.testmod.common.animation.OneShotTileEntity;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(Side.CLIENT)
public class ModModelManager
{
    
    public static final ModModelManager INSTANCE =  new ModModelManager();
    private ModModelManager() {}
    
    @SubscribeEvent
    public static void registerAllModels(ModelRegistryEvent event) {
        INSTANCE.registerTileRenderers();
        INSTANCE.registerBlockModels();
        INSTANCE.registerItemModels();
    }
    
    private void registerBlockModels()
    {
        ModelLoader.setCustomStateMapper(ModBlocks.BLOCK_VQBTEST, new StateMap.Builder().ignore(new IProperty[]
                {
                }).build());
        ModelLoader.setCustomStateMapper(ModBlocks.BLOCK_VQBTEST2, new StateMap.Builder().ignore(new IProperty[]
                {
                }).build());
        ModelLoader.setCustomStateMapper(ModBlocks.BLOCK_HQBTEST, new StateMap.Builder().ignore(new IProperty[]
                {
                }).build());

        registerItemModel(ModBlocks.BLOCK_VQBTEST);
        registerItemModel(ModBlocks.BLOCK_VQBTEST2);
        registerItemModel(ModBlocks.BLOCK_HQBTEST);
        registerItemModel(ModBlocks.FORGE_ANIM_TEST);
        registerItemModel(ModBlocks.FORGE_SPIN_TEST);
        registerItemModel(ModBlocks.EDGAR_ALLEN_BLOCK_LEVER);
        registerItemModel(ModBlocks.ONE_SHOT);
        registerItemModel(ModBlocks.ITEM_PULL_ROPE);
        registerItemModel(ModBlocks.ITEM_CARILLON);
    }
    
    public void registerTileRenderers() {
        registerTESR(ForgeAnimTileEntity.class, new AnimationTESR<ForgeAnimTileEntity>()
        {
            @Override
            public void handleEvents(ForgeAnimTileEntity te, float time, Iterable<Event> pastEvents)
            {
                te.handleEvents(time, pastEvents);
            }
        });
        registerTESR(ForgeSpinTileEntity.class, new AnimationTESR<ForgeSpinTileEntity>()
        {
            @Override
            public void handleEvents(ForgeSpinTileEntity te, float time, Iterable<Event> pastEvents)
            {
                te.handleEvents(time, pastEvents);
            }
        });
        registerTESR(EdgarAllenTileEntity.class, new AnimationTESR<EdgarAllenTileEntity>()
        {
            @Override
            public void handleEvents(EdgarAllenTileEntity te, float time, Iterable<Event> pastEvents)
            {
                super.handleEvents(te, time, pastEvents);
                te.handleEvents(time, pastEvents);
            }
        });
        registerTESR(OneShotTileEntity.class, new AnimationTESR<OneShotTileEntity>()
        {
            @Override
            public void renderTileEntityFast(OneShotTileEntity te, double x, double y, double z, float partialTick, int breakStage, VertexBuffer renderer)
            {
                if(!te.hasCapability(CapabilityAnimation.ANIMATION_CAPABILITY, null))
                {
                    return;
                }
                if(blockRenderer == null) blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
                BlockPos pos = te.getPos();
                IBlockAccess world = MinecraftForgeClient.getRegionRenderCache(te.getWorld(), pos);
                IBlockState state = world.getBlockState(pos);
                if(state.getPropertyKeys().contains(Properties.StaticProperty))
                {
                    state = state.withProperty(Properties.StaticProperty, false);
                }
                if(state instanceof IExtendedBlockState)
                {
                    IExtendedBlockState exState = (IExtendedBlockState)state;
                    if(exState.getUnlistedNames().contains(Properties.AnimationProperty))
                    {
                        double time = ModAnimation.getWorldTime(getWorld(), partialTick);
                        IAnimationStateMachine capability = te.getCapability(CapabilityAnimation.ANIMATION_CAPABILITY, null);
                        if (capability != null)
                        {
                            Pair<IModelState, Iterable<Event>> pair = capability.apply((float)time);
                            handleEvents(te, (float) time, pair.getRight());

                            // TODO: caching?
                            IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(exState.getClean());
                            exState = exState.withProperty(Properties.AnimationProperty, pair.getLeft());

                            renderer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());

                            blockRenderer.getBlockModelRenderer().renderModel(world, model, exState, pos, renderer, false);
                        }
                    }
                }
            }
            @Override
            public void handleEvents(OneShotTileEntity te, float time, Iterable<Event> pastEvents)
            {
                super.handleEvents(te, time, pastEvents);
                te.handleEvents(time, pastEvents);
            }
        });

    }

    /**
     * Register this mod's {@link Item} models.
     */
    private void registerItemModels() {
        // registerItemModel(ModItems.ITEM_MUSIC_PAPER);
        // registerItemModel(ModItems.ITEM_SHEET_MUSIC);

        // registerVariantItemModels(ModItems.ITEM_INSTRUMENT, "item_inst", ItemInstrument.EnumType.values());

        // Then register items with default model names
        ModItems.RegistrationHandler.ITEMS.stream().filter(item -> !itemsRegistered.contains(item)).forEach(this::registerItemModel);
    }
    
    private static ArrayList<Object> tesrRenderers = new ArrayList<Object>();
    
    public static ArrayList<Object> getTESRRenderers() {return tesrRenderers;}
    
    public <T extends TileEntity> void registerTESR(Class<T> tile, TileEntitySpecialRenderer<T> renderer) {
        ClientRegistry.bindTileEntitySpecialRenderer(tile, renderer);
        tesrRenderers.add(renderer);
    }

    public <T extends Block> void registerItemModel(T block)
    {
        registerItemModel(Item.REGISTRY.getObject(block.getRegistryName()));
    }
    
    /**
     * The {@link Item}s that have had models registered so far.
     */
    private final Set<Item> itemsRegistered = new HashSet<>();

    /**
     * Register a single model for an {@link Item}.
     * <p>
     * Uses the registry name as the domain/path and {@code "inventory"} as the variant.
     *
     * @param item The Item
     */
    private void registerItemModel(Item item) {
        registerItemModel(item, item.getRegistryName().toString());
    }

    /**
     * Register a single model for an {@link Item}.
     * <p>
     * Uses {@code modelLocation} as the domain/path and {@link "inventory"} as the variant.
     *
     * @param item          The Item
     * @param modelLocation The model location
     */
    private void registerItemModel(Item item, String modelLocation) {
        final ModelResourceLocation fullModelLocation = new ModelResourceLocation(modelLocation, "inventory");
        registerItemModel(item, fullModelLocation);
    }

    /**
     * Register a single model for an {@link Item}.
     * <p>
     * Uses {@code fullModelLocation} as the domain, path and variant.
     *
     * @param item              The Item
     * @param fullModelLocation The full model location
     */
    private void registerItemModel(Item item, ModelResourceLocation fullModelLocation) {
        ModelBakery.registerItemVariants(item, fullModelLocation); // Ensure the custom model is loaded and prevent the default model from being loaded
        registerItemModel(item, MeshDefinitionFix.create(stack -> fullModelLocation));
    }

    /**
     * Register an {@link ItemMeshDefinition} for an {@link Item}.
     *
     * @param item           The Item
     * @param meshDefinition The ItemMeshDefinition
     */
    private void registerItemModel(Item item, ItemMeshDefinition meshDefinition) {
        itemsRegistered.add(item);
        ModelLoader.setCustomMeshDefinition(item, meshDefinition);
    }

    /**
     * Register a model for each metadata value of an {@link Item} corresponding to the values in {@code values}.
     * <p>
     * Uses the registry name as the domain/path and {@code "[unlocalizedName]_[valueName]"} for the item/model json.
     * <p>
     * Uses {@link IVariant#getMeta()} to determine the metadata of each value.
     *
     * @param item        The Item
     * @param values      The values
     * @param <T>         The value type
     */
    private <T extends IVariant> void registerItemModelsWithSubtypes(Item item, T[] values) {
        for (T value : values) {
            registerItemModelForMetaAndType(item, value.getMeta(), value.getName());
        }
    }
    
    /**
     * Register a model for a metadata value an {@link Item}.
     * <p>
     * Uses the registry name as the domain/path and {@code type} as the variant.
     *
     * @param item     The Item
     * @param metadata The metadata
     * @param type  The type
     */
    private void registerItemModelForMetaAndType(Item item, int metadata, String type) {
        registerItemModelForMeta(item, metadata, new ModelResourceLocation(new ResourceLocation(item.getRegistryName().toString() + "_" + type), "inventory"));
    }

    
    /**
     * Register a model for each metadata value of an {@link Item} corresponding to the values in {@code values}.
     * <p>
     * Uses the registry name as the domain/path and {@code "[variantName]=[valueName]"} as the variant.
     * <p>
     * Uses {@link IVariant#getMeta()} to determine the metadata of each value.
     *
     * @param item        The Item
     * @param variantName The variant name
     * @param values      The values
     * @param <T>         The value type
     */
    private <T extends IVariant> void registerVariantItemModels(Item item, String variantName, T[] values) {
        for (T value : values) {
            registerItemModelForMeta(item, value.getMeta(), variantName + "=" + value.getName());
        }
    }

    /**
     * Register a model for a metadata value an {@link Item}.
     * <p>
     * Uses the registry name as the domain/path and {@code variant} as the variant.
     *
     * @param item     The Item
     * @param metadata The metadata
     * @param variant  The variant
     */
    private void registerItemModelForMeta(Item item, int metadata, String variant) {
        registerItemModelForMeta(item, metadata, new ModelResourceLocation(item.getRegistryName(), variant));
    }

    /**
     * Register a model for a metadata value of an {@link Item}.
     * <p>
     * Uses {@code modelResourceLocation} as the domain, path and variant.
     *
     * @param item                  The Item
     * @param metadata              The metadata
     * @param modelResourceLocation The full model location
     */
    private void registerItemModelForMeta(Item item, int metadata, ModelResourceLocation modelResourceLocation) {
        itemsRegistered.add(item);
        ModelLoader.setCustomModelResourceLocation(item, metadata, modelResourceLocation);
    }

}
