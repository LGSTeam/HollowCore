package ru.hollowhorizon.hc.common.registry;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.objectweb.asm.Type;
import ru.hollowhorizon.hc.api.registy.HollowPacket;
import ru.hollowhorizon.hc.api.registy.HollowRegister;
import ru.hollowhorizon.hc.api.registy.StoryObject;
import ru.hollowhorizon.hc.api.utils.DelayedAction;
import ru.hollowhorizon.hc.api.utils.HollowConfig;
import ru.hollowhorizon.hc.client.config.HollowCoreConfig;
import ru.hollowhorizon.hc.client.utils.HollowPack;
import ru.hollowhorizon.hc.client.utils.ResourcePackAdapter;
import ru.hollowhorizon.hc.common.animations.CutsceneHandler;
import ru.hollowhorizon.hc.common.animations.CutsceneStartHandler;
import ru.hollowhorizon.hc.common.events.action.ActionStorage;
import ru.hollowhorizon.hc.common.events.action.HollowAction;
import ru.hollowhorizon.hc.common.handlers.GUIDialogueHandler;
import ru.hollowhorizon.hc.common.network.UniversalPacket;
import ru.hollowhorizon.hc.common.network.UniversalPacketManager;
import ru.hollowhorizon.hc.common.objects.blocks.IBlockProperties;
import ru.hollowhorizon.hc.common.objects.items.HollowArmor;
import ru.hollowhorizon.hc.common.story.HollowStoryHandler;
import ru.hollowhorizon.hc.common.story.dialogues.DialogueBuilder;
import ru.hollowhorizon.hc.common.story.dialogues.IHollowDialogue;
import ru.hollowhorizon.hc.common.story.events.StoryRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Locale;

public class HollowModProcessor {
    private static final Type HOLLOW_REGISTER = Type.getType(HollowRegister.class);
    private static final Type STORY_OBJECT = Type.getType(StoryObject.class);
    private static final Type REGISTER_ACTION = Type.getType(DelayedAction.class);
    private static final Type HOLLOW_CONFIG = Type.getType(HollowConfig.class);
    private static final Type HOLLOW_PACKET = Type.getType(HollowPacket.class);

    public static synchronized void run(String modId, ModFileScanData scanResults) {
        hollowRegister(modId, scanResults);
    }

