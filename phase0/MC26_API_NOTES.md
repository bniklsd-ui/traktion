---
status: frozen
purpose: Recherche-Notizen zu MC 26.2 Entity/Persistenz-API für P0.4-Spike. Klärt [VERIFY]-Marken: Entity-Registrierung, SavedData-API, ValueOutput/ValueInput.
read-when: vor P0.4-Spike-Code; bei Fragen zur 26.2 Entity- oder Persistenz-API
detail: L2
up: ./CLAUDE.md
down:
related: ./Fabric_Loom_Mappings_Fix_01.md
updated: 2026-07-15
---

# MC 26.2 Entity- & Persistenz-API — Recherche-Notizen

> Quelle: docs.fabricmc.net (Version 26.1.2, Stand 2026-07-15). MC 26.x ist unobfuskiert,
> Mojang-Namen direkt. 1.21.x-Tutorials sind veraltet — diese Notizen sind gegen die
> offizielle 26.1.2-Doku geprüft.

## Entity-Registrierung (26.x)

```java
public class ModEntityTypes {
    public static final EntityType<MyEntity> MY_ENTITY = register(
        "my_entity",
        EntityType.Builder.<MyEntity>of(MyEntity::new, MobCategory.MISC)
            .sized(0.75f, 1.75f)
    );

    private static <T extends Entity> EntityType<T> register(
            String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(MOD_ID, name)
        );
        return Registry.register(
            BuiltInRegistries.ENTITY_TYPE, key, builder.build(key)
        );
    }

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(
            MY_ENTITY, MyEntity.createAttributes()
        );
    }
}
```

**Änderungen zu 1.21.x:**
- `Registry.register(BuiltInRegistries.ENTITY_TYPE, key, ...)` statt `Registries.ENTITY_TYPE`
  (Name kann abweichen — in 26.x ist es `BuiltInRegistries`)
- `ResourceKey.create` statt `Identifier.of` in einigen Kontexten
- `Identifier.fromNamespaceAndPath(modId, name)` statt `Identifier.of(modId, name)`

## Entity-NBT-Persistenz (26.x)

**API-Änderung:** `writeNbt`/`readNbt` mit `NbtCompound` → `addAdditionalSaveData`/`readAdditionalSaveData` mit `ValueOutput`/`ValueInput`.

```java
@Override
protected void addAdditionalSaveData(ValueOutput valueOutput) {
    super.addAdditionalSaveData(valueOutput);
    valueOutput.putInt("my_field", this.myField);
}

@Override
protected void readAdditionalSaveData(ValueInput valueInput) {
    super.readAdditionalSaveData(valueInput);
    this.myField = valueInput.getInt("my_field").orElse(0);
}
```

**Wichtig:** `ValueInput.getInt()` gibt `Optional<Integer>` zurück, nicht `int`.
`NbtCompound` wird in Entity-Persistenz nicht mehr direkt verwendet.

## Welt-attached Persistent State (T-D15 — geklärt)

**API:** `SavedData` + `SavedDataType` + `level.getDataStorage().computeIfAbsent(TYPE)`

```java
public class MySavedData extends SavedData {
    private static final Codec<MySavedData> CODEC = Codec.INT.xmap(
        MySavedData::new,
        MySavedData::getValue
    );

    private static final SavedDataType<MySavedData> TYPE = new SavedDataType<>(
        "my_data_file",  // Dateiname in world/data/
        MySavedData::new,
        CODEC
    );

    public static MySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    @Override
    public boolean isDirty() {
        return true; // oder eigene Dirty-Logik
    }
}
```

**Änderungen zu 1.21.x:**
- `PersistentState` → `SavedData`
- `ServerLevel.getDataStorage().computeIfAbsent(TYPE)` statt
  `PersistentState.getType()`-Factory-Muster
- Codec-basiert statt manuellem `writeNbt`/`readFromNbt`
- `SavedDataType` ist ein Record/Class, der Name + Factory + Codec bündelt

**Für T-D15:** Der Graph liegt als welt-attached SavedData pro Dimension. `train-core`
liefert ein `GraphSnapshot`-Record, `train-mc` kodiert es via Codec in `SavedData`.

## Despawn / Rekonstruktion bei Spielerentfernung

**Noch ungeklärt [VERIFY]:** Die Fabric-Doku behandelt nicht explizit, wie Entities bei
Spielerentfernung despawnen und zustandserhaltend rekonstruiert werden. Das ist
Vanilla-Verhalten (Chunk-Unload → Entity wird gespeichert, Chunk-Load → Entity wird
rekonstruiert). Für den Spike muss das manuell getestet werden.