    public static void hollowRegister(String modId, ModFileScanData scanResults) {


        DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, modId);
        DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, modId);
        DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, modId);
        DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, modId);
        DeferredRegister<Structure<?>> STRUCTURES = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, modId);
        DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, modId);
        DeferredRegister<IRecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, modId);
        DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, modId);
        DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, modId);

        scanResults.getAnnotations().stream()
                .filter(annotationData ->
                        annotationData.getAnnotationType().equals(HOLLOW_REGISTER) ||
                                annotationData.getAnnotationType().equals(STORY_OBJECT) ||
                                annotationData.getAnnotationType().equals(REGISTER_ACTION) ||
                                annotationData.getAnnotationType().equals(HOLLOW_CONFIG) ||
                                annotationData.getAnnotationType().equals(HOLLOW_PACKET)

                )
                .forEach(annotationData -> {
                            String containerClassName = annotationData.getClassType().getClassName();
                            Class<?> containerClass;
                            try {
                                containerClass = Class.forName(containerClassName);
                            } catch (Throwable e) {
                                throw new RuntimeException(String.format("There was an exception while trying to load %s", containerClassName), e);
                            }

                            if (annotationData.getAnnotationType().equals(HOLLOW_REGISTER)) {
                                String fieldName = annotationData.getMemberName();
                                Field field = findField(containerClass, fieldName);
                                field.setAccessible(true);
                                String regName = fieldName.toLowerCase(Locale.ROOT);
                                boolean hasAutoModel = field.getAnnotation(HollowRegister.class).auto_model();
                                String modelName = field.getAnnotation(HollowRegister.class).model();
                                String texture = field.getAnnotation(HollowRegister.class).texture();

                                if (Modifier.isStatic(field.getModifiers())) {
                                    try {
                                        Object someObject = field.get(null);

                                        if (someObject instanceof Block) {

                                            Block block = (Block) someObject;
                                            if (hasAutoModel) {
                                                HollowPack.genBlockData.add(new ResourceLocation(modId, regName));
                                            }

                                            BLOCKS.register(regName, () -> block);
                                            if (block instanceof IBlockProperties) {
                                                ITEMS.register(regName, () -> new BlockItem(block, ((IBlockProperties) block).getProperties()));
                                            }
                                        } else if (someObject instanceof Item) {
                                            Item item = (Item) someObject;
                                            ITEMS.register(regName, () -> item);
                                            if (hasAutoModel) {
                                                HollowPack.genItemModels.add(new ResourceLocation(modId, regName));
                                            }
                                        } else if (someObject instanceof TileEntityType) {
                                            TileEntityType<?> tile = (TileEntityType<?>) someObject;

                                            TILES.register(regName, () -> tile);
                                        } else if (someObject instanceof SoundEvent) {
                                            SoundEvent sound = (SoundEvent) someObject;

                                            SOUNDS.register(regName, () -> sound);
                                        } else if (someObject instanceof Structure) {
                                            Structure<?> structure = (Structure<?>) someObject;

                                            STRUCTURES.register(regName, () -> structure);
                                        } else if (someObject instanceof EntityType<?>) {
                                            EntityType<?> entity = (EntityType<?>) someObject;

                                            ENTITIES.register(regName, () -> entity);
                                        } else if (someObject instanceof HollowArmor) {
                                            HollowArmor<?> armor = (HollowArmor<?>) someObject;
                                            if (hasAutoModel) {
                                                armor.registerModels(modId, regName);
                                            }
                                            armor.registerItems(ITEMS, regName);
                                        } else if (someObject instanceof IRecipeSerializer<?>) {
                                            IRecipeSerializer<?> serializer = (IRecipeSerializer<?>) someObject;

                                            RECIPES.register(regName, () -> serializer);
                                        } else if (someObject instanceof ContainerType<?>) {
                                            ContainerType<?> containerType = (ContainerType<?>) someObject;

                                            CONTAINERS.register(regName, () -> containerType);
                                        } else if (someObject instanceof ParticleType<?>) {
                                            if (!texture.equals(""))
                                                HollowPack.genParticles.add(new ResourceLocation(texture));

                                            PARTICLES.register(regName, () -> (ParticleType<?>) someObject);
                                        }
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else if (annotationData.getAnnotationType().equals(STORY_OBJECT)) {
                                try {

                                    if (IHollowDialogue.class.isAssignableFrom(containerClass)) {
                                        IHollowDialogue dialogue = (IHollowDialogue) containerClass.getConstructor().newInstance();
                                        GUIDialogueHandler.register(dialogue.getName(), dialogue.build(DialogueBuilder.create()));
                                    } else if (HollowStoryHandler.class.isAssignableFrom(containerClass))
                                        StoryRegistry.register((Class<? extends HollowStoryHandler>) containerClass);
                                    else if (CutsceneHandler.class.isAssignableFrom(containerClass)) {
                                        CutsceneHandler handler = (CutsceneHandler) containerClass.getConstructor().newInstance();

                                        CutsceneStartHandler.register(() -> handler);
                                    }
                                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                         NoSuchMethodException e) {
                                    e.printStackTrace();
                                }
                            } else if (annotationData.getAnnotationType().equals(REGISTER_ACTION)) {
                                String fieldName = annotationData.getMemberName();
                                Field field = findField(containerClass, fieldName);
                                field.setAccessible(true);

                                if (Modifier.isStatic(field.getModifiers())) {
                                    try {
                                        Object someObject = field.get(null);
                                        if (someObject instanceof HollowAction) {
                                            ActionStorage.registerAction(fieldName, (HollowAction) someObject);
                                        }
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else if (annotationData.getAnnotationType().equals(HOLLOW_CONFIG)) {
                                String fieldName = annotationData.getMemberName();
                                Field field = findField(containerClass, fieldName);
                                if (Modifier.isStatic(field.getModifiers())) {
                                    if (!Modifier.isFinal(field.getModifiers())) {
                                        HollowCoreConfig.FIELDS.computeIfAbsent(modId, k -> new ArrayList<>()).add(field);
                                    } else {
                                        throw new IllegalArgumentException("Field " + fieldName + " is final");
                                    }
                                }
                            } else if (annotationData.getAnnotationType().equals(HOLLOW_PACKET)) {
                                String fieldName = annotationData.getMemberName();
                                Field field = findField(containerClass, fieldName);
                                field.setAccessible(true);

                                String target = containerClass.getName() + "_" + fieldName;

                                try {
                                    UniversalPacketManager.PACKETS.put(target, (UniversalPacket<?>) field.get(null));
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                );

        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        STRUCTURES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        RECIPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());

        ResourcePackAdapter.registerResourcePack(HollowPack.getPackInstance());

        HollowCoreConfig.load();
    }

    public static Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (Throwable e) {
            throw new RuntimeException("Can't retrieve field " + fieldName + " from class " + clazz, e);
        }
    }

    static void setFinalStatic(Field field, Object newValue) {
        try {
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");

            AccessController.doPrivileged((PrivilegedAction) () -> {
                modifiersField.setAccessible(true);
                return null;
            });

            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(null, newValue);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