**Hypothese:** Wenn die Entity korrekt `addAdditionalSaveData`/`readAdditionalSaveData`
implementiert, speichert Vanilla sie beim Chunk-Unload und rekonstruiert sie beim
Chunk-Load mit erhaltenem Zustand. Der Spike muss das bestätigen.

## Entity-Spawn-API (verifiziert 2026-07-16 gegen dekompilierte JARs)

**Problem, das dieser Abschnitt klärt:** Registrierung allein bringt keine Entity in die Welt.
`onInitialize()` läuft beim Mod-Laden, nicht beim Weltstart. Eine registrierte Entity
erscheint erst, wenn etwas sie spawned — per `/summon`, per Spawn-Egg, oder per Code.

**Verifizierte API (aus dekompilierten JARs, nicht aus Doku):**

```java
// ServerLifecycleEvents.SERVER_STARTED feuert, wenn der Server hochgefahren ist.
// Callback-Signatur: void onServerStarted(MinecraftServer server)
// Quelle: fabric-lifecycle-events-v1-4.1.3 JAR, dekompiliert mit javap
ServerLifecycleEvents.SERVER_STARTED.register(server -> {
    ServerLevel overworld = server.overworld();  // MinecraftServer.overworld() -> ServerLevel
    BlockPos pos = new BlockPos(0, 1, 0);

    // EntityType.spawn(ServerLevel, BlockPos, EntitySpawnReason) -> T
    // Quelle: minecraft-merged-deobf-26.2.jar, EntityType.class
    PathEntity entity = PATH_ENTITY.spawn(overworld, pos, EntitySpawnReason.COMMAND);
});

// Alternative: ServerLevel.addFreshEntity(Entity) -> boolean
// Quelle: ServerLevel.class
```

**EntitySpawnReason-Werte (verifiziert aus EntitySpawnReason.class):**
`NATURAL` · `CHUNK_GENERATION` · `SPAWNER` · `STRUCTURE` · `BREEDING` · `MOB_SUMMONED` ·
`JOCKEY` · `EVENT` · `CONVERSION` · `REINFORCEMENT` · `TRIGGERED` · `BUCKET` ·
`SPAWN_ITEM_USE` · `COMMAND` · `DISPENSER` · `PATROL` · `TRIAL_SPAWNER` · `LOAD` ·
`DIMENSION_TRAVEL`

**Für den Spike:** `COMMAND` ist der passende Grund (programmatischer Spawn, wie `/summon`).

**Wichtig:** `onInitialize()` läuft beim Mod-Laden, bevor eine Welt existiert. Spawn-Logik
muss in `SERVER_STARTED` (oder später) laufen, nicht in `onInitialize()`.

## Entity-Renderer-API (verifiziert 2026-07-16 gegen dekompilierte JARs)

**Problem, das dieser Abschnitt klärt:** Eine Entity ohne registrierten Renderer crasht
den Client mit NPE: `EntityRenderDispatcher.shouldRender()` → `renderer` ist null.
Symptom: Client crasht im Render-Frame, sieht aus wie "hängen im Ladebildschirm".

**Verifizierte API (aus dekompilierten JARs):**

```java
// Client-Entrypoint (fabric.mod.json "client": [...])
// EntityRendererRegistry.register(EntityType, EntityRendererProvider)
// Quelle: fabric-rendering-v1-25.3.0 JAR
EntityRendererRegistry.register(MY_ENTITY, MyRenderer::new);

// Minimaler Renderer (rendert nichts, verhindert NPE):
public class MyRenderer extends EntityRenderer<MyEntity, EntityRenderState> {
    public MyRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();  // konkrete Klasse, nicht abstrakt
    }
    @Override
    public void extractRenderState(MyEntity e, EntityRenderState s, float partialTick) {
        super.extractRenderState(e, s, partialTick);
    }
    @Override
    public void submit(EntityRenderState s, PoseStack p, SubmitNodeCollector n, CameraRenderState c) {
        // leer: nichts rendern
    }
}
```

**Wichtig:**
- `EntityRenderer<T, S>` ist abstrakt, `createRenderState()` ist die einzige abstrakte Methode.
- `EntityRenderState` ist eine konkrete Klasse — keine Ableitung nötig für minimale Renderer.
- `submit()` kann leer bleiben — die Entity wird dann unsichtbar (nur Hitbox über F3+B).
- Renderer-Registrierung muss im Client-Entrypoint passieren, nicht im Main-Entrypoint.

## jqwik [VERIFY — noch offen]

Nicht in dieser Recherche geklärt. P0.4 oder P1 muss es testen.
